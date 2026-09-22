package org.schabi.newpipe.player.helper;

import android.content.Context;
import android.widget.Toast;

import com.google.android.exoplayer2.ExoPlayer;
import com.google.android.exoplayer2.Player;


import androidx.annotation.NonNull;

/**
 * Control the internal volume.
 */
public class PvcAudioReactor {
    private final ExoPlayer player;

    private final Context context;

    public static final float MAX_INTERNAL_VOLUME = 1.0f;
    public static final float MIN_INTERNAL_VOLUME = 0.0f;
    private float pvcStoreInternalVolume; // store current internal volume

    public PvcAudioReactor(@NonNull final Context context,
                             @NonNull final ExoPlayer player) {
        this.context = context;
        this.player = player;
        this.pvcStoreInternalVolume = getInternalVolume();
    }

    // even though NewPipe uses the setVolume() command without checking
    // I will follow exoplayer recommendation
    private boolean isCommandAvailable(final int command,
                                       final String commandType) {
        if (player.isCommandAvailable(command)) {
            return true;
        } else {
            final String infoMsg = commandType + " command for internal volume is not available";
            Toast.makeText(context, infoMsg, Toast.LENGTH_LONG).show();
        }
        return false;
    }

    public void setInternalVolume(final float volume) {
        if (isCommandAvailable(Player.COMMAND_SET_VOLUME, "set")) {
            player.setVolume(volume);
        }
    }

    public float getInternalVolume() {
        if (isCommandAvailable(Player.COMMAND_GET_VOLUME, "get")) {
            return player.getVolume();
        }
        return MAX_INTERNAL_VOLUME; // I use the max volume if not available
    }

    public void pvcSaveInternalVolume() {
        pvcStoreInternalVolume = getInternalVolume();
    }

    public float pvcGetSavedInternalVolume() {
        return pvcStoreInternalVolume;
    }

    public void pvcDoMute(final boolean enableMute) {
        if (enableMute) {
            if (getInternalVolume() != MIN_INTERNAL_VOLUME) {
                pvcSaveInternalVolume();
                setInternalVolume(MIN_INTERNAL_VOLUME);
            }
        } else {
            setInternalVolume(pvcGetSavedInternalVolume());
        }
    }
}
