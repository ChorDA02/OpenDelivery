package com.application.mtuci2;

import androidx.appcompat.app.AppCompatActivity;

import android.app.Activity;
import android.location.LocationManager;
import android.os.AsyncTask;
import android.os.Bundle;
import android.os.Handler;
import android.view.View;
import android.widget.Button;

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

public class MapScreen extends Activity {
    MapView mapView;
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_map_screen);
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
        Button button5 = (Button) findViewById(R.id.button5);
        button5.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                onBackPressed();
            }
        });
        while (OrderDetails.orderInfo.size() < 9) {
            assert true;
        }
        String coords = OrderDetails.orderInfo.get("coords");
        double coord1 = Double.parseDouble(coords.split(",")[0]);
        double coord2 = Double.parseDouble(coords.split(",")[1]);
        GeoPoint startPoint = new GeoPoint(coord1, coord2);
        mapController.setCenter(startPoint);
        coords = OrderDetails.orderInfo.get("coords_courier");
        coord1 = Double.parseDouble(coords.split(",")[0]);
        coord2 = Double.parseDouble(coords.split(",")[1]);
        GeoPoint position = new GeoPoint(coord1, coord2);
        Marker marker = new Marker(mapView);
        marker.setPosition(position);
        marker.setAnchor(Marker.ANCHOR_CENTER, Marker.ANCHOR_BOTTOM);
        marker.setTitle("Courier\n" + OrderDetails.orderInfo.get("courier"));
        marker.setVisible(false);
        mapView.getOverlays().add(marker);
        if (OrderDetails.orderInfo.get("status").equals("in process")) {
            Handler handler = new Handler();
            Runnable runnable = new Runnable() {
                @Override
                public void run() {
                    String courier_coords = OrderDetails.orderInfo.get("coords_courier");
                    double courier_coord1 = Double.parseDouble(courier_coords.split(",")[0]);
                    double courier_coord2 = Double.parseDouble(courier_coords.split(",")[1]);
                    GeoPoint position = new GeoPoint(courier_coord1, courier_coord2);
                    for (int i = 0; i < mapView.getOverlays().size(); i++) {
                        Overlay overlay = mapView.getOverlays().get(i);
                        if (overlay instanceof Marker && ((Marker) overlay).getTitle().equals("Courier\n" + OrderDetails.orderInfo.get("courier"))) {
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
            String coords = OrderDetails.orderInfo.get("coords");
            double coord1 = Double.parseDouble(coords.split(",")[0]);
            double coord2 = Double.parseDouble(coords.split(",")[1]);
            GeoPoint start = new GeoPoint(coord1, coord2);
            waypoints.add(start);
            coords = OrderDetails.orderInfo.get("coords2");
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
}