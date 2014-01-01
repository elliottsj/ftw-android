package com.elliottsj.ttc_ftw.utilities;

import net.sf.nextbus.publicxmlfeed.domain.*;
import net.sf.nextbus.publicxmlfeed.impl.NextbusService;
import net.sf.nextbus.publicxmlfeed.service.ServiceException;

import java.util.Collection;
import java.util.List;
import java.util.Map;

/**
 *
 */
public class AndroidNextbusService extends NextbusService {

    @Override
    public List<Agency> getAgencies() throws ServiceException {
        return super.getAgencies();
    }

    @Override
    public Agency getAgency(String id) throws ServiceException {
        return super.getAgency(id);
    }

    @Override
    public List<Route> getRoutes(Agency agency) {
        return super.getRoutes(agency);
    }

    @Override
    public RouteConfiguration getRouteConfiguration(Route parent) {
        return super.getRouteConfiguration(parent);
    }

    @Override
    public List<VehicleLocation> getVehicleLocations(Route route, long t) {
        return super.getVehicleLocations(route, t);
    }

    @Override
    public List<PredictionGroup> getPredictions(Stop s) throws ServiceException {
        return super.getPredictions(s);
    }

    @Override
    public PredictionGroup getPredictions(Route r, Stop s) throws ServiceException {
        return super.getPredictions(r, s);
    }

    @Override
    public List<PredictionGroup> getPredictions(Map<Route, Stop> stops) throws ServiceException {
        return super.getPredictions(stops);
    }

    @Override
    public List<PredictionGroup> getPredictions(Route route, Collection<Stop> s) throws ServiceException {
        return super.getPredictions(route, s);
    }

    @Override
    public List<Schedule> getSchedule(Route route) throws ServiceException {
        return super.getSchedule(route);
    }

}
