package com.elliottsj.ftw.activities;

import android.app.Fragment;
import android.app.LoaderManager;
import android.content.Intent;
import android.content.Loader;
import android.database.Cursor;
import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.Menu;
import android.view.MenuInflater;
import android.view.MenuItem;
import android.view.View;
import android.view.ViewGroup;

import com.elliottsj.ftw.R;
import com.elliottsj.ftw.loaders.PredictionsLoader;
import com.elliottsj.ftw.provider.NextbusProvider;
import com.elliottsj.nextbus.domain.Prediction;

import java.util.List;
import java.util.Map;

/**
 * Displays saved stops in a card list view
 */
public class SavedStopListFragment extends Fragment {

    private static final String TAG = SavedStopListFragment.class.getSimpleName();

    private static final int STOPS_LOADER = 0;
    private static final int PREDICTIONS_LOADER = 1;

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setHasOptionsMenu(true);
    }

    @SuppressWarnings("ConstantConditions")
    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
        View rootView = inflater.inflate(R.layout.fragment_stops, container, false);

        return rootView;
    }

    @SuppressWarnings("ConstantConditions")
    @Override
    public void onActivityCreated(Bundle savedInstanceState) {
        super.onActivityCreated(savedInstanceState);

        getLoaderManager().initLoader(STOPS_LOADER, null, new SavedStopsLoaderCallbacks());
    }

    @Override
    public void onCreateOptionsMenu(Menu menu, MenuInflater inflater) {
        inflater.inflate(R.menu.saved_stops, menu);
        super.onCreateOptionsMenu(menu, inflater);
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        switch (item.getItemId()) {
            case R.id.add_stop:
                Intent intent = new Intent(getActivity(), AddStopActivity.class);
                startActivity(intent);
                return true;
            case R.id.refresh:
                loadPredictions();
                return true;
            default:
                return super.onOptionsItemSelected(item);
        }
    }

    private void loadPredictions() {
//        if (mAdapter.getCount() > 0) {
//            if (!Util.isNetworkConnected(getActivity())) {
//                Toast.makeText(getActivity(), "Not connected to the internet", Toast.LENGTH_SHORT).show();
//                return;
//            }
//
//            // Clear predictions in the adapter
//            mAdapter.bindPredictions(null);
//
//            // Load predictions using a PredictionsLoader
//            Bundle bundle = new Bundle();
//            bundle.putString(PredictionsLoaderCallbacks.AGENCY_TAG, mAdapter.getAgencyTag());
//            bundle.putSerializable(PredictionsLoaderCallbacks.STOPS_MAP, mAdapter.getStopsMap());
//            getLoaderManager().restartLoader(PREDICTIONS_LOADER, bundle, new PredictionsLoaderCallbacks());
//        }
    }

    private class SavedStopsLoaderCallbacks implements LoaderManager.LoaderCallbacks<Cursor> {

        @Override
        public Loader<Cursor> onCreateLoader(int id, Bundle args) {
            return NextbusProvider.savedStopsLoader(getActivity());
        }

        @SuppressWarnings("ConstantConditions")
        @Override
        public void onLoadFinished(Loader<Cursor> loader, Cursor cursor) {
//            mAdapter.swapCursor(cursor);

            // Start loading predictions
            loadPredictions();
        }

        @Override
        public void onLoaderReset(Loader<Cursor> loader) {
//            mAdapter.swapCursor(null);
        }

    }

    private class PredictionsLoaderCallbacks implements LoaderManager.LoaderCallbacks<Map<String, Map<String, List<Prediction>>>> {

        public static final String AGENCY_TAG = "agency_tag";
        public static final String STOPS_MAP = "stops_map";

        @SuppressWarnings("unchecked")
        @Override
        public Loader<Map<String, Map<String, List<Prediction>>>> onCreateLoader(int id, Bundle args) {
            String agencyTag = args.getString(AGENCY_TAG);
            Map<String, List<String>> stopsMap = (Map<String, List<String>>) args.getSerializable(STOPS_MAP);

            return new PredictionsLoader(getActivity(), agencyTag, stopsMap);
        }

        @Override
        public void onLoadFinished(Loader<Map<String, Map<String, List<Prediction>>>> loader, Map<String, Map<String, List<Prediction>>> predictions) {
//            mAdapter.bindPredictions(predictions);
        }

        @Override
        public void onLoaderReset(Loader<Map<String, Map<String, List<Prediction>>>> loader) {
//            mAdapter.bindPredictions(null);
        }

    }

}