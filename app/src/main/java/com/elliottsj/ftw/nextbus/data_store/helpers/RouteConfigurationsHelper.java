package com.elliottsj.ftw.nextbus.data_store.helpers;

import android.content.ContentValues;
import android.database.Cursor;
import android.database.sqlite.SQLiteDatabase;
import android.database.sqlite.SQLiteQueryBuilder;

import net.sf.nextbus.publicxmlfeed.domain.Agency;
import net.sf.nextbus.publicxmlfeed.domain.Direction;
import net.sf.nextbus.publicxmlfeed.domain.Geolocation;
import net.sf.nextbus.publicxmlfeed.domain.Path;
import net.sf.nextbus.publicxmlfeed.domain.Route;
import net.sf.nextbus.publicxmlfeed.domain.RouteConfiguration;
import net.sf.nextbus.publicxmlfeed.domain.Stop;

import org.apache.commons.lang3.ArrayUtils;

import java.util.ArrayList;
import java.util.List;

@SuppressWarnings("ConstantConditions")
public class RouteConfigurationsHelper extends DataStoreHelper {

    private static final String[] ROUTE_CONFIGURATIONS_COLUMNS =
            { NextbusSQLiteHelper.ROUTE_CONFIGURATIONS.COLUMN_COPYRIGHT,
              NextbusSQLiteHelper.ROUTE_CONFIGURATIONS.COLUMN_TIMESTAMP,
              NextbusSQLiteHelper.ROUTE_CONFIGURATIONS.COLUMN_ROUTE,
              NextbusSQLiteHelper.ROUTE_CONFIGURATIONS.COLUMN_UI_COLOR,
              NextbusSQLiteHelper.ROUTE_CONFIGURATIONS.COLUMN_UI_OPPOSITE_COLOR };

    private static final String[] SERVICE_AREAS_COLUMNS =
            { NextbusSQLiteHelper.SERVICE_AREAS.COLUMN_LAT_MIN,
              NextbusSQLiteHelper.SERVICE_AREAS.COLUMN_LAT_MAX,
              NextbusSQLiteHelper.SERVICE_AREAS.COLUMN_LON_MIN,
              NextbusSQLiteHelper.SERVICE_AREAS.COLUMN_LON_MAX,
              NextbusSQLiteHelper.SERVICE_AREAS.COLUMN_ROUTE_CONFIGURATION };

    public static final String[] STOPS_COLUMNS =
            { NextbusSQLiteHelper.STOPS.COLUMN_COPYRIGHT,
              NextbusSQLiteHelper.STOPS.COLUMN_TIMESTAMP,
              NextbusSQLiteHelper.STOPS.COLUMN_AGENCY,
              NextbusSQLiteHelper.STOPS.COLUMN_TAG,
              NextbusSQLiteHelper.STOPS.COLUMN_TITLE,
              NextbusSQLiteHelper.STOPS.COLUMN_SHORT_TITLE,
              NextbusSQLiteHelper.STOPS.COLUMN_STOP_ID };

    private static final String[] GEOLOCATIONS_COLUMNS =
            { NextbusSQLiteHelper.GEOLOCATIONS.COLUMN_LAT,
              NextbusSQLiteHelper.GEOLOCATIONS.COLUMN_LON,
              NextbusSQLiteHelper.GEOLOCATIONS.COLUMN_STOP };

    private static final String[] DIRECTIONS_COLUMNS =
            { NextbusSQLiteHelper.DIRECTIONS.COLUMN_COPYRIGHT,
              NextbusSQLiteHelper.DIRECTIONS.COLUMN_TIMESTAMP,
              NextbusSQLiteHelper.DIRECTIONS.COLUMN_ROUTE,
              NextbusSQLiteHelper.DIRECTIONS.COLUMN_TAG,
              NextbusSQLiteHelper.DIRECTIONS.COLUMN_TITLE,
              NextbusSQLiteHelper.DIRECTIONS.COLUMN_NAME,
              NextbusSQLiteHelper.DIRECTIONS.COLUMN_ROUTE_CONFIGURATION };

    private static final String[] PATHS_COLUMNS =
            { NextbusSQLiteHelper.PATHS.COLUMN_COPYRIGHT,
              NextbusSQLiteHelper.PATHS.COLUMN_ROUTE,
              NextbusSQLiteHelper.PATHS.COLUMN_PATH_ID,
              NextbusSQLiteHelper.PATHS.COLUMN_ROUTE_CONFIGURATION };

    private static final String[] POINTS_COLUMNS =
            { NextbusSQLiteHelper.POINTS.COLUMN_LAT,
              NextbusSQLiteHelper.POINTS.COLUMN_LON,
              NextbusSQLiteHelper.POINTS.COLUMN_PATH };

    private RouteConfiguration lastFetched;

    public RouteConfigurationsHelper(SQLiteDatabase database) {
        super(database);
    }

    public boolean isRouteConfigurationCached(Route route) {
        Cursor cursor = getRouteConfigurationCursor(route);

        int count = cursor.getCount();
        cursor.close();

        return count != 0;
    }

    public long getRouteConfigurationAge(Route route) {
        if (lastFetched.getRoute().equals(route))
            return lastFetched.getAge();

        return getRouteConfiguration(route).getAge();
    }

    public RouteConfiguration getRouteConfiguration(Route route) {
        if (lastFetched.getRoute().equals(route))
            return lastFetched;

        Cursor cursor = getRouteConfigurationCursor(route);

        cursor.moveToFirst();
        RouteConfiguration routeConfiguration = getRouteConfigurationFromCursor(cursor, route);
        lastFetched = routeConfiguration;

        cursor.close();

        return routeConfiguration;
    }

    public void putRouteConfiguration(RouteConfiguration routeConfiguration) {
        // Cache the route configuration
        ContentValues values = new ContentValues();
        values.put(NextbusSQLiteHelper.ROUTE_CONFIGURATIONS.COLUMN_COPYRIGHT, routeConfiguration.getCopyright());
        values.put(NextbusSQLiteHelper.ROUTE_CONFIGURATIONS.COLUMN_TIMESTAMP, routeConfiguration.getTimestamp());
        values.put(NextbusSQLiteHelper.ROUTE_CONFIGURATIONS.COLUMN_ROUTE, RoutesHelper.getRouteAutoId(mDatabase, routeConfiguration.getRoute()));
        values.put(NextbusSQLiteHelper.ROUTE_CONFIGURATIONS.COLUMN_UI_COLOR, routeConfiguration.getUiColor().getHexColor());
        values.put(NextbusSQLiteHelper.ROUTE_CONFIGURATIONS.COLUMN_UI_OPPOSITE_COLOR, routeConfiguration.getUiOppositeColor().getHexColor());

        long routeConfigurationAutoId = mDatabase.insertOrThrow(NextbusSQLiteHelper.ROUTE_CONFIGURATIONS.TABLE, null, values);

        // Cache the service area
        putServiceArea(routeConfiguration.getServiceArea(), routeConfigurationAutoId);
        // Cache the stops
        for (Stop stop : routeConfiguration.getStops())
            putStop(stop, routeConfigurationAutoId);

        // Cache the directions
        for (Direction direction : routeConfiguration.getDirections())
            putDirection(direction, routeConfigurationAutoId);

        // Cache the paths
        for (Path path : routeConfiguration.getPaths())
            putPath(path, routeConfigurationAutoId);
    }

    public void deleteRouteConfiguration(RouteConfiguration routeConfiguration) {
        // First, construct the query to get the route's _ID
        String routeIdQuery = SQLiteQueryBuilder.buildQueryString(false, NextbusSQLiteHelper.ROUTES.TABLE,
                                                                  new String[] { NextbusSQLiteHelper.ROUTES.COLUMN_AUTO_ID },
                                                                  NextbusSQLiteHelper.ROUTES.COLUMN_TAG + " = '" +
                                                                  routeConfiguration.getRoute().getTag() + "'",
                                                                  null, null, null, null);

        // Delete the route configuration using sub-query routeIdQuery to find the _ID of the route
        // Child rows in other tables will be cascade deleted
        mDatabase.delete(NextbusSQLiteHelper.ROUTE_CONFIGURATIONS.TABLE,
                         NextbusSQLiteHelper.ROUTE_CONFIGURATIONS.COLUMN_ROUTE + " = (" +
                         routeIdQuery + ")", null);
    }

    /**
     * Gets the list of all routes for which route configurations are cached.
     *
     * @return all routes that have route configurations cached
     */
    public List<Route> getAllRouteConfigurationRoutes(Agency agency) {
        List<Route> routes = new ArrayList<Route>();

//        String[] columns = concatAll(ROUTE_CONFIGURATIONS_COLUMNS, RoutesHelper.ROUTES_COLUMNS);
        String[] columns = RoutesHelper.ROUTES_COLUMNS;

        SQLiteQueryBuilder queryBuilder = new SQLiteQueryBuilder();
        queryBuilder.setTables(NextbusSQLiteHelper.ROUTE_CONFIGURATIONS.TABLE + ", " +
                               NextbusSQLiteHelper.ROUTES.TABLE + ", " +
                               NextbusSQLiteHelper.AGENCIES.TABLE);
        queryBuilder.appendWhere(NextbusSQLiteHelper.ROUTE_CONFIGURATIONS.COLUMN_ROUTE + " = " +
                                 NextbusSQLiteHelper.ROUTES.TABLE + "." +
                                 NextbusSQLiteHelper.ROUTES.COLUMN_AUTO_ID);
        queryBuilder.appendWhere(" AND ");
        queryBuilder.appendWhere(NextbusSQLiteHelper.ROUTES.COLUMN_AGENCY + " = " +
                                 NextbusSQLiteHelper.AGENCIES.TABLE + "." +
                                 NextbusSQLiteHelper.AGENCIES.COLUMN_AUTO_ID);
        queryBuilder.appendWhere(" AND ");
        queryBuilder.appendWhere(NextbusSQLiteHelper.AGENCIES.COLUMN_TAG + " = ?");
        Cursor cursor = queryBuilder.query(mDatabase, columns, null, new String[] { agency.getTag() },
                                           null, null, null);

        // Keep references to the column positions of each property
        RoutesHelper.RouteCursorColumns routeCursorColumns =
                RoutesHelper.RouteCursorColumns.fromCursor(cursor);

        cursor.moveToFirst();
        while (!cursor.isAfterLast()) {
            routes.add(RoutesHelper.routeFromCursor(cursor, routeCursorColumns, agency));
            cursor.moveToNext();
        }
        cursor.close();

        return routes;
    }

    /**
     * Gets the list of all stops for all route configurations stored in this cache.
     *
     * @return all stops in this cache
     */
    public List<Stop> getAllStops(Agency agency) {
        List<Stop> stops = new ArrayList<Stop>();

        SQLiteQueryBuilder queryBuilder = new SQLiteQueryBuilder();
        queryBuilder.setTables(NextbusSQLiteHelper.STOPS.TABLE + ", " +
                               NextbusSQLiteHelper.AGENCIES.TABLE + ", " +
                               NextbusSQLiteHelper.GEOLOCATIONS.TABLE);
        queryBuilder.appendWhere(NextbusSQLiteHelper.STOPS.TABLE + "." +
                                 NextbusSQLiteHelper.STOPS.COLUMN_AGENCY + " = " +
                                 NextbusSQLiteHelper.AGENCIES.TABLE + "." +
                                 NextbusSQLiteHelper.AGENCIES.COLUMN_AUTO_ID);
        queryBuilder.appendWhere(" AND ");
        queryBuilder.appendWhere(NextbusSQLiteHelper.GEOLOCATIONS.TABLE + "." +
                                 NextbusSQLiteHelper.GEOLOCATIONS.COLUMN_STOP + " = " +
                                 NextbusSQLiteHelper.STOPS.TABLE + "." +
                                 NextbusSQLiteHelper.STOPS.COLUMN_AUTO_ID);
        Cursor cursor = queryBuilder.query(mDatabase,
                                           concatAll(STOPS_COLUMNS, GEOLOCATIONS_COLUMNS),
                                           NextbusSQLiteHelper.AGENCIES.COLUMN_TAG + " = ",
                                           new String[] { agency.getTag() },
                                           null, null, null);

        // Keep references to the column positions of each property
        GeolocationCursorColumns geolocationCursorColumns =
                GeolocationCursorColumns.fromCursor(cursor);
        StopCursorColumns stopCursorColumns =
                StopCursorColumns.fromCursor(cursor);

        // Iterate over the cursor to build the list of stops
        cursor.moveToFirst();
        while(!cursor.isAfterLast()) {
            Geolocation geolocation = geolocationFromCursor(cursor, geolocationCursorColumns);
            stops.add(stopFromCursor(cursor, stopCursorColumns, agency, geolocation));

            cursor.moveToNext();
        }
        cursor.close();

        return stops;
    }

    private void putServiceArea(RouteConfiguration.ServiceArea serviceArea, long routeConfigAutoId) {
        ContentValues values = new ContentValues();
        values.put(NextbusSQLiteHelper.SERVICE_AREAS.COLUMN_LAT_MIN, serviceArea.getLatMin());
        values.put(NextbusSQLiteHelper.SERVICE_AREAS.COLUMN_LAT_MAX, serviceArea.getLatMax());
        values.put(NextbusSQLiteHelper.SERVICE_AREAS.COLUMN_LON_MIN, serviceArea.getLonMin());
        values.put(NextbusSQLiteHelper.SERVICE_AREAS.COLUMN_LON_MAX, serviceArea.getLonMax());
        values.put(NextbusSQLiteHelper.SERVICE_AREAS.COLUMN_ROUTE_CONFIGURATION, routeConfigAutoId);

        mDatabase.insert(NextbusSQLiteHelper.SERVICE_AREAS.TABLE, null, values);
    }

    private void putStop(Stop stop, long routeConfigurationAutoId) {
        // Insert into stops table
        ContentValues values = new ContentValues();
        values.put(NextbusSQLiteHelper.STOPS.COLUMN_COPYRIGHT, stop.getCopyright());
        values.put(NextbusSQLiteHelper.STOPS.COLUMN_TIMESTAMP, stop.getTimestamp());
        values.put(NextbusSQLiteHelper.STOPS.COLUMN_AGENCY, AgenciesHelper.getAgencyAutoId(mDatabase, stop.getAgency()));
        values.put(NextbusSQLiteHelper.STOPS.COLUMN_TAG, stop.getTag());
        values.put(NextbusSQLiteHelper.STOPS.COLUMN_TITLE, stop.getTitle());
        values.put(NextbusSQLiteHelper.STOPS.COLUMN_SHORT_TITLE, stop.getShortTitle());
        values.put(NextbusSQLiteHelper.STOPS.COLUMN_STOP_ID, stop.getStopId());

        long stopAutoId = mDatabase.insert(NextbusSQLiteHelper.STOPS.TABLE, null, values);

        // Insert into geolocations table
        values = new ContentValues();
        values.put(NextbusSQLiteHelper.GEOLOCATIONS.COLUMN_LAT, stop.getGeolocation().getLatitude());
        values.put(NextbusSQLiteHelper.GEOLOCATIONS.COLUMN_LON, stop.getGeolocation().getLongitude());
        values.put(NextbusSQLiteHelper.GEOLOCATIONS.COLUMN_STOP, stopAutoId);

        mDatabase.insert(NextbusSQLiteHelper.GEOLOCATIONS.TABLE, null, values);

        // Insert into route configurations/stops junction table
        values = new ContentValues();
        values.put(NextbusSQLiteHelper.ROUTE_CONFIGURATIONS_STOPS.COLUMN_ROUTE_CONFIGURATION, routeConfigurationAutoId);
        values.put(NextbusSQLiteHelper.ROUTE_CONFIGURATIONS_STOPS.COLUMN_STOP, stopAutoId);
    }

    private void putDirection(Direction direction, long routeConfigurationAutoId) {
        ContentValues values = new ContentValues();
        values.put(NextbusSQLiteHelper.DIRECTIONS.COLUMN_COPYRIGHT, direction.getCopyright());
        values.put(NextbusSQLiteHelper.DIRECTIONS.COLUMN_TIMESTAMP, direction.getTimestamp());
        values.put(NextbusSQLiteHelper.DIRECTIONS.COLUMN_ROUTE, RoutesHelper.getRouteAutoId(mDatabase, direction.getRoute()));
        values.put(NextbusSQLiteHelper.DIRECTIONS.COLUMN_TAG, direction.getTag());
        values.put(NextbusSQLiteHelper.DIRECTIONS.COLUMN_TITLE, direction.getTitle());
        values.put(NextbusSQLiteHelper.DIRECTIONS.COLUMN_NAME, direction.getName());
        values.put(NextbusSQLiteHelper.DIRECTIONS.COLUMN_ROUTE_CONFIGURATION, routeConfigurationAutoId);

        long directionAutoId = mDatabase.insert(NextbusSQLiteHelper.DIRECTIONS.TABLE, null, values);

        for (Stop stop : direction.getStops()) {
            // Use raw insert statement to reduce SQL queries
            mDatabase.rawQuery("INSERT INTO " +
                               NextbusSQLiteHelper.DIRECTIONS_STOPS.TABLE + "(" +
                               NextbusSQLiteHelper.DIRECTIONS_STOPS.COLUMN_DIRECTION + ", " +
                               NextbusSQLiteHelper.DIRECTIONS_STOPS.COLUMN_STOP + ") " +
                               "VALUES(" +
                               directionAutoId + ", " +
                               "(SELECT " +
                               NextbusSQLiteHelper.STOPS.COLUMN_AUTO_ID + " FROM " +
                               NextbusSQLiteHelper.STOPS.TABLE + " WHERE " +
                               NextbusSQLiteHelper.STOPS.COLUMN_TAG + " = ? LIMIT 1))",
                               new String[] { stop.getTag() });
        }
    }

    private void putPath(Path path, long routeConfigurationAutoId) {
        ContentValues values = new ContentValues();
        values.put(NextbusSQLiteHelper.PATHS.COLUMN_COPYRIGHT, path.getCopyright());
        values.put(NextbusSQLiteHelper.PATHS.COLUMN_ROUTE, RoutesHelper.getRouteAutoId(mDatabase, path.getRoute()));
        values.put(NextbusSQLiteHelper.PATHS.COLUMN_PATH_ID, path.getPathId());
        values.put(NextbusSQLiteHelper.PATHS.COLUMN_ROUTE_CONFIGURATION, routeConfigurationAutoId);

        long pathAutoId = mDatabase.insert(NextbusSQLiteHelper.PATHS.TABLE, null, values);

        for (Geolocation point : path.getPoints()) {
            values = new ContentValues();
            values.put(NextbusSQLiteHelper.POINTS.COLUMN_LAT, point.getLatitude());
            values.put(NextbusSQLiteHelper.POINTS.COLUMN_LON, point.getLongitude());
            values.put(NextbusSQLiteHelper.POINTS.COLUMN_PATH, pathAutoId);

            mDatabase.insert(NextbusSQLiteHelper.POINTS.TABLE, null, values);
        }
    }

    /**
     * @param route a route
     * @return a cursor containing the row of the route configuration of the given route
     */
    private Cursor getRouteConfigurationCursor(Route route) {
        SQLiteQueryBuilder queryBuilder = new SQLiteQueryBuilder();
        queryBuilder.setTables(NextbusSQLiteHelper.ROUTE_CONFIGURATIONS.TABLE + ", " +
                               NextbusSQLiteHelper.ROUTES.TABLE);
        queryBuilder.appendWhere(NextbusSQLiteHelper.ROUTE_CONFIGURATIONS.COLUMN_ROUTE + " = " +
                                 NextbusSQLiteHelper.ROUTES.COLUMN_AUTO_ID);

        return queryBuilder.query(mDatabase, ROUTE_CONFIGURATIONS_COLUMNS,
                                  NextbusSQLiteHelper.ROUTES.COLUMN_TAG + " = ?",
                                  new String[] { route.getTag() },
                                  null, null, null, "1");
    }

    /**
     *
     * @param cursor a cursor pointing at a row of route configuration data
     * @param route the route that owns this configuration
     * @return the route configuration on the current row of the given cursor
     */
    private RouteConfiguration getRouteConfigurationFromCursor(Cursor cursor, Route route) {
        int routeConfigurationAutoId = cursor.getInt(0);
        String copyright = cursor.getString(1);
        long timestamp = cursor.getLong(2);
        String uiColor = cursor.getString(4);
        String uiOppositeColor = cursor.getString(5);

        return new RouteConfiguration(route,
                                      getStopsForRouteConfiguration(routeConfigurationAutoId, route.getAgency()),
                                      getDirectionsForRouteConfiguration(routeConfigurationAutoId, route),
                                      getPathsForRouteConfiguration(routeConfigurationAutoId, route),
                                      getServiceAreaForRouteConfiguration(routeConfigurationAutoId),
                                      new RouteConfiguration.UIColor(uiOppositeColor),
                                      new RouteConfiguration.UIColor(uiColor),
                                      copyright, timestamp);
    }

    private List<Stop> getStopsForRouteConfiguration(int routeConfigurationAutoId, Agency agency) {
        List<Stop> stops = new ArrayList<Stop>();

        Cursor cursor = mDatabase.query("(" +
                                        NextbusSQLiteHelper.STOPS.TABLE + " AS A JOIN " +
                                        "(SELECT " +
                                        NextbusSQLiteHelper.ROUTE_CONFIGURATIONS_STOPS.COLUMN_ROUTE_CONFIGURATION + ", " +
                                        NextbusSQLiteHelper.ROUTE_CONFIGURATIONS_STOPS.COLUMN_STOP + " FROM " +
                                        NextbusSQLiteHelper.ROUTE_CONFIGURATIONS_STOPS.TABLE + ") AS B ON " +
                                        "A." + NextbusSQLiteHelper.STOPS.COLUMN_AUTO_ID + " = " +
                                        "B." + NextbusSQLiteHelper.ROUTE_CONFIGURATIONS_STOPS.COLUMN_STOP + ") AS C JOIN " +
                                        "(SELECT " +
                                        NextbusSQLiteHelper.GEOLOCATIONS.COLUMN_LAT + ", " +
                                        NextbusSQLiteHelper.GEOLOCATIONS.COLUMN_LON + ", " +
                                        NextbusSQLiteHelper.GEOLOCATIONS.COLUMN_STOP + " FROM " +
                                        NextbusSQLiteHelper.GEOLOCATIONS.TABLE + ") AS D ON " +
                                        "C." + NextbusSQLiteHelper.STOPS.COLUMN_AUTO_ID + " = " +
                                        "D." + NextbusSQLiteHelper.ROUTE_CONFIGURATIONS_STOPS.COLUMN_STOP,
                                        ArrayUtils.addAll(STOPS_COLUMNS, NextbusSQLiteHelper.GEOLOCATIONS.COLUMN_LAT, NextbusSQLiteHelper.GEOLOCATIONS.COLUMN_LON),
                                        NextbusSQLiteHelper.ROUTE_CONFIGURATIONS_STOPS.COLUMN_ROUTE_CONFIGURATION + " = " + routeConfigurationAutoId,
                                        null, null, null, null);

        cursor.moveToFirst();
        while (!cursor.isAfterLast()) {
            stops.add(getStopFromCursor(cursor, agency));
            cursor.moveToNext();
        }
        cursor.close();

        return stops;
    }

    /**
     * @param cursor a cursor pointing at a row of stop data
     * @return the stop on the current row of the given cursor
     */
    private Stop getStopFromCursor(Cursor cursor, Agency agency) {
        String copyright = cursor.getString(1);
        long timestamp = cursor.getLong(2);
        String tag = cursor.getString(4);
        String title = cursor.getString(5);
        String shortTitle = cursor.getString(6);
        String stopId = cursor.getString(7);
        double lat = cursor.getDouble(8);
        double lon = cursor.getDouble(9);

        return new Stop(agency, stopId, tag, title, shortTitle, new Geolocation(lat, lon), copyright, timestamp);
    }

    private List<Direction> getDirectionsForRouteConfiguration(int routeConfigurationAutoId, Route route) {
        List<Direction> directions = new ArrayList<Direction>();

        Cursor cursor = mDatabase.query(NextbusSQLiteHelper.DIRECTIONS.TABLE, DIRECTIONS_COLUMNS,
                                        NextbusSQLiteHelper.DIRECTIONS.COLUMN_ROUTE_CONFIGURATION + " = " + routeConfigurationAutoId,
                                        null, null, null, null);

        cursor.moveToFirst();
        while (!cursor.isAfterLast()) {
            directions.add(getDirectionFromCursor(cursor, route));
            cursor.moveToNext();
        }
        cursor.close();

        return directions;
    }

    private Direction getDirectionFromCursor(Cursor cursor, Route route) {
        int autoId = cursor.getInt(0);
        String copyright = cursor.getString(1);
        long timestamp = cursor.getLong(2);
        String tag = cursor.getString(4);
        String title = cursor.getString(5);
        String name = cursor.getString(6);

        List<Stop> stops = getStopsForDirection(autoId, route.getAgency());

        return new Direction(route, tag, title, name, stops, copyright, timestamp);
    }

    private List<Stop> getStopsForDirection(int directionAutoId, Agency agency) {
        List<Stop> stops = new ArrayList<Stop>();

        Cursor cursor = mDatabase.query("(" +
                                        NextbusSQLiteHelper.STOPS.TABLE + " AS A JOIN " +
                                        "(SELECT " +
                                        NextbusSQLiteHelper.DIRECTIONS_STOPS.COLUMN_DIRECTION + ", " +
                                        NextbusSQLiteHelper.DIRECTIONS_STOPS.COLUMN_STOP + " FROM " +
                                        NextbusSQLiteHelper.DIRECTIONS_STOPS.TABLE + ") AS B ON " +
                                        "A." + NextbusSQLiteHelper.STOPS.COLUMN_AUTO_ID + " = " +
                                        "B." + NextbusSQLiteHelper.DIRECTIONS_STOPS.COLUMN_STOP + ") AS C JOIN " +
                                        "(SELECT " +
                                        NextbusSQLiteHelper.GEOLOCATIONS.COLUMN_LAT + ", " +
                                        NextbusSQLiteHelper.GEOLOCATIONS.COLUMN_LON + ", " +
                                        NextbusSQLiteHelper.GEOLOCATIONS.COLUMN_STOP + " FROM " +
                                        NextbusSQLiteHelper.GEOLOCATIONS.TABLE + ") AS D ON " +
                                        "C." + NextbusSQLiteHelper.STOPS.COLUMN_AUTO_ID + " = " +
                                        "D." + NextbusSQLiteHelper.DIRECTIONS_STOPS.COLUMN_STOP,
                                        ArrayUtils.addAll(STOPS_COLUMNS, NextbusSQLiteHelper.GEOLOCATIONS.COLUMN_LAT, NextbusSQLiteHelper.GEOLOCATIONS.COLUMN_LON),
                                        NextbusSQLiteHelper.DIRECTIONS_STOPS.COLUMN_DIRECTION + " = " + directionAutoId,
                                        null, null, null, null);

        cursor.moveToFirst();
        while (!cursor.isAfterLast()) {
            stops.add(getStopFromCursor(cursor, agency));
            cursor.moveToNext();
        }
        cursor.close();

        return stops;
    }

    private List<Path> getPathsForRouteConfiguration(int routeConfigurationAutoId, Route route) {
        List<Path> paths = new ArrayList<Path>();

        Cursor cursor = mDatabase.query(NextbusSQLiteHelper.PATHS.TABLE,
                                        PATHS_COLUMNS,
                                        NextbusSQLiteHelper.PATHS.COLUMN_ROUTE_CONFIGURATION + " = " + routeConfigurationAutoId,
                                        null, null, null, null);

        cursor.moveToFirst();
        while (!cursor.isAfterLast()) {
            paths.add(getPathFromCursor(cursor, route));
            cursor.moveToNext();
        }
        cursor.close();

        return paths;
    }

    private Path getPathFromCursor(Cursor cursor, Route route) {
        int autoId = cursor.getInt(0);
        String copyright = cursor.getString(1);
        String pathId = cursor.getString(2);

        return new Path(route, pathId, getPointsForPath(autoId), copyright);
    }

    private List<Geolocation> getPointsForPath(int pathAutoId) {
        List<Geolocation> points = new ArrayList<Geolocation>();

        Cursor cursor = mDatabase.query(NextbusSQLiteHelper.POINTS.TABLE, POINTS_COLUMNS,
                                        NextbusSQLiteHelper.POINTS.COLUMN_PATH + " = " + pathAutoId,
                                        null, null, null, null);

        cursor.moveToFirst();
        while (!cursor.isAfterLast()) {
            points.add(getPointFromCursor(cursor));
            cursor.moveToNext();
        }
        cursor.close();

        return points;
    }

    private Geolocation getPointFromCursor(Cursor cursor) {
        double lat = cursor.getDouble(1);
        double lon = cursor.getDouble(2);

        return new Geolocation(lat, lon);
    }

    private RouteConfiguration.ServiceArea getServiceAreaForRouteConfiguration(int routeConfigurationAutoId) {
        Cursor cursor = mDatabase.query(NextbusSQLiteHelper.SERVICE_AREAS.TABLE, SERVICE_AREAS_COLUMNS,
                                        NextbusSQLiteHelper.SERVICE_AREAS.COLUMN_ROUTE_CONFIGURATION + " = " + routeConfigurationAutoId,
                                        null, null, null, null, "1");

        cursor.moveToFirst();

        double latMin = cursor.getDouble(1);
        double latMax = cursor.getDouble(2);
        double lonMin = cursor.getDouble(3);
        double lonMax = cursor.getDouble(4);

        cursor.close();

        return new RouteConfiguration.ServiceArea(latMin, latMax, lonMin, lonMax);
    }

    /**
     * Specifies which columns in a cursor correspond to each property of an {@link net.sf.nextbus.publicxmlfeed.domain.Geolocation}
     */
    protected static class GeolocationCursorColumns {
        private final int latColumn;
        private final int lonColumn;

        private GeolocationCursorColumns(int latColumn, int lonColumn) {
            this.latColumn = latColumn;
            this.lonColumn = lonColumn;
        }

        public static GeolocationCursorColumns fromCursor(Cursor cursor) {
            return new GeolocationCursorColumns(cursor.getColumnIndexOrThrow(NextbusSQLiteHelper.GEOLOCATIONS.COLUMN_LAT),
                                                cursor.getColumnIndexOrThrow(NextbusSQLiteHelper.GEOLOCATIONS.COLUMN_LON));
        }

        public int getLatColumn() {
            return latColumn;
        }

        public int getLonColumn() {
            return lonColumn;
        }
    }

    protected static Geolocation geolocationFromCursor(Cursor cursor, GeolocationCursorColumns geolocationCursorColumns) {
        return new Geolocation(cursor.getDouble(geolocationCursorColumns.getLatColumn()),
                               cursor.getDouble(geolocationCursorColumns.getLonColumn()));
    }

    /**
     * Specifies which columns in a cursor correspond to each property of an {@link net.sf.nextbus.publicxmlfeed.domain.Stop}
     */
    protected static class StopCursorColumns {
        private final int tagColumn;
        private final int titleColumn;
        private final int shortTitleColumn;
        private final int stopIdColumn;
        private final int copyrightColumn;
        private final int timestampColumn;

        private StopCursorColumns(int tagColumn, int titleColumn, int shortTitleColumn, int stopIdColumn, int copyrightColumn, int timestampColumn) {
            this.tagColumn = tagColumn;
            this.titleColumn = titleColumn;
            this.shortTitleColumn = shortTitleColumn;
            this.stopIdColumn = stopIdColumn;
            this.copyrightColumn = copyrightColumn;
            this.timestampColumn = timestampColumn;
        }

        public static StopCursorColumns fromCursor(Cursor cursor) {
            return new StopCursorColumns(cursor.getColumnIndexOrThrow(NextbusSQLiteHelper.STOPS.COLUMN_TAG),
                                         cursor.getColumnIndexOrThrow(NextbusSQLiteHelper.STOPS.COLUMN_TITLE),
                                         cursor.getColumnIndexOrThrow(NextbusSQLiteHelper.STOPS.COLUMN_SHORT_TITLE),
                                         cursor.getColumnIndexOrThrow(NextbusSQLiteHelper.STOPS.COLUMN_STOP_ID),
                                         cursor.getColumnIndexOrThrow(NextbusSQLiteHelper.STOPS.COLUMN_COPYRIGHT),
                                         cursor.getColumnIndexOrThrow(NextbusSQLiteHelper.STOPS.COLUMN_TIMESTAMP));
        }

        public int getTagColumn() {
            return tagColumn;
        }

        public int getTitleColumn() {
            return titleColumn;
        }

        public int getShortTitleColumn() {
            return shortTitleColumn;
        }

        public int getStopIdColumn() {
            return stopIdColumn;
        }

        public int getCopyrightColumn() {
            return copyrightColumn;
        }

        public int getTimestampColumn() {
            return timestampColumn;
        }
    }

    protected static Stop stopFromCursor(Cursor cursor, StopCursorColumns stopCursorColumns, Agency agency, Geolocation geolocation) {
        return new Stop(agency,
                        cursor.getString(stopCursorColumns.getTagColumn()),
                        cursor.getString(stopCursorColumns.getTitleColumn()),
                        cursor.getString(stopCursorColumns.getShortTitleColumn()),
                        cursor.getString(stopCursorColumns.getStopIdColumn()),
                        geolocation,
                        cursor.getString(stopCursorColumns.getCopyrightColumn()),
                        cursor.getLong(stopCursorColumns.getTimestampColumn()));
    }

}
