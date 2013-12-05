package com.elliottsj.ttc_ftw.adapters;

import android.content.Context;
import android.view.View;
import android.widget.TextView;
import com.afollestad.cardsui.Card;
import com.afollestad.cardsui.CardAdapter;
import com.elliottsj.ttc_ftw.R;
import com.elliottsj.ttc_ftw.cards.RouteCard;

/**
 * A {@link com.afollestad.cardsui.CardAdapter} that displays {@link com.elliottsj.ttc_ftw.cards.RouteCard} and
 * {@link com.afollestad.cardsui.CardHeader} objects in a {@link com.afollestad.cardsui.CardListView}.
 */
public class RouteCardAdapter extends CardAdapter<Card> {

    /**
     * Initializes a new CardAdapter instance.
     *
     * @param context       The context used to inflate layouts and retrieve resources.
     * @param cardLayoutRes Sets a custom layout to be used for all cards (not including headers) in the adapter.
     */
    public RouteCardAdapter(Context context, int cardLayoutRes) {
        super(context, cardLayoutRes);
    }

    @Override
    public View onViewCreated(int index, View recycled, Card item) {
        TextView description = (TextView) recycled.findViewById(R.id.description);
        if (description != null) onProcessDescription(description, (RouteCard) item);
        TextView prediction = (TextView) recycled.findViewById(R.id.prediction);
        if (prediction != null) onProcessPrediction(prediction, (RouteCard) item);

        return super.onViewCreated(index, recycled, item);
    }

    protected boolean onProcessDescription(TextView description, RouteCard card) {
        description.setText(card.getDescription());
        return false;
    }

    protected boolean onProcessPrediction(TextView prediction, RouteCard card) {
        prediction.setText(card.getPrediction());
        return false;
    }


}
