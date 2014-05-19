package com.elliottsj.ftw.pebble;

import android.database.Cursor;

import com.elliottsj.ftw.provider.NextbusProvider;

import java.io.Serializable;
import java.util.List;

public class Stop implements Serializable {

    private String routeTag;
    private String routeTitle;
    private String directionTag;
    private String directionTitle;

    public Stop(String routeTag, String routeTitle, String directionTag, String directionTitle) {
        this.routeTag = routeTag;
        this.routeTitle = formatRouteTitle(routeTitle);
        this.directionTag = directionTag;
        this.directionTitle = formatDirectionTitle(directionTitle);
    }

    public static Stop fromCursor (Cursor cursor) {
        String routeTag = cursor.getString(cursor.getColumnIndexOrThrow(NextbusProvider.SAVED_STOPS.COLUMN_ROUTE_TAG));
        String routeTitle = cursor.getString(cursor.getColumnIndexOrThrow(NextbusProvider.SAVED_STOPS.COLUMN_ROUTE_TITLE));
        String directionTag = cursor.getString(cursor.getColumnIndexOrThrow(NextbusProvider.SAVED_STOPS.COLUMN_DIRECTION_TAG));
        String directionTitle = cursor.getString(cursor.getColumnIndexOrThrow(NextbusProvider.SAVED_STOPS.COLUMN_DIRECTION_TITLE));
        return new Stop(routeTag, routeTitle, directionTag, directionTitle);
    }

    public String getRouteTag() {
        return routeTag;
    }

    public String getRouteTitle() {
        return routeTitle;
    }

    public String getDirectionTag() {
        return directionTag;
    }

    public String getDirectionTitle() {
        return directionTitle;
    }

    public static String formatRouteTitle(String routeTitle) {
        return routeTitle.replaceAll("-", " ");
    }

    public static String formatDirectionTitle(String directionTitle) {
        return directionTitle.replaceAll("-.*towards", "»");
    }

    public static String formatPrediction(List<Integer> predictions) {
        if (predictions.isEmpty()) return "N/A";

        StringBuilder builder = new StringBuilder();
        builder.append(predictions.get(0) == 0 ? "Due" : predictions.get(0));
        if (predictions.size() > 1) {
            builder.append(" & ");
            builder.append(predictions.get(1) == 0 ? "Due" : predictions.get(1));
        }

        return builder.toString();
    }

    public static String formatMinutesLabel(List<Integer> predictions) {
        if (predictions.isEmpty()) return "";

        int lastPrediction = predictions.get(predictions.size() - 1);
        if (lastPrediction == 0)
            return "";
        else if (lastPrediction == 1)
            return "minute";
        else
            return "minutes";
    }

}
