package com.android.bike.janus;

import android.app.ProgressDialog;
import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.os.AsyncTask;
import android.support.design.widget.TextInputLayout;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.support.v7.widget.Toolbar;
import android.text.Editable;
import android.text.TextUtils;
import android.text.TextWatcher;
import android.util.Log;
import android.util.Patterns;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.Toast;

import java.io.BufferedReader;
import java.io.BufferedWriter;
import java.io.IOException;
import java.io.InputStreamReader;
import java.io.OutputStream;
import java.io.OutputStreamWriter;
import java.io.UnsupportedEncodingException;
import java.net.HttpURLConnection;
import java.net.MalformedURLException;
import java.net.URL;
import java.net.URLEncoder;
import java.util.Map;

import javax.net.ssl.HttpsURLConnection;

import MapHelperClasses.ParseJSON;

public class RegisterActivity extends AppCompatActivity {

    private final String TOOLBAR_TITLE = "Register";
    static final String TAG = RegisterActivity.class.getSimpleName();

    //SharedPreferences variables
    private SharedPreferences sharedPreferences;
    private SharedPreferences.Editor editor;

    private Toolbar toolbar;

    private EditText fullnameEditText, emailEditText, usernameEditText, passwordEditText;
    private TextInputLayout fullnameTextInputLayout, emailTextInputLayout, usernameTextInputLayout, passwordTextInputLayout;

    private Button registerButton;

    private boolean registrationSuccessful = false;

    private ProgressDialog progressDialog;


    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_register);

        //set a Toolbar to replace the ActionBar
        toolbar = (android.support.v7.widget.Toolbar) findViewById(R.id.toolbar);
        setSupportActionBar(toolbar);
        getSupportActionBar().setTitle(TOOLBAR_TITLE);

        //SharedPreferences variables
        sharedPreferences = getSharedPreferences(Config.JANUS_LOGIN_PREF, Context.MODE_PRIVATE);
        editor = sharedPreferences.edit();

        //Find TextInputLayouts
        fullnameTextInputLayout = (TextInputLayout) findViewById(R.id.fullnameTextInputLayout);
        emailTextInputLayout = (TextInputLayout) findViewById(R.id.emailTextInputLayout);
        usernameTextInputLayout = (TextInputLayout) findViewById(R.id.usernameTextInputLayout);
        passwordTextInputLayout = (TextInputLayout) findViewById(R.id.passwordTextInputLayout);

        //Find EditTexts
        fullnameEditText = (EditText) findViewById(R.id.fullnameEditText);
        emailEditText = (EditText) findViewById(R.id.emailEditText);
        usernameEditText = (EditText) findViewById(R.id.usernameEditText);
        passwordEditText = (EditText) findViewById(R.id.passwordEditText);

        //Add TextChangedListeners to EditTexts
        fullnameEditText.addTextChangedListener(new MyTextWatcher(fullnameEditText));
        emailEditText.addTextChangedListener(new MyTextWatcher(emailEditText));
        usernameEditText.addTextChangedListener(new MyTextWatcher(usernameEditText));
        passwordEditText.addTextChangedListener(new MyTextWatcher(passwordEditText));

        //Find registerButton
        registerButton = (Button) findViewById(R.id.registerButton);

        //Set OnClickListener to registerButton
        registerButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                progressDialog = ProgressDialog.show(RegisterActivity.this, "Processing....",
                        "Fetching the shortest route.", true);
                submitForm();
            }
        });
    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        // Inflate the menu; this adds items to the action bar if it is present.
        //getMenuInflater().inflate(R.menu.menu_register, menu);
        return true;
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        // Handle action bar item clicks here. The action bar will
        // automatically handle clicks on the Home/Up button, so long
        // as you specify a parent activity in AndroidManifest.xml.
        int id = item.getItemId();

        //noinspection SimplifiableIfStatement
        if (id == R.id.action_settings) {
            return true;
        }

        return super.onOptionsItemSelected(item);
    }

    private void submitForm(){
        int err = 0;
        if(!validateName()){
            err++;
            progressDialog.dismiss();
        }
        if(!validateEmail()){
            err++;
            progressDialog.dismiss();
        }
        if(!validateUsername()){
            err++;
            progressDialog.dismiss();
        }
        if(!validatePassword()){
            err++;
            progressDialog.dismiss();
        }

        if(err > 0){
            return;
        }
        else {
            RegisterAsyncTask asyncTask = new RegisterAsyncTask();
            asyncTask.execute();
        }
    }

    private boolean validateName(){
        if(fullnameEditText.getText().toString().trim().isEmpty()){
            fullnameTextInputLayout.setError("Please enter your full name.");
            return false;
        }
        else{
            fullnameTextInputLayout.setErrorEnabled(false);
        }
        return true;
    }

    private boolean validateEmail(){
        String email = emailEditText.getText().toString().trim();
        if(email.isEmpty() || !isValidEmail(email)){
            fullnameTextInputLayout.setError("Please enter a valid email address.");
            return false;
        }
        else{
            emailTextInputLayout.setErrorEnabled(false);
        }
        return true;
    }

    private boolean validateUsername(){
        if(usernameEditText.getText().toString().trim().isEmpty()){
            usernameTextInputLayout.setError("Please try a different username.");
            return false;
        }
        else{
            usernameTextInputLayout.setErrorEnabled(false);
        }
        return true;
    }

    private boolean validatePassword(){
        if(passwordEditText.getText().toString().trim().isEmpty()){
            passwordTextInputLayout.setError("Please choose a valid password.");
            return false;
        }
        else{
            passwordTextInputLayout.setErrorEnabled(false);
        }
        return true;
    }

    private class MyTextWatcher implements TextWatcher{

        private View view;

        private MyTextWatcher(View view){
            this.view = view;
        }

        @Override
        public void beforeTextChanged(CharSequence s, int start, int count, int after) {

        }

        @Override
        public void onTextChanged(CharSequence s, int start, int before, int count) {

        }

        @Override
        public void afterTextChanged(Editable s) {
            switch(view.getId()){
                case R.id.fullnameEditText:
                    validateName();
                    break;
                case R.id.emailEditText:
                    validateEmail();
                    break;
                case R.id.usernameEditText:
                    validateUsername();
                    break;
                case R.id.passwordEditText:
                    validatePassword();
                    break;
            }
        }
    }

    private boolean isValidEmail(String email) {
        return !TextUtils.isEmpty(email) && Patterns.EMAIL_ADDRESS.matcher(email).matches();
    }

    public void startMainActivity() {
        Intent intent = new Intent(this, MainActivity.class);
        startActivity(intent);
    }

    public void startLoginActivity(View view) {
        Intent intent = new Intent(this, LoginActivity.class);
        startActivity(intent);
    }

    public class RegisterAsyncTask extends AsyncTask<Void, Void, String> {

        private final String TAG = RegisterAsyncTask.class.getSimpleName();

        @Override
        protected String doInBackground(Void... params) {

            Log.i(TAG, "Reached RegisterAsyncTask");

            String http = "http://janus-59642.onmodulus.net/register";
            HttpURLConnection mUrlConnection = null;

            String response = "";

            try {
                URL url = new URL(http);

                String urlParams = encodeString();
                mUrlConnection = (HttpURLConnection) url.openConnection();
                mUrlConnection.setReadTimeout(15000);
                mUrlConnection.setConnectTimeout(15000);
                mUrlConnection.setRequestMethod("POST");
                mUrlConnection.setDoInput(true);
                mUrlConnection.setDoOutput(true);
                mUrlConnection.setRequestProperty("Content-Type",
                        "application/x-www-form-urlencoded");
                mUrlConnection.setRequestProperty("Content-Length", "" +
                        Integer.toString(urlParams.getBytes().length));

                OutputStream os = mUrlConnection.getOutputStream();
                BufferedWriter writer = new BufferedWriter(
                        new OutputStreamWriter(os, "UTF-8"));
                writer.write(urlParams);

                writer.flush();
                writer.close();
                os.close();

                int responseCode = mUrlConnection.getResponseCode();

                if (responseCode == HttpsURLConnection.HTTP_OK) {
                    Log.e(TAG, "Good response from server: "+responseCode);
                    String line;
                    BufferedReader br = new BufferedReader(new InputStreamReader(mUrlConnection.getInputStream()));
                    while ((line=br.readLine()) != null) {
                        response += line;
                    }
                    registrationSuccessful = true;
                }
                else {
                    Log.e(TAG, "Bad response from server: "+responseCode);
                    response="";
                }

            } catch (MalformedURLException e) {
                Log.e(TAG, "MalformedURLException: "+e);
                return null;

            } catch (IOException e) {
                Log.e(TAG, "IOException: " + e);
                return null;
            } finally {
                if (mUrlConnection != null) {
                    mUrlConnection.disconnect();
                }
            }

            Log.i(TAG, "Response = " + response);
            return response;
        }

        @Override
        protected void onPostExecute(String result) {
            Log.i(TAG, "Reached onPostExecute");

            if(registrationSuccessful) {
                editor.putBoolean("first",false);
                editor.commit();
                progressDialog.dismiss();
                Toast.makeText(getApplicationContext(), "Registration successful!", Toast.LENGTH_LONG).show();
                startMainActivity();
            }
        }
    }

    private String encodeString() throws UnsupportedEncodingException{
        StringBuilder result = new StringBuilder();
        result.append("&");

        result.append("name=");
        result.append(URLEncoder.encode(fullnameEditText.getText().toString(), "UTF-8"));
        result.append("&email=");
        result.append(URLEncoder.encode(emailEditText.getText().toString(), "UTF-8"));
        result.append("&password=");
        result.append(URLEncoder.encode(passwordEditText.getText().toString(), "UTF-8"));
        result.append("&key=1234");
        //result.append(URLEncoder.encode("1234", "UTF-8"));

        Log.i(TAG, "Result URL="+result.toString());

        return result.toString();
    }
}
