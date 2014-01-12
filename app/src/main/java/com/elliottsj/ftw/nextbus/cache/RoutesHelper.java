package com.elliottsj.ftw.nextbus.cache;

import android.content.ContentValues;
import android.database.Cursor;
import android.database.sqlite.SQLiteDatabase;

import net.sf.nextbus.publicxmlfeed.domain.Agency;
import net.sf.nextbus.publicxmlfeed.domain.Route;
import net.sf.nextbus.publicxmlfeed.service.ServiceException;

import java.util.ArrayList;
import java.util.List;

public class RoutesHelper extends CacheHelper {

    private static final String[] ROUTES_COLUMNS =
            { NextbusSQLiteHelper.ROUTES.COLUMN_AUTO_ID,
              NextbusSQLiteHelper.ROUTES.COLUMN_COPYRIGHT,
              NextbusSQLiteHelper.ROUTES.COLUMN_TIMESTAMP,
              NextbusSQLiteHelper.ROUTES.COLUMN_AGENCY,
              NextbusSQLiteHelper.ROUTES.COLUMN_TAG,
              NextbusSQLiteHelper.ROUTES.COLUMN_TITLE,
              NextbusSQLiteHelper.ROUTES.COLUMN_SHORT_TITLE };

    protected RoutesHelper(SQLiteDatabase database) {
        super(database);
    }

    public boolean isRoutesCached(Agency agency) {
        Cursor cursor = mDatabase.query("(SELECT " +
                                        NextbusSQLiteHelper.ROUTES.COLUMN_AGENCY + " FROM " +
                                        NextbusSQLiteHelper.ROUTES.TABLE + ") AS A JOIN " +
                                        NextbusSQLiteHelper.AGENCIES.TABLE + " AS B ON " +
                                        "A." + NextbusSQLiteHelper.ROUTES.COLUMN_AGENCY + " = " +
                                        "B." + NextbusSQLiteHelper.AGENCIES.COLUMN_AUTO_ID,
                                        new String[] { NextbusSQLiteHelper.AGENCIES.COLUMN_TAG },
                                        NextbusSQLiteHelper.AGENCIES.COLUMN_TAG + " = ?", new String[] { agency.getTag() },
                                        null, null, null, "1");

        int count = cursor.getCount();
        cursor.close();

        return count != 0;
    }

    public boolean isRouteCached(Route route) {
        Cursor cursor = getRouteCursor(mDatabase, route.getTag());
        int count = cursor.getCount();
        cursor.close();
        return count != 0;
    }

    public long getRoutesAge(Agency agency) {
        Cursor cursor = getRoutesCursor(agency);
        Route route = getRouteFromCursor(cursor, agency);
        cursor.close();
        return route.getObjectAge();
    }

    public List<Route> getRoutes(Agency agency) {
        if (!isRoutesCached(agency))
            throw new ServiceException("Routes are not cached for agency: " + agency);

        List<Route> routes = new ArrayList<Route>();

        Cursor cursor = getRoutesCursor(agency);

        cursor.moveToFirst();
        while (!cursor.isAfterLast()) {
            routes.add(getRouteFromCursor(cursor, agency));
            cursor.moveToNext();
        }
        cursor.close();

        return routes;
    }

    public void putRoutes(List<Route> routes) {
        // Get the agency tag for the first route
        String agencyTag = routes.get(0).getAgency().getTag();

        // Delete all existing routes for the agency
        mDatabase.rawQuery("DELETE FROM " + NextbusSQLiteHelper.ROUTES.TABLE + " WHERE " +
                           NextbusSQLiteHelper.ROUTES.COLUMN_AGENCY + " = " +
                           "(SELECT " + NextbusSQLiteHelper.AGENCIES.COLUMN_AUTO_ID + " FROM " +
                           NextbusSQLiteHelper.AGENCIES.TABLE + " WHERE " +
                           NextbusSQLiteHelper.AGENCIES.COLUMN_TAG + " = ? LIMIT 1)", new String[]{agencyTag});

        // Insert all routes into the table
        for (Route route : routes)
            putRoute(route);
    }

    /**
     * Inserts the given route into the database. An agency for the given route must already exist in the database
     *
     * @param route an agency
     */
    public void putRoute(Route route) {
        ContentValues values = new ContentValues();
        values.put(NextbusSQLiteHelper.ROUTES.COLUMN_COPYRIGHT, route.getCopyrightNotice());
        values.put(NextbusSQLiteHelper.ROUTES.COLUMN_TIMESTAMP, route.getObjectTimestamp());
        values.put(NextbusSQLiteHelper.ROUTES.COLUMN_AGENCY, AgenciesHelper.getAgencyAutoId(mDatabase, route.getAgency()));
        values.put(NextbusSQLiteHelper.ROUTES.COLUMN_TAG, route.getTag());
        values.put(NextbusSQLiteHelper.ROUTES.COLUMN_TITLE, route.getTitle());
        values.put(NextbusSQLiteHelper.ROUTES.COLUMN_SHORT_TITLE, route.getShortTitle());

        mDatabase.insert(NextbusSQLiteHelper.ROUTES.TABLE, null, values);
    }

    /**
     * @param agency the agency that owns the routes
     * @return a cursor pointing at the first row of containing a route
     */
    private Cursor getRoutesCursor(Agency agency) {
        Cursor cursor = mDatabase.query(NextbusSQLiteHelper.ROUTES.TABLE + " AS A JOIN " +
                                        NextbusSQLiteHelper.AGENCIES.TABLE + " AS B ON " +
                                        "A." + NextbusSQLiteHelper.ROUTES.COLUMN_AGENCY + " = " +
                                        "B." + NextbusSQLiteHelper.AGENCIES.COLUMN_AUTO_ID,
                                        stringsWithPrefix("A.", ROUTES_COLUMNS),
                                        "B." + NextbusSQLiteHelper.AGENCIES.COLUMN_TAG + " = ?", new String[] { agency.getTag() },
                                        null, null, null);

        cursor.moveToFirst();
        return cursor;
    }

    /**
     * @param tag a route tag
     * @return a cursor pointing at the row of the route with the given tag
     */
    private static Cursor getRouteCursor(SQLiteDatabase database, String tag) {
        Cursor cursor = database.query(NextbusSQLiteHelper.ROUTES.TABLE,
                                       ROUTES_COLUMNS,
                                       NextbusSQLiteHelper.ROUTES.COLUMN_TAG + " = " + tag,
                                       null, null, null, null, "1");

        cursor.moveToFirst();
        return cursor;
    }

    /**
     * @param cursor a cursor pointing at a row of route data
     * @return the route on the current row of the given cursor
     */
    private Route getRouteFromCursor(Cursor cursor, Agency agency) {
        String copyright = cursor.getString(1);
        long timestamp = cursor.getLong(2);
        String tag = cursor.getString(4);
        String title = cursor.getString(5);
        String shortTitle = cursor.getString(6);

        return new Route(agency, tag, title, shortTitle, copyright, timestamp);
    }

    /**
     * @param route a route
     * @return the primary key AUTO_ID of the given route
     */
    public static int getRouteAutoId(SQLiteDatabase database, Route route) {
        Cursor routeCursor = getRouteCursor(database, route.getTag());
        int routeAutoId = routeCursor.getInt(0);
        routeCursor.close();
        return routeAutoId;
    }

}
