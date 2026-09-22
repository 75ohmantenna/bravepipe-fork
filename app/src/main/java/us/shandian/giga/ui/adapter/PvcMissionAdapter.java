package us.shandian.giga.ui.adapter;

import android.content.Context;
import android.content.SharedPreferences;

import org.schabi.newpipe.App;
import org.schabi.newpipe.R;

import androidx.preference.PreferenceManager;
import androidx.recyclerview.widget.RecyclerView;
import us.shandian.giga.get.Mission;
import us.shandian.giga.service.DownloadManager;

public abstract class PvcMissionAdapter extends RecyclerView.Adapter<RecyclerView.ViewHolder> {

    protected void openFinishedMission(final DownloadManager.MissionItem item) {
        final Context context = App.getInstance();
        final SharedPreferences preferences =
                PreferenceManager.getDefaultSharedPreferences(context);
        if (preferences.getBoolean(context
                .getString(R.string.enable_local_player_key), false)) {
            openInternally(item.mission);
        } else {
            openExternally(item.mission);
        }
    }

    protected abstract void openInternally(Mission mission);

    protected abstract void openExternally(Mission mission);
}
