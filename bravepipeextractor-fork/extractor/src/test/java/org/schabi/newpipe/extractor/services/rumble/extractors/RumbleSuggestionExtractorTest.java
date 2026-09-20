package org.schabi.newpipe.extractor.services.rumble.extractors;

import org.junit.jupiter.api.Test;
import org.schabi.newpipe.extractor.exceptions.ParsingException;

import java.util.List;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertInstanceOf;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.schabi.newpipe.extractor.ServiceList.Rumble;

class RumbleSuggestionExtractorTest {

    @Test
    void serviceProvidesSuggestionExtractor() {
        assertInstanceOf(RumbleSuggestionExtractor.class, Rumble.getSuggestionExtractor());
    }

    @Test
    void parsesChannelsAndCategoriesInResponseOrder() throws ParsingException {
        final String response = "{\"data\":{"
                + "\"channels\":[{\"title\":\"Tech News\"},{\"title\":\"Gadgets\"}],"
                + "\"categories\":[{\"title\":\"Technology\"}]}}";

        assertEquals(List.of("Tech News", "Gadgets", "Technology"),
                RumbleSuggestionExtractor.parseSuggestions(response));
    }

    @Test
    void skipsBlankTitlesAndDuplicates() throws ParsingException {
        final String response = "{\"data\":{"
                + "\"channels\":[{\"title\":\"News\"},{\"title\":\" \"},{}],"
                + "\"categories\":[{\"title\":\"News\"},{\"title\":\"Politics\"}]}}";

        assertEquals(List.of("News", "Politics"),
                RumbleSuggestionExtractor.parseSuggestions(response));
    }

    @Test
    void returnsEmptyListWhenDataIsMissing() throws ParsingException {
        assertEquals(List.of(), RumbleSuggestionExtractor.parseSuggestions("{}"));
    }

    @Test
    void rejectsMalformedResponses() {
        assertThrows(ParsingException.class,
                () -> RumbleSuggestionExtractor.parseSuggestions("not json"));
    }
}
