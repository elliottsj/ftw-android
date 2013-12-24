package com.elliottsj.ttc_ftw.nextbus;

import net.sf.nextbus.publicxmlfeed.domain.Agency;
import net.sf.nextbus.publicxmlfeed.domain.Stop;
import net.sf.nextbus.publicxmlfeed.service.INextbusService;

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
     * Gets all stops served by the given agency.
     *
     * @param agency the agency
     * @return all stops served by the given agency
     */
    public List<Stop> getAllStops(Agency agency);

}
