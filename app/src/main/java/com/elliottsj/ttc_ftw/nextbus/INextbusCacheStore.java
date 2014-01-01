package com.elliottsj.ttc_ftw.nextbus;

import net.sf.nextbus.publicxmlfeed.domain.Agency;
import net.sf.nextbus.publicxmlfeed.domain.Route;
import net.sf.nextbus.publicxmlfeed.domain.RouteConfiguration;
import net.sf.nextbus.publicxmlfeed.domain.VehicleLocation;

import java.util.List;
import java.util.Map;

/**
 * A cache used by a {@link com.elliottsj.ttc_ftw.nextbus.ICachedNextbusService}
 * to store and retrieve NextBus data
 */
public interface INextbusCacheStore {

    /**
     * @return true iff agencies exist in this cache
     */
    public boolean isAgenciesCached();

    /**
     * @return the number of milliseconds since agencies were cached
     */
    public long getAgenciesAge();

    /**
     * @return a map of agencies stored in this cache, indexed by agency id
     */
    public Map<String, Agency> getAgencies();

    /**
     * Stores the given agencies in this cache.
     *
     * @param agencies a list of agencies to store
     */
    public void putAgencies(List<Agency> agencies);


    /**
     * @param agency the agency which the routes belong to
     * @return true iff routes are cached for the given agency
     */
    public boolean isRoutesCached(Agency agency);

    /**
     * @param agency the agency which the routes belong to
     * @return the number of milliseconds since the routes were cached
     */
    public long getRoutesAge(Agency agency);

    /**
     * @param agency the agency which the routes belong to
     * @return the routes stored in this cache for the given agency
     */
    public List<Route> getRoutes(Agency agency);

    /**
     * Stores the given routes in this cache
     *
     * @param routes a list of routes to store
     */
    public void putRoutes(List<Route> routes);

    /**
     * @param route the route corresponding to a configuration
     * @return true iff the configuration for the given route is cached
     */
    public boolean isRouteConfigurationCached(Route route);

    /**
     * @param route the route corresponding to a configuration
     * @return the number of milliseconds since the route configuration was cached
     */
    public long getRouteConfigurationAge(Route route);

    /**
     * @param route the route corresponding to a configuration
     * @return the configuration for the given route
     */
    public RouteConfiguration getRouteConfiguration(Route route);

    /**
     * Stores the given route configuration in this cache
     *
     * @param routeConfiguration the route configuration to store
     */
    public void putRouteConfiguration(RouteConfiguration routeConfiguration);

    /**
     * @param route the route which the vehicles are travelling on
     * @return true iff vehicle locations for the given route are cached
     */
    public boolean isVehicleLocationsCached(Route route);

    /**
     * @param route the route which the vehicles are travelling on
     * @return the number of milliseconds since the most recent vehicle location was cached
     */
    public long getLatestVehicleLocationCreationTime(Route route);

    /**
     * @param route the route which the vehicles are travelling on
     * @return the vehicle locations for the givel route
     */
    public List<VehicleLocation> getVehicleLocations(Route route);

    /**
     * Stores the given vehicle locations in this cache
     *
     * @param vehicleLocations the vehicle locations to store
     */
    public void putVehicleLocations(List<VehicleLocation> vehicleLocations);

}
