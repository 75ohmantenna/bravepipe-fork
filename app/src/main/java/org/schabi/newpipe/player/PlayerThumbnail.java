package org.schabi.newpipe.player;

import static coil3.Image_androidKt.toBitmap;

import android.graphics.Bitmap;
import android.util.Log;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;

import org.schabi.newpipe.extractor.Image;
import org.schabi.newpipe.util.image.CoilHelper;

import java.util.List;

import coil3.target.Target;

/** Owns the current thumbnail and replaces outstanding image requests on metadata changes. */
final class PlayerThumbnail implements Target {
    private final Player player;
    @Nullable private Bitmap bitmap;
    @Nullable private coil3.request.Disposable request;

    PlayerThumbnail(final Player player) {
        this.player = player;
    }

    @Nullable
    Bitmap get() {
        return bitmap;
    }

    void load(final List<Image> thumbnails) {
        if (request != null) {
            request.dispose();
        }
        // Clear stale artwork before a new request can update media-session metadata.
        clear();
        if (!thumbnails.isEmpty()) {
            request = CoilHelper.INSTANCE.loadScaledDownThumbnail(
                    player.getContext(), thumbnails, this);
        }
    }

    void clear() {
        update(null);
    }

    private void update(@Nullable final Bitmap image) {
        if (bitmap != image) {
            bitmap = image;
            player.UIs().call(ui -> ui.onThumbnailLoaded(image));
        }
    }

    @Override
    public void onError(@Nullable final coil3.Image error) {
        Log.e(Player.TAG, "Thumbnail - onError() called");
        clear();
    }

    @Override
    public void onStart(@Nullable final coil3.Image placeholder) {
        if (Player.DEBUG) {
            Log.d(Player.TAG, "Thumbnail - onStart() called");
        }
    }

    @Override
    public void onSuccess(@NonNull final coil3.Image result) {
        update(toBitmap(result));
    }
}
