package com.elliottsj.ttc_ftw.activities;

import android.app.Fragment;
import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.MenuItem;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Toast;
import com.afollestad.cardsui.Card;
import com.afollestad.cardsui.CardBase;
import com.afollestad.cardsui.CardHeader;
import com.afollestad.cardsui.CardListView;
import com.elliottsj.ttc_ftw.R;
import com.elliottsj.ttc_ftw.adapters.RouteCardAdapter;
import com.elliottsj.ttc_ftw.cards.RouteCard;

/**
 *
 */
public class NearbyStopsFragment extends Fragment implements CardHeader.ActionListener, Card.CardMenuListener<Card> {

    private CardListView cardList;

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
        View rootView = inflater.inflate(R.layout.fragment_stops, container, false);

        cardList = (CardListView) rootView.findViewById(R.id.card_list);

        RouteCardAdapter cardAdapter = new RouteCardAdapter(getActivity(), R.layout.route_card);
        cardAdapter.setAccentColorRes(R.color.ttc_red);

        cardAdapter.add(new CardHeader("College St At Beverly St").setAction("Save", this));
        cardAdapter.add(new RouteCard("506 Carlton", "East to Main Street Station", 2).setPopupMenu(R.menu.route, this));

        cardAdapter.add(new CardHeader("Eglinton Ave E At Redpath Ave").setAction("Save", this));
        cardAdapter.add(new RouteCard("54 Lawrence E", "West to Eglinton Station", 3).setPopupMenu(R.menu.route, this));
        cardAdapter.add(new RouteCard("103 Mt Pleasant N", "South to Eglinton Station", 1).setPopupMenu(R.menu.route, this));

        cardList.setAdapter(cardAdapter);
        cardList.setOnCardClickListener(new CardListView.CardClickListener() {
            @Override
            public void onCardClick(int index, CardBase card, View view) {
                Toast.makeText(getActivity(), "Card clicked", Toast.LENGTH_SHORT).show();
            }
        });

        return rootView;
    }

    @Override
    public void onClick(CardHeader header) {
        Toast.makeText(getActivity(), header.getTitle() + " saved", Toast.LENGTH_SHORT).show();
    }

    @Override
    public void onMenuItemClick(Card card, MenuItem item) {
        Toast.makeText(getActivity(), "Menu item clicked", Toast.LENGTH_SHORT).show();
    }
}