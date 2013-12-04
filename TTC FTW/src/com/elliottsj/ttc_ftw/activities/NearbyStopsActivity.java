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

        cardAdapter.add(new CardHeader("College St At Beverly St"));
        cardAdapter.add(new Card("East - 506 Carlton towards Main Street Station", "2 minutes"));

        cardAdapter.add(new CardHeader("Eglinton Ave East At Redpath Ave"));
        cardAdapter.add(new Card("South - 103 Mount Pleasant North towards Eglinton Station", "2 minutes"));
        cardAdapter.add(new Card("West - 54 Lawrence East towards Eglinton Station", "3 minutes"));



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
