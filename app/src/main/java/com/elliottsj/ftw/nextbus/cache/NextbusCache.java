package com.elliottsj.ftw.nextbus.cache;

import android.content.ContentProviderClient;
import android.content.Context;
import android.database.sqlite.SQLiteDatabase;

import com.elliottsj.ftw.nextbus.cache.helpers.AgenciesHelper;
import com.elliottsj.ftw.nextbus.cache.helpers.NextbusSQLiteHelper;
import com.elliottsj.ftw.nextbus.cache.helpers.RouteConfigurationsHelper;
import com.elliottsj.ftw.nextbus.cache.helpers.RoutesHelper;

import net.sf.nextbus.publicxmlfeed.domain.Agency;
import net.sf.nextbus.publicxmlfeed.domain.Route;
import net.sf.nextbus.publicxmlfeed.domain.RouteConfiguration;
import net.sf.nextbus.publicxmlfeed.domain.Stop;
import net.sf.nextbus.publicxmlfeed.domain.VehicleLocation;

import java.util.List;

/**
 * Simple implementation of {@link INextbusCache} using SQLite
 */
public class NextbusCache implements INextbusCache {

    private static final String TAG = "NextbusCache";

    private SQLiteDatabase mDatabase;
    private NextbusSQLiteHelper mDbHelper;
    private ContentProviderClient mProviderClient;

    private AgenciesHelper mAgenciesHelper;
    private RoutesHelper mRoutesHelper;
    private RouteConfigurationsHelper mRouteConfigurationsHelper;

    /**
     * Constructs this cache in the given context.
     *
     * @param context the context where this cache will exist
     */
    public NextbusCache(Context context, ContentProviderClient providerClient) {
        mDbHelper = new NextbusSQLiteHelper(context);
        mProviderClient = providerClient;
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
    }

    /*
     * Closes this cache.
     * This should be called in an activity's onPause() method.
     */
    public void close() {
        mDbHelper.close();
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
    public List<Agency> putAgencies(List<Agency> agencies) {
        mAgenciesHelper.putAgencies(agencies);
        return agencies;
    }

    @Override
    public boolean isRoutesCached(Agency agency) {
        return mAgenciesHelper.isAgencyCached(agency) && mRoutesHelper.isRoutesCached(agency);
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
    public List<Route> putRoutes(List<Route> routes) {
        // Attempting to insert an empty list will do nothing
        if (!routes.isEmpty())
            mRoutesHelper.putRoutes(routes);
        return routes;
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
    public RouteConfiguration putRouteConfiguration(RouteConfiguration routeConfiguration) {
        // Delete the existing route configuration if it exists
        mRouteConfigurationsHelper.deleteRouteConfiguration(routeConfiguration);mDatabase.be

        // Cache the agency if it is not already cached
        Agency agency = routeConfiguration.getRoute().getAgency();
        if (!mAgenciesHelper.isAgencyCached(agency))
            mAgenciesHelper.putAgency(agency);

        // Cache the route if it is not already cached
        Route route = routeConfiguration.getRoute();
        if (!mRoutesHelper.isRouteCached(route))
            mRoutesHelper.putRoute(route);

        mRouteConfigurationsHelper.putRouteConfiguration(routeConfiguration);

        return routeConfiguration;
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
    public List<VehicleLocation> putVehicleLocations(List<VehicleLocation> vehicleLocations) {
        return vehicleLocations;
    }

    @Override
    public List<Route> getAllRouteConfigurationRoutes(Agency agency) {
        return mRouteConfigurationsHelper.getAllRouteConfigurationRoutes(agency);
    }

    @Override
    public List<Stop> getAllStops(Agency agency) {
        return mRouteConfigurationsHelper.getAllStops(agency);
    }

}
