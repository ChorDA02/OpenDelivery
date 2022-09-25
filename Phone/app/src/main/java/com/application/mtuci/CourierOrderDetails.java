package com.application.mtuci;

import androidx.appcompat.app.AppCompatActivity;

import android.annotation.SuppressLint;
import android.app.ProgressDialog;
import android.location.LocationManager;
import android.os.AsyncTask;
import android.os.Bundle;
import android.os.Handler;
import android.util.Log;
import android.view.View;
import android.widget.Button;
import android.widget.TextView;

import org.apache.http.NameValuePair;
import org.apache.http.message.BasicNameValuePair;
import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;
import org.osmdroid.api.IMapController;
import org.osmdroid.bonuspack.routing.OSRMRoadManager;
import org.osmdroid.bonuspack.routing.Road;
import org.osmdroid.bonuspack.routing.RoadManager;
import org.osmdroid.config.Configuration;
import org.osmdroid.tileprovider.tilesource.TileSourceFactory;
import org.osmdroid.util.GeoPoint;
import org.osmdroid.views.MapView;
import org.osmdroid.views.overlay.Marker;
import org.osmdroid.views.overlay.Overlay;
import org.osmdroid.views.overlay.Polyline;
import org.osmdroid.views.overlay.mylocation.GpsMyLocationProvider;
import org.osmdroid.views.overlay.mylocation.MyLocationNewOverlay;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.concurrent.Executors;
import java.util.concurrent.TimeUnit;

public class CourierOrderDetails extends AppCompatActivity {
    private ProgressDialog pDialog;
    MapView mapView;
    public static int orderID = -1;
    JSONParser jParser = new JSONParser();
    public static HashMap<String, String> orderInfo = new HashMap<String, String>();
    JSONArray order = null;
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_courier_order_details);
        Configuration.getInstance().setUserAgentValue(BuildConfig.APPLICATION_ID);
        mapView = (MapView) findViewById(R.id.mapView);
        mapView.setTileSource(TileSourceFactory.MAPNIK);
        mapView.setBuiltInZoomControls(true);
        mapView.setMultiTouchControls(true);
        IMapController mapController = mapView.getController();
        mapController.setZoom(18);
        GpsMyLocationProvider provider = new GpsMyLocationProvider(getApplicationContext());
        provider.addLocationSource(LocationManager.GPS_PROVIDER);
        MyLocationNewOverlay myLocationNewOverlay = new MyLocationNewOverlay(provider, mapView);
        myLocationNewOverlay.enableMyLocation();
        mapView.getOverlays().add(myLocationNewOverlay);
        Executors.newSingleThreadScheduledExecutor().scheduleAtFixedRate(new Runnable() {
            @Override
            public void run() {
                new getOrderInfo().execute();
            }
        }, 0, 5, TimeUnit.SECONDS);
        Button button = (Button) findViewById(R.id.button9);
        button.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                orderID = -1;
                onBackPressed();
            }
        });
        Button button11 = (Button) findViewById(R.id.button11);
        button11.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                new UpdateStatus().execute();
            }
        });
        while (orderInfo.size() < 9) {
            assert true;
        }
        String coords = orderInfo.get("coords");
        double coord1 = Double.parseDouble(coords.split(",")[0]);
        double coord2 = Double.parseDouble(coords.split(",")[1]);
        GeoPoint startPoint = new GeoPoint(coord1, coord2);
        mapController.setCenter(startPoint);
        new Route().execute();
    }
    class Route extends AsyncTask<String, String, String> {
        protected String doInBackground(String... args) {
            RoadManager roadManager = new OSRMRoadManager(getApplicationContext(), BuildConfig.APPLICATION_ID);
            ArrayList<GeoPoint> waypoints = new ArrayList<GeoPoint>();
            String coords = orderInfo.get("coords");
            double coord1 = Double.parseDouble(coords.split(",")[0]);
            double coord2 = Double.parseDouble(coords.split(",")[1]);
            GeoPoint start = new GeoPoint(coord1, coord2);
            waypoints.add(start);
            coords = orderInfo.get("coords2");
            coord1 = Double.parseDouble(coords.split(",")[0]);
            coord2 = Double.parseDouble(coords.split(",")[1]);
            GeoPoint end = new GeoPoint(coord1,coord2);
            waypoints.add(end);
            Road road = roadManager.getRoad(waypoints);
            Polyline roadOverlay = RoadManager.buildRoadOverlay(road);
            mapView.getOverlays().add(roadOverlay);
            mapView.invalidate();
            return null;
        }
    }
    class getOrderInfo extends AsyncTask<String, String, String> {
        @Override
        protected void onPreExecute() {
            super.onPreExecute();
            /*pDialog = new ProgressDialog(CustomerOrderDetails.this);
            pDialog.setMessage("Загрузка заказа. Подождите...");
            pDialog.setIndeterminate(false);
            pDialog.setCancelable(false);
            pDialog.show();*/
        }
        @SuppressLint("SetTextI18n")
        protected String doInBackground(String... args) {
            List<NameValuePair> params = new ArrayList<NameValuePair>();
            params.add(new BasicNameValuePair("id", String.valueOf(orderID)));
            JSONObject json = jParser.makeHttpRequest("http://5.180.137.9/get_order.php", "POST", params);

            try {
                int success = json.getInt("success");

                if (success == 1) {
                    order = json.getJSONArray("orders");

                    JSONObject c = order.getJSONObject(0);

                    String id = c.getString("id");
                    String customer = c.getString("customer");
                    String price = c.getString("price");
                    String status = c.getString("status");
                    String address = c.getString("address");
                    String coords = c.getString("coords");
                    String address2 = c.getString("address2");
                    String coords2 = c.getString("coords2");
                    String coords_courier = c.getString("coords_courier");

                    orderInfo.put("id", id);
                    orderInfo.put("customer", customer);
                    orderInfo.put("price", price);
                    orderInfo.put("status", status);
                    orderInfo.put("address", address);
                    orderInfo.put("coords", coords);
                    orderInfo.put("address2", address2);
                    orderInfo.put("coords2", coords2);
                    orderInfo.put("coords_courier", coords_courier);
                    TextView txtID = (TextView) findViewById(R.id.textView21);
                    TextView txtCustomer = (TextView) findViewById(R.id.textView18);
                    TextView txtPrice = (TextView) findViewById(R.id.textView22);
                    TextView txtAddress = (TextView) findViewById(R.id.textView19);
                    TextView txtStatus = (TextView) findViewById(R.id.textView20);
                    txtID.setText("Order №"+orderInfo.get("id"));
                    txtCustomer.setText("Customer: "+orderInfo.get("customer"));
                    txtPrice.setText("Price: "+orderInfo.get("price")+"$");
                    txtAddress.setText("Address: "+orderInfo.get("address"));
                    txtStatus.setText(orderInfo.get("status"));
                    Button button11 = (Button) findViewById(R.id.button11);
                    if (orderInfo.get("status").equals("waiting for courier...")) {
                        button11.setText("Pick order");
                        button11.setVisibility(View.VISIBLE);
                    }
                    else if (orderInfo.get("status").equals("in process")) {
                        button11.setText("Mark as delivered");
                        button11.setVisibility(View.VISIBLE);
                    }
                    else {
                        button11.setVisibility(View.INVISIBLE);
                    }
                } else {
                    // заказ не найден
                }
            } catch (JSONException e) {
                e.printStackTrace();
            }

            return null;
        }

        protected void onPostExecute(String file_url) {
            //pDialog.dismiss();
        }

    }
    class UpdateStatus extends AsyncTask<String, String, String> {
        @SuppressLint("WrongThread")
        protected String doInBackground(String... args) {
            List<NameValuePair> params = new ArrayList<NameValuePair>();
            params.add(new BasicNameValuePair("id", String.valueOf(orderID)));
            params.add(new BasicNameValuePair("courier_id", String.valueOf(CustomerMainScreen.user.getID())));
            params.add(new BasicNameValuePair("courier", CustomerMainScreen.user.getName()));
            Button button11 = (Button) findViewById(R.id.button11);
            String text = button11.getText().toString();
            if (text.equals("Pick order")) {
                params.add(new BasicNameValuePair("status", "in process"));
            }
            else if (text.equals("Mark as delivered")) {
                params.add(new BasicNameValuePair("status", "delivered"));
            }
            else {
                return null;
            }
            JSONObject json = jParser.makeHttpRequest("http://5.180.137.9/update_status.php", "POST", params);

            try {
                int success = json.getInt("success");

                if (success == 1) {
                    new getOrderInfo().execute();
                } else {
                    // заказ не найден
                }
            } catch (JSONException e) {
                e.printStackTrace();
            }
            return null;
        }
    }
}