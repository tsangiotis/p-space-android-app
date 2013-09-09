package com.pspace.gr;

import android.app.ListFragment;
import android.content.SharedPreferences;
import android.os.AsyncTask;
import android.os.Bundle;
import android.preference.PreferenceManager;
import android.util.Log;
import android.widget.ListAdapter;
import android.widget.SimpleAdapter;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Date;
import java.util.HashMap;

/**
 * Created by tsagi on 9/9/13.
 */
public class UberdustParserFragment extends ListFragment{

    public static UberdustParserFragment newInstance() {
        return new UberdustParserFragment();
    }

    String timestamp;
    String name;
    String reading;
    String count;

    // Hashmap for ListView
    ArrayList<HashMap<String, String>> doorList;
    // creating new HashMap
    HashMap<String, String> map;

    // url to make request
    String uberurl;



    // JSON Node names
    private static final String TAG_NODE_ID = "nodeId";
    private static final String TAG_READINGS = "readings";
    private static final String TAG_TIMESTAMP = "timestamp";
    private static final String TAG_TIME = "time";
    private static final String TAG_READING = "reading";
    private static final String TAG_STRING_READING = "stringReading";
    private static final String TAG_CAPABILITY_ID = "capabilityId";

    // contacts JSONArray
    JSONArray readings = null;

    @Override
    public void onActivityCreated(Bundle savedInstanceState) {
        super.onActivityCreated(savedInstanceState);
        loadPref();
        uberurl = "http://uberdust.cti.gr/rest/testbed/5/node/urn:pspace:door/capability/urn:node:capability:card/json/limit/" + count;
        Log.d("url", uberurl);
        new ParseDoorData().execute(uberurl);


    }

    private class ParseDoorData extends AsyncTask<String, Void, Void> {

        protected Void doInBackground(String... params) {

            String url = params[0];
            // Hashmap for ListView
            doorList = new ArrayList<HashMap<String, String>>();

            // Creating JSON Parser instance
            JSONParser jParser = new JSONParser();

            // getting JSON string from URL
            JSONObject json = jParser.getJSONFromUrl(uberurl);

            //FIXME
            if (json==null)
                return null;

            try {
                // Getting Array of Readings
                readings = json.getJSONArray(TAG_READINGS);

                // looping through All Contacts
                for(int i = 0; i < readings.length(); i++){
                    JSONObject r = readings.getJSONObject(i);

                    // Storing each json item in variable
                    timestamp= r.getString(TAG_TIMESTAMP);
                    name = r.getString(TAG_STRING_READING);
                    reading = r.getString(TAG_READING);

                    // creating new HashMap
                    map = new HashMap<String, String>();

                    long dv = Long.valueOf(timestamp);
                    Date df = new java.util.Date(dv);
                    String time = new SimpleDateFormat("d MMM - H:m").format(df);

                    if(name.equals("button"))
                        name = "Remote button pressed!";
                    else
                        name = "Card was used by: " + name;

                    // adding each child node to HashMap key => value
                    map.put(TAG_TIME, time);
                    map.put(TAG_STRING_READING, name);

                    // adding HashList to ArrayList
                    doorList.add(map);
                }

            }catch (JSONException e) {
                    e.printStackTrace();
                }
            return null;
        }

        @Override
        protected void onPostExecute(Void aVoid) {
            /**
             * Updating parsed JSON data into ListView
             * */

            ListAdapter adapter = new SimpleAdapter(getActivity(), doorList,
                    R.layout.door_list_fragment,
                    new String[] { TAG_TIME, TAG_STRING_READING }, new int[] {
                    R.id.timestamp, R.id.member });

            setListAdapter(adapter);
        }
    }

    private void loadPref(){
        SharedPreferences mySharedPreferences = PreferenceManager.getDefaultSharedPreferences(getActivity());

        count = mySharedPreferences.getString("count_preference", "10");
    }
}
