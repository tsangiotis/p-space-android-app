package com.pspace.gr;

import android.app.ActivityManager;
import android.app.IntentService;
import android.app.Notification;
import android.app.NotificationManager;
import android.content.ComponentName;
import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.net.http.AndroidHttpClient;
import android.util.Log;

import org.apache.http.HttpResponse;
import org.apache.http.client.ClientProtocolException;
import org.apache.http.client.methods.HttpGet;
import org.apache.http.util.EntityUtils;

import java.io.IOException;
import java.util.List;
import java.util.Timer;
import java.util.TimerTask;

/**
 * Created by tsagi on 9/3/13.
 */
public class StatusService extends IntentService {

    public static final String ACTION_MyIntentService = "com.pspace.gr.RESPONSE";
    public static final String ACTION_MyUpdate = "com.pspace.gr.UPDATE";
    public static final String EXTRA_KEY_IN = "EXTRA_IN";
    public static final String EXTRA_KEY_OUT = "EXTRA_OUT";
    public static final String EXTRA_KEY_UPDATE = "EXTRA_UPDATE";

    int status = -1;
    int notifyid = -1;
    NotificationManager mNM;

    private SharedPreferences settings;
    boolean notificationState;

    int delay = 0;
    int period = 5000;

    public StatusService() {
        super("StatusService");
    }

    @Override
    public void onCreate() {
        super.onCreate();

        mNM = (NotificationManager)getSystemService(Context.NOTIFICATION_SERVICE);
    }

    @Override
    protected void onHandleIntent(Intent intent) {
        Timer timer = new Timer();
        timer.scheduleAtFixedRate(new TimerTask() {

            public void run() {
                checkStatus();
            }

        }, delay, period);
    }

    public void checkStatus(){
        restorePreferences();
        AndroidHttpClient client = AndroidHttpClient
                .newInstance("pspace_android");
        HttpGet request;
        request = new HttpGet(getResources().getString(R.string.pspaceurl));

        HttpResponse response = null;

        try {

            response = client.execute(request);
            client.close();

            switch (response.getStatusLine().getStatusCode()) {

                case 200:
                    int oldStatus = status;
                    status =  Integer.valueOf(EntityUtils.toString(response
                            .getEntity()));
                    Log.d("status", Integer.toString(status));
                    if(status != oldStatus)
                        chooseNotification();
                    break;


                default:
                    status = -1;
                    Log.d("status", Integer.toString(status));
            }

        } catch (ClientProtocolException e) {
            //no network
            e.printStackTrace();
            status = -1;
            chooseNotification();
        } catch (IOException e) {
            //http problem
            e.printStackTrace();
            status = -2;
            Log.d("status", Integer.toString(status));
        } catch (IllegalStateException e) {
            e.printStackTrace();
            status = -1;
            Log.d("status", Integer.toString(status));
        }
    }

    private void restorePreferences(){
        settings = getSharedPreferences(
                "com.pspace.gr", Context.MODE_PRIVATE);
        notificationState = settings.getBoolean("NOTIF", true);
    }

    public boolean isForeground(String myPackage){
        ActivityManager manager = (ActivityManager) getSystemService(ACTIVITY_SERVICE);
        List< ActivityManager.RunningTaskInfo > runningTaskInfo = manager.getRunningTasks(1);

        ComponentName componentInfo = runningTaskInfo.get(0).topActivity;
        if(componentInfo.getPackageName().equals(myPackage)) return true;
        return false;
    }

    /**
     * Show a notification while this service is running.
     */
    private void chooseNotification() {
        CharSequence text;

        if (status == 0) {
            text = "P-Space just closed...";
            mNM.cancelAll();
            notifyid = 0;
            showNotification(text);
        }
        if (status == 1) {
            text = "P-Space just opened!";
            mNM.cancel(0);
            notifyid = 1;
            showNotification(text);
        }
        else
            notifyid = -1;
    }

    private void showNotification(CharSequence text){

        //send update to the main activity to show that status changed
        Intent intentUpdate = new Intent();
        intentUpdate.setAction(ACTION_MyUpdate);
        intentUpdate.addCategory(Intent.CATEGORY_DEFAULT);
        intentUpdate.putExtra(EXTRA_KEY_UPDATE, status);
        sendBroadcast(intentUpdate);

        if(!isForeground("com.pspace.gr")&&notificationState){

        Bitmap icon = BitmapFactory.decodeResource(getApplicationContext().getResources(),
                R.drawable.ic_launcher);
        // Set the icon, scrolling text and timestamp

        Notification notification = new Notification.Builder(getApplicationContext())
                .setContentTitle("P-Space")
                .setContentText(text)
                .setDefaults(Notification.DEFAULT_SOUND)
                .setAutoCancel(true)
                .setOnlyAlertOnce(true)
                .setSmallIcon(R.drawable.ic_launcher)
                .setLargeIcon(icon)
                .build();

        mNM.notify(notifyid, notification);
        }
    }
}
