package org.schabi.newpipe.pvc.bus;

import org.greenrobot.eventbus.EventBus;
import org.schabi.newpipe.pvc.bus.events.PvcEvents;

/**
 * Wrapper class for {@link EventBus}.
 * <p>
 * -> having one common place to change the EventBus behaviour.
 */
public final class PvcBus {

    private PvcBus() { }

    /**
     * The default EventBus.
     * <p>
     * A common method to call for all {@link EventBus} interactions. So later, if a custom
     * implementation is need instead, here we have a common place to do so instead of going
     * through all the code and replace the direct calls there.
     * @return the bus we are currently using
     */
    public static EventBus getBus() {
        return EventBus.getDefault();
    }

    /**
     * Convenience methods for some PvcEvents.
     */
    public static final class Helpers {

        private static PvcEvents.EventViewPagersContentHeight eventViewPagersContentHeight =
                new PvcEvents.EventViewPagersContentHeight();

        private Helpers() { }

        public static boolean isPrefCommentRepliesSameWindowEnabled() {
            final PvcEvents.PrefEventSameWindowCommentReplies setting = getBus()
                    .getStickyEvent(PvcEvents.PrefEventSameWindowCommentReplies.class);
            return setting.doSameWindowCommentReplies;
        }

        public static void postEventViewPagersContentHeightChanged(final int height) {
            eventViewPagersContentHeight.height = height;

            final PvcEvents.PrefEventScrollOnlyBelowPlayer scrollOnlyBelowPlayer = getBus()
                    .getStickyEvent(PvcEvents.PrefEventScrollOnlyBelowPlayer.class);

            if (scrollOnlyBelowPlayer != null && scrollOnlyBelowPlayer.doScrollInViewPager) {
                getBus().postSticky(eventViewPagersContentHeight);
            }
        }
    }
}
