package com.elliottsj.ftw.activities;

import android.app.Activity;
import android.app.Fragment;
import android.app.ListFragment;
import android.app.LoaderManager;
import android.content.ContentResolver;
import android.content.ContentValues;
import android.content.Loader;
import android.database.Cursor;
import android.os.Bundle;
import android.view.View;
import android.widget.ListView;
import android.widget.SimpleCursorAdapter;

import com.elliottsj.ftw.R;
import com.elliottsj.ftw.provider.NextbusProvider;

import net.sf.nextbus.publicxmlfeed.domain.Direction;
import net.sf.nextbus.publicxmlfeed.domain.Route;
import net.sf.nextbus.publicxmlfeed.domain.Stop;

public class AddStopActivity extends Activity {

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_add_stop);

        if (savedInstanceState == null) {
            getFragmentManager().beginTransaction()
                    .add(R.id.container, new SelectRouteFragment())
                    .commit();
        }
    }

    public static class SelectRouteFragment extends ListFragment implements LoaderManager.LoaderCallbacks<Cursor> {

        public static final String ROUTE_TAG = "com.elliottsj.ftw.ROUTE_TAG";

        private SimpleCursorAdapter mAdapter;

        @Override
        public void onCreate(Bundle savedInstanceState) {
            super.onCreate(savedInstanceState);

            mAdapter = new SimpleCursorAdapter(getActivity(),
                                               android.R.layout.simple_list_item_1,
                                               null,
                                               new String[] { Route.FIELD_TITLE },
                                               new int[] { android.R.id.text1 },
                                               0);
            setListAdapter(mAdapter);

            //noinspection ConstantConditions
            getLoaderManager().initLoader(0, null, this);
        }

        @Override
        public Loader<Cursor> onCreateLoader(int id, Bundle args) {
            return NextbusProvider.routesLoader(getActivity());
        }

        @Override
        public void onLoadFinished(Loader<Cursor> loader, Cursor cursor) {
            mAdapter.swapCursor(cursor);
        }

        @Override
        public void onLoaderReset(Loader<Cursor> loader) {
            mAdapter.swapCursor(null);
        }

        @SuppressWarnings("ConstantConditions")
        @Override
        public void onListItemClick(ListView listView, View view, int position, long id) {
            Cursor cursor = (Cursor) mAdapter.getItem(position);
            if (cursor != null) {
                String routeTag = cursor.getString(cursor.getColumnIndexOrThrow(Route.FIELD_TAG));

                // Start SelectDirectionFragment
                Bundle bundle = new Bundle();
                bundle.putString(ROUTE_TAG, routeTag);
                Fragment fragment = new SelectDirectionFragment();
                fragment.setArguments(bundle);
                getFragmentManager().beginTransaction()
                        .replace(R.id.container, fragment)
                        .addToBackStack(null)
                        .commit();
            }
        }
    }

    public static class SelectDirectionFragment extends ListFragment implements LoaderManager.LoaderCallbacks<Cursor> {

        public static final String DIRECTION_TAG = "com.elliottsj.ftw.DIRECTION_TAG";

        private SimpleCursorAdapter mAdapter;
        private String mRouteTag;

        @Override
        public void onCreate(Bundle savedInstanceState) {
            super.onCreate(savedInstanceState);

            mAdapter = new SimpleCursorAdapter(getActivity(),
                                               android.R.layout.simple_list_item_1,
                                               null,
                                               new String[] { Direction.FIELD_TITLE },
                                               new int[] { android.R.id.text1 },
                                               0);
            setListAdapter(mAdapter);

            Bundle bundle = getArguments();
            LoaderManager loaderManager = getLoaderManager();
            if (bundle != null && loaderManager != null) {
                mRouteTag = bundle.getString(SelectRouteFragment.ROUTE_TAG);
                loaderManager.initLoader(0, null, this);
            }
        }

        @Override
        public Loader<Cursor> onCreateLoader(int id, Bundle args) {
            return NextbusProvider.directionsLoader(getActivity(), mRouteTag);
        }

        @Override
        public void onLoadFinished(Loader<Cursor> loader, Cursor cursor) {
            mAdapter.swapCursor(cursor);
        }

        @Override
        public void onLoaderReset(Loader<Cursor> loader) {
            mAdapter.swapCursor(null);
        }

        @SuppressWarnings("ConstantConditions")
        @Override
        public void onListItemClick(ListView listView, View view, int position, long id) {
            Cursor cursor = (Cursor) mAdapter.getItem(position);
            if (cursor != null) {
                String directionTag = cursor.getString(cursor.getColumnIndexOrThrow(Direction.FIELD_TAG));

                // Start SelectStopFragment
                Bundle bundle = new Bundle();
                bundle.putString(SelectRouteFragment.ROUTE_TAG, mRouteTag);
                bundle.putString(DIRECTION_TAG, directionTag);
                Fragment fragment = new SelectStopFragment();
                fragment.setArguments(bundle);
                getFragmentManager().beginTransaction()
                                    .replace(R.id.container, fragment)
                                    .addToBackStack(null)
                                    .commit();
            }
        }

    }

    public static class SelectStopFragment extends ListFragment implements LoaderManager.LoaderCallbacks<Cursor> {

        private SimpleCursorAdapter mAdapter;
        private String mRouteTag;
        private String mDirectionTag;

        @Override
        public void onCreate(Bundle savedInstanceState) {
            super.onCreate(savedInstanceState);

            mAdapter = new SimpleCursorAdapter(getActivity(),
                                               android.R.layout.simple_list_item_1,
                                               null,
                                               new String[] { Stop.FIELD_TITLE },
                                               new int[] { android.R.id.text1 },
                                               0);
            setListAdapter(mAdapter);

            Bundle bundle = getArguments();
            LoaderManager loaderManager = getLoaderManager();
            if (bundle != null && loaderManager != null) {
                mRouteTag = bundle.getString(SelectRouteFragment.ROUTE_TAG);
                mDirectionTag = bundle.getString(SelectDirectionFragment.DIRECTION_TAG);
                loaderManager.initLoader(0, null, this);
            }
        }

        @Override
        public Loader<Cursor> onCreateLoader(int id, Bundle args) {
            return NextbusProvider.stopsLoader(getActivity(), mRouteTag, mDirectionTag);
        }

        @Override
        public void onLoadFinished(Loader<Cursor> loader, Cursor cursor) {
            mAdapter.swapCursor(cursor);
        }

        @Override
        public void onLoaderReset(Loader<Cursor> loader) {
            mAdapter.swapCursor(null);
        }

        @SuppressWarnings("ConstantConditions")
        @Override
        public void onListItemClick(ListView listView, View view, int position, long id) {
            Cursor cursor = (Cursor) mAdapter.getItem(position);
            if (cursor != null) {
                String stopTag = cursor.getString(cursor.getColumnIndexOrThrow(Stop.FIELD_TAG));

                ContentValues values = new ContentValues();
                values.put(NextbusProvider.SAVED_STOPS.COLUMN_AGENCY_TAG, "ttc");
                values.put(NextbusProvider.SAVED_STOPS.COLUMN_ROUTE_TAG, mRouteTag);
                values.put(NextbusProvider.SAVED_STOPS.COLUMN_DIRECTION_TAG, mDirectionTag);
                values.put(NextbusProvider.SAVED_STOPS.COLUMN_STOP_TAG, stopTag);

                ContentResolver resolver = getActivity().getContentResolver();
                resolver.insert(NextbusProvider.savedStopUri(), values);
            }

            getActivity().finish();
        }

    }

}
