//package com.elliottsj.ftw.test;
//
//import android.content.Context;
//import android.test.AndroidTestCase;
//import android.test.IsolatedContext;
//
//import com.elliottsj.ftw.nextbus.cache.NextbusCache;
//
//import net.sf.nextbus.publicxmlfeed.domain.Agency;
//
//import java.io.File;
//import java.util.ArrayList;
//import java.util.Collection;
//import java.util.HashSet;
//import java.util.List;
//
//public class NextbusCacheTest extends AndroidTestCase {
//
//    NextbusCache mCache;
//    File mDirectory;
//
//    public void setUp() throws Exception {
//        super.setUp();
//        Context context = getContext();
//        mDirectory = new IsolatedContext(context.getContentResolver(), context).getCacheDir();
//        mCache = new NextbusCache(mDirectory);
//    }
//
//    public void tearDown() throws Exception {
//        for (File file : mDirectory.listFiles())
//            assertTrue(String.format("File %s could not be deleted.", file.getCanonicalPath()), file.delete());
//        assertTrue(String.format("Directory %s could not be deleted.", mDirectory.getCanonicalPath()), mDirectory.delete());
//    }
//
//    private List<Agency> sampleAgencies() {
//        List<Agency> agencies = new ArrayList<Agency>();
//        agencies.add(new Agency("ttc",
//                                "Toronto Transit Commission",
//                                "Toronto TTC",
//                                "Ontario",
//                                "All data copyright agencies listed below and NextBus Inc 2014."));
//        agencies.add(new Agency("mbta",
//                                "MBTA",
//                                null,
//                                "Massachusetts",
//                                "All data copyright agencies listed below and NextBus Inc 2014."));
//        return agencies;
//    }
//
//    public void testIsAgenciesCached() throws Exception {
//        assertFalse("Agencies are cached.", mCache.isAgenciesCached());
//
//        mCache.putAgencies(sampleAgencies());
//
//        assertTrue("Agencies not cached", mCache.isAgenciesCached());
//    }
//
//    public void testGetAgenciesAge() throws Exception {
//        assertFalse("Agencies are cached.", mCache.isAgenciesCached());
//
//        mCache.putAgencies(sampleAgencies());
//
//        long expectedAge = 3000;
//        Thread.sleep(expectedAge);
//
//        long actualAge = mCache.getAgenciesAge();
//        assertTrue("Agencies age out of range", expectedAge - 500 < actualAge && actualAge < expectedAge + 500);
//    }
//
//    public void testPutAgenciesGetAgencies() throws Exception {
//        assertFalse("Agencies are cached.", mCache.isAgenciesCached());
//
//        List<Agency> expected = sampleAgencies();
//        mCache.putAgencies(expected);
//
//        Collection<Agency> actual = mCache.getAgencies().values();
//        assertEquals("Agency list retrieved from cache does not match.",
//                     new HashSet<Agency>(actual), new HashSet<Agency>(expected));
//    }
//
//    public void testPersistAgencies() throws Exception {
//        assertFalse("Agencies are cached.", mCache.isAgenciesCached());
//
//        List<Agency> expected = sampleAgencies();
//        mCache.putAgencies(expected);
//
//        mCache.flush();
//        mCache = new NextbusCache(mDirectory);
//
//        Collection<Agency> actual = mCache.getAgencies().values();
//        assertEquals("Agency list retrieved from cache does not match.",
//                     new HashSet<Agency>(actual), new HashSet<Agency>(expected));
//    }
//
//    public void testIsRoutesCached() throws Exception {
//
//    }
//
//    public void testGetRoutesAge() throws Exception {
//
//    }
//
//    public void testGetRoutes() throws Exception {
//
//    }
//
//    public void testPutRoutes() throws Exception {
//
//    }
//
//    public void testIsRouteConfigurationCached() throws Exception {
//
//    }
//
//    public void testGetRouteConfigurationAge() throws Exception {
//
//    }
//
//    public void testGetRouteConfiguration() throws Exception {
//
//    }
//
//    public void testPutRouteConfiguration() throws Exception {
//
//    }
//
//    public void testIsVehicleLocationsCached() throws Exception {
//
//    }
//
//    public void testGetLatestVehicleLocationCreationTime() throws Exception {
//
//    }
//
//    public void testGetVehicleLocations() throws Exception {
//
//    }
//
//    public void testPutVehicleLocations() throws Exception {
//
//    }
//
//}
