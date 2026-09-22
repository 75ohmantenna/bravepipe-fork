package org.schabi.newpipe.pvc;

public final class PvcConstants {

    /**
     * just to init a dimension with a unused number. eg. WRAP_CONTENT (-2) and MATCH_PARENT (-1).
     * <p>
     * it should not be feed to any LayoutParams at all.
     */
    public static final int LAYOUT_LENGTH_UNSET = -3;

    private PvcConstants() {
    }

    public static class Helper {
        public static boolean isDimensionUnset(
                final int dimension) {
            return dimension == LAYOUT_LENGTH_UNSET;
        }
    }
}
