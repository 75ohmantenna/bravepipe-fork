package org.schabi.newpipe;

import android.view.View;

import androidx.appcompat.app.ActionBar;
import androidx.appcompat.app.ActionBarDrawerToggle;
import androidx.appcompat.widget.Toolbar;

final class ToolbarNavigationHelper {
    private ToolbarNavigationHelper() {
    }

    static void configureUpNavigation(final ActionBarDrawerToggle toggle,
                                      final ActionBar actionBar,
                                      final Toolbar toolbar,
                                      final View.OnClickListener listener) {
        if (toggle != null) {
            toggle.setDrawerIndicatorEnabled(false);
        }
        actionBar.setDisplayHomeAsUpEnabled(true);
        actionBar.setHomeActionContentDescription(
                androidx.appcompat.R.string.abc_action_bar_up_description);
        toolbar.post(() -> toolbar.setNavigationContentDescription(
                androidx.appcompat.R.string.abc_action_bar_up_description));
        toolbar.setNavigationOnClickListener(listener);
    }
}
