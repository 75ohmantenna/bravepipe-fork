package org.schabi.newpipe.fragments.detail;

import static org.schabi.newpipe.extractor.StreamingService.ServiceInfo.MediaCapability.COMMENTS;

import android.content.SharedPreferences;
import android.graphics.Rect;
import android.util.TypedValue;
import android.view.View;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.fragment.app.Fragment;
import androidx.preference.PreferenceManager;

import org.schabi.newpipe.R;
import org.schabi.newpipe.pvc.fragments.PvcHostFragment;
import org.schabi.newpipe.databinding.FragmentVideoDetailBinding;
import org.schabi.newpipe.extractor.NewPipe;
import org.schabi.newpipe.extractor.comments.CommentsInfoItem;
import org.schabi.newpipe.extractor.exceptions.ExtractionException;
import org.schabi.newpipe.extractor.stream.StreamInfo;
import org.schabi.newpipe.fragments.EmptyFragment;
import org.schabi.newpipe.fragments.list.comments.CommentsFragment;
import org.schabi.newpipe.fragments.list.videos.RelatedItemsFragment;
import org.schabi.newpipe.util.DeviceUtils;

/** Owns detail page selection, content placement and scrolling independently of playback. */
final class DetailContentCoordinator {
    private final Fragment fragment;
    private FragmentVideoDetailBinding binding;
    private TabAdapter pageAdapter;
    private static final String COMMENTS_TAB_TAG = "COMMENTS";
    private static final String RELATED_TAB_TAG = "NEXT VIDEO";
    private static final String DESCRIPTION_TAB_TAG = "DESCRIPTION TAB";
    private static final String EMPTY_TAB_TAG = "EMPTY TAB";

    // tabs
    private boolean showComments;
    private boolean showRelatedItems;
    private boolean showDescription;
    private String selectedTabTag;
    private boolean tabSettingsChanged;
    private int lastAppBarVerticalOffset = Integer.MAX_VALUE;

    private final SharedPreferences.OnSharedPreferenceChangeListener preferenceChangeListener =
            this::onPreferenceChanged;

    private void onPreferenceChanged(final SharedPreferences sharedPreferences, final String key) {
        if (fragment.getString(R.string.show_comments_key).equals(key)) {
            showComments = sharedPreferences.getBoolean(key, true);
            tabSettingsChanged = true;
        } else if (fragment.getString(R.string.show_next_video_key).equals(key)) {
            showRelatedItems = sharedPreferences.getBoolean(key, true);
            tabSettingsChanged = true;
        } else if (fragment.getString(R.string.show_description_key).equals(key)) {
            showDescription = sharedPreferences.getBoolean(key, true);
            tabSettingsChanged = true;
        }
    }

    DetailContentCoordinator(final Fragment fragment) {
        this.fragment = fragment;
        final SharedPreferences prefs = PreferenceManager.getDefaultSharedPreferences(
                fragment.requireContext());
        showComments = prefs.getBoolean(fragment.getString(R.string.show_comments_key), true);
        showRelatedItems = prefs.getBoolean(fragment.getString(R.string.show_next_video_key), true);
        showDescription = prefs.getBoolean(fragment.getString(R.string.show_description_key), true);
        selectedTabTag = prefs.getString(
                fragment.getString(R.string.stream_info_selected_tab_key), COMMENTS_TAB_TAG);
        prefs.registerOnSharedPreferenceChangeListener(preferenceChangeListener);
    }

    void attach(final FragmentVideoDetailBinding viewBinding) {
        binding = viewBinding;
        pageAdapter = new TabAdapter(fragment.getChildFragmentManager());
        binding.viewPager.setAdapter(pageAdapter);
        binding.tabLayout.setupWithViewPager(binding.viewPager);
        binding.appBarLayout.addOnOffsetChangedListener((layout, verticalOffset) -> {
            if (verticalOffset != lastAppBarVerticalOffset) {
                lastAppBarVerticalOffset = verticalOffset;
                updateTabLayoutVisibility();
            }
        });
    }

    void detach() {
        binding = null;
    }

    void destroy() {
        PreferenceManager.getDefaultSharedPreferences(fragment.requireContext())
                .unregisterOnSharedPreferenceChangeListener(preferenceChangeListener);
    }

    void saveSelection() {
        PreferenceManager.getDefaultSharedPreferences(fragment.requireContext()).edit()
                .putString(fragment.getString(R.string.stream_info_selected_tab_key),
                        pageAdapter.getItemTitle(binding.viewPager.getCurrentItem())).apply();
    }

    void refreshSettings(final int serviceId, final String url, final String title,
                         final StreamInfo info, final boolean fullscreen) {
        if (tabSettingsChanged) {
            tabSettingsChanged = false;
            initTabs(serviceId, url, title);
            if (info != null) {
                updateTabs(info, fullscreen);
            }
        }
    }

    void updateRelatedVisibility(final boolean fullscreen, final boolean loading) {
        if (binding.relatedItemsLayout != null) {
            binding.relatedItemsLayout.setVisibility(!showRelatedItems || fullscreen
                    ? View.GONE : loading ? View.INVISIBLE : View.VISIBLE);
        }
    }

    void initTabs(final int serviceId, final String url, final String title) {
        if (pageAdapter.getCount() != 0) {
            selectedTabTag = pageAdapter.getItemTitle(binding.viewPager.getCurrentItem());
        }
        pageAdapter.clearAllItems();

        if (shouldShowComments(serviceId)) {
            pageAdapter.addFragment(PvcHostFragment.newInstance(
                    CommentsFragment.getInstance(serviceId, url, title)), COMMENTS_TAB_TAG,
                    R.drawable.ic_comment, R.string.comments_tab_description);
        }

        if (showRelatedItems && binding.relatedItemsLayout == null) {
            // temp empty fragment. will be updated in handleResult
            pageAdapter.addFragment(EmptyFragment.newInstance(false), RELATED_TAB_TAG,
                    R.drawable.ic_art_track, R.string.related_items_tab_description);
        }

        if (showDescription) {
            // temp empty fragment. will be updated in handleResult
            pageAdapter.addFragment(EmptyFragment.newInstance(false), DESCRIPTION_TAB_TAG,
                    R.drawable.ic_description, R.string.description_tab_description);
        }

        if (pageAdapter.getCount() == 0) {
            pageAdapter.addFragment(EmptyFragment.newInstance(true), EMPTY_TAB_TAG);
        }
        pageAdapter.notifyDataSetUpdate();

        if (pageAdapter.getCount() >= 2) {
            final int position = pageAdapter.getItemPositionByTitle(selectedTabTag);
            if (position != -1) {
                binding.viewPager.setCurrentItem(position);
            }
            pageAdapter.updateTabPresentation(binding.tabLayout);
        }
        // the page adapter now contains tabs: show the tab layout
        updateTabLayoutVisibility();
    }

    void updateTabs(@NonNull final StreamInfo info, final boolean fullscreen) {
        if (showRelatedItems) {
            if (binding.relatedItemsLayout == null) { // phone
                pageAdapter.updateItem(RELATED_TAB_TAG, RelatedItemsFragment.getInstance(info));
            } else { // tablet + TV
                fragment.getChildFragmentManager().beginTransaction()
                        .replace(R.id.relatedItemsLayout, RelatedItemsFragment.getInstance(info))
                        .commitAllowingStateLoss();
                binding.relatedItemsLayout.setVisibility(fullscreen ? View.GONE : View.VISIBLE);
            }
        }

        if (showDescription) {
            pageAdapter.updateItem(DESCRIPTION_TAB_TAG, new DescriptionFragment(info));
        }

        binding.viewPager.setVisibility(View.VISIBLE);
        // make sure the tab layout is visible
        updateTabLayoutVisibility();
        pageAdapter.notifyDataSetUpdate();
        pageAdapter.updateTabPresentation(binding.tabLayout);
    }

    private boolean shouldShowComments(final int serviceId) {
        try {
            return showComments && NewPipe.getService(serviceId)
                    .getServiceInfo()
                    .getMediaCapabilities()
                    .contains(COMMENTS);
        } catch (final ExtractionException e) {
            return false;
        }
    }

    public void updateTabLayoutVisibility() {

        if (binding == null) {
            //If binding is null we do not need to and should not do anything with its object(s)
            return;
        }

        if (pageAdapter.getCount() < 2 || binding.viewPager.getVisibility() != View.VISIBLE) {
            // hide tab layout if there is only one tab or if the view pager is also hidden
            binding.tabLayout.setVisibility(View.GONE);
        } else {
            // call `post()` to be sure `viewPager.getHitRect()`
            // is up to date and not being currently recomputed
            binding.tabLayout.post(() -> {
                final var activity = fragment.getActivity();
                if (activity != null && binding != null) {
                    final Rect pagerHitRect = new Rect();
                    binding.viewPager.getHitRect(pagerHitRect);

                    final int height = DeviceUtils.getWindowHeight(activity.getWindowManager());
                    final int viewPagerVisibleHeight = height - pagerHitRect.top;
                    // see TabLayout.DEFAULT_HEIGHT, which is equal to 48dp
                    final float tabLayoutHeight = TypedValue.applyDimension(
                            TypedValue.COMPLEX_UNIT_DIP, 48,
                            fragment.getResources().getDisplayMetrics());

                    if (viewPagerVisibleHeight > tabLayoutHeight * 2) {
                        // no translation at all when viewPagerVisibleHeight > tabLayout.height * 3
                        binding.tabLayout.setTranslationY(
                                Math.max(0, tabLayoutHeight * 3 - viewPagerVisibleHeight));
                        binding.tabLayout.setVisibility(View.VISIBLE);
                    } else {
                        // view pager is not visible enough
                        binding.tabLayout.setVisibility(View.GONE);
                    }
                }
            });
        }
    }

    public void scrollToTop() {
        binding.appBarLayout.setExpanded(true, true);
        // notify tab layout of scrolling
        updateTabLayoutVisibility();
    }

    public void scrollToComment(final CommentsInfoItem comment) {
        final int commentsTabPos = pageAdapter.getItemPositionByTitle(COMMENTS_TAB_TAG);
        if (commentsTabPos < 0) {
            return;
        }
        final CommentsFragment commentsFragment = resolveCommentsFragment(
                pageAdapter.getItem(commentsTabPos));
        if (commentsFragment == null) {
            return;
        }

        // unexpand the app bar only if scrolling to the comment succeeded
        if (commentsFragment.scrollToComment(comment)) {
            binding.appBarLayout.setExpanded(false, false);
            binding.viewPager.setCurrentItem(commentsTabPos, false);
        }
    }

    @Nullable
    static CommentsFragment resolveCommentsFragment(final Fragment tabFragment) {
        final Fragment resolved = tabFragment instanceof PvcHostFragment
                ? ((PvcHostFragment) tabFragment).getHostedFragment() : tabFragment;
        return resolved instanceof CommentsFragment ? (CommentsFragment) resolved : null;
    }
}
