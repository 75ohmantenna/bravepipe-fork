package org.schabi.newpipe.util;

import android.content.Context;
import android.content.SharedPreferences;

import androidx.annotation.Nullable;
import androidx.preference.PreferenceManager;

import org.schabi.newpipe.R;

import java.util.HashSet;
import java.util.List;
import java.util.Set;

/** Typed access to all SponsorBlock preferences. */
public final class SponsorBlockSettings {
    private final Context context;
    private final SharedPreferences preferences;

    public SponsorBlockSettings(final Context context, final SharedPreferences preferences) {
        this.context = context;
        this.preferences = preferences;
    }

    public static SponsorBlockSettings from(final Context context) {
        return new SponsorBlockSettings(context,
                PreferenceManager.getDefaultSharedPreferences(context));
    }

    public boolean isEnabled() {
        return preferences.getBoolean(context.getString(R.string.sponsor_block_enable_key), false);
    }

    public boolean isOperational() {
        return isEnabled() && apiUrl() != null && !enabledCategories().isEmpty();
    }

    public boolean notificationsEnabled() {
        return preferences.getBoolean(
                context.getString(R.string.sponsor_block_notifications_key), false);
    }

    @Nullable
    public String apiUrl() {
        final String value = preferences.getString(
                context.getString(R.string.sponsor_block_api_url_key), null);
        return value == null || value.isBlank() ? null : value.trim();
    }

    public List<SponsorBlockCategory> enabledCategories() {
        return SponsorBlockCategory.enabled(context, preferences);
    }

    public SponsorBlockMode modeForUploader(@Nullable final String uploader) {
        if (!isOperational()) {
            return SponsorBlockMode.DISABLED;
        }
        return uploader != null && ignoredUploaders().contains(uploader)
                ? SponsorBlockMode.IGNORED : SponsorBlockMode.ENABLED;
    }

    public boolean toggleUploader(@Nullable final String uploader) {
        if (uploader == null || uploader.isBlank()) {
            return false;
        }

        final Set<String> ignored = ignoredUploaders();
        final boolean nowIgnored;
        if (ignored.remove(uploader)) {
            nowIgnored = false;
        } else {
            ignored.add(uploader);
            nowIgnored = true;
        }
        preferences.edit().putStringSet(
                context.getString(R.string.sponsor_block_whitelist_key), ignored).apply();
        return nowIgnored;
    }

    public void clearIgnoredUploaders() {
        preferences.edit().putStringSet(
                context.getString(R.string.sponsor_block_whitelist_key), new HashSet<>()).apply();
    }

    private Set<String> ignoredUploaders() {
        final Set<String> stored = preferences.getStringSet(
                context.getString(R.string.sponsor_block_whitelist_key), Set.of());
        return stored == null ? new HashSet<>() : new HashSet<>(stored);
    }
}
