package org.schabi.newpipe.settings.custom;

import static org.schabi.newpipe.player.notification.NotificationConstants.ACTION_RECREATE_NOTIFICATION;

import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.util.AttributeSet;
import android.view.View;

import androidx.annotation.NonNull;
import androidx.preference.Preference;
import androidx.preference.PreferenceViewHolder;

import org.schabi.newpipe.App;
import org.schabi.newpipe.R;
import org.schabi.newpipe.player.notification.NotificationConstants;

import java.util.stream.IntStream;

public class NotificationActionsPreference extends Preference {

    public NotificationActionsPreference(final Context context, final AttributeSet attrs) {
        super(context, attrs);
        setLayoutResource(R.layout.settings_notification);
    }


    private NotificationSlot[] notificationSlots;


    ////////////////////////////////////////////////////////////////////////////
    // Lifecycle
    ////////////////////////////////////////////////////////////////////////////

    @Override
    public void onBindViewHolder(@NonNull final PreferenceViewHolder holder) {
        super.onBindViewHolder(holder);

        holder.itemView.setClickable(false);
        setupActions(holder.itemView);
    }

    @Override
    public void onDetached() {
        super.onDetached();
        saveChanges();
        // set package to this app's package to prevent the intent from being seen outside
        getContext().sendBroadcast(new Intent(ACTION_RECREATE_NOTIFICATION)
                .setPackage(App.PACKAGE_NAME));
    }


    ////////////////////////////////////////////////////////////////////////////
    // Setup
    ////////////////////////////////////////////////////////////////////////////

    private void setupActions(@NonNull final View view) {
        notificationSlots = IntStream.range(0, NotificationConstants.SLOT_PREF_KEYS.length)
                .mapToObj(i -> new NotificationSlot(getContext(), getSharedPreferences(), i, view))
                .toArray(NotificationSlot[]::new);
    }



    ////////////////////////////////////////////////////////////////////////////
    // Saving
    ////////////////////////////////////////////////////////////////////////////

    private void saveChanges() {
        if (notificationSlots != null) {
            final SharedPreferences.Editor editor = getSharedPreferences().edit();

            for (int i = 0; i < notificationSlots.length; i++) {
                editor.putInt(getContext().getString(NotificationConstants.SLOT_PREF_KEYS[i]),
                        notificationSlots[i].getSelectedAction());
            }

            editor.apply();
        }
    }
}
