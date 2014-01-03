package com.elliottsj.ftw.cards;

import com.afollestad.cardsui.Card;

/**
 * Represents a single card displayed in a {@link com.elliottsj.ftw.adapters.RouteCardAdapter}.
 */
public class RouteCard extends Card {

    private String description;
    private int prediction;

    public RouteCard(String title, String description, int prediction) {
        super(title, String.format("%s,%d", description, prediction));

        this.description = description;
        this.prediction = prediction;
    }

    @Override
    public String getContent() {
        return String.format("%s,%d", description, prediction);
    }

    public String getDescription() {
        return description;
    }

    public String getPrediction() {
        return String.format("%d minutes", prediction);
    }

}
