package com.elliottsj.ftw.cards;

import android.database.Cursor;
import android.graphics.drawable.Drawable;

import com.afollestad.cardsui.Card;
import com.afollestad.cardsui.CardBase;
import com.afollestad.cardsui.CardHeader;
import com.elliottsj.ftw.R;
import com.elliottsj.ftw.provider.NextbusProvider;

/**
 * Represents a single card displayed in a {@link com.elliottsj.ftw.adapters.RouteCardCursorAdapter}.
 */
public class RouteCard implements CardBase {

    private boolean isHeader;

    private String agencyTag;
    private String routeTag;
    private String directionTag;
    private String stopTag;
    private String routeTitle;
    private String direction;
    private int prediction;

    public RouteCard(String agencyTag, String routeTag, String directionTag, String stopTag, String routeTitle, String direction, boolean isHeader) {
        this.agencyTag = agencyTag;
        this.routeTag = routeTag;
        this.directionTag = directionTag;
        this.stopTag = stopTag;
        this.routeTitle = formatRouteTitle(routeTitle);
        this.direction = formatDirectionTitle(direction);
        this.isHeader = isHeader;
        this.prediction = -1;
    }

    public RouteCard(String agencyTag, String routeTag, String directionTag, String stopTag, String routeTitle, String direction) {
        this(agencyTag, routeTag, directionTag, stopTag, routeTitle, direction, false);
    }

    public RouteCard(String routeTitle, String direction) {
        this(null, null, null, null, routeTitle, direction, false);
    }

    /**
     * Create a RouteCard using columns from {@link com.elliottsj.ftw.provider.NextbusProvider.SAVED_STOPS}
     *
     * @param cursor a cursor created by {@link com.elliottsj.ftw.provider.NextbusProvider}
     * @return a RouteCard
     */
    public static RouteCard fromCursor(Cursor cursor) {
        String agencyTag = cursor.getString(cursor.getColumnIndexOrThrow(NextbusProvider.SAVED_STOPS.COLUMN_AGENCY_TAG));
        String routeTag = cursor.getString(cursor.getColumnIndexOrThrow(NextbusProvider.SAVED_STOPS.COLUMN_ROUTE_TAG));
        String directionTag = cursor.getString(cursor.getColumnIndexOrThrow(NextbusProvider.SAVED_STOPS.COLUMN_DIRECTION_TAG));
        String stopTag = cursor.getString(cursor.getColumnIndexOrThrow(NextbusProvider.SAVED_STOPS.COLUMN_STOP_TAG));
        String routeTitle = cursor.getString(cursor.getColumnIndexOrThrow(NextbusProvider.SAVED_STOPS.COLUMN_ROUTE_TITLE));
        String direction = cursor.getString(cursor.getColumnIndexOrThrow(NextbusProvider.SAVED_STOPS.COLUMN_DIRECTION_TITLE));
        return new RouteCard(agencyTag, routeTag, directionTag, stopTag, routeTitle, direction);
    }

    public String getAgencyTag() {
        return agencyTag;
    }

    public String getRouteTag() {
        return routeTag;
    }

    public String getDirectionTag() {
        return directionTag;
    }

    public String getStopTag() {
        return stopTag;
    }

    public String getDirection() {
        return direction;
    }

    public String getPredictionString() {
        return prediction == -1 ? "" : String.format("%d minutes", prediction);
    }

    public void setPrediction(int prediction) {
        this.prediction = prediction;
    }

    @Override
    public String getTitle() {
        return routeTitle;
    }

    @Override
    public String getContent() {
        return direction;
    }

    @Override
    public boolean isHeader() {
        return isHeader;
    }

    @Override
    public boolean isClickable() {
        return true;
    }

    @Override
    public int getPopupMenu() {
        return 0;
    }

    @Override
    public CardHeader.ActionListener getActionCallback() {
        return null;
    }

    @Override
    public String getActionTitle() {
        return null;
    }

    @Override
    public Card.CardMenuListener<RouteCard> getPopupListener() {
        return null;
    }

    @Override
    public Drawable getThumbnail() {
        return null;
    }

    @Override
    public int getLayout() {
        return R.layout.route_card;
    }

    private static String formatRouteTitle(String nextbusTitle) {
        return nextbusTitle.replaceAll("-", " ");
    }

    private static String formatDirectionTitle(String nextbusTitle) {
        return nextbusTitle.replaceAll("-.*(?=towards)", "");
    }

}
