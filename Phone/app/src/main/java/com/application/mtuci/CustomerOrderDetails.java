package com.application.mtuci;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;
import androidx.core.app.ActivityCompat;

import android.Manifest;
import android.annotation.SuppressLint;
import android.app.ProgressDialog;
import android.content.pm.PackageManager;
import android.location.Location;
import android.location.LocationManager;
import android.os.AsyncTask;
import android.os.Bundle;
import android.os.Handler;
import android.util.Log;
import android.view.View;
import android.widget.Button;
import android.widget.TextView;
import android.widget.Toast;

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
import java.util.Map;
import java.util.Timer;
import java.util.TimerTask;
import java.util.concurrent.Executors;
import java.util.concurrent.TimeUnit;

public class CustomerOrderDetails extends AppCompatActivity {
    private ProgressDialog pDialog;
    //SupportMapFragment mapFragment;
    MapView mapView;
    public static int orderID = -1;
    JSONParser jParser = new JSONParser();
    public static HashMap<String, String> orderInfo = new HashMap<String, String>();
    JSONArray order = null;
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_customer_order_details);
        //mapFragment = (SupportMapFragment) getSupportFragmentManager().findFragmentById(R.id.mapView);
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
        /*mapFragment.getMapAsync(new OnMapReadyCallback() {
            @Override
            public void onMapReady(@NonNull GoogleMap googleMap) {
                if (ActivityCompat.checkSelfPermission(getApplicationContext(), Manifest.permission.ACCESS_FINE_LOCATION) != PackageManager.PERMISSION_GRANTED && ActivityCompat.checkSelfPermission(getApplicationContext(), Manifest.permission.ACCESS_COARSE_LOCATION) != PackageManager.PERMISSION_GRANTED) {
                    return;
                }
                googleMap.setMyLocationEnabled(true);
                String coords = orderInfo.get("coords");
                double coord1 = Double.parseDouble(coords.split(",")[0]);
                double coord2 = Double.parseDouble(coords.split(",")[1]);
                LatLng sydney = new LatLng(coord1, coord2);
                googleMap.addMarker(new MarkerOptions().position(sydney).title("Courier").snippet(orderInfo.get("courier")));
                CameraPosition cameraPosition = new CameraPosition.Builder().target(sydney).zoom(12).build();
                googleMap.animateCamera(CameraUpdateFactory.newCameraPosition(cameraPosition));
            }
        });
        Handler handler = new Handler();
        Runnable runnable = new Runnable() {
            @Override
            public void run() {
                mapFragment.getMapAsync(new OnMapReadyCallback() {
                    @Override
                    public void onMapReady(@NonNull GoogleMap googleMap) {
                        googleMap.clear();
                        String coords = orderInfo.get("coords");
                        double coord1 = Double.parseDouble(coords.split(",")[0]);
                        double coord2 = Double.parseDouble(coords.split(",")[1]);
                        LatLng sydney = new LatLng(coord1, coord2);
                        googleMap.addMarker(new MarkerOptions().position(sydney).title("Courier").snippet(orderInfo.get("courier")));
                    }
                });
                handler.postDelayed(this, 5000);
            }
        };
        handler.postDelayed(runnable, 5000);*/
        Button button = (Button) findViewById(R.id.button5);
        button.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                orderID = -1;
                onBackPressed();
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
        coords = orderInfo.get("coords_courier");
        coord1 = Double.parseDouble(coords.split(",")[0]);
        coord2 = Double.parseDouble(coords.split(",")[1]);
        GeoPoint position = new GeoPoint(coord1, coord2);
        Marker marker = new Marker(mapView);
        marker.setPosition(position);
        marker.setAnchor(Marker.ANCHOR_CENTER, Marker.ANCHOR_BOTTOM);
        marker.setTitle("Courier\n" + orderInfo.get("courier"));
        marker.setVisible(false);
        mapView.getOverlays().add(marker);
        if (orderInfo.get("status").equals("in process")) {
            Handler handler = new Handler();
            Runnable runnable = new Runnable() {
                @Override
                public void run() {
                    String courier_coords = orderInfo.get("coords_courier");
                    double courier_coord1 = Double.parseDouble(courier_coords.split(",")[0]);
                    double courier_coord2 = Double.parseDouble(courier_coords.split(",")[1]);
                    GeoPoint position = new GeoPoint(courier_coord1, courier_coord2);
                    for (int i = 0; i < mapView.getOverlays().size(); i++) {
                        Overlay overlay = mapView.getOverlays().get(i);
                        if (overlay instanceof Marker && ((Marker) overlay).getTitle().equals("Courier\n" + orderInfo.get("courier"))) {
                            ((Marker) overlay).setPosition(position);
                            ((Marker) overlay).setVisible(true);
                        }
                    }
                    mapView.invalidate();
                    handler.postDelayed(this, 5000);
                }
            };
            handler.postDelayed(runnable, 5000);
        }
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
                    String courier = c.getString("courier");
                    String price = c.getString("price");
                    String status = c.getString("status");
                    String address = c.getString("address");
                    String coords = c.getString("coords");
                    String address2 = c.getString("address2");
                    String coords2 = c.getString("coords2");
                    String coords_courier = c.getString("coords_courier");

                    orderInfo.put("id", id);
                    orderInfo.put("courier", courier);
                    orderInfo.put("price", price);
                    orderInfo.put("status", status);
                    orderInfo.put("address", address);
                    orderInfo.put("coords", coords);
                    orderInfo.put("address2", address2);
                    orderInfo.put("coords2", coords2);
                    orderInfo.put("coords_courier", coords_courier);
                    TextView txtID = (TextView) findViewById(R.id.textView6);
                    TextView txtCourier = (TextView) findViewById(R.id.textView8);
                    TextView txtPrice = (TextView) findViewById(R.id.textView9);
                    TextView txtAddress = (TextView) findViewById(R.id.textView10);
                    TextView txtStatus = (TextView) findViewById(R.id.textView14);
                    txtID.setText("Order №"+orderInfo.get("id"));
                    txtCourier.setText("Courier: "+orderInfo.get("courier"));
                    txtPrice.setText("Price: "+orderInfo.get("price")+"$");
                    txtAddress.setText("Address: "+orderInfo.get("address"));
                    txtStatus.setText(orderInfo.get("status"));
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
}