package org.schabi.newpipe.util;

import android.content.Context;
import android.content.SharedPreferences;
import android.graphics.Color;

import androidx.annotation.ColorInt;
import androidx.annotation.ColorRes;
import androidx.annotation.Nullable;
import androidx.annotation.StringRes;

import org.schabi.newpipe.R;

import java.util.Arrays;
import java.util.List;

/** SponsorBlock categories supported by PVCPipe and their presentation settings. */
public enum SponsorBlockCategory {
    SPONSOR("sponsor", R.string.sponsor_block_category_sponsor_key,
            R.string.sponsor_block_category_sponsor_color_key, R.color.sponsor_segment,
            R.string.sponsor_block_skip_sponsor_toast),
    INTRO("intro", R.string.sponsor_block_category_intro_key,
            R.string.sponsor_block_category_intro_color_key, R.color.intro_segment,
            R.string.sponsor_block_skip_intro_toast),
    OUTRO("outro", R.string.sponsor_block_category_outro_key,
            R.string.sponsor_block_category_outro_color_key, R.color.outro_segment,
            R.string.sponsor_block_skip_outro_toast),
    INTERACTION("interaction", R.string.sponsor_block_category_interaction_key,
            R.string.sponsor_block_category_interaction_color_key, R.color.interaction_segment,
            R.string.sponsor_block_skip_interaction_toast),
    SELF_PROMOTION("selfpromo", R.string.sponsor_block_category_self_promo_key,
            R.string.sponsor_block_category_self_promo_color_key, R.color.self_promo_segment,
            R.string.sponsor_block_skip_self_promo_toast),
    MUSIC_OFF_TOPIC("music_offtopic", R.string.sponsor_block_category_non_music_key,
            R.string.sponsor_block_category_non_music_color_key, R.color.non_music_segment,
            R.string.sponsor_block_skip_non_music_toast),
    PREVIEW("preview", R.string.sponsor_block_category_preview_key,
            R.string.sponsor_block_category_preview_color_key, R.color.preview_segment,
            R.string.sponsor_block_skip_preview_toast),
    HOOK("hook", R.string.sponsor_block_category_hook_key,
            R.string.sponsor_block_category_hook_color_key, R.color.hook_segment,
            R.string.sponsor_block_skip_hook_toast),
    FILLER("filler", R.string.sponsor_block_category_filler_key,
            R.string.sponsor_block_category_filler_color_key, R.color.filler_segment,
            R.string.sponsor_block_skip_filler_toast);

    private final String apiName;
    @StringRes
    private final int enabledKey;
    @StringRes
    private final int colorKey;
    @ColorRes
    private final int defaultColor;
    @StringRes
    private final int skipToast;

    SponsorBlockCategory(final String apiName, @StringRes final int enabledKey,
                         @StringRes final int colorKey, @ColorRes final int defaultColor,
                         @StringRes final int skipToast) {
        this.apiName = apiName;
        this.enabledKey = enabledKey;
        this.colorKey = colorKey;
        this.defaultColor = defaultColor;
        this.skipToast = skipToast;
    }

    public String apiName() {
        return apiName;
    }

    @StringRes
    public int colorKey() {
        return colorKey;
    }

    @ColorRes
    public int defaultColor() {
        return defaultColor;
    }

    public boolean isEnabled(final Context context, final SharedPreferences preferences) {
        return preferences.getBoolean(context.getString(enabledKey), false);
    }

    @ColorInt
    public int color(final Context context, final SharedPreferences preferences) {
        final String customColor = preferences.getString(context.getString(colorKey), null);
        if (customColor != null) {
            try {
                return Color.parseColor(customColor);
            } catch (final IllegalArgumentException invalidPreference) {
                // Fall through to the category default for malformed restored preferences.
            }
        }
        return context.getColor(defaultColor);
    }

    public String skipToast(final Context context) {
        return context.getString(skipToast);
    }

    @Nullable
    public static SponsorBlockCategory fromApiName(@Nullable final String name) {
        if (name == null) {
            return null;
        }
        for (final SponsorBlockCategory category : values()) {
            if (category.apiName.equals(name)) {
                return category;
            }
        }
        return null;
    }

    public static List<SponsorBlockCategory> enabled(final Context context,
                                                     final SharedPreferences preferences) {
        return Arrays.stream(values())
                .filter(category -> category.isEnabled(context, preferences))
                .toList();
    }
}
