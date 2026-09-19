package org.schabi.newpipe.settings;

import android.content.Intent;
import android.net.Uri;
import android.os.Bundle;
import android.view.View;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.appcompat.app.AlertDialog;
import androidx.preference.Preference;
import androidx.preference.TwoStatePreference;

import org.schabi.newpipe.R;
import org.schabi.newpipe.util.SponsorBlockSettings;


public class SponsorBlockSettingsFragment extends BasePreferenceFragment {
    private final LocalNetworkPermissionGate localNetworkPermission =
            new LocalNetworkPermissionGate(this);

    @Override
    public void onCreatePreferences(final Bundle savedInstanceState, final String rootKey) {
        addPreferencesFromResourceRegistry();

        final Preference sponsorBlockWebsitePreference =
                findPreference(getString(R.string.sponsor_block_home_page_key));
        sponsorBlockWebsitePreference.setOnPreferenceClickListener((Preference p) -> {
            final Intent i = new Intent(Intent.ACTION_VIEW,
                    Uri.parse(getString(R.string.sponsor_block_homepage_url)));
            startActivity(i);
            return true;
        });

        final Preference sponsorBlockPrivacyPreference =
                findPreference(getString(R.string.sponsor_block_privacy_key));
        sponsorBlockPrivacyPreference.setOnPreferenceClickListener((Preference p) -> {
            final Intent i = new Intent(Intent.ACTION_VIEW,
                    Uri.parse(getString(R.string.sponsor_block_privacy_policy_url)));
            startActivity(i);
            return true;
        });

        final Preference sponsorBlockApiUrlPreference =
                findPreference(getString(R.string.sponsor_block_api_url_key));
        sponsorBlockApiUrlPreference
                .setOnPreferenceChangeListener((preference, newValue) -> {
                    final String url = (String) newValue;
                    if (url.isEmpty()) {
                        updateDependencies(preference, url);
                        return true;
                    }
                    localNetworkPermission.run(url, () -> {
                        getPreferenceManager().getSharedPreferences().edit()
                                .putString(preference.getKey(), url).apply();
                        updateDependencies(preference, url);
                    });
                    return false;
                });

        final TwoStatePreference enabled =
                findPreference(getString(R.string.sponsor_block_enable_key));
        enabled.setOnPreferenceChangeListener((preference, newValue) -> {
            if (!Boolean.TRUE.equals(newValue)) {
                return true;
            }
            final String url = getPreferenceManager().getSharedPreferences()
                    .getString(getString(R.string.sponsor_block_api_url_key), "");
            localNetworkPermission.run(url, () -> enabled.setChecked(true));
            return false;
        });

        final Preference sponsorBlockClearWhitelistPreference =
                findPreference(getString(R.string.sponsor_block_clear_whitelist_key));
        sponsorBlockClearWhitelistPreference.setOnPreferenceClickListener((Preference p) -> {
            new AlertDialog.Builder(p.getContext())
                    .setMessage(R.string.sponsor_block_confirm_clear_whitelist)
                    .setPositiveButton(R.string.yes, (dialog, which) -> {
                        new SponsorBlockSettings(p.getContext(), getPreferenceManager()
                                .getSharedPreferences()).clearIgnoredUploaders();
                        Toast.makeText(p.getContext(),
                                R.string.sponsor_block_whitelist_cleared_toast,
                                Toast.LENGTH_SHORT).show();
                    })
                    .setNegativeButton(R.string.no, (dialog, which) -> {
                        dialog.dismiss();
                    })
                    .show();
            return true;
        });
    }

    @Override
    public void onViewCreated(@NonNull final View view, @Nullable final Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);

        final Preference sponsorBlockApiUrlPreference =
                findPreference(getString(R.string.sponsor_block_api_url_key));
        final String sponsorBlockApiUrlPreferenceValue =
                getPreferenceManager()
                        .getSharedPreferences()
                        .getString(getString(R.string.sponsor_block_api_url_key), null);
        updateDependencies(sponsorBlockApiUrlPreference, sponsorBlockApiUrlPreferenceValue);
        if (sponsorBlockApiUrlPreferenceValue != null
                && !sponsorBlockApiUrlPreferenceValue.isEmpty()
                && getPreferenceManager().getSharedPreferences()
                .getBoolean(getString(R.string.sponsor_block_enable_key), false)) {
            localNetworkPermission.run(sponsorBlockApiUrlPreferenceValue,
                    () -> updateDependencies(sponsorBlockApiUrlPreference,
                            sponsorBlockApiUrlPreferenceValue));
        }
    }

    @Override
    public void onDestroyView() {
        localNetworkPermission.clear();
        super.onDestroyView();
    }

    private void updateDependencies(final Preference preference, final Object newValue) {
        final boolean disabled = newValue == null || newValue.equals("");
        final int[] dependentKeys = {
            R.string.sponsor_block_enable_key,
            R.string.sponsor_block_notifications_key,
            R.string.sponsor_block_categories_key,
            R.string.sponsor_block_clear_whitelist_key
        };
        for (final int key : dependentKeys) {
            findPreference(getString(key)).onDependencyChanged(preference, disabled);
        }
    }
}
