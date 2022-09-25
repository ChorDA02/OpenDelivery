package com.application.mtuci;

import static android.Manifest.permission.ACCESS_FINE_LOCATION;

import androidx.appcompat.app.AppCompatActivity;
import androidx.core.app.ActivityCompat;

import android.app.ProgressDialog;
import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.os.AsyncTask;
import android.os.Bundle;

import org.apache.http.NameValuePair;
import org.apache.http.message.BasicNameValuePair;
import org.json.JSONException;
import org.json.JSONObject;

import java.util.ArrayList;
import java.util.List;

public class LaunchActivity extends AppCompatActivity {
    public static SharedPreferences mSettings;
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_launch);
        ActivityCompat.requestPermissions(this, new String[]{ACCESS_FINE_LOCATION}, 1);
        new CheckLogin().execute();
    }
    class CheckLogin extends AsyncTask<String, String, String> {
        protected String doInBackground(String[] args) {
            mSettings = getSharedPreferences("my_storage", Context.MODE_PRIVATE);
            if (mSettings.getBoolean("is_logged", false)) {
                JSONParser jsonParser = new JSONParser();
                List<NameValuePair> params = new ArrayList<NameValuePair>();
                params.add(new BasicNameValuePair("session_token", mSettings.getString("session_token", "")));
                JSONObject json = jsonParser.makeHttpRequest("http://5.180.137.9/get_token.php", "POST", params);
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
                            if (json.getJSONArray("user").getJSONObject(0).getInt("courier") == 1) {
                                Intent intent = new Intent(LaunchActivity.this, CourierMainScreen.class);
                                startActivity(intent);
                            }
                            else {
                                Intent intent = new Intent(LaunchActivity.this, CustomerMainScreen.class);
                                startActivity(intent);
                            }
                            finish();
                        }
                        else {
                            Intent intent = new Intent(LaunchActivity.this, MainActivity.class);
                            startActivity(intent);
                            finish();
                        }
                    }
                } catch (JSONException e) {
                    e.printStackTrace();
                }
            }
            else {
                Intent intent = new Intent(LaunchActivity.this, MainActivity.class);
                startActivity(intent);
                finish();
            }
            return null;
        }
    }
}