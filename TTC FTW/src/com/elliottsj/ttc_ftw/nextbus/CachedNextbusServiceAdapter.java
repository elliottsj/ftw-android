package com.elliottsj.ttc_ftw.nextbus;

import net.sf.nextbus.publicxmlfeed.domain.*;
import net.sf.nextbus.publicxmlfeed.service.INextbusService;
import net.sf.nextbus.publicxmlfeed.service.ServiceException;

import java.util.Collection;
import java.util.List;
import java.util.Map;

/**
 *
 */
public class CachedNextbusServiceAdapter implements ICachedNextbusService {

    private INextbusService backing;

    public CachedNextbusServiceAdapter(INextbusService backing) {
        this.backing = backing;
    }

    @Override
    public List<Stop> getAllStops(Agency agency) {
        return null;
    }

    @Override
    public List<Agency> getAgencies() throws ServiceException {
        return backing.getAgencies();
    }

    @Override
    public Agency getAgency(String id) throws ServiceException {
        return backing.getAgency(id);
    }

    @Override
    public List<Route> getRoutes(Agency agency) throws ServiceException {
        return backing.getRoutes(agency);
    }

    @Override
    public RouteConfiguration getRouteConfiguration(Route route) throws ServiceException {
        return backing.getRouteConfiguration(route);
    }

    @Override
    public List<VehicleLocation> getVehicleLocations(Route route, long deltaT) throws ServiceException {
        return backing.getVehicleLocations(route, deltaT);
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
