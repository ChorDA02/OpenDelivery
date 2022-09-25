package com.application.mtuci;

import com.google.android.gms.location.FusedLocationProviderClient;
import com.google.android.gms.location.LocationServices;
import com.google.android.gms.tasks.OnSuccessListener;
import static android.Manifest.permission.ACCESS_FINE_LOCATION;

import androidx.appcompat.app.AppCompatActivity;
import androidx.core.app.ActivityCompat;

import android.Manifest;
import android.app.ListActivity;
import android.app.ProgressDialog;
import android.content.AsyncQueryHandler;
import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.content.pm.PackageManager;
import android.location.Location;
import android.location.LocationListener;
import android.location.LocationManager;
import android.os.AsyncTask;
import android.os.Bundle;
import android.os.Handler;
import android.util.Log;
import android.view.View;
import android.widget.AdapterView;
import android.widget.Button;
import android.widget.ImageView;
import android.widget.ListAdapter;
import android.widget.ListView;
import android.widget.SimpleAdapter;
import android.widget.TextView;
import android.widget.Toast;

import com.google.android.gms.tasks.OnSuccessListener;

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
import java.util.stream.Collectors;

public class CourierMainScreen extends ListActivity {
    JSONParser jParser = new JSONParser();
    public static ArrayList<HashMap<String, String>> ordersList;
    JSONArray orders = null;
    public static Location location;
    FusedLocationProviderClient client;
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_courier_main_screen);
        ordersList = new ArrayList<HashMap<String, String>>();
        ActivityCompat.requestPermissions(this, new String[]{ACCESS_FINE_LOCATION}, 1);
        client = LocationServices.getFusedLocationProviderClient(this);
        Handler handler = new Handler();
        Runnable runnable = new Runnable() {
            @Override
            public void run() {
                if (ActivityCompat.checkSelfPermission(CourierMainScreen.this, ACCESS_FINE_LOCATION) != PackageManager.PERMISSION_GRANTED ) {
                    return;
                }
                client.getLastLocation().addOnSuccessListener(CourierMainScreen.this, new OnSuccessListener() {
                    @Override
                    public void onSuccess(Object o) {
                        location = (Location) o;
                    }
                });
                handler.postDelayed(this, 5000);
            }
        };
        handler.postDelayed(runnable, 5000);
        Executors.newSingleThreadScheduledExecutor().scheduleAtFixedRate(new Runnable() {
            @Override
            public void run() {
                new LoadOrders().execute();
                new UpdateCoords().execute();
            }
        }, 0, 5, TimeUnit.SECONDS);
        ListView orders = getListView();
        orders.setOnItemClickListener(new AdapterView.OnItemClickListener() {
            @Override
            public void onItemClick(AdapterView<?> adapterView, View view, int i, long l) {
                runOnUiThread(new Runnable() {
                    public void run() {
                        CourierOrderDetails.orderID = Integer.parseInt(String.valueOf(((TextView) view.findViewById(R.id.id)).getText().toString().split("№")[1]));
                        Intent intent = new Intent(CourierMainScreen.this, CourierOrderDetails.class);
                        startActivity(intent);
                    }
                });
            }
        });
        ImageView imageView6 = (ImageView) findViewById(R.id.imageView6);
        imageView6.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                //
            }
        });
        ImageView imageView7 = (ImageView) findViewById(R.id.imageView7);
        imageView7.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                SharedPreferences mSettings = getSharedPreferences("my_storage", Context.MODE_PRIVATE);
                SharedPreferences.Editor editor = mSettings.edit();
                editor.putBoolean("is_logged", false).apply();
                editor.putString("session_token", "NO TOKEN").apply();
                Intent intent = new Intent(CourierMainScreen.this, MainActivity.class);
                startActivity(intent);
                finish();
            }
        });
    }

    class UpdateCoords extends AsyncTask<String, String, String> {
        protected String doInBackground(String... args) {
            if (location != null) {
                List<NameValuePair> params = new ArrayList<NameValuePair>();
                params.add(new BasicNameValuePair("id", String.valueOf(CustomerMainScreen.user.getID())));
                params.add(new BasicNameValuePair("coords", location.getLatitude() + "," + location.getLongitude()));
                jParser.makeHttpRequest("http://5.180.137.9/update_coords.php", "POST", params);
            }
            return null;
        }
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
            params.add(new BasicNameValuePair("id", String.valueOf(CustomerMainScreen.user.getID())));
            JSONObject json = jParser.makeHttpRequest("http://5.180.137.9/get_courier_orders.php", "POST", params);

            try {
                int success = json.getInt("success");
                TextView textView16 = (TextView) findViewById(R.id.textView16);
                if (success == 1) {
                    orders = json.getJSONArray("orders");

                    for (int i = 0; i < orders.length(); i++) {
                        JSONObject c = orders.getJSONObject(i);

                        String id = c.getString("id");
                        String customer = c.getString("customer");
                        String price = c.getString("price");
                        String status = c.getString("status");
                        String address = c.getString("address2");

                        HashMap<String, String> map = new HashMap<String, String>();

                        map.put("id", "Order №"+id);
                        map.put("customer", "Customer: "+customer);
                        map.put("price", "Price: "+price+"$");
                        map.put("status", status);
                        map.put("address", "Address: "+address);
                        ordersList.add(map);
                    }
                    textView16.setVisibility(View.INVISIBLE);
                } else {
                    textView16.setVisibility(View.VISIBLE);
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
                            CourierMainScreen.this, ordersList,
                            R.layout.list_item, new String[] { "id",
                            "customer", "price", "status"},
                            new int[] { R.id.id, R.id.courier, R.id.price, R.id.status2 });
                    setListAdapter(adapter);
                }
            });
        }

    }
}