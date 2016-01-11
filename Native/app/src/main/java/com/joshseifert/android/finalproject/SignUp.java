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
 * Created by Josh on 11/29/2015.
 */
public class SignUp extends Activity {
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.signup_layout);
    }

    public void registerAccount(View view){
        EditText signupUsernameEditText = (EditText) findViewById(R.id.signupUsernameEditText);
        String username = signupUsernameEditText.getText().toString();
        int usernameLength = username.trim().length();


        EditText signupPasswordEditText = (EditText) findViewById(R.id.signupPasswordEditText);
        String password = signupPasswordEditText.getText().toString();
        int passwordLength = password.trim().length();

        EditText signupPasswordConfirmEditText = (EditText) findViewById(R.id.signupPasswordConfirmEditText);
        String passwordConfirm = signupPasswordConfirmEditText.getText().toString();

        if(usernameLength >= 4){
            if (passwordLength >= 4){
                if(password.equals(passwordConfirm)){
                    Toast.makeText(this, "Creating Account...", Toast.LENGTH_SHORT).show();
                    new CreateAccount().execute();
                } else {
                    Toast.makeText(this, "Passwords do not match!", Toast.LENGTH_SHORT).show();
                }
            } else {
                Toast.makeText(this, "Password must be at least 4 characters..", Toast.LENGTH_SHORT).show();
            }
        } else {
            Toast.makeText(this, "Username must be at least 4 characters.", Toast.LENGTH_SHORT).show();
        }
    }

    public void backToLogin(View view){
        Intent goToLogin = new Intent(this, MainActivity.class);
        startActivity(goToLogin);
    }

    class CreateAccount extends AsyncTask<Void, Boolean, Boolean> {
        EditText signupUsernameEditText = (EditText) findViewById(R.id.signupUsernameEditText);
        String username = signupUsernameEditText.getText().toString();

        EditText signupPasswordEditText = (EditText) findViewById(R.id.signupPasswordEditText);
        String password = signupPasswordEditText.getText().toString();

        @Override
        protected Boolean doInBackground(Void... voids) {
            Boolean success = false;

            DefaultHttpClient httpClient = new DefaultHttpClient(new BasicHttpParams());
            HttpPost httpPost = new HttpPost("http://seifert-final.appspot.com/user");
            httpPost.setHeader("Accept", "application/json");

            List<NameValuePair> params = new ArrayList<NameValuePair>(2);

            params.add(new BasicNameValuePair("name", username));
            params.add(new BasicNameValuePair("password", password));

            try{
                UrlEncodedFormEntity formEntity = new UrlEncodedFormEntity(params);
                httpPost.setEntity(formEntity);

                HttpResponse response = httpClient.execute(httpPost);

                int statusCode = response.getStatusLine().getStatusCode();

                //User successfully registered
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
            TextView signupResult = (TextView) findViewById(R.id.signupResult);
            if(success){
                signupResult.setText("Successfully registered! Redirecting to user page...");
                Intent loginIntent = new Intent();
                loginIntent.setClass(getApplicationContext(),User.class);
                loginIntent.putExtra("username", username);
                startActivity(loginIntent);
            } else {
                signupResult.setText("That username is taken.");
            }
        }
    }
}
