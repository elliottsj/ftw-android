package com.afollestad.cardsui;

import android.content.Context;
import android.util.AttributeSet;
import android.view.View;
import android.widget.AdapterView;
import android.widget.ListAdapter;
import android.widget.ListView;

/**
 * A {@link ListView} that automates many card related things, such as disabling the background list selector,
 * removing the list divider, and calling a {@link CardHeader}'s ActionListener when tapped by the user.
 *
 * @author Aidan Follestad (afollestad)
 */
public class CardListView extends ListView implements AdapterView.OnItemClickListener {

    public CardListView(Context context) {
        super(context);
        init();
    }

    public CardListView(Context context, AttributeSet attrs) {
        super(context, attrs);
        init();
    }

    public CardListView(Context context, AttributeSet attrs, int defStyle) {
        super(context, attrs, defStyle);
        init();
    }

    private OnItemClickListener mItemClickListener;

    private void init() {
        setDivider(null);
        setDividerHeight(0);
        int gray = getResources().getColor(R.color.card_gray);
        setBackgroundColor(gray);
        setCacheColorHint(gray);
        setSelector(android.R.color.transparent);
        super.setOnItemClickListener(this);
    }

    /**
     * @deprecated Use {@link #setAdapter(CardAdapter)} instead.
     */
    @Override
    public void setAdapter(ListAdapter adapter) {
        if (adapter instanceof CardAdapter) {
            setAdapter((CardAdapter) adapter);
            return;
        }
        throw new RuntimeException("The CardListView only accepts CardAdapters.");
    }

    /**
     * Sets the list's adapter, enforces the use of only a CardAdapter, not any other type of adapter
     */
    public void setAdapter(CardAdapter adapter) {
        super.setAdapter(adapter);
    }

    @Override
    public void setOnItemClickListener(OnItemClickListener listener) {
        mItemClickListener = listener;
    }

    @Override
    public void onItemClick(AdapterView<?> parent, View view, int position, long id) {
        CardBase item = ((CardAdapter) getAdapter()).getItem(position);
        if (item.isHeader()) {
            CardHeader header = (CardHeader) item;
            if (header.getActionCallback() != null)
                header.getActionCallback().onClick(header);
            else if (mItemClickListener != null) mItemClickListener.onItemClick(parent, view, position, id);
        } else if (mItemClickListener != null) mItemClickListener.onItemClick(parent, view, position, id);
    }
}