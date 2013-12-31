package com.elliottsj.ttc_ftw.nextbus;

import net.sf.nextbus.publicxmlfeed.domain.*;
import net.sf.nextbus.publicxmlfeed.service.INextbusService;
import net.sf.nextbus.publicxmlfeed.service.ServiceException;

import java.util.ArrayList;
import java.util.Collection;
import java.util.List;
import java.util.Map;

/**
 * An implementation of a cached NextBus service adapter.
 */
public class CachedNextbusServiceAdapter implements ICachedNextbusService {

    private INextbusService backing;
    private INextbusCacheStore cache;

    private static final long AGE_LIMIT_24HOURS = 24*60*60*1000;

    /** Cache age limit for static data (Route, RouteConfig, Schedule) */
    private long staticDataAgeLimit = AGE_LIMIT_24HOURS;

    public CachedNextbusServiceAdapter(INextbusService backing, INextbusCacheStore cache) {
        this.backing = backing;
        this.cache = cache;
    }

    @Override
    public List<Stop> getAllStops(Agency agency) {
        return null;
    }

    @Override
    public List<Agency> getAgencies() throws ServiceException {
        if (!cache.isAgenciesCached() || cache.getAgenciesAge() > staticDataAgeLimit)
            return cacheAgenciesFromNetwork();
        return new ArrayList<Agency>(cache.getAgencies().values());
    }

    @Override
    public Agency getAgency(String id) throws ServiceException {
        if (!cache.isAgenciesCached() || cache.getAgenciesAge() > staticDataAgeLimit)
            cacheAgenciesFromNetwork();
        return cache.getAgencies().get(id);
    }

    private List<Agency> cacheAgenciesFromNetwork() {
        List<Agency> backingResult = backing.getAgencies();
        cache.putAgencies(backingResult);
        return backingResult;
    }

    @Override
    public List<Route> getRoutes(Agency agency) throws ServiceException {
        if (!cache.isAgenciesCached() || cache.getAgenciesAge() > staticDataAgeLimit)
            return cacheRoutesFromNetwork(agency);
        return cache.getRoutes(agency);
    }

    private List<Route> cacheRoutesFromNetwork(Agency agency) {
        List<Route> backingResult = backing.getRoutes(agency);
        cache.putRoutes(backingResult);
        return backingResult;
    }

    @Override
    public RouteConfiguration getRouteConfiguration(Route route) throws ServiceException {
        if (!cache.isRouteConfigurationCached(route) || cache.getRouteConfigurationAge(route) > staticDataAgeLimit)
            return cacheRouteConfigurationFromNetwork(route);
        return cache.getRouteConfiguration(route);
    }

    private RouteConfiguration cacheRouteConfigurationFromNetwork(Route route) {
        RouteConfiguration routeConfiguration = backing.getRouteConfiguration(route);
        cache.putRouteConfiguration(routeConfiguration);
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
            if (vehicleLocation.getObjectTimestamp() > timeOfLastUpdate)
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
        if (cache.isVehicleLocationsCached(route))
            cachedVehicleLocations = cache.getVehicleLocations(route);
        else
            cachedVehicleLocations = new ArrayList<VehicleLocation>();

        // Find the epoch time milliseconds when the cached locations were last updated
        long timeOfLastUpdate = cache.getLatestVehicleLocationCreationTime(route);

        List<VehicleLocation> networkVehicleLocations = backing.getVehicleLocations(route, timeOfLastUpdate);

        // Replace old vehicle locations with the updated ones
        // This works since VehicleLocation.equals() is composite only of the vehicle id, parent route, and direction
        cachedVehicleLocations.removeAll(networkVehicleLocations);
        cachedVehicleLocations.addAll(networkVehicleLocations);

        cache.putVehicleLocations(route, cachedVehicleLocations);

        return cachedVehicleLocations;
    }

    @Override
    public List<PredictionGroup> getPredictions(Stop s) throws ServiceException {
        return backing.getPredictions(s);
    }

    @Override
    public PredictionGroup getPredictions(Route r, Stop s) throws ServiceException {
        return backing.getPredictions(r, s);
    }

    @Override
    public List<PredictionGroup> getPredictions(Route route, Collection<Stop> stops) throws ServiceException {
        return backing.getPredictions(route, stops);
    }

    @Override
    public List<PredictionGroup> getPredictions(Map<Route, Stop> stops) throws ServiceException {
        return backing.getPredictions(stops);
    }

    @Override
    public List<Schedule> getSchedule(Route route) throws ServiceException {
        return backing.getSchedule(route);
    }

}
