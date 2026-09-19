package org.schabi.newpipe.settings;

import android.content.SharedPreferences;
import android.os.Bundle;
import android.widget.Toast;

import androidx.annotation.Nullable;
import androidx.appcompat.app.AlertDialog;
import androidx.preference.Preference;

import org.schabi.newpipe.R;
import org.schabi.newpipe.settings.custom.EditColorPreference;
import org.schabi.newpipe.util.SponsorBlockCategory;

public class SponsorBlockCategoriesSettingsFragment extends BasePreferenceFragment {
    @Override
    public void onCreate(@Nullable final Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
    }

    @Override
    public void onCreatePreferences(final Bundle savedInstanceState, final String rootKey) {
        addPreferencesFromResourceRegistry();

        final Preference resetPreference =
                findPreference(getString(R.string.sponsor_block_category_reset_key));
        resetPreference.setOnPreferenceClickListener(p -> {
            new AlertDialog.Builder(p.getContext())
                    .setMessage(R.string.sponsor_block_confirm_reset_colors)
                    .setPositiveButton(R.string.yes, (dialog, which) -> {
                        final SharedPreferences.Editor editor =
                                getPreferenceManager()
                                        .getSharedPreferences()
                                        .edit();

                        for (final SponsorBlockCategory category
                                : SponsorBlockCategory.values()) {
                            setColorPreference(editor, category);
                        }

                        editor.apply();

                        Toast.makeText(p.getContext(), R.string.sponsor_block_reset_colors_toast,
                                Toast.LENGTH_SHORT).show();
                    })
                    .setNegativeButton(R.string.no, (dialog, which) -> {
                        dialog.dismiss();
                    })
                    .show();
            return true;
        });
    }

    private void setColorPreference(final SharedPreferences.Editor editor,
                                    final SponsorBlockCategory category) {
        final String colorStr = "#" + Integer.toHexString(
                getResources().getColor(category.defaultColor()));
        editor.putString(getString(category.colorKey()), colorStr);
        final EditColorPreference colorPreference =
                findPreference(getString(category.colorKey()));
        colorPreference.setText(colorStr);
    }
}
