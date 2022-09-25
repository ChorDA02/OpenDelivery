package com.application.mtuci2;

import static android.Manifest.permission.ACCESS_FINE_LOCATION;

import androidx.appcompat.app.AppCompatActivity;
import androidx.core.app.ActivityCompat;

import android.app.Activity;
import android.app.ListActivity;
import android.app.ProgressDialog;
import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.os.AsyncTask;
import android.os.Bundle;
import android.view.View;
import android.widget.AdapterView;
import android.widget.ImageView;
import android.widget.ListAdapter;
import android.widget.ListView;
import android.widget.SimpleAdapter;
import android.widget.TextView;

import org.apache.http.NameValuePair;
import org.apache.http.message.BasicNameValuePair;
import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.concurrent.Executors;
import java.util.concurrent.TimeUnit;

public class MainScreen extends ListActivity {
    private ProgressDialog pDialog;
    public static User user;
    JSONParser jParser = new JSONParser();
    public static ArrayList<HashMap<String, String>> ordersList;
    JSONArray orders = null;
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main_screen);
        ordersList = new ArrayList<HashMap<String, String>>();
        ActivityCompat.requestPermissions(this, new String[]{ACCESS_FINE_LOCATION}, 1);
        Executors.newSingleThreadScheduledExecutor().scheduleAtFixedRate(new Runnable() {
            @Override
            public void run() {
                new LoadOrders().execute();
            }
        }, 0, 5, TimeUnit.SECONDS);
        ListView orders = getListView();
        orders.setOnItemClickListener(new AdapterView.OnItemClickListener() {
            @Override
            public void onItemClick(AdapterView<?> adapterView, View view, int i, long l) {
                OrderDetails.orderID = Integer.parseInt(String.valueOf(((TextView) view.findViewById(R.id.id)).getText().toString().split("№")[1]));
                Intent intent = new Intent(MainScreen.this, OrderDetails.class);
                startActivity(intent);
            }
        });
        ImageView imageView3 = (ImageView) findViewById(R.id.imageView3);
        imageView3.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                SharedPreferences mSettings = getSharedPreferences("my_storage", Context.MODE_PRIVATE);
                SharedPreferences.Editor editor = mSettings.edit();
                editor.putBoolean("is_logged", false).apply();
                editor.putString("session_token", "NO TOKEN").apply();
                Intent intent = new Intent(MainScreen.this, MainActivity.class);
                startActivity(intent);
                finish();
            }
        });
    }
    class LoadOrders extends AsyncTask<String, String, String> {
        @Override
        protected void onPreExecute() {
            super.onPreExecute();
            ordersList.clear();
            /*pDialog = new ProgressDialog(CustomerMainScreen.this);
            pDialog.setMessage("Загрузка заказов. Подождите...");
            pDialog.setIndeterminate(false);
            pDialog.setCancelable(false);
            pDialog.show();*/
        }
        protected String doInBackground(String... args) {
            List<NameValuePair> params = new ArrayList<NameValuePair>();
            params.add(new BasicNameValuePair("id", String.valueOf(user.getID())));
            JSONObject json = jParser.makeHttpRequest("http://5.180.137.9/get_orders.php", "POST", params);

            try {
                int success = json.getInt("success");
                TextView textView3 = (TextView) findViewById(R.id.textView3);
                if (success == 1) {
                    orders = json.getJSONArray("orders");

                    for (int i = 0; i < orders.length(); i++) {
                        JSONObject c = orders.getJSONObject(i);

                        String id = c.getString("id");
                        String courier = c.getString("courier");
                        String price = c.getString("price");
                        String status = c.getString("status");
                        String address = c.getString("address2");

                        HashMap<String, String> map = new HashMap<String, String>();

                        map.put("id", "Order №"+id);
                        map.put("courier", "Courier: "+courier);
                        map.put("price", "Price: "+price+"$");
                        map.put("status", status);
                        map.put("address", "Address: "+address);

                        ordersList.add(map);
                    }
                    textView3.setVisibility(View.INVISIBLE);
                } else {
                    textView3.setVisibility(View.VISIBLE);
                }
            } catch (JSONException e) {
                e.printStackTrace();
            }

            return null;
        }

        protected void onPostExecute(String file_url) {
            //pDialog.dismiss();
            runOnUiThread(new Runnable() {
                public void run() {
                    ListAdapter adapter = new SimpleAdapter(
                            MainScreen.this, ordersList,
                            R.layout.list_item, new String[] { "id",
                            "courier", "price", "status"},
                            new int[] { R.id.id, R.id.courier, R.id.price, R.id.status2 });
                    setListAdapter(adapter);
                }
            });
        }

    }
}