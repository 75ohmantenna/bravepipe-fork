package org.schabi.newpipe.pvc.fragments;

import android.os.Bundle;
import android.view.View;
import android.view.ViewGroup;

import org.greenrobot.eventbus.Subscribe;
import org.greenrobot.eventbus.ThreadMode;
import org.schabi.newpipe.pvc.PvcConstants;
import org.schabi.newpipe.pvc.bus.PvcBus;
import org.schabi.newpipe.pvc.bus.events.PvcEvents;
import org.schabi.newpipe.error.UserAction;
import org.schabi.newpipe.extractor.InfoItem;
import org.schabi.newpipe.extractor.ListInfo;
import org.schabi.newpipe.fragments.list.BaseListInfoFragment;
import org.schabi.newpipe.util.DeviceUtils;

import androidx.annotation.NonNull;

public abstract class PvcEventBusBaseListInfoFragment<I extends InfoItem, L extends ListInfo<I>>
        extends BaseListInfoFragment<I, L>
        implements PvcEvents.PrefEventScrollOnlyBelowPlayer.Handler,
        PvcEvents.EventViewPagersContentHeight.Handler {

    private int originalRecyclerViewHeight = PvcConstants.LAYOUT_LENGTH_UNSET;

    protected PvcEventBusBaseListInfoFragment(
            final UserAction errorUserAction) {
        super(errorUserAction);
    }

    @Override
    public void onViewCreated(
            final @NonNull View rootView,
            final Bundle savedInstanceState) {
        super.onViewCreated(rootView, savedInstanceState);
        PvcBus.getBus().register(this);
    }

    @Override
    public void onCreate(final Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
    }

    @Override
    public void onDestroyView() {
        super.onDestroyView();
        PvcBus.getBus().unregister(this);
    }

    @Override
    public void onDestroy() {
        super.onDestroy();
    }

    @Override
    @Subscribe(sticky = true, threadMode = ThreadMode.MAIN)
    public void handleEventViewPagersContentHeight(
            final PvcEvents.EventViewPagersContentHeight event) {
        doResizeViewPagersContent(event.height);
    }

    @Override
    protected void initListeners() {
        super.initListeners();
    }

    @Override
    @Subscribe(sticky = true, threadMode = ThreadMode.MAIN)
    public void handlePrefEventScrollOnlyBelowPlayer(
            final PvcEvents.PrefEventScrollOnlyBelowPlayer event) {

        if (!DeviceUtils.isLandscape(requireContext()) && event.doScrollInViewPager) {
            itemsList.setNestedScrollingEnabled(false);
        } else {
            itemsList.setNestedScrollingEnabled(true);
            // reset to former layout height
            if (!PvcConstants.Helper.isDimensionUnset(originalRecyclerViewHeight)) {
                storeOriginalRecyclerViewLayoutHeightAndSetNew(originalRecyclerViewHeight);
                originalRecyclerViewHeight = PvcConstants.LAYOUT_LENGTH_UNSET;
            }
        }
    }

    private void storeOriginalRecyclerViewLayoutHeightAndSetNew(
            final int height) {
        final ViewGroup.LayoutParams params = itemsList.getLayoutParams();

        // store original height if not already done
        if (PvcConstants.Helper.isDimensionUnset(originalRecyclerViewHeight)) {
            originalRecyclerViewHeight = params.height;
        }
        if (params.height != height) {
            params.height = height;
            itemsList.setLayoutParams(params);
            itemsList.requestLayout();
        }
    }

    /**
     * Do the resize of the ViewPager's content View.
     *
     * @param height new height the view inside the ViewPager should have
     */
    protected void doResizeViewPagersContent(
            final int height) {
        storeOriginalRecyclerViewLayoutHeightAndSetNew(height);
    }
}
