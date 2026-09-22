package org.schabi.newpipe.extractor.services.youtube.extractors;

import com.grack.nanojson.JsonObject;

import org.schabi.newpipe.extractor.ServiceList;
import org.schabi.newpipe.extractor.services.youtube.settings.YoutubeSettings;

import static org.schabi.newpipe.extractor.utils.Utils.isNullOrEmpty;

public class PvcYoutubeShortsLockupHelper {
    private final JsonObject shortsLockupViewModel;

    public PvcYoutubeShortsLockupHelper(final JsonObject shortsLockupViewModel) {
        this.shortsLockupViewModel = shortsLockupViewModel;
    }

    // this code json property is used for both views or if it is members only
    protected String pvcExtractViewsOrMembersOnlyText() {
        return shortsLockupViewModel.getObject("overlayMetadata")
                .getObject("secondaryText")
                .getString("content");
    }

    protected boolean pvcIsMembersOnlyText(final String text) {
        return text.contains("Members only");
    }

    protected boolean pvcDoIgnoreMembersOnly() {
        if (ServiceList.YouTube.getServiceSettings()
                .isSettingEnabled(YoutubeSettings.HIDE_MEMBERS_ONLY_STREAMS)) {
            return pvcIsMembersOnly();
        }
        return false;
    }

    private boolean pvcIsMembersOnly() {
        final String membersOnlyText = pvcExtractViewsOrMembersOnlyText();
        if (!isNullOrEmpty(membersOnlyText)) {
            return pvcIsMembersOnlyText(membersOnlyText);
        }
        return false;
    }
}
