package com.afollestad.cardsui;

import android.content.Context;
import android.view.MenuInflater;
import android.view.MenuItem;
import android.view.View;
import android.widget.PopupMenu;
import android.widget.TextView;
import com.afollestad.silk.adapters.SilkAdapter;

/**
 * @author Aidan Follestad (afollestad)
 */
public class CardAdapter extends SilkAdapter<Card> {

    public CardAdapter(Context context) {
        super(context);
    }

    private int mAccentColor;
    private int mPopupMenu;
    private Card.CardMenuListener mPopupListener;
    private boolean mCardsClickable = true;

    @Override
    public boolean isEnabled(int position) {
        Card item = getItem(position);
        if (!mCardsClickable && !item.isHeader()) return false;
        if (item.isHeader())
            return ((CardHeader) item).getActionCallback() != null;
        return item.isClickable();
    }

    public CardAdapter setAccentColor(int color) {
        mAccentColor = color;
        return this;
    }

    public CardAdapter setAccentColorRes(int colorRes) {
        setAccentColor(getContext().getResources().getColor(colorRes));
        return this;
    }

    public CardAdapter setPopupMenu(int menuRes, Card.CardMenuListener listener) {
        mPopupMenu = menuRes;
        mPopupListener = listener;
        return this;
    }

    public CardAdapter setCardsClickable(boolean clickable) {
        mCardsClickable = clickable;
        return this;
    }

    @Override
    public int getLayout(int type) {
        if (type == 1)
            return R.layout.list_item_header;
        return R.layout.list_item_card;
    }

    private void setupHeader(CardHeader header, View view) {
        ((TextView) view.findViewById(R.id.title)).setText(header.getTitle());
        TextView button = (TextView) view.findViewById(R.id.button);
        if (header.getActionCallback() != null) {
            button.setVisibility(View.VISIBLE);
            button.setBackgroundColor(mAccentColor);
            button.setText(header.getActionTitle());
        } else button.setVisibility(View.GONE);
    }

    private void setupMenu(final Card card, final View view) {
        int menuRes = mPopupMenu;
        if (card.getPopupMenu() != 0) menuRes = card.getPopupMenu();
        if (menuRes == 0) {
            // No menu for the adapter or the card
            view.setVisibility(View.GONE);
            return;
        }
        view.setVisibility(View.VISIBLE);
        view.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                int menuRes = mPopupMenu;
                if (card.getPopupMenu() != 0) menuRes = card.getPopupMenu();
                Context themedContext = getContext();
                themedContext.setTheme(android.R.style.Theme_Holo_Light);
                PopupMenu popup = new PopupMenu(themedContext, view);
                MenuInflater inflater = popup.getMenuInflater();
                inflater.inflate(menuRes, popup.getMenu());
                popup.setOnMenuItemClickListener(new PopupMenu.OnMenuItemClickListener() {
                    @Override
                    public boolean onMenuItemClick(MenuItem item) {
                        if (card.getPopupMenu() > 0 && card.getPopupListener() != null) {
                            // This individual card has it unique menu
                            card.getPopupListener().onMenuItemClick(card, item);
                        } else if (mPopupListener != null) {
                            // The card does not have a unique menu, use the adapter's default
                            mPopupListener.onMenuItemClick(card, item);
                        }
                        return false;
                    }
                });
                popup.show();
            }
        });
    }

    @Override
    public View onViewCreated(int index, View recycled, Card item) {
        if (item.isHeader()) {
            final CardHeader header = (CardHeader) item;
            setupHeader(header, recycled);
            return recycled;
        }
        TextView title = (TextView) recycled.findViewById(R.id.title);
        title.setText(item.getTitle());
        title.setTextColor(mAccentColor);
        ((TextView) recycled.findViewById(R.id.content)).setText(item.getContent());
        setupMenu(item, recycled.findViewById(R.id.menu));
        return recycled;
    }

    @Override
    public int getViewTypeCount() {
        return 2;
    }

    @Override
    public int getItemViewType(int position) {
        if (getItem(position).isHeader()) return 1;
        return 0;
    }
}