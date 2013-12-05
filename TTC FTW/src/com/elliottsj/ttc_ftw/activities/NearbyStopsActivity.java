package com.elliottsj.ttc_ftw.activities;

import android.app.Activity;
import android.os.Bundle;
import android.view.MenuItem;
import android.view.View;
import android.widget.Toast;
import com.afollestad.cardsui.*;
import com.elliottsj.ttc_ftw.R;
import com.elliottsj.ttc_ftw.adapters.RouteCardAdapter;
import com.elliottsj.ttc_ftw.cards.RouteCard;

public class NearbyStopsActivity extends Activity implements CardHeader.ActionListener {

    private CardListView cardList;

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.main);

        setTitle("TTC: Faster Than Walking");

        cardList = (CardListView) findViewById(R.id.card_list);

        RouteCardAdapter cardAdapter = new RouteCardAdapter(this, R.layout.route_card);

        cardAdapter.add(new CardHeader("College St At Beverly St").setAction("Save", new CardHeader.ActionListener() {
            @Override
            public void onClick(CardHeader header) {
                Toast.makeText(NearbyStopsActivity.this, header.getTitle() + " saved", Toast.LENGTH_SHORT).show();
            }
        }));
        cardAdapter.add(new RouteCard("506 Carlton", "East to Main Street Station", 2).setPopupMenu(R.menu.route, new Card.CardMenuListener<Card>() {
            @Override
            public void onMenuItemClick(Card card, MenuItem item) {
                Toast.makeText(NearbyStopsActivity.this, "Menu item clicked", Toast.LENGTH_SHORT).show();
            }
        }));

        cardAdapter.add(new CardHeader("Eglinton Ave East At Redpath Ave"));
        cardAdapter.add(new RouteCard("54 Lawrence East", "West to Eglinton Station", 3));

        cardList.setAdapter(cardAdapter);
        cardList.setOnCardClickListener(new CardListView.CardClickListener() {
            @Override
            public void onCardClick(int index, CardBase card, View view) {
                Toast.makeText(NearbyStopsActivity.this, "Card clicked", Toast.LENGTH_SHORT).show();
            }
        });
    }

    @Override
    public void onClick(CardHeader header) {

    }
}
