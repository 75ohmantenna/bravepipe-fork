package org.schabi.newpipe.player;

import android.util.Log;
import android.widget.Toast;

import androidx.annotation.Nullable;

import com.google.android.exoplayer2.ExoPlayer;
import com.google.android.exoplayer2.SeekParameters;

import org.schabi.newpipe.R;
import org.schabi.newpipe.player.playqueue.PlayQueueItem;
import org.schabi.newpipe.util.SponsorBlockMode;
import org.schabi.newpipe.util.SponsorBlockUtils;
import org.schabi.newpipe.util.VideoSegment;

/** Keeps skip policy and repeated-target state independent of playback UI type. */
final class SponsorBlockController {
    private SponsorBlockMode mode = SponsorBlockMode.DISABLED;
    private int lastSkipTarget = -1;

    SponsorBlockMode getMode() {
        return mode;
    }

    void setMode(final SponsorBlockMode newMode) {
        mode = newMode;
    }

    @Nullable
    static VideoSegment segmentAt(@Nullable final PlayQueueItem item, final int progress) {
        final VideoSegment[] segments = item == null ? null : item.getVideoSegments();
        if (segments != null) {
            for (final VideoSegment segment : segments) {
                if (!(progress < segment.startTime || progress > segment.endTime)) {
                    return segment;
                }
            }
        }
        return null;
    }

    void skip(final Player player, final int progress, final boolean rewind) {
        if (mode != SponsorBlockMode.ENABLED) {
            return;
        }
        final VideoSegment segment = segmentAt(player.getCurrentItem(), progress);
        if (segment == null) {
            lastSkipTarget = -1;
            return;
        }
        final int target = Math.max(0, rewind
                ? (int) Math.ceil(segment.startTime) - 1 : (int) Math.ceil(segment.endTime));
        if (lastSkipTarget == target) {
            return;
        }
        lastSkipTarget = target;

        final ExoPlayer exoPlayer = player.getExoPlayer();
        final SeekParameters previous = exoPlayer.getSeekParameters();
        exoPlayer.setSeekParameters(SeekParameters.EXACT);
        player.seekTo(target);
        exoPlayer.setSeekParameters(previous);

        final var context = player.getContext();
        if (player.getPrefs().getBoolean(
                context.getString(R.string.sponsor_block_notifications_key), false)) {
            Toast.makeText(context, SponsorBlockUtils.getSkipToast(context, segment.category),
                    Toast.LENGTH_SHORT).show();
        }
        if (Player.DEBUG) {
            Log.d("SPONSOR_BLOCK", "Skipped segment: currentProgress = ["
                    + progress + "], skipped to = [" + target + "]");
        }
    }
}
