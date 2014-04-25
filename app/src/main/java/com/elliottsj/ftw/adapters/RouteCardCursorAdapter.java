package com.elliottsj.ftw.adapters;

import android.content.Context;
import android.database.Cursor;
import android.view.MenuItem;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;

import com.afollestad.cardsui.CardBase;
import com.afollestad.cardsui.CardCursorAdapter;
import com.afollestad.cardsui.CardHeader;
import com.elliottsj.ftw.R;
import com.elliottsj.ftw.cards.RouteCard;
import com.elliottsj.ftw.loaders.PredictionsLoader;
import com.elliottsj.ftw.provider.NextbusProvider;

import net.sf.nextbus.publicxmlfeed.domain.PredictionGroup;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

/**
 * A {@link com.afollestad.cardsui.CardAdapter} that displays {@link com.elliottsj.ftw.cards.RouteCard} and
 * {@link com.afollestad.cardsui.CardHeader} objects in a {@link com.afollestad.cardsui.CardListView}.
 */
public class RouteCardCursorAdapter extends CardCursorAdapter<CardBase> {

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
            remove(card);
        }
    }

    @Override
    public void populateArray(Cursor cursor) {
        String stopTitle = null;
        cursor.moveToFirst();
        while (!cursor.isAfterLast()) {
            String newStopTitle = cursor.getString(cursor.getColumnIndexOrThrow(NextbusProvider.SAVED_STOPS.COLUMN_STOP_TITLE));
            if (newStopTitle == null)
                throw new RuntimeException("Stop title is null");
            if (!newStopTitle.equals(stopTitle)) {
                stopTitle = newStopTitle;
                // Add a header card
                add(new CardHeader(stopTitle));
            }
            // Add a route card
            add(RouteCard.fromCursor(cursor));

            // Advance the cursor
            cursor.moveToNext();
        }
    }
    
    public void bindPredictions(List<PredictionGroup> predictions) {
        if (predictions == null) {
            int cardCount = getCount();
            for (int i = 0; i < cardCount; i++) {
                CardBase card = getItem(i);
                if (card instanceof RouteCard) {
                    RouteCard routeCard = (RouteCard) card;
                    routeCard.setPrediction(-1);
                }
            }
        } else {
            Map<String, Map<String, Map<String, Map<String, List<Integer>>>>> predictionsMap = PredictionsLoader.predictionsAsMap(predictions);

            int cardCount = getCount();
            for (int i = 0; i < cardCount; i++) {
                CardBase card = getItem(i);
                if (card instanceof RouteCard) {
                    RouteCard routeCard = (RouteCard) card;
                    String agencyTag = routeCard.getAgencyTag();
                    String routeTag = routeCard.getRouteTag();
                    String directionTag = routeCard.getDirectionTag();
                    String stopTag = routeCard.getStopTag();

                    Map<String, Map<String, List<Integer>>> directionMap = predictionsMap.get(agencyTag).get(routeTag);
                    if (directionMap != null) {
                        Map<String, List<Integer>> stopMap = directionMap.get(directionTag);
                        if (stopMap != null) {
                            // Predictions are available for this direction
                            List<Integer> stopPredictions = stopMap.get(stopTag);
                            if (!stopPredictions.isEmpty()) {
                                int prediction = stopPredictions.get(0);
                                routeCard.setPrediction(prediction);
                            }
                        } else {
                            // There are no predictions for this direction
                            routeCard.setPrediction(-1);
                        }
                    }
                }
            }
        }
        notifyDataSetChanged();
    }

    /**
     * @return the agency tag of a card in this adapter, or null if there is none
     */
    public String getAgencyTag() {
        int cardCount = getCount();
        for (int i = 0; i < cardCount; i++) {
            CardBase card = getItem(i);
            if (card instanceof RouteCard) {
                RouteCard routeCard = (RouteCard) card;
                return routeCard.getAgencyTag();
            }
        }

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

}
