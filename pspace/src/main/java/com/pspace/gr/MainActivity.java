package com.pspace.gr;

import android.app.Activity;
import android.app.FragmentManager;
import android.app.FragmentTransaction;
import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.content.SharedPreferences;
import android.net.ConnectivityManager;
import android.net.http.AndroidHttpClient;
import android.os.AsyncTask;
import android.os.Bundle;
import android.text.Html;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.widget.Button;
import android.widget.TextView;

import org.apache.http.HttpEntity;
import org.apache.http.HttpResponse;
import org.apache.http.client.ClientProtocolException;
import org.apache.http.client.methods.HttpPost;
import org.apache.http.entity.BufferedHttpEntity;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;

public class MainActivity extends Activity {

    int status;
    boolean notificationState;
    boolean testMenu;

    TextView textStatus;

    private MyBroadcastReceiver myBroadcastReceiver;
    private MyBroadcastReceiver_Update myBroadcastReceiver_Update;

    String close = " <font color='#EE0000'>Closed</font>!";
    String open= " <font color='#22b327'>Open</font>!";


    private SharedPreferences settings;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);
        textStatus = (TextView)findViewById(R.id.textStatus);

        //Start MyIntentService
        Intent statusIntent = new Intent(this, StatusService.class);
        startService(statusIntent);

        FragmentManager fm = getFragmentManager();
        FragmentTransaction ft = fm.beginTransaction();

        UberdustParserFragment fragment = new UberdustParserFragment();
        ft.replace(R.id.list, fragment);
        ft.commit();

        myBroadcastReceiver = new MyBroadcastReceiver();
        myBroadcastReceiver_Update = new MyBroadcastReceiver_Update();

        //register BroadcastReceiver
        IntentFilter intentFilter = new IntentFilter(StatusService.ACTION_MyIntentService);
        intentFilter.addCategory(Intent.CATEGORY_DEFAULT);
        registerReceiver(myBroadcastReceiver, intentFilter);

        IntentFilter intentFilter_update = new IntentFilter(StatusService.ACTION_MyUpdate);
        intentFilter_update.addCategory(Intent.CATEGORY_DEFAULT);
        registerReceiver(myBroadcastReceiver_Update, intentFilter_update);

        //create the button that change code
        final Button change_button = (Button) findViewById(R.id.statusButton);
        change_button.setOnClickListener(new View.OnClickListener() {
            public void onClick(View v) {

                StatusSelect();
            }
        });

    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        // Inflate the menu; this adds items to the action bar if it is present.
        getMenuInflater().inflate(R.menu.main, menu);
        // Just for the logout
        return true;
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {

        switch (item.getItemId()) {
            case R.id.settings:
                Intent intent = new Intent();
                intent.setClass(MainActivity.this, SetPreferenceActivity.class);
                startActivityForResult(intent, 0);

                return true;
            default:
                return super.onOptionsItemSelected(item);
        }
    }

    public void StatusSelect(){ //uses a switch to control if change
        //status-button results to on or off
        String url;
        if(testMenu)
            url = getString(R.string.testpspaceurl);
        else
            url = getString(R.string.pspaceurl);

        switch (status){
            case 0:
                //Open
                new StatusChange().execute(url + "set.php?open");
                break;
            case 1:
                //Close
                new StatusChange().execute(url + "set.php?close");
                break;
        }
    }

    private class StatusChange extends AsyncTask<String, Void, Void> {

        protected Void doInBackground(String... params) {

            String url = params[0];
            AndroidHttpClient client = AndroidHttpClient
                    .newInstance("pspace_android"); //start server
            HttpPost httppost = new HttpPost(url);


            try {
                // Execute HTTP Post Request

                HttpResponse response = client.execute(httppost);

                HttpEntity ht = response.getEntity();

                BufferedHttpEntity buf = new BufferedHttpEntity(ht);

                InputStream is = buf.getContent();

                BufferedReader r = new BufferedReader(new InputStreamReader(is));

                StringBuilder total = new StringBuilder();
                String line;
                while ((line = r.readLine()) != null) {
                    total.append(line);
                }

            } catch (ClientProtocolException e) {
            } catch (IOException e) {
                e.printStackTrace();
            }
            return null;
        }
    }

    @Override
    protected void onDestroy() {
        super.onDestroy();
        //un-register BroadcastReceiver
        unregisterReceiver(myBroadcastReceiver);
        unregisterReceiver(myBroadcastReceiver_Update);
    }

    public class MyBroadcastReceiver extends BroadcastReceiver {

        @Override
        public void onReceive(Context context, Intent intent) {
            String result = intent.getStringExtra(StatusService.EXTRA_KEY_OUT);
        }
    }

    public class MyBroadcastReceiver_Update extends BroadcastReceiver {

        @Override
        public void onReceive(Context context, Intent intent) {
            status = intent.getIntExtra(StatusService.EXTRA_KEY_UPDATE, 0);
            statusIndicate();
        }
    }

    private void statusIndicate(){
        switch (status){
            case 1:
                textStatus.setText(Html.fromHtml(getString(R.string.statusview) + open));
                break;
            case 0:
                textStatus.setText(Html.fromHtml(getString(R.string.statusview) + close));
                break;
            case -2:
                textStatus.setText(getString(R.string.serverproblem));
                break;
            default:
                if(!isNetworkConnected())
                    textStatus.setText(getString(R.string.noconectivity));
                else
                    textStatus.setText(getString(R.string.conectivityproblem));
        }
    }

    private boolean isNetworkConnected() {
        ConnectivityManager cm = (ConnectivityManager) getSystemService(Context.CONNECTIVITY_SERVICE);
        return (cm.getActiveNetworkInfo() != null);
    }

}