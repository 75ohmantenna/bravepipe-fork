package org.schabi.newpipe.player.notification;

import android.content.Context;

import androidx.annotation.DrawableRes;
import androidx.annotation.IntDef;
import androidx.annotation.NonNull;

import org.schabi.newpipe.App;
import org.schabi.newpipe.R;
import org.schabi.newpipe.util.Localization;

import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;

public final class NotificationConstants {

    private NotificationConstants() {
    }



    /*//////////////////////////////////////////////////////////////////////////
    // Intent actions
    //////////////////////////////////////////////////////////////////////////*/

    private static final String BASE_ACTION =
            App.PACKAGE_NAME + ".player.MainPlayer.";
    public static final String ACTION_CLOSE =
            BASE_ACTION + "CLOSE";
    public static final String ACTION_PLAY_PAUSE =
            BASE_ACTION + ".player.MainPlayer.PLAY_PAUSE";
    public static final String ACTION_REPEAT =
            BASE_ACTION + ".player.MainPlayer.REPEAT";
    public static final String ACTION_PLAY_NEXT =
            BASE_ACTION + ".player.MainPlayer.ACTION_PLAY_NEXT";
    public static final String ACTION_PLAY_PREVIOUS =
            BASE_ACTION + ".player.MainPlayer.ACTION_PLAY_PREVIOUS";
    public static final String ACTION_FAST_REWIND =
            BASE_ACTION + ".player.MainPlayer.ACTION_FAST_REWIND";
    public static final String ACTION_FAST_FORWARD =
            BASE_ACTION + ".player.MainPlayer.ACTION_FAST_FORWARD";
    public static final String ACTION_SHUFFLE =
            BASE_ACTION + ".player.MainPlayer.ACTION_SHUFFLE";
    public static final String ACTION_RECREATE_NOTIFICATION =
            BASE_ACTION + ".player.MainPlayer.ACTION_RECREATE_NOTIFICATION";


    public static final int NOTHING = 0;
    public static final int PREVIOUS = 1;
    public static final int NEXT = 2;
    public static final int REWIND = 3;
    public static final int FORWARD = 4;
    public static final int SMART_REWIND_PREVIOUS = 5;
    public static final int SMART_FORWARD_NEXT = 6;
    public static final int PLAY_PAUSE = 7;
    public static final int PLAY_PAUSE_BUFFERING = 8;
    public static final int REPEAT = 9;
    public static final int SHUFFLE = 10;
    public static final int CLOSE = 11;

    @Retention(RetentionPolicy.SOURCE)
    @IntDef({NOTHING, PREVIOUS, NEXT, REWIND, FORWARD,
            SMART_REWIND_PREVIOUS, SMART_FORWARD_NEXT, PLAY_PAUSE, PLAY_PAUSE_BUFFERING, REPEAT,
            SHUFFLE, CLOSE})
    public @interface Action { }

    @Action
    public static final int[] ALL_ACTIONS = {NOTHING, PREVIOUS, NEXT, REWIND, FORWARD,
            SMART_REWIND_PREVIOUS, SMART_FORWARD_NEXT, PLAY_PAUSE, PLAY_PAUSE_BUFFERING, REPEAT,
            SHUFFLE, CLOSE};

    @DrawableRes
    public static final int[] ACTION_ICONS = {
            0,
            com.google.android.exoplayer2.ui.R.drawable.exo_icon_previous,
            com.google.android.exoplayer2.ui.R.drawable.exo_icon_next,
            com.google.android.exoplayer2.ui.R.drawable.exo_icon_rewind,
            com.google.android.exoplayer2.ui.R.drawable.exo_icon_fastforward,
            com.google.android.exoplayer2.ui.R.drawable.exo_icon_previous,
            com.google.android.exoplayer2.ui.R.drawable.exo_icon_next,
            R.drawable.ic_pause,
            R.drawable.ic_hourglass_top,
            com.google.android.exoplayer2.ui.R.drawable.exo_icon_repeat_all,
            com.google.android.exoplayer2.ui.R.drawable.exo_icon_shuffle_on,
            R.drawable.ic_close,
    };


    @Action
    public static final int[] SLOT_DEFAULTS = {
            REPEAT,
            CLOSE,
    };

    public static final int[] SLOT_PREF_KEYS = {
            R.string.notification_slot_3_key,
            R.string.notification_slot_4_key,
    };


    public static String getActionName(@NonNull final Context context, @Action final int action) {
        switch (action) {
            case PREVIOUS:
                return context.getString(com.google.android.exoplayer2.ui.R.string
                        .exo_controls_previous_description);
            case NEXT:
                return context.getString(com.google.android.exoplayer2.ui.R.string
                        .exo_controls_next_description);
            case REWIND:
                return context.getString(com.google.android.exoplayer2.ui.R.string
                        .exo_controls_rewind_description);
            case FORWARD:
                return context.getString(com.google.android.exoplayer2.ui.R.string
                        .exo_controls_fastforward_description);
            case SMART_REWIND_PREVIOUS:
                return Localization.concatenateStrings(
                        context.getString(com.google.android.exoplayer2.ui.R.string
                                .exo_controls_rewind_description),
                        context.getString(com.google.android.exoplayer2.ui.R.string
                                .exo_controls_previous_description));
            case SMART_FORWARD_NEXT:
                return Localization.concatenateStrings(
                        context.getString(com.google.android.exoplayer2.ui.R.string
                                .exo_controls_fastforward_description),
                        context.getString(com.google.android.exoplayer2.ui.R.string
                                .exo_controls_next_description));
            case PLAY_PAUSE:
                return Localization.concatenateStrings(
                        context.getString(com.google.android.exoplayer2.ui.R.string
                                .exo_controls_play_description),
                        context.getString(com.google.android.exoplayer2.ui.R.string
                                .exo_controls_pause_description));
            case PLAY_PAUSE_BUFFERING:
                return Localization.concatenateStrings(
                        context.getString(com.google.android.exoplayer2.ui.R.string
                                .exo_controls_play_description),
                        context.getString(com.google.android.exoplayer2.ui.R.string
                                .exo_controls_pause_description),
                        context.getString(R.string.notification_action_buffering));
            case REPEAT:
                return context.getString(R.string.notification_action_repeat);
            case SHUFFLE:
                return context.getString(R.string.notification_action_shuffle);
            case CLOSE:
                return context.getString(R.string.close);
            case NOTHING: default:
                return context.getString(R.string.notification_action_nothing);
        }
    }

}
