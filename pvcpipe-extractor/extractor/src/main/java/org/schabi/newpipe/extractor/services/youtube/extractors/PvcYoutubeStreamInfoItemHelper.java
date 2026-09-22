package org.schabi.newpipe.extractor.services.youtube.extractors;

import com.grack.nanojson.JsonArray;
import com.grack.nanojson.JsonObject;

import org.schabi.newpipe.extractor.ServiceList;
import org.schabi.newpipe.extractor.services.youtube.settings.YoutubeSettings;

public class PvcYoutubeStreamInfoItemHelper {
    private final JsonObject videoInfo;

    public PvcYoutubeStreamInfoItemHelper(final JsonObject videoInfoItem) {
        this.videoInfo = videoInfoItem;
    }

    protected boolean pvcIsMembersOnly() {
        final JsonArray badges = videoInfo.getArray("badges");
        for (final Object badge : badges) {
            if (((JsonObject) badge).getObject("metadataBadgeRenderer")
                    .getString("label", "").equals("Members only")) {
                return true;
            }
        }
        return false;
    }

    protected boolean pvcDoIgnoreMembersOnly() {
        if (ServiceList.YouTube.getServiceSettings()
                .isSettingEnabled(YoutubeSettings.HIDE_MEMBERS_ONLY_STREAMS)) {
            return pvcIsMembersOnly();
        }
        return false;
    }
}
