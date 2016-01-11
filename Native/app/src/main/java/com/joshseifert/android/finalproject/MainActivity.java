package com.joshseifert.android.finalproject;

import android.content.Intent;
import android.os.AsyncTask;
import android.os.Bundle;
import android.support.v7.app.AppCompatActivity;
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


public class MainActivity extends AppCompatActivity {

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

    }

    public void logIn(View view){

		// Get information from mobile interface
        EditText loginUsernameEditText = (EditText) findViewById(R.id.loginUsernameEditText);
        int loginUsernameEditTextLength = loginUsernameEditText.getText().toString().trim().length();

        EditText loginPasswordEditText = (EditText) findViewById(R.id.loginPasswordEditText);
        int loginPasswordEditTextLength = loginPasswordEditText.getText().toString().trim().length();

        // Check both username and password are entered
        if(loginUsernameEditTextLength > 0){
            if(loginPasswordEditTextLength > 0){
				// Create AsyncTask to log in
                Toast.makeText(this, "Logging In...", Toast.LENGTH_SHORT).show();
                new LogIn().execute();
            } else {
                Toast.makeText(this, "Please enter a password.", Toast.LENGTH_SHORT).show();
            }
        } else {
            Toast.makeText(this, "Please enter a user name.", Toast.LENGTH_SHORT).show();
        }
    }

	// All AsyncTask code modeled on tutorials by Derek Banas' youtube series on Android Development
	// found at https://www.youtube.com/playlist?list=PLGLfVvz_LVvSPjWpLPFEfOCbezi6vATIh
	
    class LogIn extends AsyncTask<Void, Boolean, Boolean> {

        EditText loginUsernameEditText = (EditText) findViewById(R.id.loginUsernameEditText);
        String username = loginUsernameEditText.getText().toString();

        EditText loginPasswordEditText = (EditText) findViewById(R.id.loginPasswordEditText);
        String password = loginPasswordEditText.getText().toString();

		// Because this may take some time to run, do in background, allow user to keep interacting with app
        @Override
        protected Boolean doInBackground(Void... voids) {
            Boolean success = false;

            DefaultHttpClient httpClient = new DefaultHttpClient(new BasicHttpParams());
            HttpPost httpPost = new HttpPost("http://seifert-final.appspot.com/user");
            httpPost.setHeader("Accept", "application/json");

			// These are the values passed in the API call			
            List<NameValuePair> params = new ArrayList<NameValuePair>(3);

            params.add(new BasicNameValuePair("name", username));
            params.add(new BasicNameValuePair("password", password));
            params.add(new BasicNameValuePair("login", "True"));

            try{
                UrlEncodedFormEntity formEntity = new UrlEncodedFormEntity(params);
                httpPost.setEntity(formEntity);

                HttpResponse response = httpClient.execute(httpPost);

                int statusCode = response.getStatusLine().getStatusCode();

                //User successfully logged in
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
			// Return results to the screen so user can see results
            TextView loginResult = (TextView) findViewById(R.id.loginResult);
            if(success){
				// If logged in, move to next page after passing username value
                loginResult.setText("Logged in! Redirecting to user page....");
                Intent loginIntent = new Intent();
                loginIntent.setClass(getApplicationContext(),User.class);
                loginIntent.putExtra("username", username);
                startActivity(loginIntent);
            } else {
                loginResult.setText("Account not found. Please check your username and password.");
            }
        }
    }

    public void getPumped(View view) {
        Intent getPumpedIntent = new Intent(this, GetPumped.class);
        startActivity(getPumpedIntent);
    }


    public void signUp(View view){
        Intent signupIntent = new Intent(this, SignUp.class);
        startActivity(signupIntent);
    }
}
