package com.example.pspace;

//import java.io.BufferedInputStream;
import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;

import org.apache.http.HttpEntity;
import org.apache.http.HttpResponse;
import org.apache.http.client.ClientProtocolException;
import org.apache.http.client.methods.HttpGet;
import org.apache.http.client.methods.HttpPost;
import org.apache.http.entity.BufferedHttpEntity;
import org.apache.http.util.EntityUtils;

import android.net.http.AndroidHttpClient;
import android.os.AsyncTask;
import android.os.Bundle;
import android.app.Activity;
import android.text.Html;
import android.view.Menu;
import android.view.View;
import android.widget.Button;
import android.widget.TextView;

public class StatusChanger extends Activity {
int status = -1;
	@Override
	public void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		setContentView(R.layout.activity_status_changer);
		
		StatusCheck statusCheck = new StatusCheck();
		statusCheck.execute("http://www.p-space.gr/statustest/");
								
		final Button change_button = (Button) findViewById(R.id.button1);
		change_button.setOnClickListener(new View.OnClickListener() {
			public void onClick(View v) {
				
				StatusSelect();
			}
		});
		
		final Button check_button = (Button) findViewById(R.id.button2);
		check_button.setOnClickListener(new View.OnClickListener() {
			public void onClick(View v) {
				
				StatusCheck statusCheck = new StatusCheck();
				statusCheck.execute("http://www.p-space.gr/statustest/");
			}
		});
	}

	@Override
	public boolean onCreateOptionsMenu(Menu menu) {
		getMenuInflater().inflate(R.menu.activity_status_changer, menu);
		return true;
	}
	
	public void StatusSelect(){
		switch (status){
		case 0:
			//Open
			new StatusChange().execute("http://www.p-space.gr/statustest/set.php?open");
			break;
		case 1:
			//Close
			new StatusChange().execute("http://www.p-space.gr/statustest/set.php?close");
			break;
		}
	}
	
	private class StatusChange extends AsyncTask<String, Void, Void> {
		
		protected Void doInBackground(String... params) {
			
			String url = params[0];
			AndroidHttpClient client = AndroidHttpClient
					.newInstance("pspace_android");
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
				// TODO Auto-generated catch block
			} catch (IOException e) {
				// TODO Auto-generated catch block
				e.printStackTrace();
			}
			return null;
		}

		protected void onPostExecute(Void result) {

			StatusCheck statusCheckinChecker = new StatusCheck();
			statusCheckinChecker.execute("http://www.p-space.gr/statustest/");

		}
	}

	
	private class StatusCheck extends AsyncTask<String, Void, Integer> {
		protected Integer doInBackground(String... params) {

			String url = params[0];
			AndroidHttpClient client = AndroidHttpClient
					.newInstance("pspace_android");
			HttpGet request = new HttpGet(url);
			HttpResponse response = null;
			
			try {

				response = client.execute(request);

				switch (response.getStatusLine().getStatusCode()) {

				case 200:			//TODO check this http 200 !
					return Integer.valueOf(EntityUtils.toString(response
							.getEntity()));

				default:
					return -1;
				}

			} catch (ClientProtocolException e) {
				//no network
				e.printStackTrace();
				return -1;
			} catch (IOException e) {
				//http problem
				e.printStackTrace();
				return -2;
			} catch (IllegalStateException e) {
				e.printStackTrace();
				return -1;
			}
		}

		protected void onPostExecute(Integer result) {
			TextView statusText = (TextView)findViewById(R.id.textView1);
			
			String close = "<font color='#EE0000'>Closed</font>";
			String open= "<font color='#22b327'>Open</font>";
			
			switch (result){
			case 1:			
				status = 1;
				statusText.setText(Html.fromHtml("P-Space is " + open + "!"));
				break;
			case 0:
				status = 0;
				statusText.setText(Html.fromHtml("P-Space is " + close + "!"));
				break;
			case -2:
				statusText.setText("P-Space's server problem");
				break;
			default:
				status = -1;
				statusText.setText("You are not connected to the network!");
			}
		}
	}
}
