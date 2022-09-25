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
import android.widget.Toast;

import org.apache.http.NameValuePair;
import org.apache.http.message.BasicNameValuePair;
import org.json.JSONException;
import org.json.JSONObject;

import java.util.ArrayList;
import java.util.List;

public class SignUpActivity extends AppCompatActivity {
    private ProgressDialog pDialog;
    @SuppressLint("StaticFieldLeak")
    static EditText email;
    @SuppressLint("StaticFieldLeak")
    static EditText password;
    @SuppressLint("StaticFieldLeak")
    static EditText name;
    @SuppressLint("StaticFieldLeak")
    static EditText phone;
    @SuppressLint("ClickableViewAccessibility")
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_sign_up);
        Button button = (Button) findViewById(R.id.button7);
        email = (EditText) findViewById(R.id.editTextTextEmailAddress1);
        password = (EditText) findViewById(R.id.editTextTextPassword1);
        name = (EditText) findViewById(R.id.editTextTextPersonName);
        phone = (EditText) findViewById(R.id.editTextPhone);
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
                InputMethodManager imm = (InputMethodManager)getSystemService(Context.INPUT_METHOD_SERVICE);
                imm.hideSoftInputFromWindow(view.getWindowToken(), 0);
                return false;
            }
        });
        Button reg = (Button) findViewById(R.id.button8);
        reg.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                if(!email.getText().toString().contains("@")) Toast.makeText(getApplicationContext(), "Input correct e-mail", Toast.LENGTH_SHORT).show();
                else if(email.getText().toString().length() < 8) Toast.makeText(getApplicationContext(), "Input correct e-mail", Toast.LENGTH_SHORT).show();
                else if(password.getText().toString().length() < 8) Toast.makeText(getApplicationContext(), "Input correct password", Toast.LENGTH_SHORT).show();
                else if(!name.getText().toString().contains(" ")) Toast.makeText(getApplicationContext(), "Input correct name", Toast.LENGTH_SHORT).show();
                else if(name.getText().toString().length() < 8) Toast.makeText(getApplicationContext(), "Input correct name", Toast.LENGTH_SHORT).show();
                else if(phone.getText().toString().length() < 8) Toast.makeText(getApplicationContext(), "Input correct phone", Toast.LENGTH_SHORT).show();
                else new RegUser().execute();
            }
        });
    }
    class RegUser extends AsyncTask<String, String, String> {
        String message;
        @Override
        protected void onPreExecute() {
            super.onPreExecute();
            pDialog = new ProgressDialog(SignUpActivity.this);
            pDialog.setMessage("Выполняется регистрация");
            pDialog.setIndeterminate(false);
            pDialog.setCancelable(true);
            pDialog.show();
        }
        protected String doInBackground(String[] args) {
            String email = SignUpActivity.email.getText().toString();
            String password = SignUpActivity.password.getText().toString();
            String name = SignUpActivity.name.getText().toString();
            String phone = SignUpActivity.phone.getText().toString();
            List<NameValuePair> params = new ArrayList<NameValuePair>();
            params.add(new BasicNameValuePair("email", email));
            params.add(new BasicNameValuePair("password", password));
            params.add(new BasicNameValuePair("name", name));
            params.add(new BasicNameValuePair("phone", phone));

            JSONParser jsonParser = new JSONParser();
            JSONObject json = jsonParser.makeHttpRequest("http://5.180.137.9/reg_user.php", "POST", params);

            try {
                if (json != null)
                {
                    int success = json.getInt("success");
                    message = json.getString("message");
                    if (success == 1) {
                        CustomerMainScreen.user = new User(json.getInt("id"), email, name, phone);
                        LaunchActivity.mSettings = getSharedPreferences("my_storage", Context.MODE_PRIVATE);
                        SharedPreferences.Editor editor = LaunchActivity.mSettings.edit();
                        String hash = json.getString("token");
                        editor.putBoolean("is_logged", true).apply();
                        editor.putString("session_token", hash).apply();
                        Intent intent = new Intent(SignUpActivity.this, CustomerMainScreen.class);
                        startActivity(intent);
                    }
                }
            } catch (JSONException e) {
                e.printStackTrace();
            }

            return null;
        }
        protected void onPostExecute(String file_url) {
            pDialog.dismiss();
            Toast.makeText(getApplicationContext(), message, Toast.LENGTH_SHORT).show();
        }
    }
}