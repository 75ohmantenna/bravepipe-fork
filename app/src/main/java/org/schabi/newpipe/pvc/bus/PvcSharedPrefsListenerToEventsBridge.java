package org.schabi.newpipe.pvc.bus;

import android.content.Context;
import android.content.SharedPreferences;

import org.schabi.newpipe.R;
import org.schabi.newpipe.pvc.bus.events.PvcEvents;
import org.schabi.newpipe.pvc.feature.challenge.PvcCfChallengeConfig;

import androidx.annotation.Nullable;
import androidx.annotation.StringRes;
import androidx.preference.PreferenceManager;

/**
 * generate Events from some PVCPipe specific config options.
 *
 * Not limited to EventBus Events
 */
public class PvcSharedPrefsListenerToEventsBridge
        implements SharedPreferences.OnSharedPreferenceChangeListener {
    private final Context context;

    public PvcSharedPrefsListenerToEventsBridge(
            final Context context) {
        this.context = context;
        initSharedPrefs();
    }

    @Override
    public void onSharedPreferenceChanged(
            final SharedPreferences prefs,
            @Nullable final String key) {

        if (hasPrefChanged(R.string.pvc_settings_scroll_only_below_player_key, key)) {
            postPrefEventScrollOnlyBelowPlayer(prefs);
        } else if (hasPrefChanged(R.string.pvc_settings_comment_replies_same_window_key, key)) {
            postPrefEventSameWindowCommentReplies(prefs);
        } else if (hasPrefChanged(
                R.string.pvc_settings_handle_cloudflare_challenge_interactive_enable_key, key)) {
            postPrefCloudflareChallengeInteractive(prefs);
        }
    }

    private void postPrefCloudflareChallengeInteractive(final SharedPreferences prefs) {
        final boolean isInteractive = prefs.getBoolean(context.getString(
                        R.string.pvc_settings_handle_cloudflare_challenge_interactive_enable_key),
                false);
        PvcCfChallengeConfig.INSTANCE.updateFloatingVisible(isInteractive);
    }
    private void initPrefCloudflareChallengeInteractive(final SharedPreferences prefs) {
        final boolean isInteractive = prefs.getBoolean(context.getString(
                        R.string.pvc_settings_handle_cloudflare_challenge_interactive_enable_key),
                false);
        PvcCfChallengeConfig.INSTANCE.init(isInteractive);
    }

    private void initSharedPrefs() {
        final SharedPreferences prefs = PreferenceManager.getDefaultSharedPreferences(context);
        postPrefEventScrollOnlyBelowPlayer(prefs);
        postPrefEventSameWindowCommentReplies(prefs);
        initPrefCloudflareChallengeInteractive(prefs);
    }

    private void postPrefEventSameWindowCommentReplies(
            final SharedPreferences prefs) {
        final boolean doSameWindowCommentReplies = prefs.getBoolean(context.getString(
                R.string.pvc_settings_comment_replies_same_window_key), false);

        PvcBus.getBus().postSticky(new PvcEvents
                .PrefEventSameWindowCommentReplies(doSameWindowCommentReplies));
    }

    private void postPrefEventScrollOnlyBelowPlayer(
            final SharedPreferences prefs) {
        final boolean doScrollOnlyInViewPager = prefs.getBoolean(context.getString(
                R.string.pvc_settings_scroll_only_below_player_key), false);

        PvcBus.getBus().postSticky(new PvcEvents
                .PrefEventScrollOnlyBelowPlayer(doScrollOnlyInViewPager));
    }

    private boolean hasPrefChanged(
            @StringRes final int resId,
            @Nullable final String key) {
        return context.getString(resId).equals(key);
    }
}
