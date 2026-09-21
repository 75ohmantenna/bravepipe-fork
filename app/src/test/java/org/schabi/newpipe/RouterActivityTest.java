package org.schabi.newpipe;

import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertTrue;

import org.junit.Test;

public class RouterActivityTest {
    @Test
    public void choiceDialogKeepsRouterAliveForPopupPermissionRationale() {
        assertFalse(RouterActivity.shouldFinishAfterChoiceDialogDismissed(false, true));
    }

    @Test
    public void choiceDialogFinishesWithoutPendingWork() {
        assertTrue(RouterActivity.shouldFinishAfterChoiceDialogDismissed(false, false));
    }

    @Test
    public void choiceDialogKeepsRouterAliveForPendingExtraction() {
        assertFalse(RouterActivity.shouldFinishAfterChoiceDialogDismissed(true, false));
    }
}
