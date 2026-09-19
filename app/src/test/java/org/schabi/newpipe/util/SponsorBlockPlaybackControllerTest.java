package org.schabi.newpipe.util;

import org.junit.Test;

import static org.junit.Assert.assertEquals;

public class SponsorBlockPlaybackControllerTest {
    private static final SponsorBlockSegment SEGMENT = new SponsorBlockSegment(
            0, 5_000, SponsorBlockCategory.SPONSOR);
    private static final SponsorBlockSegment[] SEGMENTS = {SEGMENT};

    @Test
    public void skipsForwardDuringNormalPlayback() {
        final SponsorBlockPlaybackController controller =
                new SponsorBlockPlaybackController();

        assertEquals(5_000, controller.target(SEGMENTS, 1_000, false));
    }

    @Test
    public void retriesAForwardSkipThatDidNotSettle() {
        final SponsorBlockPlaybackController controller =
                new SponsorBlockPlaybackController();

        assertEquals(5_000, controller.target(SEGMENTS, 1_000, false));
        assertEquals(5_000, controller.target(SEGMENTS, 2_000, false));
    }

    @Test
    public void settlesARewindWhenSegmentStartsAtZero() {
        final SponsorBlockPlaybackController controller =
                new SponsorBlockPlaybackController();
        controller.target(SEGMENTS, 4_000, false);

        assertEquals(0, controller.target(SEGMENTS, 1_000, true));
        assertEquals(-1, controller.target(SEGMENTS, 0, false));
        assertEquals(5_000, controller.target(SEGMENTS, 2_000, false));
    }

    @Test
    public void allowsAnotherRewindAfterLeavingSegment() {
        final SponsorBlockPlaybackController controller =
                new SponsorBlockPlaybackController();
        controller.target(SEGMENTS, 1_000, true);
        controller.target(SEGMENTS, 6_000, false);

        assertEquals(0, controller.target(SEGMENTS, 1_000, false));
        assertEquals(5_000, controller.target(SEGMENTS, 2_000, false));
    }

    @Test
    public void resetClearsDirectionHistory() {
        final SponsorBlockPlaybackController controller =
                new SponsorBlockPlaybackController();
        controller.target(SEGMENTS, 4_000, false);
        controller.reset();

        assertEquals(5_000, controller.target(SEGMENTS, 1_000, false));
    }

    @Test
    public void settlesRewindAcrossOverlappingSegmentsAtZero() {
        final SponsorBlockPlaybackController controller =
                new SponsorBlockPlaybackController();
        final SponsorBlockSegment[] overlapping = {
            SEGMENT,
            new SponsorBlockSegment(4_000, 8_000, SponsorBlockCategory.INTRO)
        };

        assertEquals(0, controller.target(overlapping, 3_000, true));
        assertEquals(-1, controller.target(overlapping, 0, false));
        assertEquals(8_000, controller.target(overlapping, 5_000, false));
        assertEquals(8_000, controller.target(overlapping, 7_999, false));
        assertEquals(-1, controller.target(overlapping, 8_000, false));
    }

    @Test
    public void settlesRewindAcrossTouchingSegmentsAtZero() {
        final SponsorBlockPlaybackController controller =
                new SponsorBlockPlaybackController();
        final SponsorBlockSegment[] touching = {
            SEGMENT,
            new SponsorBlockSegment(5_000, 8_000, SponsorBlockCategory.INTRO)
        };

        assertEquals(0, controller.target(touching, 3_000, true));
        assertEquals(-1, controller.target(touching, 0, false));
        assertEquals(8_000, controller.target(touching, 5_000, false));
        assertEquals(8_000, controller.target(touching, 7_999, false));
    }
}
