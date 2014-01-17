package com.elliottsj.ftw.nextbus;

import com.elliottsj.ftw.nextbus.cache.INextbusCache;

import net.sf.nextbus.publicxmlfeed.domain.*;
import net.sf.nextbus.publicxmlfeed.service.INextbusService;
import net.sf.nextbus.publicxmlfeed.service.ServiceException;

import java.util.*;

/**
 * An implementation of a cached NextBus service adapter.
 */
public class CachedNextbusServiceAdapter implements ICachedNextbusService {

    private INextbusService mBacking;
    private INextbusCache mCache;
    private Callbacks mCallbacks;

    private static final long AGE_LIMIT_24HOURS = 24*60*60*1000;

    /** Cache age limit for static data (Route, RouteConfig, Schedule) */
    private long staticDataAgeLimit = AGE_LIMIT_24HOURS;

    public CachedNextbusServiceAdapter(INextbusService backing, INextbusCache cache, Callbacks callbacks) {
        this.mBacking = backing;
        this.mCache = cache;
        this.mCallbacks = callbacks;
    }

    @Override
    public List<Stop> getAllStops(Agency agency) {
        final List<Stop> stops = new ArrayList<Stop>();
        for (final Route route : getRoutes(agency)) {
            new FetchRouteConfigurationTask(new FetchRouteConfigurationTask.Callbacks() {
                @Override
                public void onRouteConfigurationFetched(RouteConfiguration routeConfiguration) {
                    stops.addAll(routeConfiguration.getStops());
                    mCallbacks.onStopsCached(route);
                }
            }).execute(route);
        }
        return stops;
    }

    @Override
    public List<Agency> getAgencies() throws ServiceException {
        if (!mCache.isAgenciesCached() || mCache.getAgenciesAge() > staticDataAgeLimit)
            return cacheAgenciesFromNetwork();
        return mCache.getAgencies();
    }

    @Override
    public Agency getAgency(String tag) throws ServiceException {
        if (!mCache.isAgenciesCached() || mCache.getAgenciesAge() > staticDataAgeLimit)
            cacheAgenciesFromNetwork();
        return mCache.getAgency(tag);
    }

    private List<Agency> cacheAgenciesFromNetwork() {
        List<Agency> backingResult = mBacking.getAgencies();
        mCache.putAgencies(backingResult);
        return backingResult;
    }

    @Override
    public List<Route> getRoutes(Agency agency) throws ServiceException {
        if (!mCache.isRoutesCached(agency) || mCache.getRoutesAge(agency) > staticDataAgeLimit)
            return cacheRoutesFromNetwork(agency);
        return mCache.getRoutes(agency);
    }

    private List<Route> cacheRoutesFromNetwork(Agency agency) {
        List<Route> backingResult = mBacking.getRoutes(agency);
        mCache.putRoutes(backingResult);
        return backingResult;
    }

    @Override
    public RouteConfiguration getRouteConfiguration(Route route) throws ServiceException {
        if (!mCache.isRouteConfigurationCached(route) || mCache.getRouteConfigurationAge(route) > staticDataAgeLimit)
            return cacheRouteConfigurationFromNetwork(route);
        return mCache.getRouteConfiguration(route);
    }

    private RouteConfiguration cacheRouteConfigurationFromNetwork(Route route) {
        RouteConfiguration routeConfiguration = mBacking.getRouteConfiguration(route);
        mCache.putRouteConfiguration(routeConfiguration);
        return routeConfiguration;
    }

    /**
     * Gets only the vehicle locations that have been updated since {@code timeOfLastUpdate}.
     *
     * @param route
     * @param timeOfLastUpdate
     * @return
     * @throws ServiceException
     */
    @Override
    public List<VehicleLocation> getVehicleLocations(Route route, long timeOfLastUpdate) throws ServiceException {
        List<VehicleLocation> cachedVehicleLocations = cacheVehicleLocationsFromNetwork(route);
        List<VehicleLocation> vehicleLocationsSinceTime = new ArrayList<VehicleLocation>(cachedVehicleLocations.size());

        for (VehicleLocation vehicleLocation : cachedVehicleLocations)
            if (vehicleLocation.getTimestamp() > timeOfLastUpdate)
                vehicleLocationsSinceTime.add(vehicleLocation);

        return vehicleLocationsSinceTime;
    }

    @Override
    public List<VehicleLocation> getVehicleLocations(Route route) throws ServiceException {
        return cacheVehicleLocationsFromNetwork(route);
    }

    /**
     * Fetches vehicle locations from the network that have not been cached and caches them.
     *
     * @param route
     * @return
     */
    private List<VehicleLocation> cacheVehicleLocationsFromNetwork(Route route) {
        List<VehicleLocation> cachedVehicleLocations;
        if (mCache.isVehicleLocationsCached(route))
            cachedVehicleLocations = mCache.getVehicleLocations(route);
        else
            cachedVehicleLocations = new ArrayList<VehicleLocation>();

        // Find the epoch time milliseconds when the cached locations were last updated
        long timeOfLastUpdate = mCache.getLatestVehicleLocationCreationTime(route);

        List<VehicleLocation> networkVehicleLocations = mBacking.getVehicleLocations(route, timeOfLastUpdate);

        // Replace old vehicle locations with the updated ones
        // This works since VehicleLocation.equals() is composite only of the vehicle id, parent route, and direction
        cachedVehicleLocations.removeAll(networkVehicleLocations);
        cachedVehicleLocations.addAll(networkVehicleLocations);

        mCache.putVehicleLocations(cachedVehicleLocations);

        return cachedVehicleLocations;
    }

    @Override
    public List<PredictionGroup> getPredictions(Stop s) throws ServiceException {
        return mBacking.getPredictions(s);
    }

    @Override
    public PredictionGroup getPredictions(Route r, Stop s) throws ServiceException {
        return mBacking.getPredictions(r, s);
    }

    @Override
    public List<PredictionGroup> getPredictions(Route route, Collection<Stop> stops) throws ServiceException {
        return mBacking.getPredictions(route, stops);
    }

    @Override
    public List<PredictionGroup> getPredictions(Map<Route, Stop> stops) throws ServiceException {
        return mBacking.getPredictions(stops);
    }

    @Override
    public List<Schedule> getSchedule(Route route) throws ServiceException {
        return mBacking.getSchedule(route);
    }

    public static interface Callbacks {

        /**
         * Called when the CachedNextbusServiceAdapter successfully caches stops for {@code route}
         *
         * @param route the route for which stops were cached
         */
        public abstract void onStopsCached(Route route);

    }

}
