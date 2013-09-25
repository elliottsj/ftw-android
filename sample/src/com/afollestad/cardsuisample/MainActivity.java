package com.afollestad.cardsuisample;

import android.app.Activity;
import android.graphics.drawable.ColorDrawable;
import android.os.Bundle;
import android.view.MenuItem;
import android.widget.Toast;
import com.afollestad.cardsui.Card;
import com.afollestad.cardsui.CardAdapter;
import com.afollestad.cardsui.CardHeader;
import com.afollestad.cardsui.CardListView;

public class MainActivity extends Activity implements Card.CardMenuListener<Card> {

    @Override
    public void onCreate(Bundle savedInstanceState) {
        // This is quick way of theming the action bar without using styles.xml (e.g. using ActionBar Style Generator)
        getActionBar().setBackgroundDrawable(new ColorDrawable(getResources().getColor(android.R.color.holo_blue_dark)));
        getActionBar().setDisplayShowHomeEnabled(false);

        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        // Initializes a CardAdapter with a blue accent color and basic popup menu for each card
        CardAdapter<Card> cardsAdapter = new CardAdapter<Card>(this)
                .setAccentColorRes(android.R.color.holo_blue_light)
                .setPopupMenu(R.menu.card_popup, this);

        CardListView cardsList = (CardListView) findViewById(R.id.cardsList);
        cardsList.setAdapter(cardsAdapter);

        cardsAdapter.add(new CardHeader("Week Days"));
        cardsAdapter.add(new Card("Monday", "Back to work :("));
        cardsAdapter.add(new Card("Tuesday", "Arguably the worst day of the week."));
        cardsAdapter.add(new Card("Wednesday", "Hump day!"));
        cardsAdapter.add(new Card("Thursday", "Almost there..."));
        cardsAdapter.add(new Card("Friday", "We made it!"));

        cardsAdapter.add(new CardHeader("Companies")
                // The action text here is set to a string resource, if you don't specify a context and/or string the default "See More" is used
                .setAction(this, R.string.what_else, new CardHeader.ActionListener() {
                    @Override
                    public void onClick(CardHeader header) {
                        Toast.makeText(getApplicationContext(), "TODO", Toast.LENGTH_LONG).show();
                    }
                }));
        cardsAdapter.add(new Card("Google", "Android is the best!")
                .setThumbnail(this, R.drawable.android)  // sets a thumbnail image from drawable resources
                .setPopupMenu(-1, null));  // -1 disables the popup menu for this individual card
        cardsAdapter.add(new Card("Microsoft", "We're trying.")
                .setThumbnail(this, R.drawable.wp)
                .setPopupMenu(-1, null));
        cardsAdapter.add(new Card("Apple", "We added a finger print scanner, give us your money.")
                .setThumbnail(this, R.drawable.ios)
                .setPopupMenu(-1, null));
    }

    @Override
    public void onMenuItemClick(Card card, MenuItem item) {
        Toast.makeText(this, card.getTitle() + ": " + item.getTitle(), Toast.LENGTH_SHORT).show();
    }
}
