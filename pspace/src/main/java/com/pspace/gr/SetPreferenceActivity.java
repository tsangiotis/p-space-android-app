package com.pspace.gr;

import android.app.Activity;
import android.os.Bundle;

/**
 * Created by tsagi on 9/9/13.
 */
public class SetPreferenceActivity extends Activity {
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        getFragmentManager().beginTransaction().replace(android.R.id.content,
                new PrefsFragment()).commit();
    }
}