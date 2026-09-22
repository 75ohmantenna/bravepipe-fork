package org.schabi.newpipe;

import org.schabi.newpipe.pvc.bus.PvcSharedPrefsListenerToEventsBridge;

import androidx.preference.PreferenceManager;

public class PvcCommonApp extends android.app.Application {
    private PvcExtractorSettings extractorSettings;

    // keep a reference otherwise the listener will be garbage collected
    // sharedpreferences-onsharedpreferencechangelistener-not-being-called-consistently
    // -> https://stackoverflow.com/questions/2542938
    //
    private PvcSharedPrefsListenerToEventsBridge onPrefsChangeListener;

    @Override
    public void onCreate() {
        super.onCreate();
        extractorSettings = new PvcExtractorSettings(this);
        extractorSettings.initExtractorConfig();
        onPrefsChangeListener = new PvcSharedPrefsListenerToEventsBridge(getApplicationContext());

        PreferenceManager.getDefaultSharedPreferences(getApplicationContext())
                .registerOnSharedPreferenceChangeListener(onPrefsChangeListener);
    }

    public PvcExtractorSettings getExtractorSettings() {
        return extractorSettings;
    }
}
