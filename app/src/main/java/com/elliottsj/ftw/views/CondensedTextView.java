package com.elliottsj.ftw.views;

import android.content.Context;
import android.graphics.Typeface;
import android.util.AttributeSet;
import android.widget.TextView;

/**
 * A TextView that automatically sets its typeface to Roboto Condensed. The font is loaded
 * from the library's assets so it will work on any version of Android.
 */
public class CondensedTextView extends TextView {

    public CondensedTextView(Context context) {
        super(context);
        init(context);
    }

    public CondensedTextView(Context context, AttributeSet attrs) {
        super(context, attrs);
        init(context);
    }

    public CondensedTextView(Context context, AttributeSet attrs, int defStyle) {
        super(context, attrs, defStyle);
        init(context);
    }

    protected void init(Context context) {
        if (!isInEditMode()) {
            try {
                Typeface tf = Typeface.createFromAsset(context.getAssets(), "Roboto-Condensed.ttf");
                setTypeface(tf);
            } catch (RuntimeException e) {
                throw new RuntimeException("Make sure you copied the 'assets' folder from Silk to your own project; " + e.getMessage());
            }
        }
    }
}
