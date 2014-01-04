package com.elliottsj.ftw.nextbus.cache;

import android.annotation.SuppressLint;
import android.content.Context;
import android.util.Log;

import net.sf.nextbus.publicxmlfeed.domain.Agency;
import net.sf.nextbus.publicxmlfeed.domain.Route;
import net.sf.nextbus.publicxmlfeed.domain.RouteConfiguration;
import net.sf.nextbus.publicxmlfeed.domain.VehicleLocation;
import net.sf.nextbus.publicxmlfeed.service.ServiceException;

import java.io.*;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

/**
 * Simple implementation of {@link INextbusCache} using SQLite
 */
public class NextbusCache implements INextbusCache {

    private static final String TAG = "NextbusCache";

    private AgenciesDataSource mAgenciesDataSource;

    private enum Command {
        AGENCIES,
        ROUTES,
        ROUTE_CONFIGURATIONS,
        VEHICLE_LOCATIONS
    }

    private enum EntryType {
        CREATE_TIME,
        DATA
    }

    private File file;
    private Map<Command, Map<EntryType, Serializable>> data;

    /**
     * Constructs this cache in the given context.
     *
     * @param context the context where this cache will exist
     */
    public NextbusCache(Context context) {
        mAgenciesDataSource = new AgenciesDataSource(context);
    }

    /*
     * Opens this cache for reading and writing.
     * This should be called in an activity's onResume() method.
     */
    public void open() {
        mAgenciesDataSource.open();
    }

    /*
     * Closes this cache.
     * This should be called in an activity's onPause() method.
     */
    public void close() {
        mAgenciesDataSource.close();
    }

    @Override
    public boolean isAgenciesCached() {
        return !mAgenciesDataSource.isEmpty();
    }

    @Override
    public long getAgenciesAge() {
        if (!isAgenciesCached())
            throw new ServiceException("Agencies are not cached");

        return mAgenciesDataSource.getAnAgency().getObjectAge();
    }

    /**
     * Gets the agencies in this cache in a map indexed by agency id.
     *
     * @return the agencies in this cache
     */
    @Override
    public List<Agency> getAgencies() {
        if (!isAgenciesCached())
            throw new ServiceException("Agencies are not cached");

        return mAgenciesDataSource.getAllAgencies();
    }

    @Override
    public void putAgencies(List<Agency> agencies) {
        mAgenciesDataSource.putAgencies(agencies);
    }

    @Override
    public boolean isRoutesCached(Agency agency) {
        return false;
    }

    @Override
    public long getRoutesAge(Agency agency) {
        return 0;
    }

    @Override
    public List<Route> getRoutes(Agency agency) {
        return null;
    }

    @Override
    public void putRoutes(List<Route> routes) {

    }

    @Override
    public boolean isRouteConfigurationCached(Route route) {
        return false;
    }

    @Override
    public long getRouteConfigurationAge(Route route) {
        return 0;
    }

    @Override
    public RouteConfiguration getRouteConfiguration(Route route) {
        return null;
    }

    @Override
    public void putRouteConfiguration(RouteConfiguration routeConfiguration) {

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
