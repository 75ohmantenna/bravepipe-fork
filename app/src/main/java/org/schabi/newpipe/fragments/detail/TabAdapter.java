package org.schabi.newpipe.fragments.detail;

import android.view.ViewGroup;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.fragment.app.Fragment;
import androidx.fragment.app.FragmentManager;
import androidx.fragment.app.FragmentPagerAdapter;

import com.google.android.material.tabs.TabLayout;

import java.util.ArrayList;
import java.util.List;

public class TabAdapter extends FragmentPagerAdapter {
    private static final class TabEntry {
        private Fragment fragment;
        private final String title;
        private final int icon;
        private final int description;

        TabEntry(final Fragment fragment, final String title, final int icon,
                 final int description) {
            this.fragment = fragment;
            this.title = title;
            this.icon = icon;
            this.description = description;
        }
    }

    private final List<TabEntry> tabs = new ArrayList<>();
    private final FragmentManager fragmentManager;

    public TabAdapter(final FragmentManager fm) {
        // Resume-only behavior crashes when opening a stream enqueued in the background:
        // "Cannot setMaxLifecycle for Fragment not attached to FragmentManager".
        super(fm, BEHAVIOR_SET_USER_VISIBLE_HINT);
        this.fragmentManager = fm;
    }

    @NonNull
    @Override
    public Fragment getItem(final int position) {
        return tabs.get(position).fragment;
    }

    @Override
    public int getCount() {
        return tabs.size();
    }

    public void addFragment(final Fragment fragment, final String title) {
        addFragment(fragment, title, 0, 0);
    }

    void addFragment(final Fragment fragment, final String title, final int icon,
                     final int description) {
        tabs.add(new TabEntry(fragment, title, icon, description));
    }

    public void clearAllItems() {
        tabs.clear();
    }

    public void removeItem(final int position) {
        tabs.remove(position == 0 ? 0 : position - 1);
    }

    public void updateItem(final int position, final Fragment fragment) {
        tabs.get(position).fragment = fragment;
    }

    public void updateItem(final String title, final Fragment fragment) {
        final int index = getItemPositionByTitle(title);
        if (index != -1) {
            updateItem(index, fragment);
        }
    }

    @Override
    public int getItemPosition(@NonNull final Object object) {
        for (int i = 0; i < tabs.size(); i++) {
            if (tabs.get(i).fragment.equals(object)) {
                return i;
            }
        }
        return POSITION_NONE;
    }

    public int getItemPositionByTitle(final String title) {
        for (int i = 0; i < tabs.size(); i++) {
            if (java.util.Objects.equals(tabs.get(i).title, title)) {
                return i;
            }
        }
        return -1;
    }

    @Nullable
    public String getItemTitle(final int position) {
        return position < 0 || position >= tabs.size() ? null : tabs.get(position).title;
    }

    public void notifyDataSetUpdate() {
        notifyDataSetChanged();
    }

    void updateTabPresentation(final TabLayout layout) {
        for (int i = 0; i < tabs.size(); i++) {
            final TabEntry entry = tabs.get(i);
            final TabLayout.Tab tab = layout.getTabAt(i);
            if (tab != null && entry.icon != 0) {
                tab.setIcon(entry.icon);
                tab.setContentDescription(entry.description);
            }
        }
    }

    @Override
    public void destroyItem(@NonNull final ViewGroup container,
                            final int position,
                            @NonNull final Object object) {
        fragmentManager.beginTransaction().remove((Fragment) object).commitNowAllowingStateLoss();
    }
}
