package com.afollestad.cardsui;

import android.content.Context;
import android.view.MenuInflater;
import android.view.MenuItem;
import android.view.View;
import android.widget.ImageView;
import android.widget.PopupMenu;
import android.widget.TextView;
import com.afollestad.silk.adapters.SilkAdapter;

/**
 * A {@link SilkAdapter} that displays {@link Card} and {@link CardHeader} objects in a {@link CardListView}.
 *
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

    /**
     * Sets the accent color used on card titles and header action buttons.
     * You <b>should</b> call this method before adding any cards to the adapter to avoid issues.
     *
     * @param color The resolved color to use as an accent.
     */
    public final CardAdapter setAccentColor(int color) {
        mAccentColor = color;
        return this;
    }

    /**
     * Sets the accent color resource used on card titles and header action buttons.
     * You <b>should</b> call this method before adding any cards to the adapter to avoid issues.
     *
     * @param colorRes The color resource ID to use as an accent.
     */
    public final CardAdapter setAccentColorRes(int colorRes) {
        setAccentColor(getContext().getResources().getColor(colorRes));
        return this;
    }

    /**
     * Sets a popup menu used for every card in the adapter, this will not override individual card popup menus.
     * You <b>should</b> call this method before adding any cards to the adapter to avoid issues.
     *
     * @param menuRes  The menu resource ID to use for the card's popup menu.
     * @param listener A listener invoked when an option in the popup menu is tapped by the user.
     */
    public final CardAdapter setPopupMenu(int menuRes, Card.CardMenuListener listener) {
        mPopupMenu = menuRes;
        mPopupListener = listener;
        return this;
    }

    /**
     * Sets whether or not cards in the adapter are clickable, setting it to false will turn card's list selectors off
     * and the list's OnItemClickListener will not be called. This <b>will</b> override individual isClickable values
     * set to {@link Card}s.
     */
    public final CardAdapter setCardsClickable(boolean clickable) {
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
        TextView subtitle = (TextView) view.findViewById(R.id.subtitle);
        if (header.getContent() != null && !header.getContent().trim().isEmpty()) {
            subtitle.setVisibility(View.VISIBLE);
            subtitle.setText(header.getContent());
        } else subtitle.setVisibility(View.GONE);
        TextView button = (TextView) view.findViewById(R.id.button);
        if (header.getActionCallback() != null) {
            button.setVisibility(View.VISIBLE);
            button.setBackgroundColor(mAccentColor);
            String title = header.getActionTitle();
            if (header.getActionTitle() == null || header.getActionTitle().trim().isEmpty())
                title = getContext().getString(R.string.see_more);
            button.setText(title);
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

    private void setupThumbnail(Card card, ImageView view) {
        if (card.getThumbnail() == null) {
            view.setVisibility(View.GONE);
            return;
        }
        view.setVisibility(View.VISIBLE);
        view.setImageDrawable(card.getThumbnail());
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
        setupThumbnail(item, (ImageView) recycled.findViewById(R.id.thumbnail));
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