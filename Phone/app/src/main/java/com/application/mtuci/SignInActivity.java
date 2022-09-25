package com.application.mtuci;

import androidx.appcompat.app.AppCompatActivity;

import android.annotation.SuppressLint;
import android.app.ProgressDialog;
import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.os.AsyncTask;
import android.os.Bundle;
import android.view.MotionEvent;
import android.view.View;
import android.view.inputmethod.InputMethodManager;
import android.widget.Button;
import android.widget.EditText;
import android.widget.LinearLayout;
import android.widget.TextView;
import android.widget.Toast;

import org.apache.http.NameValuePair;
import org.apache.http.message.BasicNameValuePair;
import org.json.JSONException;
import org.json.JSONObject;

import java.util.ArrayList;
import java.util.List;

public class SignInActivity extends AppCompatActivity {
    private ProgressDialog pDialog;
    @SuppressLint("StaticFieldLeak")
    static EditText email;
    @SuppressLint("StaticFieldLeak")
    static EditText password;
    @SuppressLint("ClickableViewAccessibility")
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_sign_in);
        Button button = (Button) findViewById(R.id.button7);
        email = (EditText) findViewById(R.id.editTextTextEmailAddress1);
        password = (EditText) findViewById(R.id.editTextTextPassword1);
        button.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                onBackPressed();
            }
        });
        final LinearLayout bg = (LinearLayout) findViewById(R.id.linearLayout);
        bg.setOnTouchListener(new View.OnTouchListener() {
            @Override
            public boolean onTouch(View view, MotionEvent motionEvent) {
                InputMethodManager imm = (InputMethodManager) getSystemService(Context.INPUT_METHOD_SERVICE);
                imm.hideSoftInputFromWindow(view.getWindowToken(), 0);
                return false;
            }
        });
        final TextView textView7 = (TextView) findViewById(R.id.textView7);
        textView7.setOnTouchListener(new View.OnTouchListener() {
            @Override
            public boolean onTouch(View view, MotionEvent motionEvent) {
                return false;
            }
        });
        Button login = (Button) findViewById(R.id.button8);
        login.setOnClickListener(new View.OnClickListener() {
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
            pDialog = new ProgressDialog(SignInActivity.this);
            pDialog.setMessage("Выполняется авторизация");
            pDialog.setIndeterminate(false);
            pDialog.setCancelable(true);
            pDialog.show();
        }

        protected String doInBackground(String[] args) {
            String email = SignInActivity.email.getText().toString();
            String password = SignInActivity.password.getText().toString();

            List<NameValuePair> params = new ArrayList<NameValuePair>();
            params.add(new BasicNameValuePair("email", email));
            params.add(new BasicNameValuePair("password", password));

            JSONParser jsonParser = new JSONParser();
            JSONObject json = jsonParser.makeHttpRequest("http://5.180.137.9/login_user.php", "POST", params);

            try {
                if (json != null) {
                    int success = json.getInt("success");
                    if (success == 1) {
                        CustomerMainScreen.user = new User(
                                json.getJSONArray("user").getJSONObject(0).getInt("id"),
                                json.getJSONArray("user").getJSONObject(0).getString("email"),
                                json.getJSONArray("user").getJSONObject(0).getString("name"),
                                json.getJSONArray("user").getJSONObject(0).getString("phone")
                        );
                        LaunchActivity.mSettings = getSharedPreferences("my_storage", Context.MODE_PRIVATE);
                        SharedPreferences.Editor editor = LaunchActivity.mSettings.edit();
                        String hash = json.getJSONArray("user").getJSONObject(0).getString("token");
                        editor.putBoolean("is_logged", true).apply();
                        editor.putString("session_token", hash).apply();
                        if (json.getJSONArray("user").getJSONObject(0).getInt("courier") == 1) {
                            Intent intent = new Intent(SignInActivity.this, CourierMainScreen.class);
                            startActivity(intent);
                        }
                        else {
                            Intent intent = new Intent(SignInActivity.this, CustomerMainScreen.class);
                            startActivity(intent);
                        }
                    }
                    else {
                        String message = json.getString("message");
                        runOnUiThread(new Runnable() {
                            @Override
                            public void run() {
                                Toast.makeText(SignInActivity.this, message, Toast.LENGTH_SHORT).show();
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