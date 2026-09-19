package org.schabi.newpipe.util;

public enum SponsorBlockMode {
    DISABLED,
    ENABLED,
    IGNORED;

    public SponsorBlockMode toggled() {
        return switch (this) {
            case DISABLED -> ENABLED;
            case ENABLED -> DISABLED;
            case IGNORED -> IGNORED;
        };
    }
}
