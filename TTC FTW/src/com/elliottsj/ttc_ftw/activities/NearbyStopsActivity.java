package com.elliottsj.ttc_ftw.activities;

import android.app.Activity;
import android.os.Bundle;
import android.view.View;
import android.widget.Toast;
import com.afollestad.cardsui.*;
import com.elliottsj.ttc_ftw.R;
import org.simpleframework.xml.Serializer;
import org.simpleframework.xml.core.Persister;

public class NearbyStopsActivity extends Activity {

    private CardListView cardList;

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.main);

        cardList = (CardListView) findViewById(R.id.card_list);

        CardAdapter<Card> cardAdapter = new CardAdapter<Card>(this);

        cardAdapter.add(new CardHeader("Header 1"));
        cardAdapter.add(new Card("One", "Example 1"));
        cardAdapter.add(new Card("Two", "Example 2"));

        cardList.setAdapter(cardAdapter);
        cardList.setOnCardClickListener(new CardListView.CardClickListener() {
            @Override
            public void onCardClick(int index, CardBase card, View view) {
                Toast.makeText(NearbyStopsActivity.this, "Card clicked", Toast.LENGTH_SHORT).show();
            }
        });

        Serializer s = new Persister();

//        INextbusService svc = new SimplestNextbusServiceAdapter();
//        Agency ttc = svc.getAgency("ttc");
//        List<Route> ttcRoutes = svc.getRoutes(ttc);

//        Route route = Route.find(ttcRoutes, )

    }
}
