package com.application.mtuci2;

import android.annotation.SuppressLint;
import android.app.Activity;
import android.app.ProgressDialog;
import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.os.AsyncTask;
import android.os.Bundle;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.LinearLayout;
import android.widget.Toast;

import org.apache.http.NameValuePair;
import org.apache.http.message.BasicNameValuePair;
import org.json.JSONException;
import org.json.JSONObject;

import java.util.ArrayList;
import java.util.List;

public class SignInScreen extends Activity {
    public ProgressDialog pDialog;
    @SuppressLint("StaticFieldLeak")
    static EditText email;
    @SuppressLint("StaticFieldLeak")
    static EditText password;
    @SuppressLint("ClickableViewAccessibility")
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_sign_in_screen);
        email = (EditText) findViewById(R.id.editTextTextEmailAddress);
        password = (EditText) findViewById(R.id.editTextTextPassword);
        Button button = (Button) findViewById(R.id.button);
        button.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                onBackPressed();
            }
        });
        final LinearLayout bg = (LinearLayout) findViewById(R.id.linearLayout);
        bg.setOnTouchListener(new OnSwipeTouchListener(SignInScreen.this) {
            public void onSwipeLeft() {
                onBackPressed();
            }
        });
        Button button8 = (Button) findViewById(R.id.button8);
        button8.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                new LoginUser().execute();
            }
        });
    }
    class LoginUser extends AsyncTask<String, String, String> {
        @Override
        protected void onPreExecute() {
            super.onPreExecute();
            pDialog = new ProgressDialog(SignInScreen.this);
            pDialog.setMessage("Выполняется авторизация");
            pDialog.setIndeterminate(false);
            pDialog.setCancelable(true);
            pDialog.show();
        }

        protected String doInBackground(String[] args) {
            String email = SignInScreen.email.getText().toString();
            String password = SignInScreen.password.getText().toString();

            List<NameValuePair> params = new ArrayList<NameValuePair>();
            params.add(new BasicNameValuePair("email", email));
            params.add(new BasicNameValuePair("password", password));

            JSONParser jsonParser = new JSONParser();
            JSONObject json = jsonParser.makeHttpRequest("http://5.180.137.9/login_user.php", "POST", params);

            try {
                if (json != null) {
                    int success = json.getInt("success");
                    if (success == 1) {
                        MainScreen.user = new User(
                                json.getJSONArray("user").getJSONObject(0).getInt("id"),
                                json.getJSONArray("user").getJSONObject(0).getString("email"),
                                json.getJSONArray("user").getJSONObject(0).getString("name"),
                                json.getJSONArray("user").getJSONObject(0).getString("phone")
                        );
                        LaunchScreen.mSettings = getSharedPreferences("my_storage", Context.MODE_PRIVATE);
                        SharedPreferences.Editor editor = LaunchScreen.mSettings.edit();
                        String hash = json.getJSONArray("user").getJSONObject(0).getString("token");
                        editor.putBoolean("is_logged", true).apply();
                        editor.putString("session_token", hash).apply();
                        Intent intent = new Intent(SignInScreen.this, MainScreen.class);
                        startActivity(intent);
                    }
                    else {
                        String message = json.getString("message");
                        runOnUiThread(new Runnable() {
                            @Override
                            public void run() {
                                Toast.makeText(SignInScreen.this, message, Toast.LENGTH_SHORT).show();
                            }
                        });
                    }
                }
            } catch (JSONException e) {
                e.printStackTrace();
            }

            return null;
        }

        protected void onPostExecute(String file_url) {
            pDialog.dismiss();
        }
    }
}