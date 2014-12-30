package com.elliottsj.ftw.provider;

import com.j256.ormlite.stmt.QueryBuilder;

import com.elliottsj.nextbus.domain.Agency;
import com.elliottsj.nextbus.domain.Direction;
import com.elliottsj.nextbus.domain.DirectionStop;
import com.elliottsj.nextbus.domain.Route;
import com.elliottsj.nextbus.domain.Stop;

import java.sql.SQLException;

public class NextbusQueryBuilderFactory {

    private NextbusSQLiteHelper mDbHelper;

    public NextbusQueryBuilderFactory(NextbusSQLiteHelper mDbHelper) {
        this.mDbHelper = mDbHelper;
    }

    public QueryBuilder<Agency, Integer> agenciesQb(String agencyTag) throws SQLException {
        QueryBuilder<Agency, Integer> agenciesQb = mDbHelper.getAgenciesDao().queryBuilder();
        if (agencyTag != null) agenciesQb.where().eq(Agency.FIELD_TAG, agencyTag);
        return agenciesQb;
    }

    public QueryBuilder<Agency, Integer> agenciesQb() throws SQLException {
        return agenciesQb(null);
    }

    public QueryBuilder<Route, Integer> routesQb(String agencyTag, String routeTag) throws SQLException {
        QueryBuilder<Route, Integer> routesQb = mDbHelper.getRoutesDao().queryBuilder();
        routesQb.join(agenciesQb(agencyTag));
        if (routeTag != null) routesQb.where().eq(Route.FIELD_TAG, routeTag);
        return routesQb;
    }

    public QueryBuilder<Route, Integer> routesQb(String agencyTag) throws SQLException {
        return routesQb(agencyTag, null);
    }

    public QueryBuilder<Direction, Integer> directionsQb(String agencyTag, String routeTag, String directionTag) throws SQLException {
        QueryBuilder<Direction, Integer> directionsQb = mDbHelper.getDirectionsDao().queryBuilder();
        directionsQb.join(routesQb(agencyTag, routeTag));
        if (directionTag != null) directionsQb.where().eq(Direction.FIELD_TAG, directionTag);
        return directionsQb;
    }

    public QueryBuilder<Direction, Integer> directionsQb(String agencyTag, String routeTag) throws SQLException {
        return directionsQb(agencyTag, routeTag, null);
    }

    public QueryBuilder<Stop, Integer> stopsQb(String agencyTag, String routeTag, String directionTag, String stopTag) throws SQLException {
        QueryBuilder<DirectionStop, Integer> directionStopsQb = mDbHelper.getDirectionStopsDao().queryBuilder();
        directionStopsQb.join(directionsQb(agencyTag, routeTag, directionTag));
        QueryBuilder<Stop, Integer> stopsQb = mDbHelper.getStopsDao().queryBuilder();
        stopsQb.join(directionStopsQb);
        if (stopTag != null) stopsQb.where().eq(Stop.FIELD_TAG, stopTag);
        return stopsQb;
    }

    public QueryBuilder<Stop, Integer> stopsQb(String agencyTag, String routeTag, String directionTag) throws SQLException {
        return stopsQb(agencyTag, routeTag, directionTag, null);
    }

}
