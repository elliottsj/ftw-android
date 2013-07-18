package com.afollestad.silk.views;

import android.content.Context;
import android.graphics.Typeface;
import android.util.AttributeSet;
import android.widget.EditText;

/**
 * A EditView that automatically sets its typeface to Roboto Light (a thinner version of Roboto). The font is loaded
 * from the library's assets so it will work on any version of Android.
 *
 * @author Aidan Follestad
 */
public class SilkCondensedEditText extends EditText {

    public SilkCondensedEditText(Context context) {
        super(context);
        init(context);
    }

    public SilkCondensedEditText(Context context, AttributeSet attrs) {
        super(context, attrs);
        init(context);
    }

    public SilkCondensedEditText(Context context, AttributeSet attrs, int defStyle) {
        super(context, attrs, defStyle);
        init(context);
    }

    private void init(Context context) {
        Typeface tf = Typeface.createFromAsset(context.getAssets(), "Roboto-Condensed.ttf");
        setTypeface(tf);
    }
}
