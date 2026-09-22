package org.schabi.newpipe.util;

import android.content.Context;
import android.util.Log;

import androidx.annotation.Nullable;

import com.grack.nanojson.JsonArray;
import com.grack.nanojson.JsonObject;
import com.grack.nanojson.JsonParser;
import com.grack.nanojson.JsonStringWriter;
import com.grack.nanojson.JsonWriter;

import org.schabi.newpipe.PvcTimeoutInterceptor;
import org.schabi.newpipe.MainActivity;
import org.schabi.newpipe.extractor.ServiceList;
import org.schabi.newpipe.extractor.downloader.Response;
import org.schabi.newpipe.extractor.stream.StreamInfo;

import java.nio.charset.StandardCharsets;
import java.security.MessageDigest;
import java.security.NoSuchAlgorithmException;
import java.util.ArrayList;
import java.util.Comparator;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.function.LongSupplier;

import okhttp3.HttpUrl;

/** SponsorBlock API client with privacy-preserving lookup and bounded caching. */
public final class SponsorBlock {
    private static final String TAG = SponsorBlock.class.getSimpleName();
    private static final String SKIP_ACTION = "skip";
    private static final int HASH_PREFIX_LENGTH = 4;
    private static final int REQUEST_TIMEOUT_SECONDS = 3;
    private static final int MAX_CACHE_ENTRIES = 20;
    private static final long CACHE_DURATION_NANOS = 5L * 60 * 1_000_000_000;
    private static final long EMPTY_CACHE_DURATION_NANOS = 60L * 1_000_000_000;
    private static final SponsorBlockSegment[] NO_SEGMENTS = new SponsorBlockSegment[0];

    private static final SponsorBlockCache SEGMENT_CACHE = new SponsorBlockCache(
            MAX_CACHE_ENTRIES, CACHE_DURATION_NANOS, EMPTY_CACHE_DURATION_NANOS,
            System::nanoTime);

    private SponsorBlock() {
    }

    public static boolean canFetch(final Context context, final StreamInfo streamInfo) {
        final SponsorBlockSettings settings = SponsorBlockSettings.from(context);
        return streamInfo.getServiceId() == ServiceList.YouTube.getServiceId()
                && settings.isOperational();
    }

    /**
     * Fetches validated auto-skip segments. Failures and unsupported streams produce an empty
     * array so every caller has the same simple contract.
     *
     * @param context application or UI context used to read preferences
     * @param streamInfo stream metadata containing the service, ID, and duration
     * @return sorted, validated skip segments; never {@code null}
     */
    public static SponsorBlockSegment[] getSegments(final Context context,
                                                    final StreamInfo streamInfo) {
        final SponsorBlockSettings settings = SponsorBlockSettings.from(context);
        final String apiUrl = settings.apiUrl();
        final List<SponsorBlockCategory> categories = settings.enabledCategories();
        if (streamInfo.getServiceId() != ServiceList.YouTube.getServiceId()
                || !settings.isEnabled() || apiUrl == null || categories.isEmpty()) {
            return NO_SEGMENTS;
        }

        final HttpUrl requestUrl = buildRequestUrl(apiUrl, streamInfo.getId(), categories);
        if (requestUrl == null) {
            Log.w(TAG, "Ignoring invalid SponsorBlock API URL");
            return NO_SEGMENTS;
        }

        final String cacheKey = cacheKey(
                requestUrl, streamInfo.getId(), streamInfo.getDuration());
        final SponsorBlockSegment[] cached = SEGMENT_CACHE.get(cacheKey);
        if (cached != null) {
            return cached;
        }

        try {
            final Response response = PvcTimeoutInterceptor
                    .get(requestUrl.toString(), REQUEST_TIMEOUT_SECONDS);
            if (response.responseCode() == 404) {
                cache(cacheKey, NO_SEGMENTS);
                return NO_SEGMENTS;
            }
            if (response.responseCode() < 200 || response.responseCode() >= 300) {
                if (MainActivity.DEBUG) {
                    Log.w(TAG, "SponsorBlock returned HTTP " + response.responseCode());
                }
                return NO_SEGMENTS;
            }
            final SponsorBlockSegment[] segments = parseResponse(
                    response.responseBody(), streamInfo.getId(), streamInfo.getDuration(),
                    Set.copyOf(categories));
            cache(cacheKey, segments);
            return segments.clone();
        } catch (final Exception exception) {
            if (MainActivity.DEBUG) {
                Log.w(TAG, "SponsorBlock request failed", exception);
            }
            return NO_SEGMENTS;
        }
    }

    private static void cache(final String cacheKey, final SponsorBlockSegment[] segments) {
        SEGMENT_CACHE.put(cacheKey, segments);
    }

    static String cacheKey(final HttpUrl requestUrl, final String videoId, final long duration) {
        return requestUrl + "#" + videoId + "#" + duration;
    }

    @Nullable
    static HttpUrl buildRequestUrl(final String apiUrl, final String videoId,
                                   final List<SponsorBlockCategory> categories) {
        final HttpUrl baseUrl = HttpUrl.parse(apiUrl);
        if (baseUrl == null) {
            return null;
        }

        final JsonStringWriter categoryJson = JsonWriter.string().array();
        categories.forEach(category -> categoryJson.value(category.apiName()));

        return baseUrl.newBuilder()
                .addPathSegment("skipSegments")
                .addPathSegment(sha256(videoId).substring(0, HASH_PREFIX_LENGTH))
                .addQueryParameter("categories", categoryJson.end().done())
                .addQueryParameter("actionTypes", "[\"skip\"]")
                .addQueryParameter("trimUUIDs", "true")
                .build();
    }

    static SponsorBlockSegment[] parseResponse(final String responseBody, final String videoId,
                                               final long videoDurationSeconds,
                                               final Set<SponsorBlockCategory> categories)
            throws Exception {
        final JsonArray videos = JsonParser.array().from(responseBody);
        final List<SponsorBlockSegment> result = new ArrayList<>();
        final long durationMillis = videoDurationSeconds > 0
                && videoDurationSeconds <= Long.MAX_VALUE / 1000
                ? videoDurationSeconds * 1000 : Long.MAX_VALUE;

        for (final Object value : videos) {
            if (!(value instanceof JsonObject video)
                    || !videoId.equals(video.getString("videoID"))) {
                continue;
            }
            final Object segmentValue = video.get("segments");
            if (!(segmentValue instanceof JsonArray segments)) {
                continue;
            }
            for (final Object item : segments) {
                final SponsorBlockSegment segment = parseSegment(
                        item, videoDurationSeconds, durationMillis, categories);
                if (segment != null) {
                    result.add(segment);
                }
            }
        }

        result.sort(Comparator.comparingLong(SponsorBlockSegment::startTime)
                .thenComparingLong(SponsorBlockSegment::endTime));
        return result.toArray(NO_SEGMENTS);
    }

    @Nullable
    private static SponsorBlockSegment parseSegment(
            final Object value, final long videoDurationSeconds,
            final long videoDurationMillis,
            final Set<SponsorBlockCategory> selectedCategories) {
        try {
            if (!(value instanceof JsonObject object)
                    || !(object.get("segment") instanceof JsonArray times)) {
                return null;
            }

            final SponsorBlockCategory category =
                    SponsorBlockCategory.fromApiName(object.getString("category"));
            final String actionType = object.containsKey("actionType")
                    ? object.getString("actionType") : SKIP_ACTION;
            if (category == null || !selectedCategories.contains(category)
                    || !SKIP_ACTION.equals(actionType) || times.size() != 2
                    || !(times.get(0) instanceof Number startValue)
                    || !(times.get(1) instanceof Number endValue)) {
                return null;
            }

            final double startSeconds = startValue.doubleValue();
            final double endSeconds = endValue.doubleValue();
            final Number submittedDurationValue = object.getNumber("videoDuration");
            final double submittedDuration = submittedDurationValue == null
                    ? 0 : submittedDurationValue.doubleValue();
            if (!Double.isFinite(startSeconds) || !Double.isFinite(endSeconds)
                    || !Double.isFinite(submittedDuration)
                    || startSeconds < 0 || endSeconds <= startSeconds || submittedDuration < 0
                    || submittedDuration > 0 && videoDurationSeconds > 0
                    && Math.abs(submittedDuration - videoDurationSeconds) > 1) {
                return null;
            }

            final long startMillis = (long) Math.floor(startSeconds * 1000);
            final long endMillis = Math.min(
                    (long) Math.ceil(endSeconds * 1000), videoDurationMillis);
            return startMillis < endMillis
                    ? new SponsorBlockSegment(startMillis, endMillis, category) : null;
        } catch (final RuntimeException malformedSegment) {
            return null;
        }
    }

    private static String sha256(final String value) {
        try {
            final byte[] digest = MessageDigest.getInstance("SHA-256")
                    .digest(value.getBytes(StandardCharsets.UTF_8));
            final StringBuilder hex = new StringBuilder(digest.length * 2);
            for (final byte item : digest) {
                hex.append(Character.forDigit((item >>> 4) & 0xf, 16));
                hex.append(Character.forDigit(item & 0xf, 16));
            }
            return hex.toString();
        } catch (final NoSuchAlgorithmException impossible) {
            throw new IllegalStateException("SHA-256 is unavailable", impossible);
        }
    }
}

/** Small expiring cache that never exposes its stored arrays for mutation. */
final class SponsorBlockCache {
    private final int maximumSize;
    private final long populatedLifetime;
    private final long emptyLifetime;
    private final LongSupplier clock;
    private final Map<String, Entry> entries;

    private record Entry(SponsorBlockSegment[] segments, long expiresAt) {
    }

    SponsorBlockCache(final int maximumSize, final long populatedLifetime,
                      final long emptyLifetime, final LongSupplier clock) {
        this.maximumSize = maximumSize;
        this.populatedLifetime = populatedLifetime;
        this.emptyLifetime = emptyLifetime;
        this.clock = clock;
        entries = new LinkedHashMap<>(maximumSize, 0.75f, true) {
            @Override
            protected boolean removeEldestEntry(
                    final Map.Entry<String, SponsorBlockCache.Entry> eldest) {
                return size() > SponsorBlockCache.this.maximumSize;
            }
        };
    }

    @Nullable
    synchronized SponsorBlockSegment[] get(final String key) {
        final Entry entry = entries.get(key);
        if (entry == null) {
            return null;
        }
        if (entry.expiresAt() <= clock.getAsLong()) {
            entries.remove(key);
            return null;
        }
        return entry.segments().clone();
    }

    synchronized void put(final String key, final SponsorBlockSegment[] segments) {
        final long lifetime = segments.length == 0 ? emptyLifetime : populatedLifetime;
        entries.put(key, new Entry(segments.clone(), clock.getAsLong() + lifetime));
    }
}
