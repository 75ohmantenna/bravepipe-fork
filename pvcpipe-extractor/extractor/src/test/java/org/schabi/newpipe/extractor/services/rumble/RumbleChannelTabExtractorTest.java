package org.schabi.newpipe.extractor.services.rumble;

import org.junit.jupiter.api.Test;
import org.schabi.newpipe.extractor.InfoItem;
import org.schabi.newpipe.extractor.StreamingService;
import org.schabi.newpipe.extractor.channel.tabs.ChannelTabExtractor;
import org.schabi.newpipe.extractor.channel.tabs.ChannelTabs;
import org.schabi.newpipe.extractor.services.DefaultListExtractorTest;

import static org.schabi.newpipe.extractor.ServiceList.Rumble;
import static org.schabi.newpipe.extractor.services.DefaultTests.defaultTestGetPageInNewExtractor;

class RumbleChannelTabExtractorTest {

    static class All extends DefaultListExtractorTest<ChannelTabExtractor> {
        @Override protected ChannelTabExtractor createExtractor() throws Exception {
            return Rumble.getChannelTabExtractorFromId("c/Bongino", ChannelTabs.VIDEOS);
        }
        @Override public StreamingService expectedService() throws Exception { return Rumble; }
        @Override public String expectedName() throws Exception { return ChannelTabs.VIDEOS.getNameId().name(); }
        @Override public String expectedId() throws Exception { return "c/Bongino"; }
        @Override public String expectedUrlContains() throws Exception { return "https://rumble.com/c/Bongino"; }
        @Override public String expectedOriginalUrlContains() throws Exception { return "https://rumble.com/c/Bongino"; }
        @Override public InfoItem.InfoType expectedInfoItemType() { return InfoItem.InfoType.STREAM; }
        @Override public boolean expectedHasMoreItems() { return true; }

        @Test
        void testGetPageInNewExtractor() throws Exception {
            final ChannelTabExtractor newTabExtractor =
                    Rumble.getChannelTabExtractorFromId("c/Bongino", ChannelTabs.VIDEOS);
            defaultTestGetPageInNewExtractor(extractor(), newTabExtractor);
        }
    }

    static class Videos extends DefaultListExtractorTest<ChannelTabExtractor> {
        @Override protected ChannelTabExtractor createExtractor() throws Exception {
            return Rumble.getChannelTabExtractorFromId("c/Bongino/videos", ChannelTabs.VIDEOS);
        }
        @Override public StreamingService expectedService() throws Exception { return Rumble; }
        @Override public String expectedName() throws Exception { return ChannelTabs.VIDEOS.getNameId().name(); }
        @Override public String expectedId() throws Exception { return "c/Bongino/videos"; }
        @Override public String expectedUrlContains() throws Exception { return "https://rumble.com/c/Bongino/videos"; }
        @Override public String expectedOriginalUrlContains() throws Exception { return "https://rumble.com/c/Bongino/videos"; }
        @Override public InfoItem.InfoType expectedInfoItemType() { return InfoItem.InfoType.STREAM; }
        @Override public boolean expectedHasMoreItems() { return true; }

        @Test
        void testGetPageInNewExtractor() throws Exception {
            final ChannelTabExtractor newTabExtractor =
                    Rumble.getChannelTabExtractorFromId("c/Bongino/videos", ChannelTabs.VIDEOS);
            defaultTestGetPageInNewExtractor(extractor(), newTabExtractor);
        }
    }

    static class Live extends DefaultListExtractorTest<ChannelTabExtractor> {
        @Override protected ChannelTabExtractor createExtractor() throws Exception {
            return Rumble.getChannelTabExtractorFromId(
                    "c/Bongino/livestreams", ChannelTabs.LIVESTREAMS);
        }
        @Override public StreamingService expectedService() throws Exception { return Rumble; }
        @Override public String expectedName() throws Exception { return ChannelTabs.LIVESTREAMS.getNameId().name(); }
        @Override public String expectedId() throws Exception { return "c/Bongino/livestreams"; }
        @Override public String expectedUrlContains() throws Exception { return "https://rumble.com/c/Bongino/livestreams"; }
        @Override public String expectedOriginalUrlContains() throws Exception { return "https://rumble.com/c/Bongino/livestreams"; }
        @Override public InfoItem.InfoType expectedInfoItemType() { return InfoItem.InfoType.STREAM; }
        @Override public boolean expectedHasMoreItems() { return true; }

        @Test
        void testGetPageInNewExtractor() throws Exception {
            final ChannelTabExtractor newTabExtractor =
                    Rumble.getChannelTabExtractorFromId("c/Bongino/livestreams", ChannelTabs.LIVESTREAMS);
            defaultTestGetPageInNewExtractor(extractor(), newTabExtractor);
        }
    }

}
