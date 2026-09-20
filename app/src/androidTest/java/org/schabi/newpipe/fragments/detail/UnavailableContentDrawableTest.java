package org.schabi.newpipe.fragments.detail;

import static org.assertj.core.api.Assertions.assertThat;

import android.content.Context;
import android.graphics.drawable.Drawable;

import androidx.appcompat.content.res.AppCompatResources;
import androidx.test.core.app.ApplicationProvider;
import androidx.test.ext.junit.runners.AndroidJUnit4;

import org.junit.Test;
import org.junit.runner.RunWith;
import org.schabi.newpipe.R;

@RunWith(AndroidJUnit4.class)
public class UnavailableContentDrawableTest {
    @Test
    public void unavailableArtworkHasDisplayScaleIntrinsicSize() {
        final Context context = ApplicationProvider.getApplicationContext();
        final Drawable drawable = AppCompatResources.getDrawable(
                context, R.drawable.not_available_monkey);
        final float density = context.getResources().getDisplayMetrics().density;

        assertThat(drawable).isNotNull();
        assertThat(drawable.getIntrinsicWidth()).isEqualTo(Math.round(260 * density));
        assertThat(drawable.getIntrinsicHeight()).isEqualTo(Math.round(147 * density));
    }
}
