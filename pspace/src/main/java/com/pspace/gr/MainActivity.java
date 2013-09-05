package com.pspace.gr;

import android.app.Activity;
import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.net.http.AndroidHttpClient;
import android.os.AsyncTask;
import android.os.Bundle;
import android.text.Html;
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

    TextView textStatus;

    private MyBroadcastReceiver myBroadcastReceiver;
    private MyBroadcastReceiver_Update myBroadcastReceiver_Update;

    String close = "<font color='#EE0000'>Closed</font>";
    String open= "<font color='#22b327'>Open</font>";

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);
        textStatus = (TextView)findViewById(R.id.textStatus);

        //Start MyIntentService
        Intent statusIntent = new Intent(this, StatusService.class);
        startService(statusIntent);

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

    public void StatusSelect(){ //uses a switch to control if change
        //status-button results to on or off
        switch (status){
            case 0:
                //Open
                new StatusChange().execute(getString(R.string.pspaceurl) + "set.php?open");
                break;
            case 1:
                //Close
                new StatusChange().execute(getString(R.string.pspaceurl) + "set.php?close");
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
                textStatus.setText(Html.fromHtml("P-Space is " + open + "!"));
                break;
            case 0:
                textStatus.setText(Html.fromHtml("P-Space is " + close + "!"));
                break;
            case -2:
                textStatus.setText("P-Space's server problem");
                break;
            default:
                textStatus.setText("You are not connected to the network!");
        }
    }

}