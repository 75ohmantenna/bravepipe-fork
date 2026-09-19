package org.schabi.newpipe.util;

import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertTrue;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.times;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

import android.content.Context;
import android.content.Intent;
import android.content.ServiceConnection;

import org.junit.Test;

public class ServiceBindingTest {
    private final Context context = mock(Context.class);
    private final Intent intent = mock(Intent.class);
    private final ServiceConnection connection = mock(ServiceConnection.class);
    private final ServiceBinding binding = new ServiceBinding(context, intent, connection);

    @Test
    public void failedBindDoesNotAllowUnbind() {
        when(context.bindService(intent, connection, Context.BIND_AUTO_CREATE)).thenReturn(false);

        assertFalse(binding.bind(Context.BIND_AUTO_CREATE));
        assertFalse(binding.unbind());

        verify(context, never()).unbindService(connection);
    }

    @Test
    public void repeatedBindAndUnbindAreIdempotent() {
        when(context.bindService(intent, connection, Context.BIND_AUTO_CREATE)).thenReturn(true);

        assertTrue(binding.bind(Context.BIND_AUTO_CREATE));
        assertTrue(binding.bind(Context.BIND_AUTO_CREATE));
        assertTrue(binding.isBound());
        assertTrue(binding.unbind());
        assertFalse(binding.unbind());
        assertFalse(binding.isBound());

        verify(context, times(1)).bindService(intent, connection, Context.BIND_AUTO_CREATE);
        verify(context, times(1)).unbindService(connection);
    }
}
