package com.elliottsj.ftw.nextbus.cache;

import android.content.ContentValues;
import android.database.Cursor;
import android.database.sqlite.SQLiteDatabase;

import net.sf.nextbus.publicxmlfeed.domain.Agency;
import net.sf.nextbus.publicxmlfeed.domain.Direction;
import net.sf.nextbus.publicxmlfeed.domain.Geolocation;
import net.sf.nextbus.publicxmlfeed.domain.Path;
import net.sf.nextbus.publicxmlfeed.domain.Route;
import net.sf.nextbus.publicxmlfeed.domain.RouteConfiguration;
import net.sf.nextbus.publicxmlfeed.domain.Stop;
import net.sf.nextbus.publicxmlfeed.service.ServiceException;

import org.apache.commons.lang3.ArrayUtils;

import java.util.ArrayList;
import java.util.List;

public class RouteConfigurationsHelper extends CacheHelper {

    private static final String[] ROUTE_CONFIGURATIONS_COLUMNS =
            { NextbusSQLiteHelper.ROUTE_CONFIGURATIONS.COLUMN_AUTO_ID,
              NextbusSQLiteHelper.ROUTE_CONFIGURATIONS.COLUMN_COPYRIGHT,
              NextbusSQLiteHelper.ROUTE_CONFIGURATIONS.COLUMN_TIMESTAMP,
              NextbusSQLiteHelper.ROUTE_CONFIGURATIONS.COLUMN_ROUTE,
              NextbusSQLiteHelper.ROUTE_CONFIGURATIONS.COLUMN_UI_COLOR,
              NextbusSQLiteHelper.ROUTE_CONFIGURATIONS.COLUMN_UI_OPPOSITE_COLOR };

    private static final String[] SERVICE_AREAS_COLUMNS =
            { NextbusSQLiteHelper.SERVICE_AREAS.COLUMN_AUTO_ID,
              NextbusSQLiteHelper.SERVICE_AREAS.COLUMN_LAT_MIN,
              NextbusSQLiteHelper.SERVICE_AREAS.COLUMN_LAT_MAX,
              NextbusSQLiteHelper.SERVICE_AREAS.COLUMN_LON_MIN,
              NextbusSQLiteHelper.SERVICE_AREAS.COLUMN_LON_MAX,
              NextbusSQLiteHelper.SERVICE_AREAS.COLUMN_ROUTE_CONFIGURATION };

    private static final String[] STOPS_COLUMNS =
            { NextbusSQLiteHelper.STOPS.COLUMN_AUTO_ID,
              NextbusSQLiteHelper.STOPS.COLUMN_COPYRIGHT,
              NextbusSQLiteHelper.STOPS.COLUMN_TIMESTAMP,
              NextbusSQLiteHelper.STOPS.COLUMN_AGENCY,
              NextbusSQLiteHelper.STOPS.COLUMN_TAG,
              NextbusSQLiteHelper.STOPS.COLUMN_TITLE,
              NextbusSQLiteHelper.STOPS.COLUMN_SHORT_TITLE,
              NextbusSQLiteHelper.STOPS.COLUMN_STOP_ID};

    private static final String[] DIRECTIONS_COLUMNS =
            { NextbusSQLiteHelper.DIRECTIONS.COLUMN_AUTO_ID,
              NextbusSQLiteHelper.DIRECTIONS.COLUMN_COPYRIGHT,
              NextbusSQLiteHelper.DIRECTIONS.COLUMN_TIMESTAMP,
              NextbusSQLiteHelper.DIRECTIONS.COLUMN_ROUTE,
              NextbusSQLiteHelper.DIRECTIONS.COLUMN_TAG,
              NextbusSQLiteHelper.DIRECTIONS.COLUMN_TITLE,
              NextbusSQLiteHelper.DIRECTIONS.COLUMN_NAME,
              NextbusSQLiteHelper.DIRECTIONS.COLUMN_ROUTE_CONFIGURATION };

    private static final String[] PATHS_COLUMNS =
            { NextbusSQLiteHelper.PATHS.COLUMN_AUTO_ID,
              NextbusSQLiteHelper.PATHS.COLUMN_ROUTE,
              NextbusSQLiteHelper.PATHS.COLUMN_PATH_ID,
              NextbusSQLiteHelper.PATHS.COLUMN_ROUTE_CONFIGURATION };

    private static final String[] POINTS_COLUMNS =
            { NextbusSQLiteHelper.POINTS.COLUMN_AUTO_ID,
              NextbusSQLiteHelper.POINTS.COLUMN_LAT,
              NextbusSQLiteHelper.POINTS.COLUMN_LON,
              NextbusSQLiteHelper.POINTS.COLUMN_PATH };

    protected RouteConfigurationsHelper(SQLiteDatabase database) {
        super(database);
    }

    public boolean isRouteConfigurationCached(Route route) {
        Cursor cursor = mDatabase.query("(SELECT " +
                                        NextbusSQLiteHelper.ROUTE_CONFIGURATIONS.COLUMN_ROUTE + " FROM " +
                                        NextbusSQLiteHelper.ROUTE_CONFIGURATIONS.TABLE + ") AS A JOIN " +
                                        NextbusSQLiteHelper.ROUTES.TABLE + " AS B ON " +
                                        "A." + NextbusSQLiteHelper.ROUTE_CONFIGURATIONS.COLUMN_ROUTE + " = " +
                                        "B." + NextbusSQLiteHelper.ROUTES.COLUMN_AUTO_ID,
                                        new String[] { NextbusSQLiteHelper.ROUTES.COLUMN_TAG },
                                        NextbusSQLiteHelper.ROUTES.COLUMN_TAG + " = ?", new String[] { route.getTag() },
                                        null, null, null, "1");

        int count = cursor.getCount();
        cursor.close();

        return count != 0;
    }

    public long getRouteConfigurationAge(Route route) {
        Cursor cursor = getRouteConfigurationCursor(route);
        RouteConfiguration routeConfiguration = getRouteConfigurationFromCursor(cursor, route);
        cursor.close();
        return routeConfiguration.getObjectAge();
    }

    public RouteConfiguration getRouteConfiguration(Route route) {
        if (!isRouteConfigurationCached(route))
            throw new ServiceException("Route configuration is not cached for route: " + route);

        Cursor cursor = getRouteConfigurationCursor(route);

        cursor.moveToFirst();
        RouteConfiguration routeConfiguration = getRouteConfigurationFromCursor(cursor, route);

        cursor.close();

        return routeConfiguration;
    }

    public void putRouteConfiguration(RouteConfiguration routeConfiguration) {
        // Cache the route configuration
        ContentValues values = new ContentValues();
        values.put(NextbusSQLiteHelper.ROUTE_CONFIGURATIONS.COLUMN_COPYRIGHT, routeConfiguration.getCopyrightNotice());
        values.put(NextbusSQLiteHelper.ROUTE_CONFIGURATIONS.COLUMN_TIMESTAMP, routeConfiguration.getObjectTimestamp());
        values.put(NextbusSQLiteHelper.ROUTE_CONFIGURATIONS.COLUMN_ROUTE, RoutesHelper.getRouteAutoId(mDatabase, routeConfiguration.getRoute()));
        values.put(NextbusSQLiteHelper.ROUTE_CONFIGURATIONS.COLUMN_UI_COLOR, routeConfiguration.getUiColor().getHexColor());
        values.put(NextbusSQLiteHelper.ROUTE_CONFIGURATIONS.COLUMN_UI_OPPOSITE_COLOR, routeConfiguration.getUiOppositeColor().getHexColor());

        long routeConfigurationAutoId = mDatabase.insert(NextbusSQLiteHelper.ROUTE_CONFIGURATIONS.TABLE, null, values);

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
        int routeConfigurationAutoId = getRouteConfigurationAutoId(routeConfiguration);

        // Delete the cached paths
        mDatabase.execSQL("DELETE FROM " +
                          NextbusSQLiteHelper.PATHS.TABLE + " WHERE " +
                          NextbusSQLiteHelper.PATHS.COLUMN_ROUTE_CONFIGURATION + " = " +
                          routeConfigurationAutoId);

        // Delete the cached stops
        mDatabase.execSQL("DELETE FROM " +
                          NextbusSQLiteHelper.STOPS.TABLE + " WHERE " +
                          NextbusSQLiteHelper.STOPS.COLUMN_AUTO_ID + " IN " +
                          "(SELECT " +
                          NextbusSQLiteHelper.ROUTE_CONFIGURATIONS_STOPS.COLUMN_STOP + " FROM " +
                          NextbusSQLiteHelper.ROUTE_CONFIGURATIONS_STOPS.TABLE + " WHERE " +
                          NextbusSQLiteHelper.ROUTE_CONFIGURATIONS_STOPS.COLUMN_ROUTE_CONFIGURATION + " = " +
                          routeConfigurationAutoId +
                          ")");

        // Delete the cached directions
        mDatabase.execSQL("DELETE FROM " +
                          NextbusSQLiteHelper.DIRECTIONS.TABLE + " WHERE " +
                          NextbusSQLiteHelper.DIRECTIONS.COLUMN_ROUTE_CONFIGURATION + " = " +
                          routeConfigurationAutoId +
                          ")");

        // Delete the cached service area
        mDatabase.execSQL("DELETE FROM " +
                          NextbusSQLiteHelper.SERVICE_AREAS.TABLE + " WHERE " +
                          NextbusSQLiteHelper.SERVICE_AREAS.COLUMN_ROUTE_CONFIGURATION + " = " +
                          routeConfigurationAutoId +
                          ")");

        // Delete the cached route configuration
        mDatabase.execSQL("DELETE FROM " + NextbusSQLiteHelper.ROUTE_CONFIGURATIONS.TABLE + " WHERE " +
                          NextbusSQLiteHelper.ROUTE_CONFIGURATIONS.COLUMN_AUTO_ID + " = " + routeConfigurationAutoId);
    }

    private void putServiceArea(RouteConfiguration.ServiceArea serviceArea, long routeConfigAutoId) {
        ContentValues values = new ContentValues();
        values.put(NextbusSQLiteHelper.SERVICE_AREAS.COLUMN_LAT_MIN, serviceArea.getLatMin());
        values.put(NextbusSQLiteHelper.SERVICE_AREAS.COLUMN_LAT_MAX, serviceArea.getLatMax());
        values.put(NextbusSQLiteHelper.SERVICE_AREAS.COLUMN_LON_MIN, serviceArea.getLongMin());
        values.put(NextbusSQLiteHelper.SERVICE_AREAS.COLUMN_LON_MAX, serviceArea.getLongMax());
        values.put(NextbusSQLiteHelper.SERVICE_AREAS.COLUMN_ROUTE_CONFIGURATION, routeConfigAutoId);

        mDatabase.insert(NextbusSQLiteHelper.SERVICE_AREAS.TABLE, null, values);
    }

    private void putStop(Stop stop, long routeConfigurationAutoId) {
        // Insert into stops table
        ContentValues values = new ContentValues();
        values.put(NextbusSQLiteHelper.STOPS.COLUMN_COPYRIGHT, stop.getCopyrightNotice());
        values.put(NextbusSQLiteHelper.STOPS.COLUMN_TIMESTAMP, stop.getObjectTimestamp());
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
        values.put(NextbusSQLiteHelper.DIRECTIONS.COLUMN_COPYRIGHT, direction.getCopyrightNotice());
        values.put(NextbusSQLiteHelper.DIRECTIONS.COLUMN_TIMESTAMP, direction.getObjectTimestamp());
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
                               NextbusSQLiteHelper.STOPS.COLUMN_TAG + " = " +
                               stop.getTag() + " LIMIT 1)" +
                               ")",
                               null);
        }
    }

    private void putPath(Path path, long routeConfigurationAutoId) {
        ContentValues values = new ContentValues();
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
     * @return a cursor pointing at the row of the route configuration of the given route
     */
    private Cursor getRouteConfigurationCursor(Route route) {
        Cursor cursor = mDatabase.query(NextbusSQLiteHelper.ROUTE_CONFIGURATIONS.TABLE + " AS A JOIN " +
                                        NextbusSQLiteHelper.ROUTES.TABLE + " AS B ON " +
                                        "A." + NextbusSQLiteHelper.ROUTE_CONFIGURATIONS.COLUMN_ROUTE + " = " +
                                        "B." + NextbusSQLiteHelper.ROUTES.COLUMN_AUTO_ID,
                                        stringsWithPrefix("A.", ROUTE_CONFIGURATIONS_COLUMNS),
                                        NextbusSQLiteHelper.ROUTES.COLUMN_TAG + " = ?", new String[] { route.getTag() },
                                        null, null, null, "1");

        cursor.moveToFirst();
        return cursor;
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

    /**
     * @param routeConfiguration a route configuration
     * @return the primary key AUTO_ID of the given route
     */
    private int getRouteConfigurationAutoId(RouteConfiguration routeConfiguration) {
        Cursor cursor = getRouteConfigurationCursor(routeConfiguration.getRoute());
        int autoId = cursor.getInt(0);
        cursor.close();
        return autoId;
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
        String pathId = cursor.getString(2);

        return new Path(route, pathId, getPointsForPath(autoId));
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

}
