package org.schabi.newpipe.util;

import android.app.Application;
import android.content.Context;
import android.content.SharedPreferences;
import android.graphics.Color;
import android.net.ConnectivityManager;
import android.text.TextUtils;
import android.util.Log;

import androidx.preference.PreferenceManager;

import com.grack.nanojson.JsonArray;
import com.grack.nanojson.JsonObject;
import com.grack.nanojson.JsonParser;

import org.schabi.newpipe.App;
import org.schabi.newpipe.MainActivity;
import org.schabi.newpipe.R;
import org.schabi.newpipe.BraveTimeoutInterceptor;
import org.schabi.newpipe.extractor.ServiceList;
import org.schabi.newpipe.extractor.stream.StreamInfo;
import org.schabi.newpipe.player.playqueue.PlayQueueItem;
import org.schabi.newpipe.views.MarkableSeekBar;
import org.schabi.newpipe.views.SeekBarMarker;

import java.io.UnsupportedEncodingException;
import java.net.URLEncoder;
import java.nio.charset.StandardCharsets;
import java.security.MessageDigest;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.Map;

public final class SponsorBlockUtils {
    private static final Application APP = App.getInstance();
    private static final String TAG = SponsorBlockUtils.class.getSimpleName();
    private static final boolean DEBUG = MainActivity.DEBUG;
    private static Map<String, VideoSegment[]> videoSegmentsCache = new HashMap<>();

    private record Category(String name, int enabledKey, int colorKey, int defaultColor,
                            int skipToast) { }

    private static final Category[] CATEGORIES = {
        new Category("sponsor", R.string.sponsor_block_category_sponsor_key,
                R.string.sponsor_block_category_sponsor_color_key, R.color.sponsor_segment,
                R.string.sponsor_block_skip_sponsor_toast),
        new Category("intro", R.string.sponsor_block_category_intro_key,
                R.string.sponsor_block_category_intro_color_key, R.color.intro_segment,
                R.string.sponsor_block_skip_intro_toast),
        new Category("outro", R.string.sponsor_block_category_outro_key,
                R.string.sponsor_block_category_outro_color_key, R.color.outro_segment,
                R.string.sponsor_block_skip_outro_toast),
        new Category("interaction", R.string.sponsor_block_category_interaction_key,
                R.string.sponsor_block_category_interaction_color_key, R.color.interaction_segment,
                R.string.sponsor_block_skip_interaction_toast),
        new Category("selfpromo", R.string.sponsor_block_category_self_promo_key,
                R.string.sponsor_block_category_self_promo_color_key, R.color.self_promo_segment,
                R.string.sponsor_block_skip_self_promo_toast),
        new Category("music_offtopic", R.string.sponsor_block_category_non_music_key,
                R.string.sponsor_block_category_non_music_color_key, R.color.non_music_segment,
                R.string.sponsor_block_skip_non_music_toast),
        new Category("preview", R.string.sponsor_block_category_preview_key,
                R.string.sponsor_block_category_preview_color_key, R.color.preview_segment,
                R.string.sponsor_block_skip_preview_toast),
        new Category("filler", R.string.sponsor_block_category_filler_key,
                R.string.sponsor_block_category_filler_color_key, R.color.filler_segment,
                R.string.sponsor_block_skip_filler_toast)
    };

    private SponsorBlockUtils() {
    }

    @SuppressWarnings("CheckStyle")
    public static VideoSegment[] getYouTubeVideoSegments(final Context context,
                                                         final StreamInfo streamInfo)
            throws UnsupportedEncodingException {
        final SharedPreferences prefs = PreferenceManager.getDefaultSharedPreferences(context);

        final boolean isSponsorBlockEnabled = prefs.getBoolean(context
                .getString(R.string.sponsor_block_enable_key), false);

        if (!isSponsorBlockEnabled) {
            return null;
        }

        final String apiUrl = prefs.getString(context
                .getString(R.string.sponsor_block_api_url_key), null);

        if (streamInfo.getServiceId() != ServiceList.YouTube.getServiceId()
                || apiUrl == null
                || apiUrl.isEmpty()) {
            return null;
        }

        final ArrayList<String> categoryParamList = new ArrayList<>();
        for (final Category category : CATEGORIES) {
            if (prefs.getBoolean(context.getString(category.enabledKey()), false)) {
                categoryParamList.add(category.name());
            }
        }

        if (categoryParamList.isEmpty()) {
            return null;
        }

        String categoryParams = "[\"" + TextUtils.join("\",\"", categoryParamList) + "\"]";
        categoryParams = URLEncoder.encode(categoryParams, "utf-8");

        final String videoIdHash = toSha256(streamInfo.getId());

        if (videoIdHash == null) {
            return null;
        }

        final String params = "skipSegments/" + videoIdHash.substring(0, 4)
                + "?categories=" + categoryParams;

        final VideoSegment[] alreadyFetchedVideoSegments = videoSegmentsCache.get(params);
        if (alreadyFetchedVideoSegments != null) {
            return alreadyFetchedVideoSegments;
        }

        if (!isConnected()) {
            return null;
        }

        JsonArray responseArray = null;

        try {
            final String responseBody = BraveTimeoutInterceptor
                    .get(apiUrl + params, 3)
                    .responseBody();


            responseArray = JsonParser.array().from(responseBody);

        } catch (final Exception ex) {
            if (DEBUG) {
                Log.w(TAG, Log.getStackTraceString(ex));
            }
        }

        if (responseArray == null) {
            return null;
        }

        final ArrayList<VideoSegment> result = new ArrayList<>();

        for (final Object obj1 : responseArray) {
            final JsonObject jObj1 = (JsonObject) obj1;

            final String responseVideoId = jObj1.getString("videoID");
            if (!responseVideoId.equals(streamInfo.getId())) {
                continue;
            }

            final JsonArray segmentArray = (JsonArray) jObj1.get("segments");
            if (segmentArray == null) {
                continue;
            }

            for (final Object obj2 : segmentArray) {
                final JsonObject jObj2 = (JsonObject) obj2;

                final JsonArray segmentInfo = (JsonArray) jObj2.get("segment");
                if (segmentInfo == null) {
                    continue;
                }

                final double startTime = segmentInfo.getDouble(0) * 1000;
                final double endTime = segmentInfo.getDouble(1) * 1000;
                final String category = jObj2.getString("category");

                final VideoSegment segment = new VideoSegment(startTime, endTime, category);
                result.add(segment);
            }
        }
        final VideoSegment[] segments = result.toArray(new VideoSegment[0]);
        videoSegmentsCache.put(params, segments);
        return segments;
    }

    private static boolean isConnected() {
        final ConnectivityManager cm =
                (ConnectivityManager) APP.getSystemService(Context.CONNECTIVITY_SERVICE);
        return cm.getActiveNetworkInfo() != null
                && cm.getActiveNetworkInfo().isConnected();
    }

    private static String toSha256(final String videoId) {
        try {
            final MessageDigest digest = MessageDigest.getInstance("SHA-256");
            final byte[] bytes = digest.digest(videoId.getBytes(StandardCharsets.UTF_8));
            final StringBuilder sb = new StringBuilder();

            for (final byte b : bytes) {
                final String hex = Integer.toHexString(0xff & b);

                if (hex.length() == 1) {
                    sb.append('0');
                }

                sb.append(hex);
            }

            return sb.toString();
        } catch (final Exception e) {
            Log.e("SPONSOR_BLOCK", "Error getting video ID hash.", e);
            return null;
        }
    }

    static Integer parseSegmentCategory(
            final String category,
            final Context context,
            final SharedPreferences prefs
    ) {
        for (final Category definition : CATEGORIES) {
            if (category.equals(definition.name())) {
                if (!prefs.getBoolean(context.getString(definition.enabledKey()), false)) {
                    return null;
                }
                final String color = prefs.getString(context.getString(definition.colorKey()), null);
                return color == null ? context.getColor(definition.defaultColor())
                        : Color.parseColor(color);
            }
        }

        return null;
    }

    public static String getSkipToast(final Context context, final String category) {
        for (final Category definition : CATEGORIES) {
            if (category.equals(definition.name())) {
                return context.getString(definition.skipToast());
            }
        }
        return "";
    }

    public static void markSegments(
            final PlayQueueItem currentItem,
            final MarkableSeekBar seekBar,
            final Context context,
            final SharedPreferences prefs
    ) {
        seekBar.clearMarkers();

        if (currentItem == null) {
            return;
        }

        final VideoSegment[] segments = currentItem.getVideoSegments();

        if (segments == null || segments.length == 0) {
            return;
        }

        for (final VideoSegment segment : segments) {
            final Integer color = parseSegmentCategory(segment.category, context, prefs);

            // if null, then this category should not be marked
            if (color == null) {
                continue;
            }

            // Duration is in seconds, we need millis
            final int length = (int) currentItem.getDuration() * 1000;

            final SeekBarMarker seekBarMarker =
                    new SeekBarMarker(segment.startTime, segment.endTime,
                            length, color);
            seekBar.seekBarMarkers.add(seekBarMarker);
        }

        seekBar.drawMarkers();
    }
}
