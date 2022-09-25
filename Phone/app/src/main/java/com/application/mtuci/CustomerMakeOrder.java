package com.application.mtuci;

import androidx.appcompat.app.AppCompatActivity;

import android.annotation.SuppressLint;
import android.content.AsyncQueryHandler;
import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.location.Address;
import android.location.LocationManager;
import android.os.AsyncTask;
import android.os.Bundle;
import android.os.Handler;
import android.text.Editable;
import android.text.TextWatcher;
import android.util.Log;
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
import org.osmdroid.api.IMapController;
import org.osmdroid.bonuspack.location.GeocoderNominatim;
import org.osmdroid.bonuspack.routing.OSRMRoadManager;
import org.osmdroid.bonuspack.routing.Road;
import org.osmdroid.bonuspack.routing.RoadManager;
import org.osmdroid.config.Configuration;
import org.osmdroid.events.MapEventsReceiver;
import org.osmdroid.tileprovider.tilesource.TileSourceFactory;
import org.osmdroid.util.GeoPoint;
import org.osmdroid.views.MapView;
import org.osmdroid.views.overlay.MapEventsOverlay;
import org.osmdroid.views.overlay.Marker;
import org.osmdroid.views.overlay.Overlay;
import org.osmdroid.views.overlay.Polyline;
import org.osmdroid.views.overlay.mylocation.GpsMyLocationProvider;
import org.osmdroid.views.overlay.mylocation.MyLocationNewOverlay;

import java.io.IOException;
import java.util.ArrayList;
import java.util.List;

public class CustomerMakeOrder extends AppCompatActivity {
    MapView mapView;
    Polyline roadOverlay;
    public static GeoPoint geoPointFrom;
    public static GeoPoint geoPointTo;
    public static Marker markerFrom;
    public static Marker markerTo;
    String From = "";
    String To = "";
    String price = "";
    @SuppressLint("ClickableViewAccessibility")
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_customer_make_order);
        Configuration.getInstance().setUserAgentValue(BuildConfig.APPLICATION_ID);
        mapView = (MapView) findViewById(R.id.mapView);
        mapView.setTileSource(TileSourceFactory.MAPNIK);
        mapView.setBuiltInZoomControls(true);
        mapView.setMultiTouchControls(true);
        IMapController mapController = mapView.getController();
        mapController.setZoom(2);
        GpsMyLocationProvider provider = new GpsMyLocationProvider(getApplicationContext());
        provider.addLocationSource(LocationManager.GPS_PROVIDER);
        MyLocationNewOverlay myLocationNewOverlay = new MyLocationNewOverlay(provider, mapView);
        myLocationNewOverlay.enableMyLocation();
        mapView.getOverlays().add(myLocationNewOverlay);
        EditText editTextFrom = (EditText) findViewById(R.id.editTextTextFrom);
        EditText editTextTo = (EditText) findViewById(R.id.editTextTextTo);
        EditText editTextPrice = (EditText) findViewById(R.id.editTextTextPrice);
        Button button9 = (Button) findViewById(R.id.button9);
        button9.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                onBackPressed();
            }
        });
        Button button10 = (Button) findViewById(R.id.button10);
        button10.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                price = editTextPrice.getText().toString();
                if (From.equals("") || To.equals("") || price.equals("")) {
                    Toast.makeText(CustomerMakeOrder.this, "You should fill all fields", Toast.LENGTH_SHORT).show();
                }
                else {
                    new MakeOrder().execute();
                }
            }
        });
        final LinearLayout bg = (LinearLayout) findViewById(R.id.linearLayout);
        bg.setOnTouchListener(new View.OnTouchListener() {
            @Override
            public boolean onTouch(View view, MotionEvent motionEvent) {
                InputMethodManager imm = (InputMethodManager) getSystemService(Context.INPUT_METHOD_SERVICE);
                imm.hideSoftInputFromWindow(view.getWindowToken(), 0);
                if ((!From.equals(editTextFrom.getText().toString()) || !To.equals(editTextTo.getText().toString()))) {
                    if (!editTextFrom.getText().toString().equals("")) new setMarkerFrom().execute();
                    if (!editTextTo.getText().toString().equals("")) new setMarkerTo().execute();
                }
                return false;
            }
        });
        Handler handler = new Handler();
        Runnable runnable = new Runnable() {
            @Override
            public void run() {
                if ((!From.equals(editTextFrom.getText().toString()) || !To.equals(editTextTo.getText().toString())) && geoPointFrom != null && geoPointTo != null) {
                    new setMarkerFrom().execute();
                    new setMarkerTo().execute();
                    for (int j = 0; j < mapView.getOverlays().size(); j++) {
                        Overlay overlay = mapView.getOverlays().get(j);
                        if (overlay instanceof Marker && ((Marker) overlay).getTitle().equals("From")) {
                            ((Marker) overlay).setPosition(geoPointFrom);
                            ((Marker) overlay).setVisible(true);
                        }
                        if (overlay instanceof Marker && ((Marker) overlay).getTitle().equals("To")) {
                            ((Marker) overlay).setPosition(geoPointTo);
                            ((Marker) overlay).setVisible(true);
                        }
                    }
                    From = editTextFrom.getText().toString();
                    To = editTextTo.getText().toString();
                    if (geoPointFrom != null && geoPointTo != null) new Route().execute();
                }
                handler.postDelayed(this, 1000);
            }
        };
        handler.postDelayed(runnable, 1000);
        markerFrom = new Marker(mapView);
        markerFrom.setPosition(new GeoPoint(0,0));
        markerFrom.setAnchor(Marker.ANCHOR_CENTER, Marker.ANCHOR_BOTTOM);
        markerFrom.setTitle("From");
        markerFrom.setVisible(false);
        mapView.getOverlays().add(markerFrom);
        markerTo = new Marker(mapView);
        markerTo.setPosition(new GeoPoint(0,0));
        markerTo.setAnchor(Marker.ANCHOR_CENTER, Marker.ANCHOR_BOTTOM);
        markerTo.setTitle("To");
        markerTo.setVisible(false);
        mapView.getOverlays().add(markerTo);
        MapEventsReceiver mReceive = new MapEventsReceiver() {
            @Override
            public boolean singleTapConfirmedHelper(GeoPoint p) {
                if (From.equals("")) {
                    markerFrom.setPosition(p);
                    geoPointFrom = new GeoPoint(p.getLatitude(), p.getLongitude());
                    new getAddressFrom().execute();
                }
                else if (To.equals("")) {
                    markerTo.setPosition(p);
                    geoPointTo = new GeoPoint(p.getLatitude(), p.getLongitude());
                    new getAddressTo().execute();
                    new Route().execute();
                }
                return false;
            }

            @Override
            public boolean longPressHelper(GeoPoint p) {
                From = "";
                To = "";
                editTextFrom.setText("");
                editTextTo.setText("");
                markerFrom.setVisible(false);
                markerTo.setVisible(false);
                mapView.getOverlays().remove(roadOverlay);
                return false;
            }
        };
        MapEventsOverlay mOverlay = new MapEventsOverlay(getBaseContext(), mReceive);
        mapView.getOverlays().add(mOverlay);
        mapView.invalidate();
    }
    class setMarkerFrom extends AsyncTask<String, String, String> {
        protected String doInBackground(String... args) {
            GeocoderNominatim geocoder = new GeocoderNominatim(Configuration.getInstance().getNormalizedUserAgent());
            EditText editTextFrom = (EditText) findViewById(R.id.editTextTextFrom);
            try {
                List<Address> coords = geocoder.getFromLocationName(editTextFrom.getText().toString(), 1);
                if (coords.size() > 0) {
                    Address address = coords.get(0);
                    geoPointFrom = new GeoPoint(address.getLatitude(), address.getLongitude());
                }

            } catch (IOException e) {
                e.printStackTrace();
            }
            return null;
        }
    }
    class setMarkerTo extends AsyncTask<String, String, String> {
        protected String doInBackground(String... args) {
            GeocoderNominatim geocoder = new GeocoderNominatim(Configuration.getInstance().getNormalizedUserAgent());
            EditText editTextTo = (EditText) findViewById(R.id.editTextTextTo);
            try {
                List<Address> coords = geocoder.getFromLocationName(editTextTo.getText().toString(), 1);
                if (coords.size() > 0) {
                    Address address = coords.get(0);
                    geoPointTo = new GeoPoint(address.getLatitude(), address.getLongitude());
                }

            } catch (IOException e) {
                e.printStackTrace();
            }
            return null;
        }
    }
    class getAddressFrom extends AsyncTask<String, String, String> {
        @SuppressLint("SetTextI18n")
        protected String doInBackground(String... args) {
            GeocoderNominatim geocoder = new GeocoderNominatim(Configuration.getInstance().getNormalizedUserAgent());
            EditText editTextFrom = (EditText) findViewById(R.id.editTextTextFrom);
            try {
                GeoPoint position = markerFrom.getPosition();
                List<Address> address = geocoder.getFromLocation(position.getLatitude(), position.getLongitude(), 1);
                if (address.size() > 0) {
                    Address adress = address.get(0);
                    String adr;
                    if(adress.getLocality() != null) adr = adress.getLocality();
                    else adr = adress.getAdminArea();
                    if(adress.getThoroughfare() != null) adr += ", "+adress.getThoroughfare();
                    if(adress.getSubThoroughfare() != null) adr += ", "+adress.getSubThoroughfare();
                    editTextFrom.setText(adr);
                    From = adr;
                }
                else {
                    editTextFrom.setText(position.getLatitude()+", "+position.getLongitude());
                    From = position.getLatitude()+", "+position.getLongitude();
                }
                markerFrom.setVisible(true);
            } catch (Exception e) {
                e.printStackTrace();
            }
            return null;
        }
    }
    class getAddressTo extends AsyncTask<String, String, String> {
        @SuppressLint("SetTextI18n")
        protected String doInBackground(String... args) {
            GeocoderNominatim geocoder = new GeocoderNominatim(Configuration.getInstance().getNormalizedUserAgent());
            EditText editTextTo = (EditText) findViewById(R.id.editTextTextTo);
            try {
                GeoPoint position = markerTo.getPosition();
                List<Address> address = geocoder.getFromLocation(position.getLatitude(), position.getLongitude(), 1);
                if (address.size() > 0) {
                    Address adress = address.get(0);
                    String adr;
                    if(adress.getLocality() != null) adr = adress.getLocality();
                    else adr = adress.getAdminArea();
                    if(adress.getThoroughfare() != null) adr += ", "+adress.getThoroughfare();
                    if(adress.getSubThoroughfare() != null) adr += ", "+adress.getSubThoroughfare();
                    editTextTo.setText(adr);
                    To = adr;
                }
                else {
                    editTextTo.setText(position.getLatitude()+", "+position.getLongitude());
                    To = position.getLatitude()+", "+position.getLongitude();
                }
                markerTo.setVisible(true);
            } catch (Exception e) {
                e.printStackTrace();
            }
            return null;
        }
    }
    class Route extends AsyncTask<String, String, String> {
        protected String doInBackground(String... args) {
            mapView.getOverlays().remove(roadOverlay);
            RoadManager roadManager = new OSRMRoadManager(getApplicationContext(), BuildConfig.APPLICATION_ID);
            ArrayList<GeoPoint> waypoints = new ArrayList<GeoPoint>();
            String coords = geoPointFrom.getLatitude()+","+geoPointFrom.getLongitude();
            double coord1 = Double.parseDouble(coords.split(",")[0]);
            double coord2 = Double.parseDouble(coords.split(",")[1]);
            GeoPoint start = new GeoPoint(coord1, coord2);
            waypoints.add(start);
            coords = geoPointTo.getLatitude()+","+geoPointTo.getLongitude();
            coord1 = Double.parseDouble(coords.split(",")[0]);
            coord2 = Double.parseDouble(coords.split(",")[1]);
            GeoPoint end = new GeoPoint(coord1,coord2);
            waypoints.add(end);
            Road road = roadManager.getRoad(waypoints);
            roadOverlay = RoadManager.buildRoadOverlay(road);
            mapView.getOverlays().add(roadOverlay);
            mapView.invalidate();
            return null;
        }
    }
    class MakeOrder extends AsyncTask<String, String, String> {
        protected String doInBackground(String... args) {
            List<NameValuePair> params = new ArrayList<NameValuePair>();
            params.add(new BasicNameValuePair("customer_id", String.valueOf(CustomerMainScreen.user.getID())));
            params.add(new BasicNameValuePair("customer", CustomerMainScreen.user.getName()));
            params.add(new BasicNameValuePair("price", price));
            params.add(new BasicNameValuePair("address", From));
            params.add(new BasicNameValuePair("coords", geoPointFrom.getLatitude()+","+geoPointFrom.getLongitude()));
            params.add(new BasicNameValuePair("address2", To));
            params.add(new BasicNameValuePair("coords2", geoPointTo.getLatitude()+","+geoPointTo.getLongitude()));
            JSONParser jsonParser = new JSONParser();
            JSONObject json = jsonParser.makeHttpRequest("http://5.180.137.9/make_order.php", "POST", params);
            try {
                if (json != null) {
                    int success = json.getInt("success");
                    if (success == 1) {
                        CustomerOrderDetails.orderID = json.getInt("id");
                        Intent intent = new Intent(CustomerMakeOrder.this, CustomerOrderDetails.class);
                        startActivity(intent);
                        finish();
                    }
                    else {
                        String message = json.getString("message");
                        runOnUiThread(new Runnable() {
                            @Override
                            public void run() {
                                Toast.makeText(CustomerMakeOrder.this, message, Toast.LENGTH_SHORT).show();
                            }
                        });
                    }
                }
            } catch (JSONException e) {
                e.printStackTrace();
            }
            return null;
        }
    }
}