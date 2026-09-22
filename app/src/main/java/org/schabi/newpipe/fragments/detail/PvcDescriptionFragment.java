package org.schabi.newpipe.fragments.detail;

import android.os.Bundle;
import android.view.View;
import android.view.ViewGroup;

import org.greenrobot.eventbus.Subscribe;
import org.greenrobot.eventbus.ThreadMode;
import org.schabi.newpipe.pvc.bus.PvcBus;
import org.schabi.newpipe.pvc.PvcConstants;
import org.schabi.newpipe.pvc.bus.events.PvcEvents;
import org.schabi.newpipe.util.DeviceUtils;

import androidx.annotation.NonNull;

public abstract class PvcDescriptionFragment extends BaseDescriptionFragment
        implements PvcEvents.PrefEventScrollOnlyBelowPlayer.Handler,
        PvcEvents.EventViewPagersContentHeight.Handler {

    private int originalScrollViewHeight = PvcConstants.LAYOUT_LENGTH_UNSET;

    @Override
    public void onViewCreated(
            final @NonNull View rootView,
            final Bundle savedInstanceState) {
        super.onViewCreated(rootView, savedInstanceState);
        PvcBus.getBus().register(this);
    }

    @Override
    public void onDestroyView() {
        super.onDestroyView();
        PvcBus.getBus().unregister(this);
    }

    @Override
    @Subscribe(sticky = true, threadMode = ThreadMode.MAIN)
    public void handleEventViewPagersContentHeight(
            final PvcEvents.EventViewPagersContentHeight event) {
        storeOriginalScrollViewLayoutHeightAndSetNew(event.height);
    }

    private void storeOriginalScrollViewLayoutHeightAndSetNew(
            final int height) {
        final ViewGroup.LayoutParams params = binding.scrollView.getLayoutParams();

        // store original height if not already done
        if (PvcConstants.Helper.isDimensionUnset(originalScrollViewHeight)) {
            originalScrollViewHeight = params.height;
        }

        if (params.height != height) {
            params.height = height;
            binding.scrollView.setLayoutParams(params);
            binding.scrollView.requestLayout();
        }
    }

    @Override
    @Subscribe(sticky = true, threadMode = ThreadMode.MAIN)
    public void handlePrefEventScrollOnlyBelowPlayer(
            final PvcEvents.PrefEventScrollOnlyBelowPlayer event) {
        if (!DeviceUtils.isLandscape(requireContext()) && event.doScrollInViewPager) {
            binding.scrollView.setNestedScrollingEnabled(false);
        } else {
            binding.scrollView.setNestedScrollingEnabled(true);
            if (!PvcConstants.Helper.isDimensionUnset(originalScrollViewHeight)) {
                storeOriginalScrollViewLayoutHeightAndSetNew(originalScrollViewHeight);
                originalScrollViewHeight = PvcConstants.LAYOUT_LENGTH_UNSET;
            }
        }
    }
}
