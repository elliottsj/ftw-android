package com.afollestad.cardsui;

import android.content.Context;
import android.view.MenuItem;
import com.afollestad.silk.cache.SilkComparable;

/**
 * @author Aidan Follestad (afollestad)
 */
public class Card implements SilkComparable<Card> {

    protected Card() {
    }

    protected Card(String title, String subtitle, boolean isHeader) {
        this(title, subtitle);
        this.isHeader = isHeader;
    }

    public Card(String title, String content) {
        this.isClickable = true;
        this.title = title;
        this.content = content;
    }

    public Card(Context context, String title, int contentRes) {
        this(title, context.getString(contentRes));
    }

    public Card(Context context, int titleRes, String content) {
        this(context.getString(titleRes), content);
    }

    public Card(Context context, int titleRes, int contentRes) {
        this(context.getString(titleRes), context.getString(contentRes));
    }

    public interface CardMenuListener {
        public void onMenuItemClick(Card card, MenuItem item);
    }

    private String title;
    private String content;
    private boolean isHeader;
    private int mPopupMenu;
    private CardMenuListener mPopupListener;
    private boolean isClickable;
    private Object mTag;

    public String getTitle() {
        return title;
    }

    public String getContent() {
        return content;
    }

    public boolean isHeader() {
        return isHeader;
    }

    public boolean isClickable() {
        return isClickable;
    }

    public Card setClickable(boolean clickable) {
        isClickable = clickable;
        return this;
    }

    public Object getTag() {
        return mTag;
    }

    public Card setTag(Object tag) {
        mTag = tag;
        return this;
    }

    public int getPopupMenu() {
        return mPopupMenu;
    }

    public CardMenuListener getPopupListener() {
        return mPopupListener;
    }

    public Card setPopupMenu(int menuRes, CardMenuListener listener) {
        mPopupMenu = menuRes;
        mPopupListener = listener;
        return this;
    }

    @Override
    public boolean isSameAs(Card another) {
        boolean equal = getTitle().equals(another.getTitle()) &&
                isHeader() == another.isHeader();
        if (getContent() != null) equal = equal && getContent().equals(another.getContent());
        return equal;
    }

    @Override
    public boolean shouldIgnore() {
        return isHeader;
    }
}
