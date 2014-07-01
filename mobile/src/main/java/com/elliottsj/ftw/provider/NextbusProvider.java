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

import com.elliottsj.ftw.provider.model.SavedStop;
import com.j256.ormlite.android.apptools.OpenHelperManager;
import com.j256.ormlite.stmt.DeleteBuilder;
import com.j256.ormlite.stmt.QueryBuilder;

import net.sf.nextbus.publicxmlfeed.domain.Agency;
import net.sf.nextbus.publicxmlfeed.domain.Direction;
import net.sf.nextbus.publicxmlfeed.domain.Route;
import net.sf.nextbus.publicxmlfeed.domain.Stop;
import net.sf.nextbus.publicxmlfeed.impl.NextbusService;

import java.sql.SQLException;
import java.util.ArrayList;
import java.util.List;

public class NextbusProvider extends ContentProvider {

    private static final String TAG = NextbusProvider.class.getSimpleName();

    public static final String AUTHORITY = "com.elliottsj.ftw.provider";
    public static final Uri CONTENT_URI = Uri.parse("content://" + AUTHORITY);

    public static final class SAVED_STOPS {
        public static final String COLUMN_AGENCY_TAG = "agency_tag";
        public static final String COLUMN_AGENCY_TITLE = "agency_title";
        public static final String COLUMN_STOP_TAG = "stop_tag";
        public static final String COLUMN_STOP_TITLE = "stop_title";
        public static final String COLUMN_ROUTE_TAG = "route_tag";
        public static final String COLUMN_ROUTE_TITLE = "route_title";
        public static final String COLUMN_ROUTE_SHORT_TITLE = "route_short_title";
        public static final String COLUMN_DIRECTION_TAG = "direction_tag";
        public static final String COLUMN_DIRECTION_TITLE = "direction_title";
        public static final String COLUMN_DIRECTION_NAME = "direction_name";
    }

    public static final String[] SAVED_STOPS_CURSOR_COLUMNS =
            { SAVED_STOPS.COLUMN_AGENCY_TAG,
              SAVED_STOPS.COLUMN_AGENCY_TITLE,
              SAVED_STOPS.COLUMN_STOP_TAG,
              SAVED_STOPS.COLUMN_STOP_TITLE,
              SAVED_STOPS.COLUMN_ROUTE_TAG,
              SAVED_STOPS.COLUMN_ROUTE_TITLE,
              SAVED_STOPS.COLUMN_ROUTE_SHORT_TITLE,
              SAVED_STOPS.COLUMN_DIRECTION_TAG,
              SAVED_STOPS.COLUMN_DIRECTION_TITLE,
              SAVED_STOPS.COLUMN_DIRECTION_NAME };

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
        public static final int SAVED_STOPS = 0;
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

    private NextbusQueryHelper mQueryHelper;
    private NextbusService mNextbusService;

    private NextbusSQLiteHelper mDbHelper;
    private SQLiteDatabase mDatabase;
    private NextbusQueryBuilderFactory mQueryBuilderFactory;

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
        mQueryHelper = new NextbusQueryHelper(getContext());
        return true;
    }

    @SuppressWarnings("ConstantConditions")
    @Override
    public Cursor query(Uri uri, String[] projection, String selection, String[] selectionArgs, String sortOrder) {
        // Helper classes: UriMatcher, ContentUris, Uri, Uri.Builder
        Cursor cursor = null;
        List<String> pathSegments;
        int match = sUriMatcher.match(uri);
        switch (match) {
            case URI_CODE.SAVED_STOPS: {
                // e.g. content://com.elliottsj.ftw.provider/saved-stops
                try {
                    MatrixCursor matrixCursor = new MatrixCursor(SAVED_STOPS_CURSOR_COLUMNS);

                    // Add each saved stop to a MatrixCursor
                    for (SavedStop savedStop : getHelper().getSavedStopsDao()) {
                        // Refresh each of the foreign fields in the saved stop
                        Stop stop = savedStop.getStop();
                        getHelper().getStopsDao().refresh(stop);
                        Agency agency = stop.getAgency();
                        getHelper().getAgenciesDao().refresh(agency);
                        Direction direction = savedStop.getDirection();
                        getHelper().getDirectionsDao().refresh(direction);
                        Route route = direction.getRoute();
                        getHelper().getRoutesDao().refresh(route);

                        // Add relevant fields to the cursor according to SAVED_STOPS_CURSOR_COLUMNS
                        List<String> row = new ArrayList<String>(SAVED_STOPS_CURSOR_COLUMNS.length);
                        row.add(agency.getTag());
                        row.add(agency.getTitle());
                        row.add(stop.getTag());
                        row.add(stop.getTitle());
                        row.add(route.getTag());
                        row.add(route.getTitle());
                        row.add(route.getShortTitle());
                        row.add(direction.getTag());
                        row.add(direction.getTitle());
                        row.add(direction.getName());

                        matrixCursor.addRow(row);
                    }

                    // Done loading saved stops; set the result cursor
                    cursor = matrixCursor;
                } catch (SQLException e) {
                    throw new RuntimeException(e);
                }
                break;
            }
            case URI_CODE.AGENCIES: {
                // e.g. content://com.elliottsj.ftw.provider/agencies
                break;
            }
            case URI_CODE.AGENCIES_TAG: {
                // e.g. content://com.elliottsj.ftw.provider/agencies/ttc
                break;
            }
            case URI_CODE.AGENCIES_ROUTES: {
                // e.g. content://com.elliottsj.ftw.provider/agencies/ttc/routes
                pathSegments = uri.getPathSegments();
                if (pathSegments != null) {
                    String agencyTag = pathSegments.get(1);
                    try {
                        // Fetch routes from the network if necessary
                        mQueryHelper.fetchRoutes(agencyTag);
                        cursor = OrmUtil.cursorFromQueryBuilder(getQbFactory().routesQb(agencyTag),
                                                                projection, selection, selectionArgs, sortOrder);
                    } catch (SQLException e) {
                        throw new RuntimeException(e);
                    }
                }
                break;
            }
            case URI_CODE.AGENCIES_ROUTES_TAG: {
                // e.g. content://com.elliottsj.ftw.provider/agencies/ttc/routes/506
                break;
            }
            case URI_CODE.AGENCIES_ROUTES_SERVICE_AREA: {
                // e.g. content://com.elliottsj.ftw.provider/agencies/ttc/routes/506/stops/5292
                break;
            }
            case URI_CODE.AGENCIES_ROUTES_PATHS: {
                // e.g. content://com.elliottsj.ftw.provider/agencies/ttc/routes/506/paths
                break;
            }
            case URI_CODE.AGENCIES_ROUTES_PATHS_ID: {
                // e.g. content://com.elliottsj.ftw.provider/agencies/ttc/routes/506/paths/56
                break;
            }
            case URI_CODE.AGENCIES_ROUTES_VEHICLE_LOCATIONS: {
                // e.g. content://com.elliottsj.ftw.provider/agencies/ttc/routes/506/vehicle-locations
                break;
            }
            case URI_CODE.AGENCIES_ROUTES_STOPS: {
                // e.g. content://com.elliottsj.ftw.provider/agencies/ttc/routes/506/stops
                break;
            }
            case URI_CODE.AGENCIES_ROUTES_STOPS_TAG: {
                // e.g. content://com.elliottsj.ftw.provider/agencies/ttc/routes/506/stops/5292
                break;
            }
            case URI_CODE.AGENCIES_ROUTES_DIRECTIONS: {
                // e.g. content://com.elliottsj.ftw.provider/agencies/ttc/routes/506/directions
                pathSegments = uri.getPathSegments();
                if (pathSegments != null) {
                    String agencyTag = pathSegments.get(1);
                    String routeTag = pathSegments.get(3);
                    try {
                        // Fetch directions & stops from the network if necessary
                        mQueryHelper.fetchDirections(agencyTag, routeTag, null);
                        cursor = OrmUtil.cursorFromQueryBuilder(getQbFactory().directionsQb(agencyTag, routeTag),
                                                                projection, selection, selectionArgs, sortOrder);
                    } catch (SQLException e) {
                        throw new RuntimeException(e);
                    }
                }
                break;
            }
            case URI_CODE.AGENCIES_ROUTES_DIRECTIONS_TAG: {
                // e.g. content://com.elliottsj.ftw.provider/agencies/ttc/routes/506/directions/506_1_506Sun
                break;
            }
            case URI_CODE.AGENCIES_ROUTES_DIRECTIONS_STOPS: {
                // e.g. content://com.elliottsj.ftw.provider/agencies/ttc/routes/506/directions/506_1_506Sun/stops
                pathSegments = uri.getPathSegments();
                if (pathSegments != null) {
                    String agencyTag = pathSegments.get(1);
                    String routeTag = pathSegments.get(3);
                    String directionTag = pathSegments.get(5);
                    try {
                        // Fetch directions & stops from the network if necessary
                        mQueryHelper.fetchDirections(agencyTag, routeTag, directionTag);
                        cursor = OrmUtil.cursorFromQueryBuilder(getQbFactory().stopsQb(agencyTag, routeTag, directionTag),
                                                                projection, selection, selectionArgs, sortOrder);
                    } catch (SQLException e) {
                        throw new RuntimeException(e);
                    }
                }
                break;
            }
            case URI_CODE.AGENCIES_ROUTES_DIRECTIONS_STOPS_TAG: {
                // e.g. content://com.elliottsj.ftw.provider/agencies/ttc/routes/506/directions/506_1_506Sun/stops/5292
                break;
            }
            default:
                Log.w(TAG, "No match found for uri: " + uri.toString());
                break;
        }

        if (cursor != null)
            cursor.setNotificationUri(getContext().getContentResolver(), uri);
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

    @SuppressWarnings("ConstantConditions")
    @Override
    public Uri insert(Uri uri, ContentValues values) {
        List<String> pathSegments;
        int match = sUriMatcher.match(uri);
        switch (match) {
            case URI_CODE.SAVED_STOPS:
                // e.g. content://com.elliottsj.ftw.provider/saved-stops
                try {
                    String agencyTag = values.getAsString(SAVED_STOPS.COLUMN_AGENCY_TAG);
                    String routeTag = values.getAsString(SAVED_STOPS.COLUMN_ROUTE_TAG);
                    String directionTag = values.getAsString(SAVED_STOPS.COLUMN_DIRECTION_TAG);
                    String stopTag = values.getAsString(SAVED_STOPS.COLUMN_STOP_TAG);

                    Direction direction = getQbFactory().directionsQb(agencyTag, routeTag, directionTag).queryForFirst();
                    Stop stop = getQbFactory().stopsQb(agencyTag, routeTag, directionTag, stopTag).queryForFirst();

                    SavedStop savedStop = new SavedStop(stop, direction);
                    getHelper().getSavedStopsDao().createIfNotExists(savedStop);

                    // Notify observers
                    getContext().getContentResolver().notifyChange(uri, null, false);

                    String path = String.format("agencies/%s/routes/%s/directions/%s/stops/%s",
                                                agencyTag, routeTag, directionTag, stopTag);
                    return Uri.withAppendedPath(CONTENT_URI, path);
                } catch (SQLException e) {
                    throw new RuntimeException(e);
                }
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

    @SuppressWarnings("ConstantConditions")
    @Override
    public int delete(Uri uri, String selection, String[] selectionArgs) {
        int match = sUriMatcher.match(uri);
        switch (match) {
            case URI_CODE.SAVED_STOPS:
                try {
                    String agencyTag = selectionArgs[0];
                    String routeTag = selectionArgs[1];
                    String directionTag = selectionArgs[2];
                    String stopTag = selectionArgs[3];

                    QueryBuilder<SavedStop, Integer> queryBuilder = getHelper().getSavedStopsDao().queryBuilder();
                    queryBuilder.join(getQbFactory().stopsQb(agencyTag, routeTag, directionTag, stopTag));
                    queryBuilder.selectColumns(SavedStop.FIELD_ID);

                    DeleteBuilder<SavedStop, Integer> deleteBuilder = getHelper().getSavedStopsDao().deleteBuilder();
                    deleteBuilder.where().in(SavedStop.FIELD_ID, queryBuilder);

                    int rowsAffected = deleteBuilder.delete();

                    getContext().getContentResolver().notifyChange(uri, null, false);

                    return rowsAffected;
                } catch (SQLException e) {
                    throw new RuntimeException(e);
                }
            default:
                return 0;
        }
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

    private NextbusQueryBuilderFactory getQbFactory() {
        if (mQueryBuilderFactory == null)
            mQueryBuilderFactory = new NextbusQueryBuilderFactory(getHelper());
        return mQueryBuilderFactory;
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
                                ROUTES_CURSOR_COLUMNS, null, null, null);
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

    /**
     * Get a uri that can be used to insert a saved stop into NextbusProvider
     *
     * @return a uri
     */
    public static Uri savedStopUri() {
        return Uri.withAppendedPath(CONTENT_URI, "saved-stops");
    }

    public static void deleteSavedStop(ContentResolver contentResolver, String agencyTag, String routeTag, String directionTag, String stopTag) {
        String where = String.format("%s = ? AND %s = ? AND %s = ? AND %s = ?",
                                     Agency.FIELD_TAG,
                                     Route.FIELD_TAG,
                                     Direction.FIELD_TAG,
                                     Stop.FIELD_TAG);
        contentResolver.delete(savedStopUri(), where, new String[] { agencyTag, routeTag, directionTag, stopTag });
    }

}
