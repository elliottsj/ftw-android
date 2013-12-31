package com.elliottsj.ttc_ftw.activities;

import android.app.Activity;
import android.app.Dialog;
import android.app.DialogFragment;
import android.app.Fragment;
import android.content.Intent;
import android.content.IntentSender;
import android.location.Location;
import android.os.AsyncTask;
import android.os.Bundle;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Toast;
import com.afollestad.cardsui.CardBase;
import com.afollestad.cardsui.CardHeader;
import com.afollestad.cardsui.CardListView;
import com.elliottsj.ttc_ftw.R;
import com.elliottsj.ttc_ftw.adapters.RouteCardAdapter;
import com.elliottsj.ttc_ftw.cards.RouteCard;
import com.elliottsj.ttc_ftw.nextbus.CachedNextbusServiceAdapter;
import com.elliottsj.ttc_ftw.nextbus.NextbusNextbusCache;
import com.elliottsj.ttc_ftw.utilities.AndroidRPCImpl;
import com.google.android.gms.common.ConnectionResult;
import com.google.android.gms.common.GooglePlayServicesClient;
import com.google.android.gms.common.GooglePlayServicesUtil;
import com.google.android.gms.location.LocationClient;
import net.sf.nextbus.publicxmlfeed.domain.Agency;
import net.sf.nextbus.publicxmlfeed.domain.Stop;
import net.sf.nextbus.publicxmlfeed.impl.NextbusService;

import java.io.IOException;
import java.util.ArrayList;
import java.util.List;

/**
 *
 */
public class NearbyStopsFragment extends Fragment implements CardHeader.ActionListener,
                                                             CardListView.CardClickListener,
                                                             GooglePlayServicesClient.ConnectionCallbacks,
                                                             GooglePlayServicesClient.OnConnectionFailedListener {

    private static final String TAG = "NearbyStopsFragment";

    /*
     * Define a request code to send to Google Play services
     * This code is returned in Activity.onActivityResult
     */
    private final static int CONNECTION_FAILURE_RESOLUTION_REQUEST = 9000;

    private CardListView mCardList;

    LocationClient mLocationClient;

    // Global variable to hold the current location
    Location mCurrentLocation;

    CachedNextbusServiceAdapter mNextbusService;

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
        View rootView = inflater.inflate(R.layout.fragment_stops, container, false);

        mCardList = (CardListView) rootView.findViewById(R.id.card_list);

        RouteCardAdapter cardAdapter = new RouteCardAdapter(getActivity(), R.layout.route_card);
        cardAdapter.setAccentColorRes(R.color.ttc_red);

        cardAdapter.add(new CardHeader("College St At Beverly St").setAction("Save", this));
        cardAdapter.add(new RouteCard("506 Carlton", "East to Main Street Station", 2));

        cardAdapter.add(new CardHeader("Eglinton Ave E At Redpath Ave").setAction("Save", this));
        cardAdapter.add(new RouteCard("54 Lawrence E", "West to Eglinton Station", 3));
        cardAdapter.add(new RouteCard("103 Mt Pleasant N", "South to Eglinton Station", 1));

        mCardList.setAdapter(cardAdapter);
        mCardList.setOnCardClickListener(this);

        return rootView;
    }

    /*
     * Called when the Activity becomes visible.
     */
    @Override
    public void onStart() {
        super.onStart();
        // Connect the client.
        mLocationClient.connect();
    }

    /*
     * Called when the Activity is no longer visible.
     */
    @Override
    public void onStop() {
        // Disconnecting the client invalidates it.
        mLocationClient.disconnect();
        super.onStop();
    }

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        /*
         * Create a new location client, using the enclosing class to
         * handle callbacks.
         */
        mLocationClient = new LocationClient(getActivity(), this, this);

        NextbusService backing = new NextbusService(new AndroidRPCImpl());
        NextbusNextbusCache cache = null;
        try {
            cache = new NextbusNextbusCache(getActivity());
        } catch (IOException e) {
            e.printStackTrace();
        }
        mNextbusService = new CachedNextbusServiceAdapter(backing, cache);
    }

    @Override
    public void onClick(CardHeader header) {
        Toast.makeText(getActivity(), header.getTitle() + " saved", Toast.LENGTH_SHORT).show();
    }

    @Override
    public void onCardClick(int index, CardBase card, View view) {
        Intent intent = new Intent(getActivity(), MapActivity.class);
        intent.putExtra(MapActivity.ARG_ROUTE, "506 Carlton");
        startActivity(intent);
    }

    /*
     * Called by Location Services when the request to connect the
     * client finishes successfully. At this point, you can
     * request the current location or start periodic updates
     */
    @Override
    public void onConnected(Bundle bundle) {
        // Display the connection status
        Toast.makeText(getActivity(), "Connected", Toast.LENGTH_SHORT).show();

        new LoadNearbyStopsTask().execute(mLocationClient.getLastLocation());
    }

    /*
     * Called by Location Services if the connection to the
     * location client drops because of an error.
     */
    @Override
    public void onDisconnected() {
        // Display the connection status
        Toast.makeText(getActivity(), "Disconnected. Please re-connect.", Toast.LENGTH_SHORT).show();
    }

    /*
     * Called by Location Services if the attempt to connect to Location Services fails.
     */
    @Override
    public void onConnectionFailed(ConnectionResult connectionResult) {
        /*
         * Google Play services can resolve some errors it detects.
         * If the error has a resolution, try sending an Intent to
         * start a Google Play services activity that can resolve
         * error.
         */
        if (connectionResult.hasResolution()) {
            try {
                // Start an Activity that tries to resolve the error
                connectionResult.startResolutionForResult(getActivity(), CONNECTION_FAILURE_RESOLUTION_REQUEST);
            } catch (IntentSender.SendIntentException e) {
                /*
                 * Thrown if Google Play services canceled the original
                 * PendingIntent
                 */
                // Log the error
                e.printStackTrace();
            }
        } else {
            /*
             * If no resolution is available, display a dialog to the
             * user with the error.
             */
            showErrorDialog(connectionResult.getErrorCode());
        }
    }

    private boolean showErrorDialog(int errorCode) {
        // Google Play services was not available for some reason
        // Get the error dialog from Google Play services
        Dialog errorDialog = GooglePlayServicesUtil.getErrorDialog(
                errorCode,
                getActivity(),
                CONNECTION_FAILURE_RESOLUTION_REQUEST);

        if (errorDialog == null) {
            // Successfully connected to Google Play Services
            // In debug mode, log the status
            Log.d("Location Updates", "Google Play services is available.");
            // Continue
            return true;
        } else {
            // If Google Play services can provide an error dialog
            // Create a new DialogFragment for the error dialog
            ErrorDialogFragment errorFragment = new ErrorDialogFragment();
            // Set the dialog in the DialogFragment
            errorFragment.setDialog(errorDialog);
            // Show the error dialog in the DialogFragment
            errorFragment.show(getFragmentManager(), "Location Updates");
            return false;
        }
    }

    /*
     * Handle results returned to the FragmentActivity by Google Play services
     */
    @Override
    public void onActivityResult(int requestCode, int resultCode, Intent data) {
        super.onActivityResult(requestCode, resultCode, data);
        // Decide what to do based on the original request code
        switch (requestCode) {
            case CONNECTION_FAILURE_RESOLUTION_REQUEST:
                /*
                 * If the result code is Activity.RESULT_OK, try
                 * to connect again
                 */
                switch (resultCode) {
                    case Activity.RESULT_OK:
                        /*
                         * Try the request again
                         */
                        break;
                }
        }
    }

    // Define a DialogFragment that displays the error dialog
    public static class ErrorDialogFragment extends DialogFragment {

        // Global field to contain the error dialog
        private Dialog mDialog;

        // Default constructor. Sets the dialog field to null
        public ErrorDialogFragment() {
            super();
            mDialog = null;
        }

        // Set the dialog to display
        public void setDialog(Dialog dialog) {
            mDialog = dialog;
        }

        // Return a Dialog to the DialogFragment.
        @Override
        public Dialog onCreateDialog(Bundle savedInstanceState) {
            return mDialog;
        }
    }

    private class LoadNearbyStopsTask extends AsyncTask<Location, Integer, List<Stop>> {

        @Override
        protected List<Stop> doInBackground(Location... locations) {
            List<Agency> agencies = mNextbusService.getAgencies();

            return new ArrayList<Stop>();

//            List<Stop> allStops = new ArrayList<Stop>();
//            Geolocation here = new Geolocation(locations[0].getLatitude(), locations[0].getLongitude());
//
//            Agency ttc = mNextbusService.getAgency("ttc");
//            for (Route route : mNextbusService.getRoutes(ttc).subList(0, 1)) {
//                allStops.addAll(mNextbusService.getRouteConfiguration(route).getStops());
//                HttpResponseCache httpResponseCache = HttpResponseCache.getInstalled();
//                Log.i(TAG, "Network count: " + httpResponseCache.getNetworkCount());
//                Log.i(TAG, "Hit count: " + httpResponseCache.getHitCount());
//            }
//
//            return Geolocation.sortedByClosest(allStops, here, 10, 1);
        }

        @Override
        protected void onPostExecute(List<Stop> stops) {

        }

    }

}