package com.elliottsj.ftw.provider;

import com.j256.ormlite.stmt.QueryBuilder;

import net.sf.nextbus.publicxmlfeed.domain.Agency;
import net.sf.nextbus.publicxmlfeed.domain.Direction;
import net.sf.nextbus.publicxmlfeed.domain.DirectionStop;
import net.sf.nextbus.publicxmlfeed.domain.Route;
import net.sf.nextbus.publicxmlfeed.domain.Stop;

import java.sql.SQLException;

class NextbusQueryBuilderFactory {

    private NextbusSQLiteHelper mDbHelper;

    NextbusQueryBuilderFactory(NextbusSQLiteHelper mDbHelper) {
        this.mDbHelper = mDbHelper;
    }

    QueryBuilder<Agency, Integer> agenciesQb(String agencyTag) throws SQLException {
        QueryBuilder<Agency, Integer> agenciesQb = mDbHelper.getAgenciesDao().queryBuilder();
        if (agencyTag != null) agenciesQb.where().eq(Agency.FIELD_TAG, agencyTag);
        return agenciesQb;
    }

    QueryBuilder<Agency, Integer> agenciesQb() throws SQLException {
        return agenciesQb(null);
    }

    QueryBuilder<Route, Integer> routesQb(String agencyTag, String routeTag) throws SQLException {
        QueryBuilder<Route, Integer> routesQb = mDbHelper.getRoutesDao().queryBuilder();
        routesQb.join(agenciesQb(agencyTag));
        if (routeTag != null) routesQb.where().eq(Route.FIELD_TAG, routeTag);
        return routesQb;
    }

    QueryBuilder<Route, Integer> routesQb(String agencyTag) throws SQLException {
        return routesQb(agencyTag, null);
    }

    QueryBuilder<Direction, Integer> directionsQb(String agencyTag, String routeTag, String directionTag) throws SQLException {
        QueryBuilder<Direction, Integer> directionsQb = mDbHelper.getDirectionsDao().queryBuilder();
        directionsQb.join(routesQb(agencyTag, routeTag));
        if (directionTag != null) directionsQb.where().eq(Direction.FIELD_TAG, directionTag);
        return directionsQb;
    }

    QueryBuilder<Direction, Integer> directionsQb(String agencyTag, String routeTag) throws SQLException {
        return directionsQb(agencyTag, routeTag, null);
    }

    QueryBuilder<Stop, Integer> stopsQb(String agencyTag, String routeTag, String directionTag, String stopTag) throws SQLException {
        QueryBuilder<DirectionStop, Integer> directionStopsQb = mDbHelper.getDirectionStopsDao().queryBuilder();
        directionStopsQb.join(directionsQb(agencyTag, routeTag, directionTag));
        QueryBuilder<Stop, Integer> stopsQb = mDbHelper.getStopsDao().queryBuilder();
        stopsQb.join(directionStopsQb);
        if (stopTag != null) stopsQb.where().eq(Stop.FIELD_TAG, stopTag);
        return stopsQb;
    }

    QueryBuilder<Stop, Integer> stopsQb(String agencyTag, String routeTag, String directionTag) throws SQLException {
        return stopsQb(agencyTag, routeTag, directionTag, null);
    }

}
