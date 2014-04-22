package com.elliottsj.ftw.activities;

import android.app.Fragment;
import android.app.LoaderManager;
import android.content.Intent;
import android.content.Loader;
import android.database.Cursor;
import android.os.Bundle;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.Menu;
import android.view.MenuInflater;
import android.view.MenuItem;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Toast;

import com.afollestad.cardsui.Card;
import com.afollestad.cardsui.CardAdapter;
import com.afollestad.cardsui.CardBase;
import com.afollestad.cardsui.CardListView;
import com.elliottsj.ftw.R;
import com.elliottsj.ftw.adapters.RouteCardCursorAdapter;
import com.elliottsj.ftw.provider.NextbusProvider;

/**
 *
 */
public class SavedStopsFragment extends Fragment implements LoaderManager.LoaderCallbacks<Cursor>,
                                                            CardListView.CardClickListener {

    private static final String TAG = SavedStopsFragment.class.getSimpleName();

    private static final int STOPS_LOADER = 0;

    private CardAdapter<Card> testAdapter;

    private RouteCardCursorAdapter mAdapter;
    private CardListView mCardList;

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        Log.i(TAG, "onCreate");

        setHasOptionsMenu(true);
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
        View rootView = inflater.inflate(R.layout.fragment_stops, container, false);

        Log.i(TAG, "onCreateView");

        //noinspection ConstantConditions
        mCardList = (CardListView) rootView.findViewById(R.id.card_list);
        mCardList.setOnCardClickListener(this);

        return rootView;
    }

    @Override
    public void onActivityCreated(Bundle savedInstanceState) {
        super.onActivityCreated(savedInstanceState);
        Log.i(TAG, "onActivityCreated");

        mAdapter = new RouteCardCursorAdapter(getActivity());
        mCardList.setAdapter(mAdapter);

        getLoaderManager().initLoader(STOPS_LOADER, null, this);
    }

    @Override
    public void onStart() {
        super.onStart();
        Log.i(TAG, "onStart");
    }

    @Override
    public Loader<Cursor> onCreateLoader(int id, Bundle args) {
        switch (id) {
            case STOPS_LOADER:
                return NextbusProvider.savedStopsLoader(getActivity());
            default:
                return null;
        }
    }

    @Override
    public void onLoadFinished(Loader<Cursor> loader, Cursor cursor) {
        mAdapter.swapCursor(cursor);
    }

    @Override
    public void onLoaderReset(Loader<Cursor> loader) {
        mAdapter.swapCursor(null);
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
            default:
                return super.onOptionsItemSelected(item);
        }
    }

    @Override
    public void onCardClick(int index, CardBase card, View view) {
        Toast.makeText(getActivity(), "Card clicked", Toast.LENGTH_SHORT).show();
    }

}