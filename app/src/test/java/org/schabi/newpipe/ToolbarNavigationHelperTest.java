package org.schabi.newpipe;

import static org.mockito.Mockito.inOrder;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.verify;

import android.view.View;

import androidx.appcompat.app.ActionBar;
import androidx.appcompat.app.ActionBarDrawerToggle;
import androidx.appcompat.widget.Toolbar;

import org.junit.Test;
import org.mockito.ArgumentCaptor;
import org.mockito.InOrder;

public class ToolbarNavigationHelperTest {
    @Test
    public void upNavigationUsesLocalizedUpDescription() {
        final Toolbar toolbar = mock(Toolbar.class);
        final ActionBarDrawerToggle toggle = mock(ActionBarDrawerToggle.class);
        final ActionBar actionBar = mock(ActionBar.class);
        final View.OnClickListener listener = mock(View.OnClickListener.class);

        ToolbarNavigationHelper.configureUpNavigation(toggle, actionBar, toolbar, listener);

        final InOrder navigationState = inOrder(toggle, actionBar);
        navigationState.verify(toggle).setDrawerIndicatorEnabled(false);
        navigationState.verify(actionBar).setDisplayHomeAsUpEnabled(true);
        navigationState.verify(actionBar).setHomeActionContentDescription(
                androidx.appcompat.R.string.abc_action_bar_up_description);
        final ArgumentCaptor<Runnable> postedUpdate = ArgumentCaptor.forClass(Runnable.class);
        verify(toolbar).post(postedUpdate.capture());
        postedUpdate.getValue().run();
        verify(toolbar).setNavigationContentDescription(
                androidx.appcompat.R.string.abc_action_bar_up_description);
        verify(toolbar).setNavigationOnClickListener(listener);
    }
}
