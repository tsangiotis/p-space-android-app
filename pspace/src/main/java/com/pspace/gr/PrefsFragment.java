package com.pspace.gr;

import android.os.Bundle;
import android.preference.PreferenceFragment;

/**
 * Created by tsagi on 9/9/13.
 */
public class PrefsFragment extends PreferenceFragment {

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        // Load the preferences from an XML resource
        addPreferencesFromResource(R.xml.preferences);
    }
}