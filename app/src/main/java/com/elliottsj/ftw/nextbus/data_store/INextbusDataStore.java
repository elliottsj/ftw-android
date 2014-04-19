package com.elliottsj.ftw.nextbus.data_store;

import net.sf.nextbus.publicxmlfeed.domain.Agency;
import net.sf.nextbus.publicxmlfeed.domain.Route;
import net.sf.nextbus.publicxmlfeed.domain.RouteConfiguration;
import net.sf.nextbus.publicxmlfeed.domain.Stop;
import net.sf.nextbus.publicxmlfeed.domain.VehicleLocation;

import java.util.List;

/**
 * A data store used by a {@link com.elliottsj.ftw.nextbus.ICachedNextbusService}
 * to store and retrieve NextBus data, and by {@link com.elliottsj.ftw.preferences.PreferencesDataSource}
 * to store and retrieve preferred stops.
 */
public interface INextbusDataStore {

    /**
     * Open this data store for reading/writing; must be called before attempting to read/write
     */
    public void open();

    /**
     * Closes this cache for reading/writing.
     */
    public void close();

    /**
     * @return true iff agencies exist in this data store
     */
    public boolean isAgenciesStored();

    /**
     * @return the number of milliseconds since agencies were stored
     */
    public long getAgenciesAge();

    /**
     * @return a list of agencies stored in this data store
     */
    public List<Agency> getAgencies();

    /**
     * @param tag the tag of the agency to retrieve
     * @return the agency with the given tag
     */
    public Agency getAgency(String tag);

    /**
     * Deletes any agencies in this data store and stores the given agencies in this data store.
     *
     * @param agencies a list of agencies to store
     * @return the newly-stored agencies
     */
    public List<Agency> putAgencies(List<Agency> agencies);


    /**
     * @param agency the agency which the routes belong to
     * @return true iff routes are stored for the given agency
     */
    public boolean isRoutesCached(Agency agency);

    /**
     * @param agency the agency which the routes belong to
     * @return the number of milliseconds since the routes were stored
     */
    public long getRoutesAge(Agency agency);

    /**
     * @param agency the agency which the routes belong to
     * @return the routes stored in this data store for the given agency
     */
    public List<Route> getRoutes(Agency agency);

    /**
     * Deletes routes owned by the given routes' agency from this data store and
     * stores the given routes in this data store
     *
     * @param routes a list of routes to store
     * @return the newly-stored routes
     */
    public List<Route> putRoutes(List<Route> routes);

    /**
     * @param route the route corresponding to a configuration
     * @return true iff the route and the configuration for the given route is stored
     */
    public boolean isRouteConfigurationCached(Route route);

    /**
     * @param route the route corresponding to a configuration
     * @return the number of milliseconds since the route configuration was stored
     */
    public long getRouteConfigurationAge(Route route);

    /**
     * @param route the route corresponding to a configuration
     * @return the configuration for the given route
     */
    public RouteConfiguration getRouteConfiguration(Route route);

    /**
     * Stores the given route configuration in this data store
     *
     * @param routeConfiguration the route configuration to store
     * @return the newly-stored route configuration
     */
    public RouteConfiguration putRouteConfiguration(RouteConfiguration routeConfiguration);

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
     * @return the newly-cached vehicle locations
     */
    public List<VehicleLocation> putVehicleLocations(List<VehicleLocation> vehicleLocations);

    /**
     * Gets all routes which have route configurations cached for the given agency.
     *
     * @param agency the agency which the route configuration belong to
     * @return all routes which have route configurations cached for the given agency
     */
    public List<Route> getAllRouteConfigurationRoutes(Agency agency);

    /**
     * Gets all stops stored in this cache.
     *
     * @param agency the agency for which to retrieve stops
     * @return all stops stored in this cache
     */
    public List<Stop> getAllStops(Agency agency);

    /**
     * Gets a list
     *
     * @param agency
     * @return
     */
    public List<Stop> getPreferredStops(Agency agency);

}
