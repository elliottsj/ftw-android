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
public class SilkEditText extends EditText {

    public SilkEditText(Context context) {
        super(context);
        init();
    }

    public SilkEditText(Context context, AttributeSet attrs) {
        super(context, attrs);
        init();
    }

    public SilkEditText(Context context, AttributeSet attrs, int defStyle) {
        super(context, attrs, defStyle);
        init();
    }

    private void init() {
        Typeface tf = Typeface.createFromAsset(getContext().getAssets(), "Roboto-Light.ttf");
        setTypeface(tf);
    }
}
