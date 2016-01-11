package com.joshseifert.android.finalproject;

import android.app.Activity;
import android.content.Intent;
import android.os.AsyncTask;
import android.os.Bundle;
import android.view.View;
import android.widget.EditText;
import android.widget.TextView;

import org.apache.http.HttpEntity;
import org.apache.http.HttpResponse;
import org.apache.http.NameValuePair;
import org.apache.http.client.ClientProtocolException;
import org.apache.http.client.entity.UrlEncodedFormEntity;
import org.apache.http.client.methods.HttpGet;
import org.apache.http.client.methods.HttpPut;
import org.apache.http.impl.client.DefaultHttpClient;
import org.apache.http.message.BasicNameValuePair;
import org.apache.http.params.BasicHttpParams;
import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.io.UnsupportedEncodingException;
import java.net.MalformedURLException;
import java.util.ArrayList;
import java.util.List;

/**
 * Created by Josh on 12/1/2015.
 */
public class EditLift extends Activity {

    String username;
    String date;
    String liftName;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.edit_lift_layout);

        Intent fromLogin = getIntent();
        username = fromLogin.getExtras().getString("username");
        date = fromLogin.getExtras().getString("date");
        liftName = fromLogin.getExtras().getString("liftName");

        TextView liftNameText = (TextView) findViewById(R.id.editLiftLiftName);
        liftNameText.setText(liftName);

        new GetLift().execute();
    }

    class GetLift extends AsyncTask<Void, Void, Void> {
        String jsonString = "";
        List<String> result = new ArrayList<String>();
        String urlUsername = username.replace(" ", "%20");

        @Override
        protected Void doInBackground(Void... voids) {
            DefaultHttpClient httpClient = new DefaultHttpClient(new BasicHttpParams());
            HttpGet httpGet = new HttpGet("http://seifert-final.appspot.com/user/" + urlUsername + "/session/" + date + "/lift/" + liftName);
            httpGet.setHeader("Content-type", "application/json");
            httpGet.setHeader("Accept", "application/json");

            InputStream inputStream = null;

            try{
                HttpResponse response = httpClient.execute(httpGet);
                HttpEntity entity = response.getEntity();
                inputStream = entity.getContent();

                BufferedReader reader = new BufferedReader(new InputStreamReader(inputStream, "UTF-8"), 8);
                StringBuilder sb = new StringBuilder();
                String line = null;

                while((line = reader.readLine()) != null){
                    sb.append(line + "\n");
                }

                jsonString = sb.toString();

                //Make JSON object from the raw string
                JSONArray jArray = new JSONArray(jsonString);

                //String[] fields = {"password", "target_weight", "current_weight", "name", "sessions"};

                try{


                    JSONObject liftObject = jArray.getJSONObject(0);

                    result.add(liftObject.getString("weight"));
                    result.add(liftObject.getString("reps"));
                    result.add(liftObject.getString("sets"));

                } catch (JSONException e) {
                    e.printStackTrace();
                }

            } catch (MalformedURLException e) {
                e.printStackTrace();
            } catch (UnsupportedEncodingException e) {
                e.printStackTrace();
            } catch (ClientProtocolException e) {
                e.printStackTrace();
            } catch (IOException e) {
                e.printStackTrace();
            } catch (JSONException e) {
                e.printStackTrace();
            }

            return null;
        }

        @Override
        protected void onPostExecute(Void aVoid) {

            TextView weightText = (TextView) findViewById(R.id.editLiftWeight);
            weightText.setText(result.get(0));

            TextView repsText = (TextView) findViewById(R.id.editLiftReps);
            repsText.setText(result.get(1));

            TextView setsText = (TextView) findViewById(R.id.editLiftSets);
            setsText.setText(result.get(2));

        }
    }

    public void editLift(View view){ new EditNewLift().execute(); }

    class EditNewLift extends AsyncTask<Void, Boolean, Boolean> {

        EditText liftNameEditText = (EditText) findViewById(R.id.editLiftLiftName);
        String liftName = liftNameEditText.getText().toString();

        EditText weightEditText = (EditText) findViewById(R.id.editLiftWeight);
        String weight = weightEditText.getText().toString();

        EditText setsEditText = (EditText) findViewById(R.id.editLiftSets);
        String sets = setsEditText.getText().toString();

        EditText repsEditText = (EditText) findViewById(R.id.editLiftReps);
        String reps = repsEditText.getText().toString();

        String urlUsername = username.replace(" ", "%20");

        @Override
        protected Boolean doInBackground(Void... voids) {
            Boolean success = false;

            DefaultHttpClient httpClient = new DefaultHttpClient(new BasicHttpParams());
            HttpPut httpPut = new HttpPut("http://seifert-final.appspot.com/user/" + urlUsername + "/session/" + date + "/lift/" + liftName);
            httpPut.setHeader("Accept", "application/json");

            List<NameValuePair> params = new ArrayList<NameValuePair>(4);

            params.add(new BasicNameValuePair("lift", liftName));
            params.add(new BasicNameValuePair("weight", weight));
            params.add(new BasicNameValuePair("sets", sets));
            params.add(new BasicNameValuePair("reps", reps));

            try{
                UrlEncodedFormEntity formEntity = new UrlEncodedFormEntity(params);
                httpPut.setEntity(formEntity);

                HttpResponse response = httpClient.execute(httpPut);

                int statusCode = response.getStatusLine().getStatusCode();

                //Lift successfully edited
                if(statusCode == 200){
                    success = true;
                }

            } catch (MalformedURLException e) {
                e.printStackTrace();
            } catch (UnsupportedEncodingException e) {
                e.printStackTrace();
            } catch (ClientProtocolException e) {
                e.printStackTrace();
            } catch (IOException e) {
                e.printStackTrace();
            }
            return success;
        }

        protected void onPostExecute(Boolean success) {
            TextView addLiftResults = (TextView) findViewById(R.id.editLiftResults);
            if(success){
                addLiftResults.setText("Lift Successfully edited.");
            } else {
                addLiftResults.setText("Error editing lift. Lift name must be unique, and weight, sets, reps must be integers.");
            }
        }
    }
}