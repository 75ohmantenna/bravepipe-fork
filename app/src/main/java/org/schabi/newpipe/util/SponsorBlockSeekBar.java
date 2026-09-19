package org.schabi.newpipe.util;

import android.content.Context;
import android.content.SharedPreferences;

import org.schabi.newpipe.player.playqueue.PlayQueueItem;
import org.schabi.newpipe.views.MarkableSeekBar;
import org.schabi.newpipe.views.SeekBarMarker;

/** Renders enabled SponsorBlock categories on a player seek bar. */
public final class SponsorBlockSeekBar {
    private SponsorBlockSeekBar() {
    }

    public static void markSegments(final PlayQueueItem currentItem,
                                    final MarkableSeekBar seekBar,
                                    final Context context,
                                    final SharedPreferences preferences) {
        seekBar.clearMarkers();
        if (currentItem == null) {
            return;
        }

        final SponsorBlockSegment[] segments = currentItem.getSponsorBlockSegments();
        final long durationSeconds = currentItem.getDuration();
        if (durationSeconds <= 0) {
            seekBar.drawMarkers();
            return;
        }
        if (segments != null) {
            final int durationMillis = durationSeconds >= Integer.MAX_VALUE / 1000
                    ? Integer.MAX_VALUE : (int) durationSeconds * 1000;
            for (final SponsorBlockSegment segment : segments) {
                if (segment.category().isEnabled(context, preferences)) {
                    seekBar.seekBarMarkers.add(new SeekBarMarker(
                            segment.startTime(), segment.endTime(), durationMillis,
                            segment.category().color(context, preferences)));
                }
            }
        }
        seekBar.drawMarkers();
    }
}
