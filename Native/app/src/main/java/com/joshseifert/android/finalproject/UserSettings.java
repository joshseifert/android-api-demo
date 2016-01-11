package com.joshseifert.android.finalproject;

import android.app.Activity;
import android.content.Intent;
import android.os.AsyncTask;
import android.os.Bundle;
import android.view.View;
import android.widget.EditText;
import android.widget.TextView;
import android.widget.Toast;

import org.apache.http.HttpResponse;
import org.apache.http.NameValuePair;
import org.apache.http.client.ClientProtocolException;
import org.apache.http.client.entity.UrlEncodedFormEntity;
import org.apache.http.client.methods.HttpPut;
import org.apache.http.impl.client.DefaultHttpClient;
import org.apache.http.message.BasicNameValuePair;
import org.apache.http.params.BasicHttpParams;

import java.io.IOException;
import java.io.UnsupportedEncodingException;
import java.net.MalformedURLException;
import java.util.ArrayList;
import java.util.List;

/**
 * Created by Josh on 11/30/2015.
 */
public class UserSettings extends Activity {

    String username;
    String password;
    String currentWeight;
    String targetWeight;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.user_settings_layout);

        Intent fromUser = getIntent(); //get data passed in from MainActivity
        username = fromUser.getExtras().getString("username");
        password = fromUser.getExtras().getString("password");
        targetWeight = fromUser.getExtras().getString("targetWeight");
        currentWeight = fromUser.getExtras().getString("currentWeight");

        TextView currentWeightTextView = (TextView) findViewById(R.id.userSettingsCurrentWeightEditText);
        currentWeightTextView.setText(currentWeight);

        TextView targetWeightTextView = (TextView) findViewById(R.id.userSettingsTargetWeightEditText);
        targetWeightTextView.setText(targetWeight);

        TextView passwordTextView = (TextView) findViewById(R.id.userSettingsPasswordEditText);
        passwordTextView.setText(password);

        TextView welcomeUser = (TextView) findViewById(R.id.userSettingsWelcome);
        welcomeUser.append("Edit Settings for " + username);
    }

    public void logout(View view){
        Intent goToLogin = new Intent(this, MainActivity.class);
        startActivity(goToLogin);
    }

    public void saveChanges(View view){
        Toast.makeText(this, "Editing profile...", Toast.LENGTH_SHORT).show();
        new EditUser().execute();
    }



    class EditUser extends AsyncTask<Void, Boolean, Boolean>{
        EditText targetWeightEditText = (EditText) findViewById(R.id.userSettingsTargetWeightEditText);
        String targetWeight = targetWeightEditText.getText().toString();

        EditText currentWeightEditText = (EditText) findViewById(R.id.userSettingsCurrentWeightEditText);
        String currentWeight = currentWeightEditText.getText().toString();

        EditText passwordEditText = (EditText) findViewById(R.id.userSettingsPasswordEditText);
        String password = passwordEditText.getText().toString();
        int passwordLength = password.trim().length();

        /*
        if(passwordLength <= 4){
            Toast.makeText(this, "Password must be at least 4 characters..", Toast.LENGTH_SHORT).show();
        } else {
            //
        } */ //Weird error here, unexpected token?



        @Override
        protected Boolean doInBackground(Void... voids) {
            Boolean success = false;

            String urlUsername = username.replace(" ", "%20"); //TEST THIS!

            DefaultHttpClient httpClient = new DefaultHttpClient(new BasicHttpParams());
            HttpPut httpPut = new HttpPut("http://seifert-final.appspot.com/user/" + urlUsername);
            httpPut.setHeader("Accept", "application/json");

            List<NameValuePair> params = new ArrayList<NameValuePair>(3);

            params.add(new BasicNameValuePair("target_weight", targetWeight));
            params.add(new BasicNameValuePair("current_weight", currentWeight));
            params.add(new BasicNameValuePair("password", password));

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
            TextView editUserResult = (TextView) findViewById(R.id.userSettingsResult);
            if(success){
                editUserResult.setText("User Details successfully changed.");
                Intent loginIntent = new Intent();
                loginIntent.setClass(getApplicationContext(),User.class);
                loginIntent.putExtra("username", username);
                startActivity(loginIntent);
            } else {
                editUserResult.setText("http://seifert-final.appspot.com/user/" + username);
            }
        }
    }

}
