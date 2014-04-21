package com.elliottsj.ftw.provider;

import android.content.ContentProvider;
import android.content.ContentResolver;
import android.content.ContentValues;
import android.content.Context;
import android.content.CursorLoader;
import android.content.Loader;
import android.content.UriMatcher;
import android.database.Cursor;
import android.database.MatrixCursor;
import android.database.sqlite.SQLiteDatabase;
import android.net.Uri;
import android.util.Log;

import com.elliottsj.ftw.utilities.AndroidNextbusService;
import com.j256.ormlite.android.apptools.OpenHelperManager;
import com.j256.ormlite.dao.Dao;

import net.sf.nextbus.publicxmlfeed.domain.Agency;
import net.sf.nextbus.publicxmlfeed.domain.Direction;
import net.sf.nextbus.publicxmlfeed.domain.Route;
import net.sf.nextbus.publicxmlfeed.domain.Stop;
import net.sf.nextbus.publicxmlfeed.impl.NextbusService;

import java.sql.SQLException;
import java.util.Collection;
import java.util.List;

public class NextbusProvider extends ContentProvider {

    private static final String TAG = NextbusProvider.class.getSimpleName();

    public static final String AUTHORITY = "com.elliottsj.ftw.provider";
    public static final Uri CONTENT_URI = Uri.parse("content://" + AUTHORITY);

    public static final class SAVED_STOPS {
        public static final String COLUMN_AGENCY_TAG = "agency_tag";
        public static final String COLUMN_STOP_TAG = "stop_tag";
        public static final String COLUMN_STOP_TITLE = "stop_title";
        public static final String COLUMN_ROUTE_TITLE = "route_title";
        public static final String COLUMN_ROUTE_SHORT_TITLE = "route_short_title";
        public static final String COLUMN_DIRECTION = "direction";
    }

    public static final String[] SAVED_STOPS_CURSOR_COLUMNS =
            { SAVED_STOPS.COLUMN_AGENCY_TAG,
              SAVED_STOPS.COLUMN_STOP_TAG,
              SAVED_STOPS.COLUMN_STOP_TITLE,
              SAVED_STOPS.COLUMN_ROUTE_TITLE,
              SAVED_STOPS.COLUMN_ROUTE_SHORT_TITLE,
              SAVED_STOPS.COLUMN_DIRECTION };

    public static final String[] AGENCIES_CURSOR_COLUMNS =
            { Agency.FIELD_ID,
              Agency.FIELD_TAG,
              Agency.FIELD_TITLE,
              Agency.FIELD_SHORT_TITLE,
              Agency.FIELD_REGION_TITLE,
              Agency.FIELD_COPYRIGHT,
              Agency.FIELD_TIMESTAMP };

    public static final String[] ROUTES_CURSOR_COLUMNS =
            { Route.FIELD_ID,
              Route.FIELD_TAG,
              Route.FIELD_TITLE,
              Route.FIELD_SHORT_TITLE };

    public static final String[] DIRECTIONS_CURSOR_COLUMNS =
            { Direction.FIELD_ID,
              Direction.FIELD_TAG,
              Direction.FIELD_TITLE,
              Direction.FIELD_NAME };

    public static final String[] STOPS_CURSOR_COLUMNS =
            { Stop.FIELD_ID,
              Stop.FIELD_TAG,
              Stop.FIELD_TITLE,
              Stop.FIELD_SHORT_TITLE };

    private static class URI_CODE {
        public static final int SAVED_STOPS = 1;
        public static final int AGENCIES = 2;
        public static final int AGENCIES_TAG = 3;
        public static final int AGENCIES_ROUTES = 4;
        public static final int AGENCIES_ROUTES_TAG = 5;
        public static final int AGENCIES_ROUTES_SERVICE_AREA = 6;
        public static final int AGENCIES_ROUTES_PATHS = 7;
        public static final int AGENCIES_ROUTES_PATHS_ID = 8;
        public static final int AGENCIES_ROUTES_VEHICLE_LOCATIONS = 9;
        public static final int AGENCIES_ROUTES_STOPS = 10;
        public static final int AGENCIES_ROUTES_STOPS_TAG = 11;
        public static final int AGENCIES_ROUTES_DIRECTIONS = 12;
        public static final int AGENCIES_ROUTES_DIRECTIONS_TAG = 13;
        public static final int AGENCIES_ROUTES_DIRECTIONS_STOPS = 14;
        public static final int AGENCIES_ROUTES_DIRECTIONS_STOPS_TAG = 15;
    }

    private NextbusService mNextbusService;

    private NextbusSQLiteHelper mDbHelper;
    private SQLiteDatabase mDatabase;

    private static final UriMatcher sUriMatcher = new UriMatcher(UriMatcher.NO_MATCH);

    static {
        sUriMatcher.addURI(AUTHORITY, "saved-stops", URI_CODE.SAVED_STOPS);
        sUriMatcher.addURI(AUTHORITY, "agencies", URI_CODE.AGENCIES);
        sUriMatcher.addURI(AUTHORITY, "agencies/*", URI_CODE.AGENCIES_TAG);
        sUriMatcher.addURI(AUTHORITY, "agencies/*/routes", URI_CODE.AGENCIES_ROUTES);
        sUriMatcher.addURI(AUTHORITY, "agencies/*/routes/*", URI_CODE.AGENCIES_ROUTES_TAG);
        sUriMatcher.addURI(AUTHORITY, "agencies/*/routes/*/service-area", URI_CODE.AGENCIES_ROUTES_SERVICE_AREA);
        sUriMatcher.addURI(AUTHORITY, "agencies/*/routes/*/paths", URI_CODE.AGENCIES_ROUTES_PATHS);
        sUriMatcher.addURI(AUTHORITY, "agencies/*/routes/*/paths/#", URI_CODE.AGENCIES_ROUTES_PATHS_ID);
        sUriMatcher.addURI(AUTHORITY, "agencies/*/routes/*/vehicle-locations", URI_CODE.AGENCIES_ROUTES_VEHICLE_LOCATIONS);
        sUriMatcher.addURI(AUTHORITY, "agencies/*/routes/*/stops", URI_CODE.AGENCIES_ROUTES_STOPS);
        sUriMatcher.addURI(AUTHORITY, "agencies/*/routes/*/stops/*", URI_CODE.AGENCIES_ROUTES_STOPS_TAG);
        sUriMatcher.addURI(AUTHORITY, "agencies/*/routes/*/directions", URI_CODE.AGENCIES_ROUTES_DIRECTIONS);
        sUriMatcher.addURI(AUTHORITY, "agencies/*/routes/*/directions/*", URI_CODE.AGENCIES_ROUTES_DIRECTIONS_TAG);
        sUriMatcher.addURI(AUTHORITY, "agencies/*/routes/*/directions/*/stops", URI_CODE.AGENCIES_ROUTES_DIRECTIONS_STOPS);
        sUriMatcher.addURI(AUTHORITY, "agencies/*/routes/*/directions/*/stops/*", URI_CODE.AGENCIES_ROUTES_DIRECTIONS_STOPS_TAG);
    }

    @Override
    public boolean onCreate() {
        mNextbusService = new AndroidNextbusService();
        return true;
    }

    @Override
    public Cursor query(Uri uri, String[] projection, String selection, String[] selectionArgs, String sortOrder) {
        // Helper classes: UriMatcher, ContentUris, Uri, Uri.Builder
        Log.i(TAG, "Called NextbusProvider.query() with Uri: " + uri.toString());
        Dao dao;
        Cursor cursor = null;
        List<String> pathSegments;
        int match = sUriMatcher.match(uri);
        switch (match) {
            case URI_CODE.SAVED_STOPS:
                // e.g. content://com.elliottsj.ftw.provider/saved-stops
                break;
            case URI_CODE.AGENCIES:
                // e.g. content://com.elliottsj.ftw.provider/agencies
                try {
                    dao = getHelper().getAgencyDao();
                    cursor = OrmUtil.cursorFromDao(dao, projection, selection, selectionArgs, sortOrder);
                } catch (SQLException e) {
                    throw new RuntimeException(e);
                }
                break;
            case URI_CODE.AGENCIES_TAG:
                // e.g. content://com.elliottsj.ftw.provider/agencies/ttc
                break;
            case URI_CODE.AGENCIES_ROUTES:
                // e.g. content://com.elliottsj.ftw.provider/agencies/ttc/routes
                //noinspection ConstantConditions
                pathSegments = uri.getPathSegments();
                if (pathSegments != null) {
                    String agencyTag = pathSegments.get(1);

                    // Get routes from the network and put them into a MatrixCursor
                    MatrixCursor matrixCursor = new MatrixCursor(ROUTES_CURSOR_COLUMNS);
                    int _id = 0;
                    for (Route route : mNextbusService.getRoutes(mNextbusService.getAgency(agencyTag))) {
                        String tag = route.getTag();
                        String title = route.getTitle();
                        String shortTitle = route.getShortTitle();
                        matrixCursor.addRow(new Object[] { _id++, tag, title, shortTitle });
                    }
                    cursor = matrixCursor;

                    // TODO: store in database
                }
                break;
            case URI_CODE.AGENCIES_ROUTES_TAG:
                // e.g. content://com.elliottsj.ftw.provider/agencies/ttc/routes/506
                break;
            case URI_CODE.AGENCIES_ROUTES_SERVICE_AREA:
                // e.g. content://com.elliottsj.ftw.provider/agencies/ttc/routes/506/stops/5292
                break;
            case URI_CODE.AGENCIES_ROUTES_PATHS:
                // e.g. content://com.elliottsj.ftw.provider/agencies/ttc/routes/506/paths
                break;
            case URI_CODE.AGENCIES_ROUTES_PATHS_ID:
                // e.g. content://com.elliottsj.ftw.provider/agencies/ttc/routes/506/paths/56
                break;
            case URI_CODE.AGENCIES_ROUTES_VEHICLE_LOCATIONS:
                // e.g. content://com.elliottsj.ftw.provider/agencies/ttc/routes/506/vehicle-locations
                break;
            case URI_CODE.AGENCIES_ROUTES_STOPS:
                // e.g. content://com.elliottsj.ftw.provider/agencies/ttc/routes/506/stops
                break;
            case URI_CODE.AGENCIES_ROUTES_STOPS_TAG:
                // e.g. content://com.elliottsj.ftw.provider/agencies/ttc/routes/506/stops/5292
                break;
            case URI_CODE.AGENCIES_ROUTES_DIRECTIONS:
                // e.g. content://com.elliottsj.ftw.provider/agencies/ttc/routes/506/directions
                pathSegments = uri.getPathSegments();
                if (pathSegments != null) {
                    String agencyTag = pathSegments.get(1);
                    String routeTag = pathSegments.get(3);

                    List<Direction> directions = null;

                    // Get routes from the network and find the one matching the parameter
                    for (Route route : mNextbusService.getRoutes(mNextbusService.getAgency(agencyTag)))
                        if (route.getTag().equals(routeTag))
                            directions = mNextbusService.getRouteConfiguration(route).getDirections();

                    // Put directions into a MatrixCursor
                    if (directions != null) {
                        MatrixCursor matrixCursor = new MatrixCursor(DIRECTIONS_CURSOR_COLUMNS);
                        int _id = 0;
                        for (Direction direction : directions) {
                            String tag = direction.getTag();
                            String title = direction.getTitle();
                            String name = direction.getName();
                            matrixCursor.addRow(new Object[] { _id++, tag, title, name });
                        }
                        cursor = matrixCursor;
                    }
                }
                break;
            case URI_CODE.AGENCIES_ROUTES_DIRECTIONS_TAG:
                // e.g. content://com.elliottsj.ftw.provider/agencies/ttc/routes/506/directions/506_1_506Sun
                break;
            case URI_CODE.AGENCIES_ROUTES_DIRECTIONS_STOPS:
                // e.g. content://com.elliottsj.ftw.provider/agencies/ttc/routes/506/directions/506_1_506Sun/stops
                pathSegments = uri.getPathSegments();
                if (pathSegments != null) {
                    String agencyTag = pathSegments.get(1);
                    String routeTag = pathSegments.get(3);
                    String directionTag = pathSegments.get(5);

                    Collection<Stop> stops = null;

                    // Get routes from the network and find the one matching the parameter
                    for (Route route : mNextbusService.getRoutes(mNextbusService.getAgency(agencyTag)))
                        if (route.getTag().equals(routeTag))
                            for (Direction direction : mNextbusService.getRouteConfiguration(route).getDirections())
                                if (direction.getTag().equals(directionTag))
                                    stops = direction.getStops();

                    // Put directions into a MatrixCursor
                    if (stops != null) {
                        MatrixCursor matrixCursor = new MatrixCursor(STOPS_CURSOR_COLUMNS);
                        int _id = 0;
                        for (Stop stop : stops) {
                            String tag = stop.getTag();
                            String title = stop.getTitle();
                            String shortTitle = stop.getShortTitle();
                            matrixCursor.addRow(new Object[] { _id++, tag, title, shortTitle });
                        }
                        cursor = matrixCursor;
                    }
                }
                break;
            case URI_CODE.AGENCIES_ROUTES_DIRECTIONS_STOPS_TAG:
                // e.g. content://com.elliottsj.ftw.provider/agencies/ttc/routes/506/directions/506_1_506Sun/stops/5292
                break;
            default:
                Log.w(TAG, "No match found for uri: " + uri.toString());
                break;
        }

        return cursor;
    }

    @Override
    public String getType(Uri uri) {
        int match = sUriMatcher.match(uri);
        switch (match) {
            case URI_CODE.SAVED_STOPS:
                // e.g. content://com.elliottsj.ftw.provider/saved-stops
                return ContentResolver.CURSOR_DIR_BASE_TYPE + "/vnd.elliottsj.stop";
            case URI_CODE.AGENCIES:
                // e.g. content://com.elliottsj.ftw.provider/agencies
                return ContentResolver.CURSOR_DIR_BASE_TYPE + "/vnd.elliottsj.agency";
            case URI_CODE.AGENCIES_TAG:
                // e.g. content://com.elliottsj.ftw.provider/agencies/ttc
                return ContentResolver.CURSOR_ITEM_BASE_TYPE + "/vnd.elliottsj.agency";
            case URI_CODE.AGENCIES_ROUTES:
                // e.g. content://com.elliottsj.ftw.provider/agencies/ttc/routes
                return ContentResolver.CURSOR_DIR_BASE_TYPE + "/vnd.elliottsj.route";
            case URI_CODE.AGENCIES_ROUTES_TAG:
                // e.g. content://com.elliottsj.ftw.provider/agencies/ttc/routes/506
                return ContentResolver.CURSOR_ITEM_BASE_TYPE + "/vnd.elliottsj.route";
            case URI_CODE.AGENCIES_ROUTES_SERVICE_AREA:
                // e.g. content://com.elliottsj.ftw.provider/agencies/ttc/routes/506/stops/5292
                return ContentResolver.CURSOR_ITEM_BASE_TYPE + "/vnd.elliottsj.service-area";
            case URI_CODE.AGENCIES_ROUTES_PATHS:
                // e.g. content://com.elliottsj.ftw.provider/agencies/ttc/routes/506/paths
                return ContentResolver.CURSOR_DIR_BASE_TYPE + "/vnd.elliottsj.path";
            case URI_CODE.AGENCIES_ROUTES_PATHS_ID:
                // e.g. content://com.elliottsj.ftw.provider/agencies/ttc/routes/506/paths/56
                return ContentResolver.CURSOR_ITEM_BASE_TYPE + "/vnd.elliottsj.path";
            case URI_CODE.AGENCIES_ROUTES_VEHICLE_LOCATIONS:
                // e.g. content://com.elliottsj.ftw.provider/agencies/ttc/routes/506/vehicle-locations
                return ContentResolver.CURSOR_DIR_BASE_TYPE + "/vnd.elliottsj.vehicle-location";
            case URI_CODE.AGENCIES_ROUTES_STOPS:
                // e.g. content://com.elliottsj.ftw.provider/agencies/ttc/routes/506/stops
                return ContentResolver.CURSOR_DIR_BASE_TYPE + "/vnd.elliottsj.stop";
            case URI_CODE.AGENCIES_ROUTES_STOPS_TAG:
                // e.g. content://com.elliottsj.ftw.provider/agencies/ttc/routes/506/stops/5292
                return ContentResolver.CURSOR_ITEM_BASE_TYPE + "/vnd.elliottsj.stop";
            case URI_CODE.AGENCIES_ROUTES_DIRECTIONS:
                // e.g. content://com.elliottsj.ftw.provider/agencies/ttc/routes/506/directions
                return ContentResolver.CURSOR_DIR_BASE_TYPE + "/vnd.elliottsj.direction";
            case URI_CODE.AGENCIES_ROUTES_DIRECTIONS_TAG:
                // e.g. content://com.elliottsj.ftw.provider/agencies/ttc/routes/506/directions/506_1_506Sun
                return ContentResolver.CURSOR_ITEM_BASE_TYPE + "/vnd.elliottsj.direction";
            case URI_CODE.AGENCIES_ROUTES_DIRECTIONS_STOPS:
                // e.g. content://com.elliottsj.ftw.provider/agencies/ttc/routes/506/directions/506_1_506Sun/stops
                return ContentResolver.CURSOR_DIR_BASE_TYPE + "/vnd.elliottsj.stop";
            case URI_CODE.AGENCIES_ROUTES_DIRECTIONS_STOPS_TAG:
                // e.g. content://com.elliottsj.ftw.provider/agencies/ttc/routes/506/directions/506_1_506Sun/stops/5292
                return ContentResolver.CURSOR_ITEM_BASE_TYPE + "/vnd.elliottsj.stop";
            default:
                return null;
        }
    }

    @Override
    public Uri insert(Uri uri, ContentValues values) {
        int match = sUriMatcher.match(uri);
        switch (match) {
            case URI_CODE.SAVED_STOPS:
                // e.g. content://com.elliottsj.ftw.provider/saved-stops
                values.getAsString(SAVED_STOPS.COLUMN_AGENCY_TAG);
                values.getAsString(SAVED_STOPS.COLUMN_STOP_TAG);
                values.getAsString(SAVED_STOPS.COLUMN_STOP_TITLE);



                return null;
            case URI_CODE.AGENCIES:
                // e.g. content://com.elliottsj.ftw.provider/agencies
                Log.e(TAG, "Inserting agencies into NextbusProvider is not yet supported");
                return null;
            case URI_CODE.AGENCIES_TAG:
                // e.g. content://com.elliottsj.ftw.provider/agencies/ttc
                Log.e(TAG, "Inserting agencies into NextbusProvider is not yet supported");
                return null;
            case URI_CODE.AGENCIES_ROUTES:
                // e.g. content://com.elliottsj.ftw.provider/agencies/ttc/routes
                Log.e(TAG, "Inserting routes into NextbusProvider is not yet supported");
                return null;
            case URI_CODE.AGENCIES_ROUTES_TAG:
                // e.g. content://com.elliottsj.ftw.provider/agencies/ttc/routes/506
                Log.e(TAG, "Inserting agencies into NextbusProvider is not yet supported");
                return null;
            case URI_CODE.AGENCIES_ROUTES_SERVICE_AREA:
                // e.g. content://com.elliottsj.ftw.provider/agencies/ttc/routes/506/stops/5292
                Log.e(TAG, "Inserting agencies into NextbusProvider is not yet supported");
                return null;
            case URI_CODE.AGENCIES_ROUTES_PATHS:
                // e.g. content://com.elliottsj.ftw.provider/agencies/ttc/routes/506/paths
                Log.e(TAG, "Inserting agencies into NextbusProvider is not yet supported");
                return null;
            case URI_CODE.AGENCIES_ROUTES_PATHS_ID:
                // e.g. content://com.elliottsj.ftw.provider/agencies/ttc/routes/506/paths/56
                Log.e(TAG, "Inserting agencies into NextbusProvider is not yet supported");
                return null;
            case URI_CODE.AGENCIES_ROUTES_VEHICLE_LOCATIONS:
                // e.g. content://com.elliottsj.ftw.provider/agencies/ttc/routes/506/vehicle-locations
                Log.e(TAG, "Inserting agencies into NextbusProvider is not yet supported");
                return null;
            case URI_CODE.AGENCIES_ROUTES_STOPS:
                // e.g. content://com.elliottsj.ftw.provider/agencies/ttc/routes/506/stops
                Log.e(TAG, "Inserting agencies into NextbusProvider is not yet supported");
                return null;
            case URI_CODE.AGENCIES_ROUTES_STOPS_TAG:
                // e.g. content://com.elliottsj.ftw.provider/agencies/ttc/routes/506/stops/5292
                Log.e(TAG, "Inserting agencies into NextbusProvider is not yet supported");
                return null;
            case URI_CODE.AGENCIES_ROUTES_DIRECTIONS:
                // e.g. content://com.elliottsj.ftw.provider/agencies/ttc/routes/506/directions
                Log.e(TAG, "Inserting agencies into NextbusProvider is not yet supported");
                return null;
            case URI_CODE.AGENCIES_ROUTES_DIRECTIONS_TAG:
                // e.g. content://com.elliottsj.ftw.provider/agencies/ttc/routes/506/directions/506_1_506Sun
                Log.e(TAG, "Inserting agencies into NextbusProvider is not yet supported");
                return null;
            case URI_CODE.AGENCIES_ROUTES_DIRECTIONS_STOPS:
                // e.g. content://com.elliottsj.ftw.provider/agencies/ttc/routes/506/directions/506_1_506Sun/stops
                Log.e(TAG, "Inserting agencies into NextbusProvider is not yet supported");
                return null;
            case URI_CODE.AGENCIES_ROUTES_DIRECTIONS_STOPS_TAG:
                // e.g. content://com.elliottsj.ftw.provider/agencies/ttc/routes/506/directions/506_1_506Sun/stops/5292
                Log.e(TAG, "Inserting agencies into NextbusProvider is not yet supported");
                return null;
            default:
                Log.e(TAG, "Invalid URI");
                return null;
        }
    }

    @Override
    public int delete(Uri uri, String selection, String[] selectionArgs) {
        return 0;
    }

    @Override
    public int update(Uri uri, ContentValues values, String selection, String[] selectionArgs) {
        return 0;
    }

    private NextbusSQLiteHelper getHelper() {
        if (mDbHelper == null)
            mDbHelper = OpenHelperManager.getHelper(getContext(), NextbusSQLiteHelper.class);
        return mDbHelper;
    }

    /**
     * Get a cursor loader which loads the saved stops from the content provider.
     *
     * @param context context for the cursor loader
     * @return a cursor loader
     */
    public static Loader<Cursor> savedStopsLoader(Context context) {
        return new CursorLoader(context, Uri.withAppendedPath(CONTENT_URI, "saved-stops"),
                                SAVED_STOPS_CURSOR_COLUMNS, null, null,
                                SAVED_STOPS.COLUMN_STOP_TITLE);
    }

    /**
     * Get a cursor loader which loads transit routes from the content provider.
     *
     * @param context context for the cursor loader
     * @return a cursor loader
     */
    public static Loader<Cursor> routesLoader(Context context) {
        return new CursorLoader(context, Uri.withAppendedPath(CONTENT_URI, "agencies/ttc/routes"),
                                ROUTES_CURSOR_COLUMNS, null, null,
                                Route.FIELD_TITLE);
    }

    /**
     * Get a cursor loader which loads transit directions from the content provider.
     *
     * @param context context for the cursor loader
     * @param routeTag unique route tag for which to retrieve directions
     * @return a cursor loader
     */
    public static Loader<Cursor> directionsLoader(Context context, String routeTag) {
        String path = String.format("agencies/ttc/routes/%s/directions", routeTag);
        return new CursorLoader(context, Uri.withAppendedPath(CONTENT_URI, path),
                                DIRECTIONS_CURSOR_COLUMNS, null, null,
                                Direction.FIELD_TITLE);
    }

    /**
     * Get a cursor loader which loads transit stops from the content provider.
     *
     * @param context context for the cursor loader
     * @param routeTag unique route tag for which to retrieve stops
     * @param directionTag unique direction tag for which to retrieve stops
     * @return a cursor loader
     */
    public static Loader<Cursor> stopsLoader(Context context, String routeTag, String directionTag) {
        String path = String.format("agencies/ttc/routes/%s/directions/%s/stops", routeTag, directionTag);
        return new CursorLoader(context, Uri.withAppendedPath(CONTENT_URI, path),
                                STOPS_CURSOR_COLUMNS, null, null,
                                Stop.FIELD_TITLE);
    }

}
