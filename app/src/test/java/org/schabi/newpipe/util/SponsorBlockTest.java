package org.schabi.newpipe.util;

import org.junit.Test;

import java.util.List;
import java.util.Set;
import java.util.concurrent.atomic.AtomicLong;

import okhttp3.HttpUrl;

import static org.junit.Assert.assertArrayEquals;
import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertNotEquals;
import static org.junit.Assert.assertNull;

public class SponsorBlockTest {
    private static final String VIDEO_ID = "dQw4w9WgXcQ";

    @Test
    public void buildsDocumentedPrivacyRequest() {
        final HttpUrl url = SponsorBlock.buildRequestUrl(
                "https://sponsor.example/api/", VIDEO_ID,
                List.of(SponsorBlockCategory.SPONSOR, SponsorBlockCategory.INTRO));

        assertEquals("https://sponsor.example/api/skipSegments/5f6b", url.newBuilder()
                .query(null).build().toString());
        assertEquals("[\"sponsor\",\"intro\"]", url.queryParameter("categories"));
        assertEquals("[\"skip\"]", url.queryParameter("actionTypes"));
        assertEquals("true", url.queryParameter("trimUUIDs"));
    }

    @Test
    public void rejectsInvalidApiUrl() {
        assertNull(SponsorBlock.buildRequestUrl(
                "not a URL", VIDEO_ID, List.of(SponsorBlockCategory.SPONSOR)));
    }

    @Test
    public void parsesSortsAndBoundsSkipSegments() throws Exception {
        final String response = """
                [{"videoID":"other","segments":[
                    {"segment":[1,2],"category":"sponsor"}]},
                 {"videoID":"dQw4w9WgXcQ","segments":[
                    {"segment":[9.5,12],"category":"intro","actionType":"skip"},
                    {"segment":[1.125,2.25],"category":"sponsor","actionType":"skip"},
                    {"segment":[3,4],"category":"sponsor","actionType":"mute"},
                    {"segment":[5,6],"category":"outro","actionType":"skip"},
                    {"segment":[7,6],"category":"sponsor","actionType":"skip"},
                    {"segment":["bad",8],"category":"sponsor","actionType":"skip"}
                 ]}]
                """;

        final SponsorBlockSegment[] segments = SponsorBlock.parseResponse(
                response, VIDEO_ID, 10,
                Set.of(SponsorBlockCategory.SPONSOR, SponsorBlockCategory.INTRO));

        assertArrayEquals(new SponsorBlockSegment[]{
            new SponsorBlockSegment(1_125, 2_250, SponsorBlockCategory.SPONSOR),
            new SponsorBlockSegment(9_500, 10_000, SponsorBlockCategory.INTRO)
        }, segments);
    }

    @Test
    public void acceptsMissingActionTypeAsDocumentedDefault() throws Exception {
        final SponsorBlockSegment[] segments = SponsorBlock.parseResponse(
                "[{\"videoID\":\"dQw4w9WgXcQ\",\"segments\":["
                        + "{\"segment\":[0,1],\"category\":\"sponsor\"}]}]",
                VIDEO_ID, 10, Set.of(SponsorBlockCategory.SPONSOR));

        assertArrayEquals(new SponsorBlockSegment[]{
            new SponsorBlockSegment(0, 1_000, SponsorBlockCategory.SPONSOR)
        }, segments);
    }

    @Test
    public void rejectsSegmentsSubmittedForAStaleVideoDuration() throws Exception {
        final SponsorBlockSegment[] segments = SponsorBlock.parseResponse(
                "[{\"videoID\":\"dQw4w9WgXcQ\",\"segments\":["
                        + "{\"segment\":[0,1],\"category\":\"sponsor\","
                        + "\"videoDuration\":12.01}]}]",
                VIDEO_ID, 10, Set.of(SponsorBlockCategory.SPONSOR));

        assertArrayEquals(new SponsorBlockSegment[0], segments);
    }

    @Test
    public void acceptsUnknownAndToleratedSubmittedDurations() throws Exception {
        final SponsorBlockSegment[] segments = SponsorBlock.parseResponse(
                "[{\"videoID\":\"dQw4w9WgXcQ\",\"segments\":["
                        + "{\"segment\":[0,1],\"category\":\"sponsor\","
                        + "\"videoDuration\":0},"
                        + "{\"segment\":[2,3],\"category\":\"hook\","
                        + "\"videoDuration\":10.99}]}]",
                VIDEO_ID, 10,
                Set.of(SponsorBlockCategory.SPONSOR, SponsorBlockCategory.HOOK));

        assertArrayEquals(new SponsorBlockSegment[]{
            new SponsorBlockSegment(0, 1_000, SponsorBlockCategory.SPONSOR),
            new SponsorBlockSegment(2_000, 3_000, SponsorBlockCategory.HOOK)
        }, segments);
    }

    @Test
    public void rejectsNegativeSubmittedDuration() throws Exception {
        final SponsorBlockSegment[] segments = SponsorBlock.parseResponse(
                "[{\"videoID\":\"dQw4w9WgXcQ\",\"segments\":["
                        + "{\"segment\":[0,1],\"category\":\"sponsor\","
                        + "\"videoDuration\":-1}]}]",
                VIDEO_ID, 10, Set.of(SponsorBlockCategory.SPONSOR));

        assertArrayEquals(new SponsorBlockSegment[0], segments);
    }

    @Test
    public void separatesCacheKeysByVideoDuration() {
        final HttpUrl url = SponsorBlock.buildRequestUrl(
                "https://sponsor.example/api/", VIDEO_ID,
                List.of(SponsorBlockCategory.SPONSOR));

        assertNotEquals(SponsorBlock.cacheKey(url, VIDEO_ID, 10),
                SponsorBlock.cacheKey(url, VIDEO_ID, 11));
    }

    @Test
    public void expiresEmptyCacheEntriesSooner() {
        final AtomicLong clock = new AtomicLong();
        final SponsorBlockCache cache = new SponsorBlockCache(2, 10, 2, clock::get);
        final SponsorBlockSegment[] populated = {
            new SponsorBlockSegment(0, 1_000, SponsorBlockCategory.SPONSOR)
        };

        cache.put("populated", populated);
        cache.put("empty", new SponsorBlockSegment[0]);
        clock.set(2);

        assertNull(cache.get("empty"));
        assertArrayEquals(populated, cache.get("populated"));

        clock.set(10);
        assertNull(cache.get("populated"));
    }
}
