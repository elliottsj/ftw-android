package com.afollestad.cardsui;

import android.content.Context;
import com.afollestad.silk.cache.SilkComparable;

/**
 * @author Aidan Follestad (afollestad)
 */
public class Card implements SilkComparable<Card> {

    protected Card() {
    }

    protected Card(String title, boolean isHeader) {
        this.title = title;
        this.isHeader = isHeader;
    }

    public Card(Context context, int titleRes, String content) {
        this.title = context.getString(titleRes);
        this.content = content;
    }

    public Card(String title, String content) {
        this.title = title;
        this.content = content;
    }

    private String title;
    private String content;
    private boolean isHeader;

    public String getTitle() {
        return title;
    }

    public String getContent() {
        return content;
    }

    public boolean isHeader() {
        return isHeader;
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
