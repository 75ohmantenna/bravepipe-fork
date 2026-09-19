package org.schabi.newpipe.util;

import androidx.annotation.Nullable;

import com.grack.nanojson.JsonArray;
import com.grack.nanojson.JsonObject;
import com.grack.nanojson.JsonParser;
import com.grack.nanojson.JsonStringWriter;
import com.grack.nanojson.JsonWriter;

import java.io.Serializable;
import java.util.ArrayList;
import java.util.Comparator;
import java.util.List;
import java.util.Objects;

/**
 * An immutable, validated SponsorBlock skip interval in milliseconds.
 *
 * @param startTime inclusive start time
 * @param endTime exclusive end time
 * @param category segment category
 */
public record SponsorBlockSegment(long startTime, long endTime, SponsorBlockCategory category)
        implements Serializable {

    public SponsorBlockSegment {
        if (startTime < 0 || endTime <= startTime) {
            throw new IllegalArgumentException("A segment must have a positive duration");
        }
        Objects.requireNonNull(category);
    }

    @Nullable
    public static SponsorBlockSegment at(@Nullable final SponsorBlockSegment[] segments,
                                         final long position) {
        if (segments == null) {
            return null;
        }
        for (final SponsorBlockSegment segment : segments) {
            if (position < segment.startTime) {
                continue;
            }
            if (position < segment.endTime) {
                return segment;
            }
        }
        return null;
    }

    /**
     * Finds the outside edge of every overlapping skip interval around {@code position}.
     * This prevents a chain of seeks when categories overlap.
     */
    public static long skipTarget(@Nullable final SponsorBlockSegment[] segments,
                                  final long position, final boolean rewind) {
        final SponsorBlockSegment current = at(segments, position);
        if (current == null) {
            return -1;
        }

        long boundary = rewind ? current.startTime : current.endTime;
        boolean expanded;
        do {
            expanded = false;
            for (final SponsorBlockSegment segment : segments) {
                if (segment.startTime <= boundary && segment.endTime >= boundary) {
                    final long candidate = rewind ? segment.startTime : segment.endTime;
                    if (rewind ? candidate < boundary : candidate > boundary) {
                        boundary = candidate;
                        expanded = true;
                    }
                }
            }
        } while (expanded);

        return Math.max(0, rewind ? boundary - 1 : boundary);
    }

    public static String toJson(final SponsorBlockSegment[] segments) {
        final JsonStringWriter writer = JsonWriter.string().object().array("segments");
        for (final SponsorBlockSegment segment : segments) {
            writer.object()
                    .value("start", segment.startTime)
                    .value("end", segment.endTime)
                    .value("category", segment.category.apiName())
                    .end();
        }
        return writer.end().end().done();
    }

    public static SponsorBlockSegment[] fromJson(@Nullable final String json) {
        if (json == null || json.isBlank()) {
            return new SponsorBlockSegment[0];
        }

        final List<SponsorBlockSegment> result = new ArrayList<>();
        try {
            final JsonArray values = JsonParser.object().from(json).getArray("segments");
            if (values == null) {
                return new SponsorBlockSegment[0];
            }
            for (final Object value : values) {
                if (!(value instanceof JsonObject object)
                        || !(object.get("start") instanceof Number start)
                        || !(object.get("end") instanceof Number end)) {
                    continue;
                }
                final SponsorBlockCategory category =
                        SponsorBlockCategory.fromApiName(object.getString("category"));
                if (category != null && start.longValue() >= 0
                        && end.longValue() > start.longValue()) {
                    result.add(new SponsorBlockSegment(
                            start.longValue(), end.longValue(), category));
                }
            }
        } catch (final Exception malformedJson) {
            return new SponsorBlockSegment[0];
        }

        result.sort(Comparator.comparingLong(SponsorBlockSegment::startTime)
                .thenComparingLong(SponsorBlockSegment::endTime));
        return result.toArray(new SponsorBlockSegment[0]);
    }
}
