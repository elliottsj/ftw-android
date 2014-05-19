package com.elliottsj.ftw.provider;

import android.content.Context;
import android.util.Log;

import com.elliottsj.ftw.utilities.AndroidNextbusService;
import com.j256.ormlite.android.apptools.OpenHelperManager;
import com.j256.ormlite.dao.Dao;
import com.j256.ormlite.misc.TransactionManager;
import com.j256.ormlite.stmt.QueryBuilder;

import net.sf.nextbus.publicxmlfeed.domain.Agency;
import net.sf.nextbus.publicxmlfeed.domain.Direction;
import net.sf.nextbus.publicxmlfeed.domain.DirectionStop;
import net.sf.nextbus.publicxmlfeed.domain.PredictionGroup;
import net.sf.nextbus.publicxmlfeed.domain.Route;
import net.sf.nextbus.publicxmlfeed.domain.Stop;
import net.sf.nextbus.publicxmlfeed.impl.NextbusService;

import java.sql.SQLException;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.concurrent.Callable;

public class NextbusQueryHelper {

    private static final String TAG = NextbusQueryHelper.class.getSimpleName();

    private Context mContext;
    private NextbusService mNextbusService;
    private NextbusSQLiteHelper mDbHelper;
    private NextbusQueryBuilderFactory mQbFactory;

    public NextbusQueryHelper(Context context) {
        mContext = context;
        mNextbusService = new AndroidNextbusService();
    }

    public NextbusSQLiteHelper getHelper() {
        if (mDbHelper == null)
            mDbHelper = OpenHelperManager.getHelper(mContext, NextbusSQLiteHelper.class);
        return mDbHelper;
    }

    public NextbusQueryBuilderFactory getQbFactory() {
        if (mQbFactory == null)
            mQbFactory = new NextbusQueryBuilderFactory(getHelper());
        return mQbFactory;
    }

    public void fetchAgencies() throws SQLException {
        final Dao<Agency, Integer> agenciesDao = getHelper().getAgenciesDao();
        final List<Agency> networkAgencies = mNextbusService.getAgencies();
        TransactionManager.callInTransaction(getHelper().getConnectionSource(), new Callable<Void>() {
            @Override
            public Void call() throws Exception {
                for (Agency agency : networkAgencies)
                    agenciesDao.createIfNotExists(agency);
                return null;
            }
        });
    }

    public void fetchRoutes(String agencyTag) throws SQLException {
        if (getQbFactory().routesQb(agencyTag).countOf() == 0) {
            // No routes found for agency; fetch them from the network and store in the database

            // Fetch agencies if necessary
            if (getQbFactory().agenciesQb(agencyTag).countOf() == 0) {
                fetchAgencies();
            }

            final Dao<Route, Integer> routesDao = getHelper().getRoutesDao();
            final Agency agency = getHelper().getAgenciesDao().queryBuilder()
                    .where()
                    .eq(Agency.FIELD_TAG, agencyTag)
                    .queryForFirst();
            final List<Route> networkRoutes = mNextbusService.getRoutes(agency);
            TransactionManager.callInTransaction(getHelper().getConnectionSource(), new Callable<Void>() {
                @Override
                public Void call() throws Exception {
                    for (Route route : networkRoutes) {
                        route.setAgency(agency);
                        routesDao.create(route);
                    }
                    return null;
                }
            });
        }
    }

    /**
     * Fetch route directions and associated stops from the network and store them in the database
     *
     * @param agencyTag unique agency tag
     * @param routeTag unique route tag
     * @throws SQLException
     */
    public void fetchDirections(String agencyTag, String routeTag, String directionTag) throws SQLException {
        if (getQbFactory().directionsQb(agencyTag, routeTag).countOf() == 0 ||
            getQbFactory().stopsQb(agencyTag, routeTag, directionTag).countOf() == 0) {
            // No directions found for route; fetch them from the network and store them in the database

            // Fetch routes if necessary
            if (getQbFactory().routesQb(agencyTag, routeTag).countOf() == 0) {
                fetchRoutes(agencyTag);
            }

            final Dao<Direction, Integer> directionsDao = getHelper().getDirectionsDao();
            final Dao<Stop, Integer> stopsDao = getHelper().getStopsDao();
            final Dao<DirectionStop, Integer> directionStopsDao = getHelper().getDirectionStopsDao();

            QueryBuilder<Agency, Integer> agenciesQb = getHelper().getAgenciesDao().queryBuilder();
            agenciesQb.where().eq(Agency.FIELD_TAG, agencyTag);
            final Agency agency = agenciesQb.queryForFirst();

            QueryBuilder<Route, Integer> routesQb = getHelper().getRoutesDao().queryBuilder();
            routesQb.join(agenciesQb).where().eq(Route.FIELD_TAG, routeTag);
            final Route route = routesQb.queryForFirst();
            route.setAgency(agency);

            // Fetch directions from network
            final List<Direction> networkDirections = mNextbusService.getRouteConfiguration(route).getDirections();
            TransactionManager.callInTransaction(getHelper().getConnectionSource(), new Callable<Void>() {
                @Override
                public Void call() throws Exception {
                    for (Direction direction : networkDirections) {
                        // Store the direction in the database
                        direction.setRoute(route);
                        directionsDao.create(direction);

                        // Store each stop in the database
                        for (Stop stop : direction.getStops()) {
                            stop.setAgency(agency);
                            stopsDao.createIfNotExists(stop);
                            DirectionStop directionStop = new DirectionStop(direction, stop);
                            directionStopsDao.create(directionStop);
                        }
                    }
                    return null;
                }
            });
        }
    }

    /**
     * Get a list of predictions for the specified stops
     *
     * @param agencyTag unique agency tag
     * @param stopTagMap a map of (route tag -> (direction tag -> stop tag))
     * @return a list of PredictionGroups
     */
    public List<PredictionGroup> loadPredictions(String agencyTag, Map<String, List<String>> stopTagMap) {
        Log.i(TAG, "Loading predictions; stopTagMap: " + stopTagMap.toString());
        try {
            Map<Route, List<Stop>> stops = new HashMap<Route, List<Stop>>(stopTagMap.size());
            for (Map.Entry<String, List<String>> entry : stopTagMap.entrySet()) {
                String routeTag = entry.getKey();
                List<String> stopTags = entry.getValue();

                // Make sure stops are downloaded into the database
                fetchDirections(agencyTag, routeTag, null);

                Route route = getQbFactory().routesQb(agencyTag, routeTag).queryForFirst();
                getHelper().getAgenciesDao().refresh(route.getAgency());
                List<Stop> routeStops = getQbFactory().stopsQb(agencyTag, routeTag, null)
                        .where()
                        .in(Stop.FIELD_TAG, stopTags)
                        .query();

                // Assign the agency for each stop
                for (Stop stop : routeStops)
                    stop.setAgency(route.getAgency());

                stops.put(route, routeStops);
            }
            return mNextbusService.getPredictions(stops);
        } catch (SQLException e) {
            throw new RuntimeException("Failed to load Nextbus objects while fetching predictions", e);
        }
    }

}
