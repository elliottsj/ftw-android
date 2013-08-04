package com.afollestad.silk;

import android.content.Context;
import android.content.SharedPreferences;
import android.content.res.Configuration;
import android.net.ConnectivityManager;
import android.net.NetworkInfo;
import android.os.Environment;

import java.io.File;

/**
 * Various convenience methods.
 *
 * @author Aidan Follestad (afollestad)
 */
public class Silk {

    /**
     * Checks if the device is currently online, works for both wifi and mobile networks.
     */
    public static boolean isOnline(Context context) {
        if (context == null)
            return false;
        boolean state = false;
        ConnectivityManager cm = (ConnectivityManager) context
                .getSystemService(Context.CONNECTIVITY_SERVICE);
        NetworkInfo wifiNetwork = cm.getNetworkInfo(ConnectivityManager.TYPE_WIFI);
        if (wifiNetwork != null)
            state = wifiNetwork.isConnectedOrConnecting();
        NetworkInfo mobileNetwork = cm.getNetworkInfo(ConnectivityManager.TYPE_MOBILE);
        if (mobileNetwork != null)
            state = mobileNetwork.isConnectedOrConnecting();
        NetworkInfo activeNetwork = cm.getActiveNetworkInfo();
        if (activeNetwork != null)
            state = activeNetwork.isConnectedOrConnecting();
        return state;
    }

    /**
     * Detects whether or not the device is a tablet.
     */
    public static boolean isTablet(Context context) {
        return (context.getResources().getConfiguration().screenLayout & Configuration.SCREENLAYOUT_SIZE_MASK) >= Configuration.SCREENLAYOUT_SIZE_LARGE;
    }

    /**
     * Clears out preferences and files persisted for Silk.
     */
    public static void clearPersistence(Context context) {
        SharedPreferences prefs = context.getSharedPreferences("feed_last_update", 0);
        prefs.edit().clear().commit();
        new File(Environment.getExternalStorageDirectory(), "Silk").delete();
    }
}