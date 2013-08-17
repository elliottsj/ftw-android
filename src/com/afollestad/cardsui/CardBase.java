package com.afollestad.cardsui;

import android.graphics.drawable.Drawable;
import com.afollestad.silk.cache.SilkComparable;

/**
 * @author Aidan Follestad (afollestad)
 */
public interface CardBase extends SilkComparable<CardBase> {

    public abstract String getTitle();

    public abstract String getContent();

    public abstract boolean isHeader();

    public abstract boolean isClickable();

    public abstract int getPopupMenu();

    public abstract Card.CardMenuListener getPopupListener();

    public abstract Drawable getThumbnail();

    public abstract int getLayout();
}
