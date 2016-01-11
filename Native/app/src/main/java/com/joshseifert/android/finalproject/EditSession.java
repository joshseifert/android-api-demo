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
import org.apache.http.client.methods.HttpDelete;
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
 * Created by Josh on 11/30/2015.
 */

public class EditSession extends Activity {

    String username;
    String date;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.edit_session_layout);

        Intent fromLogin = getIntent();
        username = fromLogin.getExtras().getString("username");
        date = fromLogin.getExtras().getString("date");


        TextView welcomeUser = (TextView) findViewById(R.id.editSessionWelcome);
        welcomeUser.append("Hello, " + username);

        TextView sessionDate = (TextView) findViewById(R.id.editSessionDateEditText);
        sessionDate.setText(date);

        new GetSession().execute();

    }

    public void addLift(View view) {
        EditText liftNameEditText = (EditText) findViewById(R.id.editSessionLiftNameEditText);
        String liftName = liftNameEditText.getText().toString();

        Intent liftIntent = new Intent();
        liftIntent.setClass(getApplicationContext(),AddLift.class);
        liftIntent.putExtra("username", username);
        liftIntent.putExtra("date", date);
        liftIntent.putExtra("liftName", liftName);
        startActivity(liftIntent);
    }

    public void editLift(View view) {
        EditText liftNameEditText = (EditText) findViewById(R.id.editSessionLiftNameEditText);
        String liftName = liftNameEditText.getText().toString();

        Intent liftIntent = new Intent();
        liftIntent.setClass(getApplicationContext(), EditLift.class);
        liftIntent.putExtra("username", username);
        liftIntent.putExtra("date", date);
        liftIntent.putExtra("liftName", liftName);
        startActivity(liftIntent);
    }

    public void deleteLift(View view) { new DeleteLift().execute(); }

    class DeleteLift extends AsyncTask<Void, Boolean, Boolean> {

        EditText liftNameEditText = (EditText) findViewById(R.id.editSessionLiftNameEditText);
        String liftName = liftNameEditText.getText().toString();
        String urlUsername = username.replace(" ", "%20");

        @Override
        protected Boolean doInBackground(Void... voids) {
            Boolean success = false;

            DefaultHttpClient httpClient = new DefaultHttpClient(new BasicHttpParams());
            HttpDelete httpDelete = new HttpDelete("http://seifert-final.appspot.com/user/" + urlUsername + "/session/" + date + "/lift/" + liftName);
            httpDelete.setHeader("Accept", "application/json");

            try{
                HttpResponse response = httpClient.execute(httpDelete);

                int statusCode = response.getStatusLine().getStatusCode();

                //Lift Succesfully deleted
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
            TextView editSessionResults = (TextView) findViewById(R.id.editSessionLiftResults);
            if(success){
                editSessionResults.setText("Lift Successfully deleted.");
                //Rerun GET request to show lift deleted.
                new GetSession().execute();
            } else {
                editSessionResults.setText("Error deleting lift. Lift name must exist in database and be entered exactly as shown.");
            }
        }
    }

    public void editSession(View view){
        new EditSessionDate().execute();
    }

    class EditSessionDate extends AsyncTask<Void, Boolean, Boolean> {
        EditText newDateEditText = (EditText) findViewById(R.id.editSessionDateEditText);
        String newDate = newDateEditText.getText().toString();

        @Override
        protected Boolean doInBackground(Void... voids) {
            Boolean success = false;

            String urlUsername = username.replace(" ", "%20");

            DefaultHttpClient httpClient = new DefaultHttpClient(new BasicHttpParams());
            HttpPut httpPut = new HttpPut("http://seifert-final.appspot.com/user/" + urlUsername + "/session/" + date);
            httpPut.setHeader("Accept", "application/json");

            List<NameValuePair> params = new ArrayList<NameValuePair>(1);

            params.add(new BasicNameValuePair("date", newDate));

            try{
                UrlEncodedFormEntity formEntity = new UrlEncodedFormEntity(params);
                httpPut.setEntity(formEntity);

                HttpResponse response = httpClient.execute(httpPut);

                int statusCode = response.getStatusLine().getStatusCode();

                //User details successfully edited
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
            TextView editDateResult = (TextView) findViewById(R.id.editSessionResult);
            if(success){
                editDateResult.setText("Session Date changed.");
            } else {
                editDateResult.setText("Please enter a unique, properly formatted date.");
            }
        }
    }

    class GetSession extends AsyncTask<Void, Void, Void> {
        String jsonString = "";
        String result = "";
        String urlUsername = username.replace(" ", "%20");

        @Override
        protected Void doInBackground(Void... voids) {
            DefaultHttpClient httpClient = new DefaultHttpClient(new BasicHttpParams());
            HttpGet httpGet = new HttpGet("http://seifert-final.appspot.com/user/" + urlUsername + "/session/" + date);
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


                    JSONObject sessionObject = jArray.getJSONObject(0);

                    result = "Lifts: \n\t" + sessionObject.getString("lifts") + "\n\n";

                    //result = result + "Target Weight: " + sessionObject.getString(fields[1]) + "\n"
                    //        + "Current Weight: " + sessionObject.getString(fields[2]) + "\n"
                    //        + "Sessions: " + sessionObject.getString(fields[4]) + "\n\n";

                    //password = sessionObject.getString(fields[0]);
                    //targetWeight = sessionObject.getString(fields[1]);
                    //currentWeight = sessionObject.getString(fields[2]);

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

            if(result.length() == 0){
                result = "Sorry, this session could not be found. Go back and check that you entered the date properly.";
            }

            TextView displaySessions = (TextView) findViewById(R.id.editSessionLiftList);
            displaySessions.setText(result);
        }
    }

    public void logout(View view){
        Intent goToLogin = new Intent(this, MainActivity.class);
        startActivity(goToLogin);
    }

}
