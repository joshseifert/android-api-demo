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
import org.apache.http.client.methods.HttpPost;
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
public class User extends Activity {

    String username;
    String password;
    String targetWeight;
    String currentWeight;
    String date;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.user_layout);

        Intent fromLogin = getIntent(); //get data passed in from MainActivity
        username = fromLogin.getExtras().getString("username");

        TextView welcomeUser = (TextView) findViewById(R.id.userWelcome);
        welcomeUser.append("Hello, " + username);

        new GetUser().execute();
    }


    public void addSession(View view){
        new AddSession().execute();
    }

    public void viewSession(View view){

        EditText userDateEditText = (EditText) findViewById(R.id.userDate);
        date = userDateEditText.getText().toString();

        Intent sessionIntent = new Intent();
        sessionIntent.setClass(getApplicationContext(),EditSession.class);
        sessionIntent.putExtra("username", username);
        sessionIntent.putExtra("date", date);
        startActivity(sessionIntent);
    }

    class AddSession extends AsyncTask<Void, Boolean, Boolean> {

        EditText userDateEditText = (EditText) findViewById(R.id.userDate);
        String date = userDateEditText.getText().toString();
        String urlUsername = username.replace(" ", "%20");

        @Override
        protected Boolean doInBackground(Void... voids) {
            Boolean success = false;

            DefaultHttpClient httpClient = new DefaultHttpClient(new BasicHttpParams());
            HttpPost httpPost = new HttpPost("http://seifert-final.appspot.com/user/" + urlUsername + "/session");
            httpPost.setHeader("Accept", "application/json");

            List<NameValuePair> params = new ArrayList<NameValuePair>(1);

            params.add(new BasicNameValuePair("date", date));;

            try{
                UrlEncodedFormEntity formEntity = new UrlEncodedFormEntity(params);
                httpPost.setEntity(formEntity);

                HttpResponse response = httpClient.execute(httpPost);

                int statusCode = response.getStatusLine().getStatusCode();

                //Session successfully added
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
            TextView addSessionResults = (TextView) findViewById(R.id.userAddSessionResults);
            if(success){
                addSessionResults.setText("Session Successfully added.");
                //Rerun GET request to show session added.
                new GetUser().execute();
            } else {
                addSessionResults.setText("Error adding session. Date must be unique and follow format YYYY-MM-DD");
            }
        }
    }


    public void deleteSession(View view){
        new DeleteSession().execute();
    }

    class DeleteSession extends AsyncTask<Void, Boolean, Boolean> {

        EditText userDateEditText = (EditText) findViewById(R.id.userDate);
        String date = userDateEditText.getText().toString();
        String urlUsername = username.replace(" ", "%20");

        @Override
        protected Boolean doInBackground(Void... voids) {
            Boolean success = false;

            DefaultHttpClient httpClient = new DefaultHttpClient(new BasicHttpParams());
            HttpDelete httpDelete = new HttpDelete("http://seifert-final.appspot.com/user/" + urlUsername + "/session/" + date);
            httpDelete.setHeader("Accept", "application/json");

            try{
                HttpResponse response = httpClient.execute(httpDelete);

                int statusCode = response.getStatusLine().getStatusCode();

                //Session successfully added
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
            TextView addSessionResults = (TextView) findViewById(R.id.userAddSessionResults);
            if(success){
                addSessionResults.setText("Session Successfully deleted.");
                //Rerun GET request to show session added.
                new GetUser().execute();
            } else {
                addSessionResults.setText("Error deleting session. Date must exist in database and follow format YYYY-MM-DD");
            }
        }
    }


    class GetUser extends AsyncTask<Void, Void, Void> {
        String jsonString = "";
        String result = "";
        String urlUsername = username.replace(" ", "%20");

        @Override
        protected Void doInBackground(Void... voids) {
            DefaultHttpClient httpClient = new DefaultHttpClient(new BasicHttpParams());
            HttpGet httpGet = new HttpGet("http://seifert-final.appspot.com/user/" + urlUsername);
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

                String[] fields = {"password", "target_weight", "current_weight", "name", "sessions"};

                try{


                    JSONObject sessionObject = jArray.getJSONObject(0);

                    result = result + "Target Weight: " + sessionObject.getString(fields[1]) + "\n"
                            + "Current Weight: " + sessionObject.getString(fields[2]) + "\n"
                            + "Sessions: " + sessionObject.getString(fields[4]) + "\n\n";

                    password = sessionObject.getString(fields[0]);
                    targetWeight = sessionObject.getString(fields[1]);
                    currentWeight = sessionObject.getString(fields[2]);

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
            TextView displaySessions = (TextView) findViewById(R.id.userAllSessions);
            displaySessions.setText(result);
        }
    }


    public void logout(View view){
        Intent goToLogin = new Intent(this, MainActivity.class);
        startActivity(goToLogin);
    }


    public void editProfile(View view){
        Intent settingsIntent = new Intent();
        settingsIntent.setClass(getApplicationContext(),UserSettings.class);
        settingsIntent.putExtra("username", username);
        settingsIntent.putExtra("password", password);
        settingsIntent.putExtra("targetWeight", targetWeight);
        settingsIntent.putExtra("currentWeight", currentWeight);
        startActivity(settingsIntent);
    }
}