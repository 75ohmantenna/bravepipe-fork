package org.schabi.newpipe.extractor.pvc;

import org.schabi.newpipe.extractor.exceptions.ParsingException;

@SuppressWarnings({"checkstyle:FinalLocalVariable", "checkstyle:FinalParameters"})
public class PvcCloudFlareChallengeException extends ParsingException {
    public PvcCloudFlareChallengeException(String errMsg) {
        super(errMsg);
    }
}
