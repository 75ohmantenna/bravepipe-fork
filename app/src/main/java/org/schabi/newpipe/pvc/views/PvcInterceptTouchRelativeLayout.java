package org.schabi.newpipe.pvc.views;

import android.content.Context;
import android.util.AttributeSet;
import android.view.MotionEvent;
import android.view.View;
import android.widget.RelativeLayout;

import org.schabi.newpipe.pvc.bus.events.PvcEvents;

/**
 * Intercept all touch events and handle the according to a listener.
 * <p>
 * In our case we do not want the controls above the comments/related items/description to be
 * scrollable in case the user enable
 * see {@link PvcEvents.PrefEventScrollOnlyBelowPlayer}
 * see https://stackoverflow.com/questions/6841971/android-intercept-and-pass-on-all-touch-events
 */
public class PvcInterceptTouchRelativeLayout extends RelativeLayout {
    private boolean mDisallowIntercept;

    public interface OnInterceptTouchEventListener {
        /*
         * If disallowIntercept is true the touch event can't be stealed and the return value is
         * ignored.
         * @see android.view.ViewGroup#onInterceptTouchEvent(android.view.MotionEvent)
         */
        boolean onInterceptTouchEvent(View view,
                                      MotionEvent ev,
                                      boolean disallowIntercept);

        /*
         * @see android.view.View#onTouchEvent(android.view.MotionEvent)
         */
        boolean onTouchEvent(View view, MotionEvent event);
    }

    private static final class DummyInterceptTouchEventListener
            implements OnInterceptTouchEventListener {

        @Override
        public boolean onInterceptTouchEvent(
                final View view,
                final MotionEvent event,
                final boolean disallowIntercept) {
            return false;
        }

        @Override
        public boolean onTouchEvent(
                final View view,
                final MotionEvent event) {
            return false;
        }
    }

    public static final class NoOuterScrollingWhileInlineCommentsEnabledListener
            implements OnInterceptTouchEventListener {

        public boolean disableTouchEventsForParents(
                final View view,
                final MotionEvent motionEvent) {
            switch (motionEvent.getAction()) {
                case MotionEvent.ACTION_DOWN:
                    view.getParent().requestDisallowInterceptTouchEvent(true);
                    break;
                case MotionEvent.ACTION_UP:
                    view.getParent().requestDisallowInterceptTouchEvent(false);
                    break;
            }
            return false;
        }
        @Override
        public boolean onInterceptTouchEvent(
                final View view,
                final MotionEvent event,
                final boolean disallowIntercept) {
            disableTouchEventsForParents(view, event);
            return false;
        }
        @Override
        public boolean onTouchEvent(
                final View view,
                final MotionEvent event) {
            return disableTouchEventsForParents(view, event);
        }
    }

    private static final OnInterceptTouchEventListener DUMMY_LISTENER =
            new DummyInterceptTouchEventListener();

    private OnInterceptTouchEventListener mInterceptTouchEventListener = DUMMY_LISTENER;

    public PvcInterceptTouchRelativeLayout(
            final Context context) {
        super(context);
    }

    public PvcInterceptTouchRelativeLayout(
            final Context context,
            final AttributeSet attrs) {
        super(context, attrs);
    }

    public PvcInterceptTouchRelativeLayout(
            final Context context,
            final AttributeSet attrs,
            final int defStyleAttr) {
        super(context, attrs, defStyleAttr);
    }

    public PvcInterceptTouchRelativeLayout(
            final Context context,
            final AttributeSet attrs,
            final int defStyleAttr,
            final int defStyle) {
        super(context, attrs, defStyleAttr, defStyle);
    }

    @Override
    public void requestDisallowInterceptTouchEvent(final boolean disallowIntercept) {
        getParent().requestDisallowInterceptTouchEvent(disallowIntercept);
        mDisallowIntercept = disallowIntercept;
    }

    public void setOnInterceptTouchEventListener(
            final OnInterceptTouchEventListener interceptTouchEventListener) {
        mInterceptTouchEventListener =
                interceptTouchEventListener != null ? interceptTouchEventListener : DUMMY_LISTENER;
    }

    @Override
    public boolean onInterceptTouchEvent(
            final MotionEvent ev) {
        final boolean stealTouchEvent =
                mInterceptTouchEventListener.onInterceptTouchEvent(this, ev, mDisallowIntercept);
        return stealTouchEvent && !mDisallowIntercept || super.onInterceptTouchEvent(ev);
    }

    @Override
    public boolean onTouchEvent(
            final MotionEvent event) {
        final boolean handled = mInterceptTouchEventListener.onTouchEvent(this, event);
        return handled || super.onTouchEvent(event);
    }
}
