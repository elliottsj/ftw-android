package com.elliottsj.ftw.nextbus;

import android.content.ContentProviderClient;
import android.content.Context;
import android.util.Log;

import com.elliottsj.ftw.nextbus.data_store.INextbusDataStore;
import com.elliottsj.ftw.nextbus.data_store.NextbusDataStore;
import com.elliottsj.ftw.utilities.AndroidRPCImpl;

import net.sf.nextbus.publicxmlfeed.domain.Agency;
import net.sf.nextbus.publicxmlfeed.domain.PredictionGroup;
import net.sf.nextbus.publicxmlfeed.domain.Route;
import net.sf.nextbus.publicxmlfeed.domain.RouteConfiguration;
import net.sf.nextbus.publicxmlfeed.domain.Schedule;
import net.sf.nextbus.publicxmlfeed.domain.Stop;
import net.sf.nextbus.publicxmlfeed.domain.VehicleLocation;
import net.sf.nextbus.publicxmlfeed.impl.NextbusService;
import net.sf.nextbus.publicxmlfeed.service.INextbusService;
import net.sf.nextbus.publicxmlfeed.service.ServiceException;

import java.util.ArrayList;
import java.util.Collection;
import java.util.List;
import java.util.Map;
import java.util.concurrent.Callable;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;

/**
 * An implementation of a cached NextBus service adapter.
 */
public class CachedNextbusServiceAdapter implements ICachedNextbusService {

    private static final String TAG = CachedNextbusServiceAdapter.class.getSimpleName();

    private Context mContext;
    private INextbusService mBacking;
    private INextbusDataStore mCache;
    private Callbacks mCallbacks;

    private static final long AGE_LIMIT_24HOURS = 24*60*60*1000;

    /** Cache age limit for static data (Route, RouteConfig, Schedule) */
    private long staticDataAgeLimit = AGE_LIMIT_24HOURS;

    public CachedNextbusServiceAdapter(Context context, ContentProviderClient provider, Callbacks callbacks) {
        mContext = context;
        mCache = new NextbusDataStore(context, provider);
        mBacking = new NextbusService(new AndroidRPCImpl());
        mCallbacks = callbacks;

        // Open the cache for reading/writing
        mCache.open();
    }

    @Override
    public void cacheRouteConfigurations(Agency agency) {
        List<Route> cachedRoutes = mCache.getAllRouteConfigurationRoutes(agency);
        List<Route> networkRoutes = mBacking.getRoutes(agency);

        // Determine which route configurations are not cached
        ArrayList<Route> uncachedRoutes = new ArrayList<Route>(networkRoutes);
        uncachedRoutes.removeAll(cachedRoutes);

        if (!uncachedRoutes.isEmpty()) {
            ExecutorService executorService = Executors.newFixedThreadPool(20);
            List<Callable<Object>> tasks = new ArrayList<Callable<Object>>();

            for (final Route route : uncachedRoutes) {
                tasks.add(new Callable<Object>() {
                    @Override
                    public Object call() throws Exception {
                        // Download and cache route configuration
                        mCache.putRouteConfiguration(mBacking.getRouteConfiguration(route));

                        // Report progress
                        mCallbacks.onRouteConfigurationCached(route);

                        return null;
                    }
                });
            }

            try {
                executorService.invokeAll(tasks);
            } catch (InterruptedException e) {
                Log.w(TAG, "Caching route configurations interrupted", e);
            }
        }


    }

    @Override
    public List<Stop> getAllStops(Agency agency) {
        return mCache.getAllStops(agency);
    }

    @Override
    public List<Agency> getAgencies() throws ServiceException {
        if (!mCache.isAgenciesStored() || mCache.getAgenciesAge() > staticDataAgeLimit)
            return cacheAgenciesFromNetwork();
        return mCache.getAgencies();
    }

    @Override
    public Agency getAgency(String tag) throws ServiceException {
        if (!mCache.isAgenciesStored() || mCache.getAgenciesAge() > staticDataAgeLimit)
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
            // Routes need to be refreshed; fetch from network and store in cache
            return mCache.putRoutes(mBacking.getRoutes(agency));

        // Cached routes are valid
        return mCache.getRoutes(agency);
    }

    @Override
    public RouteConfiguration getRouteConfiguration(Route route) throws ServiceException {
        if (!mCache.isRouteConfigurationCached(route) || mCache.getRouteConfigurationAge(route) > staticDataAgeLimit) {
            Log.i(TAG, "Route configuration for route " + route.getTag() + " not cached. Fetching from network.");
            return cacheRouteConfigurationFromNetwork(route);
        }
        Log.i(TAG, "Route configuration for route " + route.getTag() + " is cached. Fetching from cache.");
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
        public void onRouteConfigurationCached(Route route);

    }

}
