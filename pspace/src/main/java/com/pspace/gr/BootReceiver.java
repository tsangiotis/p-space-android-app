package com.pspace.gr;

import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.preference.PreferenceManager;

/**
 * Created by tsagi on 9/10/13.
 */
public class BootReceiver extends BroadcastReceiver {
    public void onReceive(Context context, Intent intent) {

        SharedPreferences mySharedPreferences = PreferenceManager.getDefaultSharedPreferences(context);
        Boolean startOnBoot = mySharedPreferences.getBoolean("boot_preference", false);

        if(startOnBoot){
        Intent myIntent = new Intent(context, StatusService.class);
        context.startService(myIntent);
        }
    }
}
