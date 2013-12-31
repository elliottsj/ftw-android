package com.elliottsj.ttc_ftw.nextbus;

import android.content.Context;
import com.jakewharton.disklrucache.DiskLruCache;
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
 *
 */
public class NextbusNextbusCache implements INextbusCacheStore {

    private static final String SUBDIRECTORY = "nextbus";

    /**
     * Static strings for each command
     */
    private static class Command {
        public static final String AGENCIES = "agencies";
    }

    private enum EntryType {
        CREATE_TIME,
        DATA
    }

    DiskLruCache diskLruCache;

    /**
     * Constructs this cache in the given context.
     *
     * @param context the context where this cache will exist
     * @throws IOException if reading or writing the cache directory fails
     */
    public NextbusNextbusCache(Context context) throws IOException {
        long maxSize = 20 * 2^20; // 20 MiB

        /**
         * Open or create the cache files on the cache directory of the context.
         * Each entry has two items:
         * - item 0 contains a Long of the creation time of the data (milliseconds since epoch)
         * - item 1 contains the data
         */
        File directory = new File(context.getCacheDir(), SUBDIRECTORY);
        this.diskLruCache = DiskLruCache.open(directory, 1, EntryType.values().length, maxSize);
    }

    @Override
    public boolean isAgenciesCached() {
        boolean result = false;
        try {
            result = diskLruCache.get(Command.AGENCIES) != null;
        } catch (IOException e) {
            e.printStackTrace();
        }
        return result;
    }

    @Override
    public long getAgenciesAge() {
        if (!isAgenciesCached())
            throw new ServiceException("Agencies are not cached");

        DiskLruCache.Snapshot snapshot = null;
        try {
            snapshot = diskLruCache.get(Command.AGENCIES);
            ObjectInputStream ois = new ObjectInputStream(snapshot.getInputStream(EntryType.CREATE_TIME.ordinal()));
            return ois.readLong();
        } catch (IOException e) {
            e.printStackTrace();
            return 0;
        } finally {
            if (snapshot != null)
                snapshot.close();
        }
    }

    /**
     * Gets the agencies in this cache in a map indexed by agency id.
     *
     * @return the agencies in this cache
     */
    @Override
    public Map<String, Agency> getAgencies() {
        DiskLruCache.Snapshot snapshot = null;
        Map<String, Agency> agencyMap = null;
        try {
            snapshot = diskLruCache.get(Command.AGENCIES);
            InputStream dataIs = snapshot.getInputStream(EntryType.DATA.ordinal());
            ObjectInputStream ois = new ObjectInputStream(new BufferedInputStream(dataIs));

            //noinspection unchecked
            agencyMap = (Map<String, Agency>) ois.readObject();
        } catch (IOException e) {
            String error = e.toString();
            e.printStackTrace();
        } catch (ClassNotFoundException e) {
            e.printStackTrace();
        } finally {
            if (snapshot != null)
                snapshot.close();
        }
        return agencyMap;
    }

    @Override
    public void putAgencies(List<Agency> agencies) {
        DiskLruCache.Editor editor = null;
        try {
            editor = diskLruCache.edit(Command.AGENCIES);

            OutputStream timeOs = editor.newOutputStream(EntryType.CREATE_TIME.ordinal());
            OutputStream dataOs = editor.newOutputStream(EntryType.DATA.ordinal());
            OutputStream timeBos = new BufferedOutputStream(timeOs);
            OutputStream dataBos = new BufferedOutputStream(dataOs);
            ObjectOutputStream timeOos = new ObjectOutputStream(timeBos);
            ObjectOutputStream dataOos = new ObjectOutputStream(dataBos);

            long createTime = System.currentTimeMillis();

            Map<String, Agency> agenciesMap = new HashMap<String, Agency>();
            for (Agency agency : agencies)
                agenciesMap.put(agency.getId(), agency);

            timeOos.writeLong(createTime);
            dataOos.writeObject(agenciesMap);

            timeOos.close();
            dataOos.close();
        } catch (IOException e) {
            e.printStackTrace();
        } finally {
            if (editor != null)
                try {
                    editor.commit();
                } catch (IOException e) {
                    e.printStackTrace();
                }
        }
    }

    @Override
    public boolean isRoutesCached(Agency agency) {
        return false;
    }

    @Override
    public long getRoutesAge(Agency agency) {
        return 0;
    }

    @Override
    public List<Route> getRoutes(Agency agency) {
        return null;
    }

    @Override
    public void putRoutes(List<Route> routes) {

    }

    @Override
    public boolean isRouteConfigurationCached(Route route) {
        return false;
    }

    @Override
    public long getRouteConfigurationAge(Route route) {
        return 0;
    }

    @Override
    public RouteConfiguration getRouteConfiguration(Route route) {
        return null;
    }

    @Override
    public void putRouteConfiguration(RouteConfiguration routeConfiguration) {

    }

    @Override
    public boolean isVehicleLocationsCached(Route route) {
        return false;
    }

    @Override
    public long getLatestVehicleLocationCreationTime(Route route) {
        return 0;
    }

    @Override
    public List<VehicleLocation> getVehicleLocations(Route route) {
        return null;
    }

    @Override
    public void putVehicleLocations(Route route, List<VehicleLocation> vehicleLocations) {

    }

}
