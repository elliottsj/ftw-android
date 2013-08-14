package com.afollestad.cardsui;

import android.content.Context;
import android.view.View;
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

    @Override
    public boolean isEnabled(int position) {
        if (getItem(position).isHeader()) {
            return ((CardHeader) getItem(position)).getActionCallback() != null;
        }
        return true;
    }

    public void setAccentColor(int colorRes) {
        mAccentColor = getContext().getResources().getColor(colorRes);
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