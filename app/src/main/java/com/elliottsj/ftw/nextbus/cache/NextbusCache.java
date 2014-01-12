package com.elliottsj.ftw.nextbus.cache;

import android.content.Context;
import android.database.sqlite.SQLiteDatabase;

import net.sf.nextbus.publicxmlfeed.domain.Agency;
import net.sf.nextbus.publicxmlfeed.domain.Route;
import net.sf.nextbus.publicxmlfeed.domain.RouteConfiguration;
import net.sf.nextbus.publicxmlfeed.domain.VehicleLocation;

import java.util.List;

/**
 * Simple implementation of {@link INextbusCache} using SQLite
 */
public class NextbusCache implements INextbusCache {

    private static final String TAG = "NextbusCache";

    private SQLiteDatabase mDatabase;
    private NextbusSQLiteHelper mDbHelper;

    private AgenciesHelper mAgenciesHelper;
    private RoutesHelper mRoutesHelper;
    private RouteConfigurationsHelper mRouteConfigurationsHelper;

    private boolean isOpen;

    /**
     * Constructs this cache in the given context.
     *
     * @param context the context where this cache will exist
     */
    public NextbusCache(Context context) {
        mDbHelper = new NextbusSQLiteHelper(context);

        isOpen = false;
    }

    /*
     * Opens this cache for reading and writing.
     * This should be called in an activity's onResume() method.
     */
    public void open() {
        mDatabase = mDbHelper.getWritableDatabase();

        mAgenciesHelper = new AgenciesHelper(mDatabase);
        mRoutesHelper = new RoutesHelper(mDatabase);
        mRouteConfigurationsHelper = new RouteConfigurationsHelper(mDatabase);

        isOpen = true;
    }

    /*
     * Closes this cache.
     * This should be called in an activity's onPause() method.
     */
    public void close() {
        mDbHelper.close();

        isOpen = false;
    }

    /**
     * Deletes all data stored in this cache.
     */
    public void delete() {
        mDbHelper.empty(mDatabase);
    }

    @Override
    public boolean isAgenciesCached() {
        return mAgenciesHelper.isAgenciesCached();
    }

    @Override
    public long getAgenciesAge() {
        return mAgenciesHelper.getAgenciesAge();
    }

    @Override
    public List<Agency> getAgencies() {
        return mAgenciesHelper.getAgencies();
    }

    @Override
    public Agency getAgency(String tag) {
        return mAgenciesHelper.getAgency(tag);
    }

    @Override
    public void putAgencies(List<Agency> agencies) {
        mAgenciesHelper.putAgencies(agencies);
    }

    @Override
    public boolean isRoutesCached(Agency agency) {
        if (!mAgenciesHelper.isAgencyCached(agency))
            return false;

        return mRoutesHelper.isRoutesCached(agency);
    }

    @Override
    public long getRoutesAge(Agency agency) {
        return mRoutesHelper.getRoutesAge(agency);
    }

    @Override
    public List<Route> getRoutes(Agency agency) {
        return mRoutesHelper.getRoutes(agency);
    }

    @Override
    public void putRoutes(List<Route> routes) {
        // Attempting to insert an empty list will do nothing
        if (!routes.isEmpty()) {
            Agency agency = routes.get(0).getAgency();
            if (!mAgenciesHelper.isAgencyCached(agency))
                mAgenciesHelper.putAgency(agency);

            mRoutesHelper.putRoutes(routes);
        }
    }

    @Override
    public boolean isRouteConfigurationCached(Route route) {
        return mRoutesHelper.isRouteCached(route) && mRouteConfigurationsHelper.isRouteConfigurationCached(route);

    }

    @Override
    public long getRouteConfigurationAge(Route route) {
        return mRouteConfigurationsHelper.getRouteConfigurationAge(route);
    }

    @Override
    public RouteConfiguration getRouteConfiguration(Route route) {
        return mRouteConfigurationsHelper.getRouteConfiguration(route);
    }

    @Override
    public void putRouteConfiguration(RouteConfiguration routeConfiguration) {
        if (isRouteConfigurationCached(routeConfiguration.getRoute()))
            mRouteConfigurationsHelper.deleteRouteConfiguration(routeConfiguration);

        // Cache the agency if it is not already cached
        Agency agency = routeConfiguration.getRoute().getAgency();
        if (!mAgenciesHelper.isAgencyCached(agency))
            mAgenciesHelper.putAgency(agency);

        // Cache the route if it is not already cached
        Route route = routeConfiguration.getRoute();
        if (!mRoutesHelper.isRouteCached(route))
            mRoutesHelper.putRoute(route);

        mRouteConfigurationsHelper.putRouteConfiguration(routeConfiguration);
    }

    @Override
    public boolean isVehicleLocationsCached(Route route) {
        return false;
    }

    @Override
    public long getLatestVehicleLocationCreationTime(Route route) {
        return 0;
    }

    @Override
    public List<VehicleLocation> getVehicleLocations(Route route) {
        return null;
    }

    @Override
    public void putVehicleLocations(List<VehicleLocation> vehicleLocations) {

    }

}
