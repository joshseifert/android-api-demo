package com.joshseifert.android.finalproject;

import android.app.Activity;
import android.content.Intent;
import android.os.AsyncTask;
import android.os.Bundle;
import android.view.View;
import android.widget.EditText;
import android.widget.TextView;

import org.apache.http.HttpResponse;
import org.apache.http.NameValuePair;
import org.apache.http.client.ClientProtocolException;
import org.apache.http.client.entity.UrlEncodedFormEntity;
import org.apache.http.client.methods.HttpPost;
import org.apache.http.impl.client.DefaultHttpClient;
import org.apache.http.message.BasicNameValuePair;
import org.apache.http.params.BasicHttpParams;

import java.io.IOException;
import java.io.UnsupportedEncodingException;
import java.net.MalformedURLException;
import java.util.ArrayList;
import java.util.List;

/**
 * Created by Josh on 12/1/2015.
 */
public class AddLift extends Activity {

    String username;
    String date;
    String liftName;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.add_lift_layout);

        Intent fromLogin = getIntent();
        username = fromLogin.getExtras().getString("username");
        date = fromLogin.getExtras().getString("date");
        liftName = fromLogin.getExtras().getString("liftName");

        TextView liftNameText = (TextView) findViewById(R.id.addLiftLiftName);
        liftNameText.setText(liftName);

    }

    public void addLift(View view){ new AddNewLift().execute(); }

    class AddNewLift extends AsyncTask<Void, Boolean, Boolean> {

        EditText liftNameEditText = (EditText) findViewById(R.id.addLiftLiftName);
        String liftName = liftNameEditText.getText().toString();

        EditText weightEditText = (EditText) findViewById(R.id.addLiftWeight);
        String weight = weightEditText.getText().toString();

        EditText setsEditText = (EditText) findViewById(R.id.addLiftSets);
        String sets = setsEditText.getText().toString();

        EditText repsEditText = (EditText) findViewById(R.id.addLiftReps);
        String reps = repsEditText.getText().toString();





        String urlUsername = username.replace(" ", "%20");

        @Override
        protected Boolean doInBackground(Void... voids) {
            Boolean success = false;

            DefaultHttpClient httpClient = new DefaultHttpClient(new BasicHttpParams());
            HttpPost httpPost = new HttpPost("http://seifert-final.appspot.com/user/" + urlUsername + "/session/" + date + "/lift");
            httpPost.setHeader("Accept", "application/json");

            List<NameValuePair> params = new ArrayList<NameValuePair>(4);

            params.add(new BasicNameValuePair("lift", liftName));
            params.add(new BasicNameValuePair("weight", weight));
            params.add(new BasicNameValuePair("sets", sets));
            params.add(new BasicNameValuePair("reps", reps));

            try{
                UrlEncodedFormEntity formEntity = new UrlEncodedFormEntity(params);
                httpPost.setEntity(formEntity);

                HttpResponse response = httpClient.execute(httpPost);

                int statusCode = response.getStatusLine().getStatusCode();

                //Lift successfully added
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
            TextView addLiftResults = (TextView) findViewById(R.id.addLiftResults);
            if(success){
                addLiftResults.setText("Lift Successfully added.");
            } else {
                addLiftResults.setText("Error adding lift. Lift name must be unique.");
            }
        }
    }
}
