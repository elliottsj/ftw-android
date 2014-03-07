package com.elliottsj.ftw.provider;

import android.content.ContentProvider;
import android.content.ContentValues;
import android.content.UriMatcher;
import android.database.Cursor;
import android.database.sqlite.SQLiteDatabase;
import android.net.Uri;

import com.elliottsj.ftw.nextbus.cache.helpers.AgenciesHelper;
import com.elliottsj.ftw.nextbus.cache.helpers.NextbusSQLiteHelper;
import com.elliottsj.ftw.nextbus.cache.helpers.RouteConfigurationsHelper;
import com.elliottsj.ftw.nextbus.cache.helpers.RoutesHelper;

public class NextbusProvider extends ContentProvider {

    public static final String AUTHORITY = "com.elliottsj.ftw.provider";

    private static final int AGENCIES = 1;
    private static final int AGENCIES_ID = 2;
    private static final int AGENCIES_TAG = 3;

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

    private AgenciesHelper mAgenciesHelper;
    private RoutesHelper mRoutesHelper;
    private RouteConfigurationsHelper mRouteConfigurationsHelper;

    private static final UriMatcher sUriMatcher = new UriMatcher(UriMatcher.NO_MATCH);

    static {
        sUriMatcher.addURI(AUTHORITY, "agencies", AGENCIES);
        sUriMatcher.addURI(AUTHORITY, "agencies/#", AGENCIES_ID);
        sUriMatcher.addURI(AUTHORITY, "agencies/*", AGENCIES_TAG);
//        sUriMatcher.addURI(AUTHORITY, "agencies/#/routes", AGENCIES_ROUTES);
//        sUriMatcher.addURI(AUTHORITY, "agencies/#/routes/#", AGENCIES_ROUTES_ID);
//        sUriMatcher.addURI(AUTHORITY, "agencies/#/routes/*", AGENCIES_ROUTES_TAG);
//        sUriMatcher.addURI(AUTHORITY, "agencies/#/routes/#/stops", AGENCIES_ROUTES_STOPS);
//        sUriMatcher.addURI(AUTHORITY, "agencies/#/routes/#/stops/#", AGENCIES_ROUTES_STOPS_ID);
//        sUriMatcher.addURI(AUTHORITY, "agencies/#/routes/#/directions", AGENCIES_ROUTES_STOPS_ID);
//        sUriMatcher.addURI(AUTHORITY, "agencies/#/routes/#/directions/#", AGENCIES_ROUTES_STOPS_ID);
//        sUriMatcher.addURI(AUTHORITY, "agencies/#/routes/#/directions/#/stops", AGENCIES_ROUTES_STOPS_ID);
//        sUriMatcher.addURI(AUTHORITY, "agencies/#/routes/#/directions/#/stops/#", AGENCIES_ROUTES_STOPS_ID);
    }

    @Override
    public boolean onCreate() {
        mDbHelper = new NextbusSQLiteHelper(getContext());
        mDatabase = mDbHelper.getWritableDatabase();

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
            case AGENCIES:
                return mDatabase.query(NextbusSQLiteHelper.AGENCIES.TABLE,
                                       AGENCIES_COLUMNS, null, null, null, null, null);
            default:
                return null;
        }
    }

    @Override
    public String getType(Uri uri) {
        return null;
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

}
