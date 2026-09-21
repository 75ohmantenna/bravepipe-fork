package org.schabi.newpipe;

import static org.assertj.core.api.Assertions.assertThat;

import android.content.Context;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.net.Uri;

import androidx.test.core.app.ApplicationProvider;
import androidx.test.ext.junit.runners.AndroidJUnit4;

import org.junit.Test;
import org.junit.runner.RunWith;

import java.util.List;

@RunWith(AndroidJUnit4.class)
public class BandcampRadioIntentFilterTest {
    private List<String> resolve(final String url) {
        final Context context = ApplicationProvider.getApplicationContext();
        final Intent intent = new Intent(Intent.ACTION_VIEW, Uri.parse(url))
                .setPackage(context.getPackageName());

        return context.getPackageManager().queryIntentActivities(
                        intent, PackageManager.MATCH_DEFAULT_ONLY).stream()
                .map(resolveInfo -> resolveInfo.activityInfo.name)
                .toList();
    }

    @Test
    public void canonicalRadioUrlsResolveToRouterActivity() {
        assertThat(resolve("https://bandcamp.com/?show=230"))
                .containsExactly(RouterActivity.class.getName());
        assertThat(resolve("http://bandcamp.com/?show=230"))
                .containsExactly(RouterActivity.class.getName());
    }

    @Test
    public void radioFilterDoesNotClaimBandcampHomepages() {
        assertThat(resolve("https://bandcamp.com/")).isEmpty();
        assertThat(resolve("http://bandcamp.com/")).isEmpty();
    }
}
