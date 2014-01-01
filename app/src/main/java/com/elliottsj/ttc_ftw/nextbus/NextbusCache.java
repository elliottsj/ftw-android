package com.elliottsj.ttc_ftw.nextbus;

import android.annotation.SuppressLint;
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
 * Simple implementation of {@link com.elliottsj.ttc_ftw.nextbus.INextbusCacheStore}
 */
public class NextbusCache implements INextbusCacheStore {

    private static final String FILENAME = "nextbus";

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
     * @param directory the directory where this cache will exist
     * @throws IOException if reading or writing the cache directory fails
     */
    public NextbusCache(File directory) {
        file = new File(directory, FILENAME);
        if (file.exists() && file.canRead()) {
            FileInputStream fis = null;
            InputStream bis = null;
            ObjectInputStream ois = null;
            try {
                fis = new FileInputStream(file);
                bis = new BufferedInputStream(fis);
                ois = new ObjectInputStream(bis);

                try {
                    //noinspection unchecked
                    data = (Map<Command, Map<EntryType, Serializable>>) ois.readObject();
                } catch (ClassNotFoundException e) {
                    if (!file.delete())
                        throw new ServiceException("Cache file is corrupted; unable to delete file", e);
                } finally {
                    ois.close();
                }
            } catch (Exception e) {
                throw new RuntimeException("Exception when constructing cache", e);
            }
        } else {
            data = new HashMap<Command, Map<EntryType, Serializable>>();
        }
    }

    private void writeToFile() {
        ObjectOutputStream ois = null;
        try {
            ois = new ObjectOutputStream(new BufferedOutputStream(new FileOutputStream(file)));

            ois.writeObject(data);
        } catch (FileNotFoundException e) {
            e.printStackTrace();
        } catch (IOException e) {
            e.printStackTrace();
        } finally {
            if (ois != null) {
                try {
                    ois.close();
                } catch (IOException e) {
                    e.printStackTrace();
                }
            }
        }
    }

    @Override
    public boolean isAgenciesCached() {
        return data.containsKey(Command.AGENCIES);
    }

    @Override
    public long getAgenciesAge() {
        if (!isAgenciesCached())
            throw new ServiceException("Agencies are not cached");

        return System.currentTimeMillis() - (Long) data.get(Command.AGENCIES).get(EntryType.CREATE_TIME);
    }

    /**
     * Gets the agencies in this cache in a map indexed by agency id.
     *
     * @return the agencies in this cache
     */
    @Override
    public Map<String, Agency> getAgencies() {
        if (!isAgenciesCached())
            throw new ServiceException("Agencies are not cached");

        //noinspection unchecked
        return (Map<String, Agency>) data.get(Command.AGENCIES).get(EntryType.DATA);
    }

    @Override
    public void putAgencies(List<Agency> agencies) {
        Map<String, Agency> agenciesMap = new HashMap<String, Agency>();
        for (Agency agency : agencies)
            agenciesMap.put(agency.getId(), agency);

        @SuppressLint("UseSparseArrays")
        Map<EntryType, Serializable> agenciesData = new HashMap<EntryType, Serializable>();
        agenciesData.put(EntryType.CREATE_TIME, System.currentTimeMillis());
        agenciesData.put(EntryType.DATA, (Serializable) agenciesMap);

        data.put(Command.AGENCIES, agenciesData);

        writeToFile();
    }

    @Override
    public boolean isRoutesCached(Agency agency) {
        if (!data.containsKey(Command.ROUTES))
            return false;

        //noinspection unchecked
        return ((Map<Agency, List<Route>>) data.get(Command.ROUTES).get(EntryType.DATA)).containsKey(agency);
    }

    @Override
    public long getRoutesAge(Agency agency) {
        if (!isRoutesCached(agency))
            throw new ServiceException("Routes are not cached");

        return System.currentTimeMillis() - (Long) data.get(Command.ROUTES).get(EntryType.CREATE_TIME);
    }

    @Override
    public List<Route> getRoutes(Agency agency) {
        if (!isRoutesCached(agency))
            throw new ServiceException("Routes are not cached");

        //noinspection unchecked
        return ((Map<Agency, List<Route>>) data.get(Command.ROUTES).get(EntryType.DATA)).get(agency);
    }

    @Override
    public void putRoutes(List<Route> routes) {
        Map<Agency, List<Route>> routesMap;
        if (data.containsKey(Command.ROUTES))
            // Map<Agency, List<Route>> already exists
            //noinspection unchecked
            routesMap = (Map<Agency, List<Route>>) data.get(Command.ROUTES).get(EntryType.DATA);
        else
            routesMap = new HashMap<Agency, List<Route>>();

        if (!routes.isEmpty())
            routesMap.put(routes.get(0).getAgency(), routes);

        @SuppressLint("UseSparseArrays")
        Map<EntryType, Serializable> routesData = new HashMap<EntryType, Serializable>();
        routesData.put(EntryType.CREATE_TIME, System.currentTimeMillis());
        routesData.put(EntryType.DATA, (Serializable) routesMap);

        data.put(Command.ROUTES, routesData);

        writeToFile();
    }

    @Override
    public boolean isRouteConfigurationCached(Route route) {
        if (!data.containsKey(Command.ROUTE_CONFIGURATIONS))
            return false;

        //noinspection unchecked
        return ((Map<Route, RouteConfiguration>) data.get(Command.ROUTE_CONFIGURATIONS).get(EntryType.DATA)).containsKey(route);
    }

    @Override
    public long getRouteConfigurationAge(Route route) {
        if (!isRouteConfigurationCached(route))
            throw new ServiceException("Route configuration is not cached");

        return System.currentTimeMillis() - (Long) data.get(Command.ROUTE_CONFIGURATIONS).get(EntryType.CREATE_TIME);
    }

    @Override
    public RouteConfiguration getRouteConfiguration(Route route) {
        if (!isRouteConfigurationCached(route))
            throw new ServiceException("Route configuration is not cached");

        //noinspection unchecked
        return ((Map<Route, RouteConfiguration>) data.get(Command.ROUTE_CONFIGURATIONS).get(EntryType.DATA)).get(route);
    }

    @Override
    public void putRouteConfiguration(RouteConfiguration routeConfiguration) {
        Map<Route, RouteConfiguration> routeConfigurationsMap;
        if (data.containsKey(Command.ROUTE_CONFIGURATIONS))
            // Map<Route, RouteConfiguration> already exists
            //noinspection unchecked
            routeConfigurationsMap = (Map<Route, RouteConfiguration>) data.get(Command.ROUTE_CONFIGURATIONS).get(EntryType.DATA);
        else
            routeConfigurationsMap = new HashMap<Route, RouteConfiguration>();

        routeConfigurationsMap.put(routeConfiguration.getRoute(), routeConfiguration);

        @SuppressLint("UseSparseArrays")
        Map<EntryType, Serializable> routesData = new HashMap<EntryType, Serializable>();
        routesData.put(EntryType.CREATE_TIME, System.currentTimeMillis());
        routesData.put(EntryType.DATA, (Serializable) routeConfigurationsMap);

        data.put(Command.ROUTE_CONFIGURATIONS, routesData);

        writeToFile();
    }

    @Override
    public boolean isVehicleLocationsCached(Route route) {
        if (!data.containsKey(Command.VEHICLE_LOCATIONS))
            return false;

        //noinspection unchecked
        return ((Map<Route, List<VehicleLocation>>) data.get(Command.VEHICLE_LOCATIONS).get(EntryType.DATA)).containsKey(route);
    }

    @Override
    public long getLatestVehicleLocationCreationTime(Route route) {
        if (!isVehicleLocationsCached(route))
            throw new ServiceException("Vehicle locations are not cached");

        return System.currentTimeMillis() - (Long) data.get(Command.VEHICLE_LOCATIONS).get(EntryType.CREATE_TIME);
    }

    @Override
    public List<VehicleLocation> getVehicleLocations(Route route) {
        if (!isVehicleLocationsCached(route))
            throw new ServiceException("Vehicle locations are not cached");

        //noinspection unchecked
        return ((Map<Route, List<VehicleLocation>>) data.get(Command.VEHICLE_LOCATIONS).get(EntryType.DATA)).get(route);
    }

    @Override
    public void putVehicleLocations(List<VehicleLocation> vehicleLocations) {
        Map<Route, List<VehicleLocation>> vehicleLocationsMap;
        if (data.containsKey(Command.VEHICLE_LOCATIONS))
            // Map<Route, List<VehicleLocation>> already exists
            //noinspection unchecked
            vehicleLocationsMap = (Map<Route, List<VehicleLocation>>) data.get(Command.VEHICLE_LOCATIONS).get(EntryType.DATA);
        else
            vehicleLocationsMap = new HashMap<Route, List<VehicleLocation>>();

        if (!vehicleLocations.isEmpty())
            vehicleLocationsMap.put(vehicleLocations.get(0).getRoute(), vehicleLocations);

        @SuppressLint("UseSparseArrays")
        Map<EntryType, Serializable> routesData = new HashMap<EntryType, Serializable>();
        routesData.put(EntryType.CREATE_TIME, System.currentTimeMillis());
        routesData.put(EntryType.DATA, (Serializable) vehicleLocationsMap);

        data.put(Command.VEHICLE_LOCATIONS, routesData);

        writeToFile();
    }

}
