package us.shandian.giga.ui.fragment;

import android.net.Uri;

import androidx.annotation.Nullable;
import androidx.lifecycle.SavedStateHandle;
import androidx.lifecycle.ViewModel;

import us.shandian.giga.get.DownloadMission;

/** Keeps an in-progress mission recovery stable across fragment and activity recreation. */
public final class MissionRecoveryViewModel extends ViewModel {
    private static final String MISSION_TIMESTAMP_KEY = "mission_timestamp";
    private static final String DESTINATION_URI_KEY = "destination_uri";

    private final SavedStateHandle state;

    public MissionRecoveryViewModel(final SavedStateHandle state) {
        this.state = state;
    }

    public void begin(final DownloadMission mission) {
        state.set(MISSION_TIMESTAMP_KEY, mission.timestamp);
        state.remove(DESTINATION_URI_KEY);
    }

    public void setDestination(@Nullable final Uri destination) {
        state.set(DESTINATION_URI_KEY, destination);
    }

    @Nullable
    public Long getMissionTimestamp() {
        return state.get(MISSION_TIMESTAMP_KEY);
    }

    @Nullable
    public Uri getDestination() {
        return state.get(DESTINATION_URI_KEY);
    }

    public void clear() {
        state.remove(MISSION_TIMESTAMP_KEY);
        state.remove(DESTINATION_URI_KEY);
    }
}
