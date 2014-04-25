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

import com.afollestad.cardsui.CardBase;
import com.afollestad.cardsui.CardListView;
import com.elliottsj.ftw.R;
import com.elliottsj.ftw.adapters.RouteCardCursorAdapter;
import com.elliottsj.ftw.cards.RouteCard;
import com.elliottsj.ftw.loaders.PredictionsLoader;
import com.elliottsj.ftw.provider.NextbusProvider;

import net.sf.nextbus.publicxmlfeed.domain.PredictionGroup;

import java.util.List;
import java.util.Map;

/**
 *
 */
public class SavedStopsFragment extends Fragment implements CardListView.CardClickListener, CardBase.CardMenuListener<CardBase> {

    @SuppressWarnings("UnusedDeclaration")
    private static final String TAG = SavedStopsFragment.class.getSimpleName();

    private static final int STOPS_LOADER = 0;
    private static final int PREDICTIONS_LOADER = 1;

    private RouteCardCursorAdapter mAdapter;
    private CardListView mCardList;

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setHasOptionsMenu(true);
    }

    @SuppressWarnings("ConstantConditions")
    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
        View rootView = inflater.inflate(R.layout.fragment_stops, container, false);

        mCardList = (CardListView) rootView.findViewById(R.id.card_list);
        mCardList.setOnCardClickListener(this);

        return rootView;
    }

    @SuppressWarnings("ConstantConditions")
    @Override
    public void onActivityCreated(Bundle savedInstanceState) {
        super.onActivityCreated(savedInstanceState);

        mAdapter = new RouteCardCursorAdapter(getActivity());
        mAdapter.setPopupMenu(R.menu.route_card_popup, this);
        mCardList.setAdapter(mAdapter);

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

    @Override
    public void onCardClick(int index, CardBase card, View view) {
//        Toast.makeText(getActivity(), "Card clicked", Toast.LENGTH_SHORT).show();
    }

    @SuppressWarnings("ConstantConditions")
    @Override
    public void onMenuItemClick(CardBase card, MenuItem item) {
        switch (item.getItemId()) {
            case R.id.delete:
                RouteCard routeCard = (RouteCard) card;
                mAdapter.removeCard(routeCard);
                NextbusProvider.deleteSavedStop(getActivity().getContentResolver(),
                                                routeCard.getAgencyTag(),
                                                routeCard.getRouteTag(),
                                                routeCard.getDirectionTag(),
                                                routeCard.getStopTag());
                break;
            default:
                break;
        }
    }

    @SuppressWarnings("ConstantConditions")
    private void loadPredictions() {
        mAdapter.bindPredictions(null);

        Bundle bundle = new Bundle();
        bundle.putString(PredictionsLoaderCallbacks.AGENCY_TAG, mAdapter.getAgencyTag());
        bundle.putSerializable(PredictionsLoaderCallbacks.STOPS_MAP, mAdapter.getStopsMap());
        getLoaderManager().restartLoader(PREDICTIONS_LOADER, bundle, new PredictionsLoaderCallbacks());
    }

    private class SavedStopsLoaderCallbacks implements LoaderManager.LoaderCallbacks<Cursor> {

        @Override
        public Loader<Cursor> onCreateLoader(int id, Bundle args) {
            switch (id) {
                case STOPS_LOADER:
                    return NextbusProvider.savedStopsLoader(getActivity());
                default:
                    return null;
            }
        }

        @SuppressWarnings("ConstantConditions")
        @Override
        public void onLoadFinished(Loader<Cursor> loader, Cursor cursor) {
            mAdapter.swapCursor(cursor);

            // Start loading predictions
            loadPredictions();
        }

        @Override
        public void onLoaderReset(Loader<Cursor> loader) {
            mAdapter.swapCursor(null);
        }

    }

    private class PredictionsLoaderCallbacks implements LoaderManager.LoaderCallbacks<List<PredictionGroup>> {

        public static final String AGENCY_TAG = "agency_tag";
        public static final String STOPS_MAP = "stops_map";

        @SuppressWarnings("unchecked")
        @Override
        public Loader<List<PredictionGroup>> onCreateLoader(int id, Bundle args) {
            String agencyTag = args.getString(AGENCY_TAG);
            Map<String, List<String>> stopsMap = (Map<String, List<String>>) args.getSerializable(STOPS_MAP);

            return new PredictionsLoader(getActivity(), agencyTag, stopsMap);
        }

        @Override
        public void onLoadFinished(Loader<List<PredictionGroup>> loader, List<PredictionGroup> predictions) {
            mAdapter.bindPredictions(predictions);
        }

        @Override
        public void onLoaderReset(Loader<List<PredictionGroup>> loader) {
            mAdapter.bindPredictions(null);
        }

    }

}