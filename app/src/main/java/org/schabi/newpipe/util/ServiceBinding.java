package org.schabi.newpipe.util;

import android.content.Context;
import android.content.Intent;
import android.content.ServiceConnection;

import androidx.annotation.NonNull;

/**
 * Owns the paired bind/unbind lifecycle for one Android service connection.
 *
 * <p>Android requires the same context and connection for both operations, including when
 * {@code bindService} returns {@code false} or throws {@link SecurityException}. Keeping that
 * pairing here gives every owner an idempotent cleanup operation.</p>
 */
public final class ServiceBinding {
    private final Context context;
    private final Intent intent;
    private final ServiceConnection connection;

    private boolean bound;

    public ServiceBinding(@NonNull final Context context,
                          @NonNull final Intent intent,
                          @NonNull final ServiceConnection connection) {
        this.context = context;
        this.intent = intent;
        this.connection = connection;
    }

    /**
     * Bind once. Repeated calls preserve the existing binding.
     *
     * @param flags Android service binding flags
     * @return whether a binding exists after this call
     */
    public boolean bind(final int flags) {
        if (!bound) {
            bound = true;
            final boolean serviceAvailable;
            try {
                serviceAvailable = context.bindService(intent, connection, flags);
            } catch (final SecurityException exception) {
                unbind();
                throw exception;
            }
            if (!serviceAvailable) {
                unbind();
            }
            return serviceAvailable;
        }
        return true;
    }

    /**
     * Release the binding if one exists.
     *
     * @return whether a binding was released
     */
    public boolean unbind() {
        if (!bound) {
            return false;
        }
        context.unbindService(connection);
        bound = false;
        return true;
    }

    public boolean isBound() {
        return bound;
    }
}
