package org.schabi.newpipe.extractor.services.rumble.linkHandler;

import org.schabi.newpipe.extractor.exceptions.ParsingException;
import org.schabi.newpipe.extractor.linkhandler.LinkHandler;
import org.schabi.newpipe.extractor.linkhandler.LinkHandlerFactory;
import org.schabi.newpipe.extractor.utils.Utils;

import java.net.MalformedURLException;
import java.net.URL;

public final class RumbleStreamLinkHandlerFactory extends LinkHandlerFactory {

    public static final String BASE_URL = "https://rumble.com";
    private static final RumbleStreamLinkHandlerFactory INSTANCE =
            new RumbleStreamLinkHandlerFactory();
    private static final String VIDEO_ID_PATTERN = "v[a-zA-Z0-9]{4,}";

    private RumbleStreamLinkHandlerFactory() {
    }

    public static RumbleStreamLinkHandlerFactory getInstance() {
        return INSTANCE;
    }

    private String assertId(final String id) throws ParsingException {
        if (id == null || "videos".equals(id) || !id.matches(VIDEO_ID_PATTERN)) {
            throw new ParsingException("Given string is not a Rumble Video ID: " + id);
        }
        return id;
    }

    @Override
    public String getUrl(final String id) throws ParsingException {
        return BASE_URL + "/" + assertId(id);
    }

    @Override
    public LinkHandler fromUrl(final String url) throws ParsingException {
        if (Utils.isNullOrEmpty(url)) {
            throw new IllegalArgumentException("The URL is null or empty");
        }
        final String polishedUrl = Utils.followGoogleRedirectIfNeeded(url);
        final String id = getId(polishedUrl);
        final URL parsedUrl;
        try {
            parsedUrl = Utils.stringToURL(polishedUrl);
        } catch (final MalformedURLException e) {
            throw new ParsingException("The given URL is not valid: " + polishedUrl, e);
        }
        final String canonicalUrl;
        if (parsedUrl.getPath().startsWith("/shorts/")) {
            canonicalUrl = BASE_URL + "/shorts/" + id;
        } else if (parsedUrl.getPath().startsWith("/embed/")) {
            canonicalUrl = BASE_URL + "/embed/" + id;
        } else {
            canonicalUrl = getUrl(id);
        }
        return new LinkHandler(polishedUrl, canonicalUrl, id);
    }

    @Override
    public String getId(final String urlString) throws ParsingException {
        final URL url;
        try {
            url = Utils.stringToURL(urlString);
            if (!Utils.isHTTP(url)
                    || !("rumble.com".equalsIgnoreCase(url.getHost())
                    || "www.rumble.com".equalsIgnoreCase(url.getHost()))) {
                throw new MalformedURLException();
            }
        } catch (final MalformedURLException e) {
            throw new ParsingException("The given URL is not a valid Rumble URL: " + urlString, e);
        }


        String path = url.getPath();
        if (path.startsWith("/shorts/v")) {
            path = path.substring(8);
        } else if (path.startsWith("/embed/")) {
            path = path.substring(7);
            final int dot = path.indexOf('.');
            if (dot >= 0) {
                path = path.substring(dot + 1);
            }
        } else if (path.startsWith("/v")) {
            path = path.substring(1);
        } else {
            throw new ParsingException("Unsupported Rumble video URL: " + urlString);
        }

        final int slash = path.indexOf('/');
        if (slash >= 0) {
            path = path.substring(0, slash);
        }
        final int dash = path.indexOf('-');
        final String videoId = dash >= 2 ? path.substring(0, dash) : path;

        return assertId(videoId);
    }

    @Override
    public boolean onAcceptUrl(final String url) throws ParsingException {
        try {
            getId(url);
            return true;
        } catch (final ParsingException e) {
            return false;
        }
    }

}
