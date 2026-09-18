package org.schabi.newpipe.util;

import android.Manifest;
import android.app.Activity;
import android.content.ActivityNotFoundException;
import android.content.Context;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.provider.Settings;
import android.text.Html;
import android.widget.Toast;

import androidx.appcompat.app.AlertDialog;
import androidx.core.app.ActivityCompat;
import androidx.core.content.ContextCompat;

import org.schabi.newpipe.App;
import org.schabi.newpipe.R;

public final class PermissionHelper {
    public static final int POST_NOTIFICATIONS_REQUEST_CODE = 779;

    private PermissionHelper() { }


    public static boolean checkPostNotificationsPermission(final Activity activity,
                                                           final int requestCode) {
        if (ContextCompat.checkSelfPermission(activity, Manifest.permission.POST_NOTIFICATIONS)
                == PackageManager.PERMISSION_GRANTED) {
            return true;
        }
        if (!App.getInstance().getNotificationsRequested()) {
            ActivityCompat.requestPermissions(activity,
                    new String[]{Manifest.permission.POST_NOTIFICATIONS}, requestCode);
            App.getInstance().setNotificationsRequested();
        }
        return false;
    }
    /**
     * Returns whether the local-network (nearby devices) permission is granted.
     *
     * @param context the context to check with
     * @return true if ACCESS_LOCAL_NETWORK is granted
     */
    public static boolean hasLocalNetworkPermission(final Context context) {
        return ContextCompat.checkSelfPermission(context, Manifest.permission.ACCESS_LOCAL_NETWORK)
                == PackageManager.PERMISSION_GRANTED;
    }

    /**
     * Requests overlay access when it has not been granted.
     *
     * @param context the context used to show the request dialog
     * @return true if overlay permission is already granted
     */
    public static boolean checkSystemAlertWindowPermission(final Context context) {
        if (Settings.canDrawOverlays(context)) {
            return true;
        }
        final String appName = context.getApplicationInfo()
                .loadLabel(context.getPackageManager()).toString();
        final String permissionName =
                context.getString(R.string.permission_display_over_apps_permission_name);
        final String message = context.getString(R.string.permission_display_over_apps_message,
                "<i>" + appName + "</i>", "<i>" + permissionName + "</i>");
        new AlertDialog.Builder(context)
                .setTitle(R.string.permission_display_over_apps)
                .setMessage(Html.fromHtml(message, Html.FROM_HTML_MODE_COMPACT))
                .setPositiveButton(R.string.ok, (dialog, which) -> {
                    final Intent intent = new Intent(Settings.ACTION_MANAGE_OVERLAY_PERMISSION)
                            .addFlags(Intent.FLAG_ACTIVITY_NEW_TASK);
                    try {
                        context.startActivity(intent);
                    } catch (final ActivityNotFoundException ignored) {
                    }
                })
                .show();
        return false;
    }

    /**
     * Determines whether the popup is enabled, and if it is not, starts the system activity to
     * request the permission with {@link #checkSystemAlertWindowPermission(Context)} and shows a
     * toast to the user explaining why the permission is needed.
     *
     * @param context the Android context
     * @return whether the popup is enabled
     */
    public static boolean isPopupEnabledElseAsk(final Context context) {
        if (checkSystemAlertWindowPermission(context)) {
            return true;
        } else {
            Toast.makeText(context, R.string.msg_popup_permission, Toast.LENGTH_LONG).show();
            return false;
        }
    }
}
