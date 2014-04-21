package com.elliottsj.ftw.activities;

import android.app.Activity;
import android.os.Bundle;

import com.elliottsj.ftw.R;
import com.readystatesoftware.systembartint.SystemBarTintManager;

public abstract class TintedStatusBarActivity extends Activity {

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        // Set status bar color
        SystemBarTintManager tintManager = new SystemBarTintManager(this);
        tintManager.setStatusBarTintEnabled(true);
        tintManager.setStatusBarTintResource(R.color.ttc_red);
    }
}
