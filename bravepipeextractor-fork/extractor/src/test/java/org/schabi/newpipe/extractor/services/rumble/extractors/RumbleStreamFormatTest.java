package org.schabi.newpipe.extractor.services.rumble.extractors;

import org.junit.jupiter.api.Tag;
import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.assertEquals;

@Tag("offline")
class RumbleStreamFormatTest {

    @Test
    void labelsResolutionAndBitrate() {
        assertEquals("1080p@3000k",
                RumbleStreamExtractor.resolutionLabel("1920x1080", 3_000_000));
    }

    @Test
    void labelsMissingResolutionAsUnknown() {
        assertEquals("unknown@1234k",
                RumbleStreamExtractor.resolutionLabel("unknown", 1_234_000));
        assertEquals("unknown@1234k",
                RumbleStreamExtractor.resolutionLabel(null, 1_234_000));
        assertEquals("unknown@1234k",
                RumbleStreamExtractor.resolutionLabel("1920x", 1_234_000));
        assertEquals("unknown@1234k",
                RumbleStreamExtractor.resolutionLabel("widex1080", 1_234_000));
    }

    @Test
    void omitsUnavailableBitrate() {
        assertEquals("720p", RumbleStreamExtractor.resolutionLabel("1280x720", 0));
        assertEquals("unknown", RumbleStreamExtractor.resolutionLabel(null, 0));
    }
}
