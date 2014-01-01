package com.elliottsj.ttc_ftw.activities;

import android.app.Activity;
import android.content.Intent;
import android.os.Bundle;
import android.view.MenuItem;
import com.elliottsj.ttc_ftw.R;
import com.google.android.gms.maps.GoogleMap;
import com.google.android.gms.maps.MapFragment;
import com.google.android.gms.maps.model.LatLng;
import com.google.android.gms.maps.model.MarkerOptions;

public class MapActivity extends Activity {

    public static final String ARG_ROUTE = "route";

    private GoogleMap mMap;

    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_map);

        Intent intent = getIntent();
        if (intent.hasExtra(ARG_ROUTE)) {
            setTitle(intent.getStringExtra(ARG_ROUTE));
        }

        mMap = ((MapFragment) getFragmentManager().findFragmentById(R.id.map)).getMap();

        mMap.addMarker(new MarkerOptions().position(new LatLng(43.661484, -79.382248)));
        mMap.addMarker(new MarkerOptions().position(new LatLng(43.675751, -79.319649)));
        mMap.addMarker(new MarkerOptions().position(new LatLng(43.671268, -79.326553)));
        mMap.addMarker(new MarkerOptions().position(new LatLng(43.655251, -79.418831)));
        mMap.addMarker(new MarkerOptions().position(new LatLng(43.663567, -79.36763)));
        mMap.addMarker(new MarkerOptions().position(new LatLng(43.658699, -79.3963009)));
        mMap.addMarker(new MarkerOptions().position(new LatLng(43.687817, -79.301613)));
        mMap.addMarker(new MarkerOptions().position(new LatLng(43.661366, -79.38343)));
        mMap.addMarker(new MarkerOptions().position(new LatLng(43.648468, -79.457863)));
        mMap.addMarker(new MarkerOptions().position(new LatLng(43.656601, -79.407303)));

//        LatLngBounds bounds = new LatLngBounds(new LatLng(43.648468, -79.457863), new LatLng(43.687817, -79.301613));
//        mMap.moveCamera(CameraUpdateFactory.newLatLngBounds(bounds, 0));
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        switch (item.getItemId()) {
            case android.R.id.home:
                finish();
                return true;
        }
        return super.onOptionsItemSelected(item);
    }

}