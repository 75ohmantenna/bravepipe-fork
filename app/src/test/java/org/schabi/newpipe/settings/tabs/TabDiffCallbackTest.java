package org.schabi.newpipe.settings.tabs;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertTrue;

import org.junit.Test;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

public class TabDiffCallbackTest {
    @Test
    public void snapshotsListsBeforeDiffing() {
        final List<Tab> oldTabs = new ArrayList<>(Arrays.asList(
                new Tab.BlankTab(),
                new Tab.SubscriptionsTab()));
        final List<Tab> newTabs = new ArrayList<>(Arrays.asList(
                new Tab.SubscriptionsTab(),
                new Tab.FeedTab()));
        final ChooseTabsFragment.TabDiffCallback callback =
                new ChooseTabsFragment.TabDiffCallback(oldTabs, newTabs);

        oldTabs.clear();
        newTabs.clear();

        assertEquals(2, callback.getOldListSize());
        assertEquals(2, callback.getNewListSize());
        assertTrue(callback.areItemsTheSame(1, 0));
        assertFalse(callback.areItemsTheSame(0, 1));
    }

    @Test
    public void distinguishesCustomTabsOfTheSameType() {
        final Tab first = new Tab.ChannelTab(0, "https://example.com/first", "First");
        final Tab second = new Tab.ChannelTab(0, "https://example.com/second", "Second");
        final ChooseTabsFragment.TabDiffCallback callback =
                new ChooseTabsFragment.TabDiffCallback(
                        Arrays.asList(first), Arrays.asList(second));

        assertFalse(callback.areItemsTheSame(0, 0));
        assertFalse(callback.areContentsTheSame(0, 0));
    }
}
