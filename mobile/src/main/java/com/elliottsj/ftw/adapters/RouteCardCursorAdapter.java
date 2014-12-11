package com.elliottsj.ftw.adapters;

import android.content.Context;
import android.database.Cursor;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;

import com.afollestad.cardsui.CardBase;
import com.afollestad.cardsui.CardCursorAdapter;
import com.afollestad.cardsui.CardHeader;
import com.elliottsj.ftw.R;
import com.elliottsj.ftw.cards.RouteCard;
import com.elliottsj.ftw.provider.NextbusProvider;

import net.sf.nextbus.publicxmlfeed.domain.Prediction;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.Iterator;
import java.util.List;
import java.util.Map;

/**
 * A {@link com.afollestad.cardsui.CardAdapter} that displays {@link com.elliottsj.ftw.cards.RouteCard} and
 * {@link com.afollestad.cardsui.CardHeader} objects in a {@link com.afollestad.cardsui.CardListView}.
 */
public class RouteCardCursorAdapter extends CardCursorAdapter<CardBase> implements Iterable<RouteCard> {

    /**
     * Initializes a new CardAdapter instance.
     *
     * @param context       The context used to inflate layouts and retrieve resources.
     */
    public RouteCardCursorAdapter(Context context) {
        super(context);
        registerLayout(R.layout.route_card);

    }

    @Override
    public View getView(int position, View convertView, ViewGroup parent) {
        View view = super.getView(position, convertView, parent);
        CardBase card = getItem(position);
        if (card instanceof RouteCard) {
            RouteCard routeCard = (RouteCard) card;

            //noinspection ConstantConditions
            TextView direction = (TextView) view.findViewById(R.id.direction);
            if (direction != null)
                direction.setText(routeCard.getDirection());

            TextView prediction = (TextView) view.findViewById(R.id.prediction);
            if (prediction != null)
                prediction.setText(routeCard.getPredictionString());
        }

        return view;
    }

    public void removeCard(RouteCard card) {
        int cardCount = getCount();
        if (cardCount <= 2) {
            clear();
        } else {
            int cardPosition = getPosition(card);
            // If previous card is a header and the next card is a header (or there is no next card),
            // then delete the header too
            if (getItem(cardPosition - 1).isHeader() && (cardPosition == cardCount - 1 || getItem(cardPosition + 1).isHeader()))
                remove(getItem(cardPosition - 1));

            // Remove the card
            remove(card);
        }
    }

    /**
     * Populate this adapter with route cards from a cursor obtained via
     * {@link com.elliottsj.ftw.provider.NextbusProvider} with columns
     * {@link com.elliottsj.ftw.provider.NextbusProvider#SAVED_STOPS_CURSOR_COLUMNS}
     *
     * @param cursor a cursor obtained from {@link com.elliottsj.ftw.provider.NextbusProvider}
     */
    @Override
    public void populateArray(Cursor cursor) {
        for (Map.Entry<String, List<RouteCard>> entry : getStopRouteGroups(cursor).entrySet()) {
            add(new CardHeader(entry.getKey()));
            for (RouteCard routeCard : entry.getValue())
                add(routeCard);
        }
    }
    
    public void bindPredictions(Map<String, Map<String, List<Prediction>>> predictions) {
        if (predictions == null) {
            // Reset the predictions in existing cards
            for (RouteCard routeCard : this) {
                routeCard.setPrediction(-1);
            }
        } else {
            for (RouteCard routeCard : this) {
                String routeTag = routeCard.getRouteTag();
                String stopTag = routeCard.getStopTag();

                Map<String, List<Prediction>> stopMap = predictions.get(routeTag);
                if (stopMap != null) {
                    // Predictions are available for this route
                    List<Prediction> stopPredictions = stopMap.get(stopTag);
                    if (stopPredictions != null && !stopPredictions.isEmpty()) {
                        Prediction firstPrediction = stopPredictions.get(0);
                        routeCard.setPrediction(firstPrediction.getMinutes());
                    }
                } else {
                    // There are no predictions for this route
                    routeCard.setPrediction(-1);
                }
            }
        }
        notifyDataSetChanged();
    }

    /**
     * @return the agency tag of a card in this adapter, or null if there is none
     */
    @SuppressWarnings("LoopStatementThatDoesntLoop")
    public String getAgencyTag() {
        for (RouteCard routeCard : this)
            return routeCard.getAgencyTag();

        return null;
    }

    /**
     * Get a map of stop tags stored in this adapter
     *
     * @return a map of (route tag -> list of stop tags)
     */
    public HashMap<String, List<String>> getStopsMap() {
        HashMap<String, List<String>> stopsMap = new HashMap<String, List<String>>();

        int cardCount = getCount();
        for (int i = 0; i < cardCount; i++) {
            CardBase card = getItem(i);
            if (card instanceof RouteCard) {
                RouteCard routeCard = (RouteCard) card;
                String routeTag = routeCard.getRouteTag();
                String stopTag = routeCard.getStopTag();

                // Create the ArrayList of stop tags if necessary
                if (!stopsMap.containsKey(routeTag))
                    stopsMap.put(routeTag, new ArrayList<String>());

                stopsMap.get(routeTag).add(stopTag);
            }
        }

        return stopsMap;
    }

    /**
     * Get a map of route cards, grouped by stop title
     *
     * @param cursor a cursor obtained from {@link com.elliottsj.ftw.provider.NextbusProvider} pointing at
     *               a row with columns specified by {@link com.elliottsj.ftw.provider.NextbusProvider#SAVED_STOPS_CURSOR_COLUMNS}
     * @return a map of (stop title -> (list of RouteCard))
     */
    private static Map<String, List<RouteCard>> getStopRouteGroups(Cursor cursor) {
        Map<String, List<RouteCard>> result = new HashMap<String, List<RouteCard>>();

        cursor.moveToFirst();
        while (!cursor.isAfterLast()) {
            String stopTitle = cursor.getString(cursor.getColumnIndexOrThrow(NextbusProvider.SAVED_STOPS.COLUMN_STOP_TITLE));
            if (!result.containsKey(stopTitle))
                result.put(stopTitle, new ArrayList<RouteCard>());

            result.get(stopTitle).add(RouteCard.fromCursor(cursor));
            cursor.moveToNext();
        }

        return result;
    }

    @Override
    public Iterator<RouteCard> iterator() {
        return new Iterator<RouteCard>() {

            private int index = -1;

            @Override
            public boolean hasNext() {
                return index < getCount() - 1;
            }

            @Override
            public RouteCard next() {
                CardBase card = getItem(index + 1);
                if (card instanceof RouteCard) {
                    // Next card is a RouteCard, so increment the index by 1 and return the card
                    index++;
                    return (RouteCard) card;
                } else {
                    // Next card is a header, so increment the index by 2 and return the card after the header
                    index += 2;
                    return (RouteCard) getItem(index);
                }
            }

            @Override
            public void remove() {
                removeCard((RouteCard) getItem(index));
            }
        };
    }

}
