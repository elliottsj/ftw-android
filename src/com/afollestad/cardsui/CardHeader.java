package com.afollestad.cardsui;

import android.content.Context;

/**
 * @author Aidan Follestad (afollestad)
 */
public class CardHeader extends Card {

    public CardHeader(String title) {
        super(title, null, true);
    }

    public CardHeader(String title, String subtitle) {
        super(title, subtitle, true);
    }

    public CardHeader(Context context, int titleRes) {
        this(context.getString(titleRes));
    }

    public CardHeader(Context context, String title, int subtitleRes) {
        this(title, context.getString(subtitleRes));
    }

    public CardHeader(Context context, int titleRes, String subtitle) {
        this(context.getString(titleRes), subtitle);
    }

    public CardHeader(Context context, int titleRes, int subtitleRes) {
        this(context.getString(titleRes), context.getString(subtitleRes));
    }

    private String mActionTitle;
    private ActionListener mCallback;

    public interface ActionListener {
        public void onClick(CardHeader header);
    }

    public CardHeader setAction(ActionListener callback) {
        mActionTitle = null;
        mCallback = callback;
        return this;
    }

    public CardHeader setAction(String title, ActionListener callback) {
        mActionTitle = title;
        mCallback = callback;
        return this;
    }

    public CardHeader setAction(Context context, int titleRes, ActionListener callback) {
        setAction(context.getString(titleRes), callback);
        return this;
    }

    public String getActionTitle() {
        return mActionTitle;
    }

    public ActionListener getActionCallback() {
        return mCallback;
    }
}
