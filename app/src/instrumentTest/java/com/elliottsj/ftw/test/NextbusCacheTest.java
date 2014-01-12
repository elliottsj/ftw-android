package com.elliottsj.ftw.test;

import android.content.ContentResolver;
import android.content.Context;
import android.test.AndroidTestCase;
import android.test.IsolatedContext;
import android.test.RenamingDelegatingContext;
import android.test.mock.MockContentResolver;
import android.test.mock.MockContext;

import com.elliottsj.ftw.nextbus.cache.NextbusCache;

import net.sf.nextbus.publicxmlfeed.domain.Agency;
import net.sf.nextbus.publicxmlfeed.domain.Direction;
import net.sf.nextbus.publicxmlfeed.domain.Geolocation;
import net.sf.nextbus.publicxmlfeed.domain.Path;
import net.sf.nextbus.publicxmlfeed.domain.Route;
import net.sf.nextbus.publicxmlfeed.domain.RouteConfiguration;
import net.sf.nextbus.publicxmlfeed.domain.Stop;

import java.util.ArrayList;
import java.util.HashSet;
import java.util.List;

public class NextbusCacheTest extends AndroidTestCase {

    NextbusCache mCache;

    public void setUp() throws Exception {
        super.setUp();

        ContentResolver resolver = new MockContentResolver();
        final String filenamePrefix = "test.";
        RenamingDelegatingContext targetContextWrapper = new RenamingDelegatingContext(
                new MockContext(), // The context that most methods are delegated to
                getContext(),      // The context that file methods are delegated to
                filenamePrefix);
        Context context = new IsolatedContext(resolver, targetContextWrapper);
        mCache = new NextbusCache(context);
        mCache.open();
    }

    public void tearDown() throws Exception {
        mCache.delete();
        mCache.close();
    }

    private List<Agency> sampleAgencies() {
        List<Agency> agencies = new ArrayList<Agency>();
        agencies.add(new Agency("ttc",
                                "Toronto Transit Commission",
                                "Toronto TTC",
                                "Ontario",
                                "All data copyright agencies listed below and NextBus Inc 2014."));
        agencies.add(new Agency("mbta",
                                "MBTA",
                                null,
                                "Massachusetts",
                                "All data copyright agencies listed below and NextBus Inc 2014."));
        return agencies;
    }

    private List<Route> sampleRoutesTTC() {
        Agency ttc = sampleAgencies().get(0);

        List<Route> routes = new ArrayList<Route>();
        routes.add(new Route(ttc, "39", "39-Finch East", null, "All data copyright agencies listed below and NextBus Inc 2014."));
        routes.add(new Route(ttc, "506", "506-Carlton", null, "All data copyright agencies listed below and NextBus Inc 2014."));

        return routes;
    }

    private List<Route> sampleRoutesMBTA() {
        Agency ttc = sampleAgencies().get(1);

        List<Route> routes = new ArrayList<Route>();
        routes.add(new Route(ttc, "441442", "441/442", "A short title", "All data copyright agencies listed below and NextBus Inc 2014."));
        routes.add(new Route(ttc, "746", "Silver Line Waterfront", null, "All data copyright agencies listed below and NextBus Inc 2014."));

        return routes;
    }

    private List<Stop> sampleStops506East() {
        Route route = sampleRoutesTTC().get(1);

        List<Stop> stops = new ArrayList<Stop>();

        stops.add(new Stop(route.getAgency(), "0815", "2748", "College St At Beverley St", null, new Geolocation(43.65855, -79.3963699), "All data copyright Toronto Transit Commission 2014."));
        stops.add(new Stop(route.getAgency(), "0837", "1459", "College St At McCaul St", null, new Geolocation(43.6590699, -79.39372), "All data copyright Toronto Transit Commission 2014."));
        stops.add(new Stop(route.getAgency(), "0815", "4865", "College St At Yonge St (College Station)", null, new Geolocation(43.6612599, -79.38329), "All data copyright Toronto Transit Commission 2014."));

        return stops;
    }

    private List<Stop> sampleStops506West() {
        Route route = sampleRoutesTTC().get(1);

        List<Stop> stops = new ArrayList<Stop>();

        stops.add(new Stop(route.getAgency(), "0838", "10367", "College St At McCaul St", null, new Geolocation(43.65931, -79.3932799), "All data copyright Toronto Transit Commission 2014."));
        stops.add(new Stop(route.getAgency(), "0845", "5013", "College St At St George St", null, new Geolocation(43.6588099, -79.39575), "All data copyright Toronto Transit Commission 2014."));
        stops.add(new Stop(route.getAgency(), "0844", "9193", "College St At Spadina Ave", null, new Geolocation(43.6580599, -79.39969), "All data copyright Toronto Transit Commission 2014."));

        return stops;
    }

    private List<Direction> sampleDirections506() {
        Route route = sampleRoutesTTC().get(1);

        List<Direction> directions = new ArrayList<Direction>();

        directions.add(new Direction(route, "506_1_506Sun", "West - 506 Carlton towards High Park", "West", sampleStops506West(), "All data copyright agencies listed below and NextBus Inc 2014."));
        directions.add(new Direction(route, "506_0_506Sun", "East - 506 Carlton towards Main Street Station", "East", sampleStops506East(), "All data copyright agencies listed below and NextBus Inc 2014."));

        return directions;
    }

    private List<Path> samplePaths506() {
        Route route = sampleRoutesTTC().get(1);

        List<Geolocation> geolocations1 = new ArrayList<Geolocation>();
        geolocations1.add(new Geolocation(43.6620599, -79.36715));
        geolocations1.add(new Geolocation(43.66194, -79.36702));
        geolocations1.add(new Geolocation(43.6625, -79.36421));
        geolocations1.add(new Geolocation(43.66308, -79.36161));
        geolocations1.add(new Geolocation(43.66358, -79.35935));
        geolocations1.add(new Geolocation(43.6637, -79.35923));
        geolocations1.add(new Geolocation(43.66448, -79.35648));
        geolocations1.add(new Geolocation(43.66469, -79.35539));
        geolocations1.add(new Geolocation(43.6654, -79.35291));

        List<Geolocation> geolocations2 = new ArrayList<Geolocation>();
        geolocations2.add(new Geolocation(43.65635, -79.40785));
        geolocations2.add(new Geolocation(43.65687, -79.40528));
        geolocations2.add(new Geolocation(43.65726, -79.40326));
        geolocations2.add(new Geolocation(43.65782, -79.4004));

        List<Path> paths = new ArrayList<Path>();

        paths.add(new Path(route, "0", geolocations1));
        paths.add(new Path(route, "1", geolocations2));

        return paths;
    }

    private RouteConfiguration sampleRouteConfiguration506() {
        return new RouteConfiguration(sampleRoutesTTC().get(1),
                                      sampleStops506East(),
                                      sampleDirections506(),
                                      samplePaths506(),
                                      new RouteConfiguration.ServiceArea(43.6477899, 43.6888599, -79.45813, -79.3002999),
                                      new RouteConfiguration.UIColor("ffffff"),
                                      new RouteConfiguration.UIColor("ff0000"),
                                      "All data copyright agencies listed below and NextBus Inc 2014.");
    }

    public void testIsAgenciesCached() throws Exception {
        assertFalse("Agencies are cached.", mCache.isAgenciesCached());

        mCache.putAgencies(sampleAgencies());

        assertTrue("Agencies not cached", mCache.isAgenciesCached());

        // This should overwrite existing agencies
        mCache.putAgencies(new ArrayList<Agency>());

        assertFalse("Agencies are cached.", mCache.isAgenciesCached());
    }

    public void testGetAgenciesAge() throws Exception {
        assertFalse("Agencies are cached.", mCache.isAgenciesCached());

        mCache.putAgencies(sampleAgencies());

        long expectedAge = 3000;
        Thread.sleep(expectedAge);

        long actualAge = mCache.getAgenciesAge();
        assertTrue("Agencies age out of range", expectedAge - 500 < actualAge && actualAge < expectedAge + 500);
    }

    public void testPutAgenciesGetAgencies() throws Exception {
        assertFalse("Agencies are cached.", mCache.isAgenciesCached());

        List<Agency> expected = sampleAgencies();
        mCache.putAgencies(expected);

        List<Agency> actual = mCache.getAgencies();
        assertEquals("Agency list retrieved from cache does not match.",
                     new HashSet<Agency>(expected), new HashSet<Agency>(actual));
    }

    public void testPersistAgencies() throws Exception {
        assertFalse("Agencies are cached.", mCache.isAgenciesCached());

        List<Agency> expected = sampleAgencies();
        mCache.putAgencies(expected);

        mCache.close();
        mCache.open();

        List<Agency> actual = mCache.getAgencies();
        assertEquals("Agency list retrieved from cache does not match.",
                     new HashSet<Agency>(expected), new HashSet<Agency>(actual));
    }

    public void testIsRoutesCachedWithAgencyShortTitle() throws Exception {
        Agency ttc = sampleAgencies().get(0);

        assertFalse("Routes are cached.", mCache.isRoutesCached(ttc));

        mCache.putRoutes(sampleRoutesTTC());

        assertTrue("Routes are not cached.", mCache.isRoutesCached(ttc));
    }

    public void testIsRoutesCachedWithoutAgencyShortTitle() throws Exception {
        Agency mbta = sampleAgencies().get(1);

        assertFalse("Routes are cached.", mCache.isRoutesCached(mbta));

        mCache.putRoutes(sampleRoutesMBTA());

        assertTrue("Routes are not cached.", mCache.isRoutesCached(mbta));
    }

    public void testGetRoutesAge() throws Exception {
        Agency ttc = sampleAgencies().get(0);

        assertFalse("Routes are cached.", mCache.isRoutesCached(ttc));

        mCache.putRoutes(sampleRoutesTTC());

        long expectedAge = 3000;
        Thread.sleep(expectedAge);

        long actualAge = mCache.getRoutesAge(ttc);
        assertTrue("Routes age out of range", expectedAge - 500 < actualAge && actualAge < expectedAge + 500);
    }

    public void testPutGetRoutes() throws Exception {
        Agency ttc = sampleAgencies().get(0);

        assertFalse("Routes are cached.", mCache.isRoutesCached(ttc));

        List<Route> expected = sampleRoutesTTC();
        mCache.putRoutes(expected);

        List<Route> actual = mCache.getRoutes(ttc);
        assertEquals("Route list retrieved from cache does not match.",
                     new HashSet<Route>(expected), new HashSet<Route>(actual));
    }

    public void testPersistRoutes() throws Exception {
        Agency ttc = sampleAgencies().get(0);

        assertFalse("Routes are cached.", mCache.isRoutesCached(ttc));

        List<Route> expected = sampleRoutesTTC();
        mCache.putRoutes(expected);

        mCache.close();
        mCache.open();

        List<Route> actual = mCache.getRoutes(ttc);
        assertEquals("Route list retrieved from cache does not match.",
                     new HashSet<Route>(actual), new HashSet<Route>(expected));
    }

    public void testIsRouteConfigurationCached() throws Exception {
        Route route = sampleRoutesTTC().get(1);

        assertFalse("Routes are cached.", mCache.isRouteConfigurationCached(route));

        mCache.putRouteConfiguration(sampleRouteConfiguration506());

        assertTrue("Routes are not cached.", mCache.isRouteConfigurationCached(route));
    }

    public void testGetRouteConfigurationAge() throws Exception {
        Route route = sampleRoutesTTC().get(1);

        assertFalse("Routes are cached.", mCache.isRouteConfigurationCached(route));

        mCache.putRouteConfiguration(sampleRouteConfiguration506());

        long expectedAge = 3000;
        Thread.sleep(expectedAge);

        long actualAge = mCache.getRouteConfigurationAge(route);
        assertTrue("Route configuration age out of range", expectedAge - 500 < actualAge && actualAge < expectedAge + 500);
    }

    public void testPutGetRouteConfiguration() throws Exception {
        Route route = sampleRoutesTTC().get(1);

        assertFalse("Routes are cached.", mCache.isRouteConfigurationCached(route));

        RouteConfiguration expected = sampleRouteConfiguration506();
        mCache.putRouteConfiguration(sampleRouteConfiguration506());

        RouteConfiguration actual = mCache.getRouteConfiguration(route);
        assertEquals("Route configuration retrieved from cache does not match.",
                     expected, actual);
    }

    public void testIsVehicleLocationsCached() throws Exception {

    }

    public void testGetLatestVehicleLocationCreationTime() throws Exception {

    }

    public void testGetVehicleLocations() throws Exception {

    }

    public void testPutVehicleLocations() throws Exception {

    }

}
