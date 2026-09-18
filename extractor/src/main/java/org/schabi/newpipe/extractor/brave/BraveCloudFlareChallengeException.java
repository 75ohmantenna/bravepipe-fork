package org.schabi.newpipe.extractor.brave;

import org.schabi.newpipe.extractor.exceptions.ParsingException;

@SuppressWarnings({"checkstyle:FinalLocalVariable", "checkstyle:FinalParameters"})
public class BraveCloudFlareChallengeException extends ParsingException {
    public BraveCloudFlareChallengeException(String errMsg) {
        super(errMsg);
    }
}
