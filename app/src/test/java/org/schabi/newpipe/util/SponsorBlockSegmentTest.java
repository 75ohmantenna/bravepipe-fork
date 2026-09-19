package org.schabi.newpipe.util;

import org.junit.Test;

import static org.junit.Assert.assertArrayEquals;
import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertNull;
import static org.junit.Assert.assertSame;

public class SponsorBlockSegmentTest {
    private static final SponsorBlockSegment FIRST = new SponsorBlockSegment(
            1_000, 5_000, SponsorBlockCategory.SPONSOR);
    private static final SponsorBlockSegment OVERLAPPING = new SponsorBlockSegment(
            4_000, 8_000, SponsorBlockCategory.INTRO);
    private static final SponsorBlockSegment LATER = new SponsorBlockSegment(
            10_000, 12_000, SponsorBlockCategory.OUTRO);
    private static final SponsorBlockSegment[] SEGMENTS = {FIRST, OVERLAPPING, LATER};

    @Test
    public void findsSegmentContainingPosition() {
        assertSame(FIRST, SponsorBlockSegment.at(SEGMENTS, 1_000));
        assertSame(OVERLAPPING, SponsorBlockSegment.at(SEGMENTS, 5_000));
        assertSame(LATER, SponsorBlockSegment.at(SEGMENTS, 11_000));
    }

    @Test
    public void returnsNullOutsideSegments() {
        assertNull(SponsorBlockSegment.at(SEGMENTS, 999));
        assertNull(SponsorBlockSegment.at(SEGMENTS, 9_000));
        assertNull(SponsorBlockSegment.at(null, 2_000));
    }

    @Test
    public void findsSegmentsInLegacyUnsortedArrays() {
        final SponsorBlockSegment[] unsorted = {LATER, FIRST, OVERLAPPING};

        assertSame(FIRST, SponsorBlockSegment.at(unsorted, 2_000));
    }

    @Test
    public void skipsAcrossOverlappingSegments() {
        assertEquals(8_000, SponsorBlockSegment.skipTarget(SEGMENTS, 2_000, false));
        assertEquals(999, SponsorBlockSegment.skipTarget(SEGMENTS, 7_000, true));
    }

    @Test
    public void reportsNoTargetOutsideSegments() {
        assertEquals(-1, SponsorBlockSegment.skipTarget(SEGMENTS, 9_000, false));
    }

    @Test
    public void roundTripsDownloadedSegmentsAndSortsLegacyJson() {
        final String legacyJson = "{\"segments\":["
                + "{\"start\":10000.0,\"end\":12000.0,\"category\":\"outro\"},"
                + "{\"start\":1000.0,\"end\":5000.0,\"category\":\"sponsor\"}]}";
        final SponsorBlockSegment[] decoded = SponsorBlockSegment.fromJson(legacyJson);

        assertEquals(FIRST, decoded[0]);
        assertEquals(LATER, decoded[1]);
        assertArrayEquals(decoded,
                SponsorBlockSegment.fromJson(SponsorBlockSegment.toJson(decoded)));
    }

    @Test
    public void ignoresMalformedDownloadedSegments() {
        assertArrayEquals(new SponsorBlockSegment[0],
                SponsorBlockSegment.fromJson("{\"segments\":[{\"start\":5}]}"));
        assertArrayEquals(new SponsorBlockSegment[0],
                SponsorBlockSegment.fromJson("not json"));
    }
}
