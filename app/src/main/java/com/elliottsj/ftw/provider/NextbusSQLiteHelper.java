package com.elliottsj.ftw.provider;

import android.content.Context;
import android.database.sqlite.SQLiteDatabase;
import android.util.Log;

import com.elliottsj.ftw.provider.model.SavedStop;
import com.j256.ormlite.android.apptools.OrmLiteSqliteOpenHelper;
import com.j256.ormlite.dao.Dao;
import com.j256.ormlite.dao.LruObjectCache;
import com.j256.ormlite.dao.ObjectCache;
import com.j256.ormlite.support.ConnectionSource;
import com.j256.ormlite.table.TableUtils;

import net.sf.nextbus.publicxmlfeed.domain.Agency;
import net.sf.nextbus.publicxmlfeed.domain.Direction;
import net.sf.nextbus.publicxmlfeed.domain.DirectionStop;
import net.sf.nextbus.publicxmlfeed.domain.Geolocation;
import net.sf.nextbus.publicxmlfeed.domain.Route;
import net.sf.nextbus.publicxmlfeed.domain.Stop;

import java.sql.SQLException;

class NextbusSQLiteHelper extends OrmLiteSqliteOpenHelper {

    private static final String TAG = NextbusSQLiteHelper.class.getName();

    protected static final int DATABASE_VERSION = 1;
    protected static final String DATABASE_NAME = "nextbus.db";

    private ObjectCache objectCache = null;

    private Dao<Agency, Integer> agenciesDao = null;
    private Dao<Route, Integer> routesDao = null;
    private Dao<Direction, Integer> directionsDao = null;
    private Dao<Stop, Integer> stopsDao = null;
    private Dao<DirectionStop, Integer> directionStopsDao = null;
    private Dao<Geolocation, Integer> geolocationsDao = null;
    private Dao<SavedStop, Integer> savedStopsDao = null;

    public NextbusSQLiteHelper(Context context) {
        super(context, DATABASE_NAME, null, DATABASE_VERSION);
    }

    @Override
    public void onCreate(SQLiteDatabase database, ConnectionSource connectionSource) {
        try {
            Log.i(TAG, "Creating Nextbus tables...");
            TableUtils.createTable(connectionSource, Agency.class);
            TableUtils.createTable(connectionSource, Route.class);
            TableUtils.createTable(connectionSource, Direction.class);
            TableUtils.createTable(connectionSource, Stop.class);
            TableUtils.createTable(connectionSource, DirectionStop.class);
            TableUtils.createTable(connectionSource, Geolocation.class);
            TableUtils.createTable(connectionSource, SavedStop.class);
        } catch (SQLException e) {
            Log.e(TAG, "Can't create database", e);
            throw new RuntimeException(e);
        }
    }

    @Override
    public void onUpgrade(SQLiteDatabase database, ConnectionSource connectionSource, int oldVersion, int newVersion) {
        try {
            Log.i(TAG, "Upgrading Nextbus tables...");
            TableUtils.dropTable(connectionSource, SavedStop.class, false);
            TableUtils.dropTable(connectionSource, Geolocation.class, false);
            TableUtils.dropTable(connectionSource, DirectionStop.class, false);
            TableUtils.dropTable(connectionSource, Stop.class, false);
            TableUtils.dropTable(connectionSource, Direction.class, false);
            TableUtils.dropTable(connectionSource, Route.class, false);
            TableUtils.dropTable(connectionSource, Agency.class, false);

            onCreate(database, connectionSource);
        } catch (SQLException e) {
            Log.e(TAG, "Can't drop databases", e);
            throw new RuntimeException(e);
        }
    }

    /**
     * Close the database connections and clear any cached DAOs.
     */
    @Override
    public void close() {
        super.close();
        agenciesDao = null;
        routesDao = null;
        stopsDao = null;
        geolocationsDao = null;
        savedStopsDao = null;
    }

    private ObjectCache getObjectCache() {
        if (objectCache == null)
            objectCache = new LruObjectCache(500);
        return objectCache;
    }

    public Dao<Agency, Integer> getAgenciesDao() throws SQLException {
        if (agenciesDao == null) {
            agenciesDao = getDao(Agency.class);
            agenciesDao.setObjectCache(objectCache);
        }
        return agenciesDao;
    }

    public Dao<Route, Integer> getRoutesDao() throws SQLException {
        if (routesDao == null) {
            routesDao = getDao(Route.class);
            routesDao.setObjectCache(objectCache);
        }
        return routesDao;
    }

    public Dao<Direction, Integer> getDirectionsDao() throws SQLException {
        if (directionsDao == null) {
            directionsDao = getDao(Direction.class);
            directionsDao.setObjectCache(objectCache);
        }
        return directionsDao;
    }

    public Dao<Stop, Integer> getStopsDao() throws SQLException {
        if (stopsDao == null) {
            stopsDao = getDao(Stop.class);
            stopsDao.setObjectCache(objectCache);
        }
        return stopsDao;
    }

    public Dao<DirectionStop, Integer> getDirectionStopsDao() throws SQLException {
        if (directionStopsDao == null) {
            directionStopsDao = getDao(DirectionStop.class);
            directionStopsDao.setObjectCache(objectCache);
        }
        return directionStopsDao;
    }

    public Dao<Geolocation, Integer> getGeolocationsDao() throws SQLException {
        if (geolocationsDao == null) {
            geolocationsDao = getDao(Geolocation.class);
            geolocationsDao.setObjectCache(objectCache);
        }
        return geolocationsDao;
    }

    public Dao<SavedStop, Integer> getSavedStopsDao() throws SQLException {
        if (savedStopsDao == null)
            savedStopsDao = getDao(SavedStop.class);
        return savedStopsDao;
    }

}
