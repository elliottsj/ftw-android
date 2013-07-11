package com.afollestad.silk;

import android.content.Context;
import android.net.ConnectivityManager;
import android.net.NetworkInfo;

/**
 * Various convenience methods.
 *
 * @author Aidan Follestad (afollestad)
 */
public class Utils {

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
}