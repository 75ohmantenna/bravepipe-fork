package org.schabi.newpipe.util;

import androidx.annotation.Nullable;

/** Tracks playback direction and suppresses duplicate seeks while a seek is settling. */
public final class SponsorBlockPlaybackController {
    private long lastPosition = -1;
    private long lastTarget = -1;

    public void reset() {
        lastPosition = -1;
        lastTarget = -1;
    }

    /**
     * Chooses a skip target for the current position.
     *
     * @param segments sorted or unsorted skip segments
     * @param position current playback position in milliseconds
     * @param forceRewind whether the current command explicitly moves backwards
     * @return target position, or {@code -1} when playback should continue
     */
    public long target(@Nullable final SponsorBlockSegment[] segments, final long position,
                       final boolean forceRewind) {
        final boolean movedBackwards = forceRewind
                || lastPosition >= 0 && position < lastPosition;
        lastPosition = position;

        final SponsorBlockSegment current = SponsorBlockSegment.at(segments, position);
        if (current == null) {
            lastTarget = -1;
            return -1;
        }
        final long target = SponsorBlockSegment.skipTarget(segments, position, movedBackwards);
        if (target == lastTarget && position == lastTarget) {
            return -1;
        }
        lastTarget = target;
        return target;
    }
}
