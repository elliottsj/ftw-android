package com.elliottsj.ftw.provider;

import android.content.ContentProvider;
import android.content.ContentResolver;
import android.content.ContentValues;
import android.content.Context;
import android.content.CursorLoader;
import android.content.Loader;
import android.content.UriMatcher;
import android.database.Cursor;
import android.database.sqlite.SQLiteDatabase;
import android.net.Uri;

import com.elliottsj.ftw.nextbus.data_store.helpers.AgenciesHelper;
import com.elliottsj.ftw.nextbus.data_store.helpers.NextbusSQLiteHelper;
import com.elliottsj.ftw.nextbus.data_store.helpers.RouteConfigurationsHelper;
import com.elliottsj.ftw.nextbus.data_store.helpers.RoutesHelper;
import com.elliottsj.ftw.nextbus.data_store.helpers.SavedStopsHelper;

public class NextbusProvider extends ContentProvider {

    public static final String AUTHORITY = "com.elliottsj.ftw.provider";
    public static final Uri CONTENT_URI = Uri.parse("content://" + AUTHORITY);

    public static final class SAVED_STOPS_CURSOR {
        public static final String COLUMN_AGENCY_TAG = "agency_tag";
        public static final String COLUMN_STOP_TAG = "stop_tag";
        public static final String COLUMN_STOP_TITLE = "stop_title";
        public static final String COLUMN_ROUTE_TITLE = "route_title";
        public static final String COLUMN_ROUTE_SHORT_TITLE = "route_short_title";
        public static final String COLUMN_DIRECTION = "direction";
    }

    public static final String[] SAVED_STOPS_CURSOR_COLUMNS =
            { SAVED_STOPS_CURSOR.COLUMN_AGENCY_TAG,
              SAVED_STOPS_CURSOR.COLUMN_STOP_TAG,
              SAVED_STOPS_CURSOR.COLUMN_STOP_TITLE,
              SAVED_STOPS_CURSOR.COLUMN_ROUTE_TITLE,
              SAVED_STOPS_CURSOR.COLUMN_ROUTE_SHORT_TITLE,
              SAVED_STOPS_CURSOR.COLUMN_DIRECTION };

    private static final int SAVED_STOPS = 1;
    private static final int AGENCIES = 2;
    private static final int AGENCIES_TAG = 3;
    private static final int AGENCIES_ROUTES = 4;
    private static final int AGENCIES_ROUTES_TAG = 5;
    private static final int AGENCIES_ROUTES_SERVICE_AREA = 6;
    private static final int AGENCIES_ROUTES_PATHS = 7;
    private static final int AGENCIES_ROUTES_PATHS_ID = 8;
    private static final int AGENCIES_ROUTES_VEHICLE_LOCATIONS = 9;
    private static final int AGENCIES_ROUTES_STOPS = 10;
    private static final int AGENCIES_ROUTES_STOPS_TAG = 11;
    private static final int AGENCIES_ROUTES_DIRECTIONS = 12;
    private static final int AGENCIES_ROUTES_DIRECTIONS_TAG = 13;
    private static final int AGENCIES_ROUTES_DIRECTIONS_STOPS = 14;
    private static final int AGENCIES_ROUTES_DIRECTIONS_STOPS_TAG = 15;

    private static final String[] AGENCIES_COLUMNS =
            { NextbusSQLiteHelper.AGENCIES.COLUMN_AUTO_ID,
              NextbusSQLiteHelper.AGENCIES.COLUMN_COPYRIGHT,
              NextbusSQLiteHelper.AGENCIES.COLUMN_TIMESTAMP,
              NextbusSQLiteHelper.AGENCIES.COLUMN_TAG,
              NextbusSQLiteHelper.AGENCIES.COLUMN_TITLE,
              NextbusSQLiteHelper.AGENCIES.COLUMN_SHORT_TITLE,
              NextbusSQLiteHelper.AGENCIES.COLUMN_REGION_TITLE };

    private NextbusSQLiteHelper mDbHelper;
    private SQLiteDatabase mDatabase;

    private SavedStopsHelper mSavedStopsHelper;
    private AgenciesHelper mAgenciesHelper;
    private RoutesHelper mRoutesHelper;
    private RouteConfigurationsHelper mRouteConfigurationsHelper;

    private static final UriMatcher sUriMatcher = new UriMatcher(UriMatcher.NO_MATCH);

    static {
        sUriMatcher.addURI(AUTHORITY, "saved-stops", SAVED_STOPS);
        sUriMatcher.addURI(AUTHORITY, "agencies", AGENCIES);
        sUriMatcher.addURI(AUTHORITY, "agencies/*", AGENCIES_TAG);
        sUriMatcher.addURI(AUTHORITY, "agencies/*/routes", AGENCIES_ROUTES);
        sUriMatcher.addURI(AUTHORITY, "agencies/*/routes/*", AGENCIES_ROUTES_TAG);
        sUriMatcher.addURI(AUTHORITY, "agencies/*/routes/*/service-area", AGENCIES_ROUTES_SERVICE_AREA);
        sUriMatcher.addURI(AUTHORITY, "agencies/*/routes/*/paths", AGENCIES_ROUTES_PATHS);
        sUriMatcher.addURI(AUTHORITY, "agencies/*/routes/*/paths/#", AGENCIES_ROUTES_PATHS_ID);
        sUriMatcher.addURI(AUTHORITY, "agencies/*/routes/*/vehicle-locations", AGENCIES_ROUTES_VEHICLE_LOCATIONS);
        sUriMatcher.addURI(AUTHORITY, "agencies/*/routes/*/stops", AGENCIES_ROUTES_STOPS);
        sUriMatcher.addURI(AUTHORITY, "agencies/*/routes/*/stops/*", AGENCIES_ROUTES_STOPS_TAG);
        sUriMatcher.addURI(AUTHORITY, "agencies/*/routes/*/directions", AGENCIES_ROUTES_DIRECTIONS);
        sUriMatcher.addURI(AUTHORITY, "agencies/*/routes/*/directions/*", AGENCIES_ROUTES_DIRECTIONS_TAG);
        sUriMatcher.addURI(AUTHORITY, "agencies/*/routes/*/directions/*/stops", AGENCIES_ROUTES_DIRECTIONS_STOPS);
        sUriMatcher.addURI(AUTHORITY, "agencies/*/routes/*/directions/*/stops/*", AGENCIES_ROUTES_DIRECTIONS_STOPS_TAG);
    }

    @Override
    public boolean onCreate() {
        mDbHelper = new NextbusSQLiteHelper(getContext());
        mDatabase = mDbHelper.getWritableDatabase();

        mSavedStopsHelper = new SavedStopsHelper(mDatabase);
        mAgenciesHelper = new AgenciesHelper(mDatabase);
        mRoutesHelper = new RoutesHelper(mDatabase);
        mRouteConfigurationsHelper = new RouteConfigurationsHelper(mDatabase);

        return true;
    }

    @Override
    public Cursor query(Uri uri, String[] projection, String selection, String[] selectionArgs, String sortOrder) {
        // Helper classes: UriMatcher, ContentUris, Uri, Uri.Builder
        int match = sUriMatcher.match(uri);
        switch (match) {
            case SAVED_STOPS:
                // e.g. content://com.elliottsj.ftw.provider/saved-stops

                return null;
            case AGENCIES:
                // e.g. content://com.elliottsj.ftw.provider/agencies
                return mDatabase.query(NextbusSQLiteHelper.AGENCIES.TABLE,
                                       AGENCIES_COLUMNS, null, null, null, null, null);
            case AGENCIES_TAG:
                // e.g. content://com.elliottsj.ftw.provider/agencies/ttc
                return null;
            case AGENCIES_ROUTES:
                // e.g. content://com.elliottsj.ftw.provider/agencies/ttc/routes
                return null;
            case AGENCIES_ROUTES_TAG:
                // e.g. content://com.elliottsj.ftw.provider/agencies/ttc/routes/506
                return null;
            case AGENCIES_ROUTES_SERVICE_AREA:
                // e.g. content://com.elliottsj.ftw.provider/agencies/ttc/routes/506/stops/5292
                return null;
            case AGENCIES_ROUTES_PATHS:
                // e.g. content://com.elliottsj.ftw.provider/agencies/ttc/routes/506/paths
                return null;
            case AGENCIES_ROUTES_PATHS_ID:
                // e.g. content://com.elliottsj.ftw.provider/agencies/ttc/routes/506/paths/56
                return null;
            case AGENCIES_ROUTES_VEHICLE_LOCATIONS:
                // e.g. content://com.elliottsj.ftw.provider/agencies/ttc/routes/506/vehicle-locations
                return null;
            case AGENCIES_ROUTES_STOPS:
                // e.g. content://com.elliottsj.ftw.provider/agencies/ttc/routes/506/stops
                return null;
            case AGENCIES_ROUTES_STOPS_TAG:
                // e.g. content://com.elliottsj.ftw.provider/agencies/ttc/routes/506/stops/5292
                return null;
            case AGENCIES_ROUTES_DIRECTIONS:
                // e.g. content://com.elliottsj.ftw.provider/agencies/ttc/routes/506/directions
                return null;
            case AGENCIES_ROUTES_DIRECTIONS_TAG:
                // e.g. content://com.elliottsj.ftw.provider/agencies/ttc/routes/506/directions/506_1_506Sun
                return null;
            case AGENCIES_ROUTES_DIRECTIONS_STOPS:
                // e.g. content://com.elliottsj.ftw.provider/agencies/ttc/routes/506/directions/506_1_506Sun/stops
                return null;
            case AGENCIES_ROUTES_DIRECTIONS_STOPS_TAG:
                // e.g. content://com.elliottsj.ftw.provider/agencies/ttc/routes/506/directions/506_1_506Sun/stops
                return null;
            default:
                return null;
        }
    }

    @Override
    public String getType(Uri uri) {
        int match = sUriMatcher.match(uri);
        switch (match) {
            case SAVED_STOPS:
                // e.g. content://com.elliottsj.ftw.provider/saved-stops
                return ContentResolver.CURSOR_DIR_BASE_TYPE + "/vnd.elliottsj.stop";
            case AGENCIES:
                // e.g. content://com.elliottsj.ftw.provider/agencies
                return ContentResolver.CURSOR_DIR_BASE_TYPE + "/vnd.elliottsj.agency";
            case AGENCIES_TAG:
                // e.g. content://com.elliottsj.ftw.provider/agencies/ttc
                return ContentResolver.CURSOR_ITEM_BASE_TYPE + "/vnd.elliottsj.agency";
            case AGENCIES_ROUTES:
                // e.g. content://com.elliottsj.ftw.provider/agencies/ttc/routes
                return ContentResolver.CURSOR_DIR_BASE_TYPE + "/vnd.elliottsj.route";
            case AGENCIES_ROUTES_TAG:
                // e.g. content://com.elliottsj.ftw.provider/agencies/ttc/routes/506
                return ContentResolver.CURSOR_ITEM_BASE_TYPE + "/vnd.elliottsj.route";
            case AGENCIES_ROUTES_SERVICE_AREA:
                // e.g. content://com.elliottsj.ftw.provider/agencies/ttc/routes/506/stops/5292
                return ContentResolver.CURSOR_ITEM_BASE_TYPE + "/vnd.elliottsj.service-area";
            case AGENCIES_ROUTES_PATHS:
                // e.g. content://com.elliottsj.ftw.provider/agencies/ttc/routes/506/paths
                return ContentResolver.CURSOR_DIR_BASE_TYPE + "/vnd.elliottsj.path";
            case AGENCIES_ROUTES_PATHS_ID:
                // e.g. content://com.elliottsj.ftw.provider/agencies/ttc/routes/506/paths/56
                return ContentResolver.CURSOR_ITEM_BASE_TYPE + "/vnd.elliottsj.path";
            case AGENCIES_ROUTES_VEHICLE_LOCATIONS:
                // e.g. content://com.elliottsj.ftw.provider/agencies/ttc/routes/506/vehicle-locations
                return ContentResolver.CURSOR_DIR_BASE_TYPE + "/vnd.elliottsj.vehicle-location";
            case AGENCIES_ROUTES_STOPS:
                // e.g. content://com.elliottsj.ftw.provider/agencies/ttc/routes/506/stops
                return ContentResolver.CURSOR_DIR_BASE_TYPE + "/vnd.elliottsj.stop";
            case AGENCIES_ROUTES_STOPS_TAG:
                // e.g. content://com.elliottsj.ftw.provider/agencies/ttc/routes/506/stops/5292
                return ContentResolver.CURSOR_ITEM_BASE_TYPE + "/vnd.elliottsj.stop";
            case AGENCIES_ROUTES_DIRECTIONS:
                // e.g. content://com.elliottsj.ftw.provider/agencies/ttc/routes/506/directions
                return ContentResolver.CURSOR_DIR_BASE_TYPE + "/vnd.elliottsj.direction";
            case AGENCIES_ROUTES_DIRECTIONS_TAG:
                // e.g. content://com.elliottsj.ftw.provider/agencies/ttc/routes/506/directions/506_1_506Sun
                return ContentResolver.CURSOR_ITEM_BASE_TYPE + "/vnd.elliottsj.direction";
            case AGENCIES_ROUTES_DIRECTIONS_STOPS:
                // e.g. content://com.elliottsj.ftw.provider/agencies/ttc/routes/506/directions/506_1_506Sun/stops
                return ContentResolver.CURSOR_DIR_BASE_TYPE + "/vnd.elliottsj.stop";
            case AGENCIES_ROUTES_DIRECTIONS_STOPS_TAG:
                // e.g. content://com.elliottsj.ftw.provider/agencies/ttc/routes/506/directions/506_1_506Sun/stops
                return ContentResolver.CURSOR_ITEM_BASE_TYPE + "/vnd.elliottsj.stop";
            default:
                return null;
        }
    }

    @Override
    public Uri insert(Uri uri, ContentValues values) {
        return null;
    }

    @Override
    public int delete(Uri uri, String selection, String[] selectionArgs) {
        return 0;
    }

    @Override
    public int update(Uri uri, ContentValues values, String selection, String[] selectionArgs) {
        return 0;
    }

    /**
     * Get a cursor loader which loads the saved stops from the content provider.
     *
     * @param context context for the cursor loader
     * @return a cursor loader
     */
    public static Loader<Cursor> savedStopsLoader(Context context) {
        return new CursorLoader(context, CONTENT_URI,
                                SAVED_STOPS_CURSOR_COLUMNS, null, null,
                                SAVED_STOPS_CURSOR.COLUMN_STOP_TITLE);
    }

}
