package org.schabi.newpipe.extractor.services.bitchute.extractor;

import com.github.bravenewpipe.json2java4nanojson.bitchute.api.results.stream.video.media.ResultsStreamVideoMedia;
import com.grack.nanojson.JsonObject;

import org.junit.jupiter.api.Test;
import org.schabi.newpipe.extractor.stream.DeliveryMethod;
import org.schabi.newpipe.extractor.stream.VideoStream;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNull;

class BitchuteStreamFormatTest {

    @Test
    void marksHlsMediaAsManifest() throws Exception {
        final String url = "https://cdn.example/video/master.m3u8?token=one";
        final VideoStream stream = BitchuteStreamExtractor.buildVideoStream(
                media("application/x-mpegURL", url));

        assertEquals(DeliveryMethod.HLS, stream.getDeliveryMethod());
        assertEquals(url, stream.getManifestUrl());
    }

    @Test
    void keepsMp4MediaProgressive() throws Exception {
        final VideoStream stream = BitchuteStreamExtractor.buildVideoStream(
                media("video/mp4", "https://cdn.example/video/file.mp4?token=two"));

        assertEquals(DeliveryMethod.PROGRESSIVE_HTTP, stream.getDeliveryMethod());
        assertNull(stream.getManifestUrl());
    }

    private static ResultsStreamVideoMedia media(final String type, final String url) {
        return new ResultsStreamVideoMedia(JsonObject.builder()
                .value("media_type", type)
                .value("media_url", url)
                .value("video_id", "UGlrF9o9b-Q")
                .done());
    }
}
