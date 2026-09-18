package org.schabi.newpipe.extractor.services.youtube.extractors;

import com.grack.nanojson.JsonObject;

import org.junit.jupiter.api.Test;
import org.schabi.newpipe.extractor.utils.Pair;

import java.util.List;

import static org.junit.jupiter.api.Assertions.assertEquals;

class YoutubeAdaptedBehaviorTest {

    @Test
    void appendsManifestParametersWithoutBreakingExistingQuery() {
        final JsonObject streamingData = JsonObject.builder()
                .value("dashManifestUrl", "https://example.com/manifest?source=web")
                .done();

        assertEquals(
                "https://example.com/manifest?source=web&pot=token&mpd_version=7",
                YoutubeStreamExtractor.getManifestUrl("dash",
                        List.of(new Pair<>(streamingData, "token")), "mpd_version=7"));
    }

    @Test
    void doesNotAddDanglingQueryMarkerToHlsManifest() {
        final JsonObject streamingData = JsonObject.builder()
                .value("hlsManifestUrl", "https://example.com/master.m3u8")
                .done();

        assertEquals("https://example.com/master.m3u8",
                YoutubeStreamExtractor.getManifestUrl("hls",
                        List.of(new Pair<>(streamingData, null)), ""));
    }

    @Test
    void extractsFirstCollaboratorFollowerCount() throws Exception {
        assertEquals(1_200_000,
                YoutubeStreamExtractor.parseSubscriberCount(
                        "5-Minute Crafts • 1.2M Abonnenten"));
    }
}
