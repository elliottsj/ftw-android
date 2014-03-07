package com.elliottsj.ftw.nextbus;

import net.sf.nextbus.publicxmlfeed.domain.Agency;
import net.sf.nextbus.publicxmlfeed.domain.Route;
import net.sf.nextbus.publicxmlfeed.domain.Stop;
import net.sf.nextbus.publicxmlfeed.domain.VehicleLocation;
import net.sf.nextbus.publicxmlfeed.service.INextbusService;
import net.sf.nextbus.publicxmlfeed.service.ServiceException;

import java.util.List;

/**
 * Adapter Interface to the NextBus Web Service intended to support caching of responses,
 * enabling requests for static data which would normally take many network requests to fetch.
 *
 * For example, it could take hundreds of routeConfig requests to retrieve all stops served by an agency,
 * but it's much faster when this data is cached on disk/memory.
 */
public interface ICachedNextbusService extends INextbusService {

    /**
     * Caches all route configurations from the network.
     *
     * @param agency the agency which owns the route configurations to cache
     */
    public void cacheRouteConfigurations(Agency agency);

    /**
     * Gets all stops served by the given agency.
     * (Only retrieves stops stored in the cache. Call {@code cacheRouteConfigurations()} first to cache stops.)
     *
     * @param agency the agency
     * @return all stops served by the given agency
     * @throws ServiceException Wraps all XML parse, I/O and data conversion faults detected from implementation classes.
     */
    public List<Stop> getAllStops(Agency agency) throws ServiceException;

    /**
     * Gets Vehicle Locations for the given route for the last 15 minutes.
     *
     * @param route Route to enumerate
     * @return A List of Vehicles, their present Locations and last-time sampled.
     * @throws ServiceException Wraps all XML parse, I/O and data conversion faults detected from implementation classes.
     */
    public List<VehicleLocation> getVehicleLocations(Route route) throws ServiceException;

}
