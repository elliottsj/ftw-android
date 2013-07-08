package com.afollestad.silk;

import android.content.Context;
import android.net.ConnectivityManager;
import android.net.NetworkInfo;
import android.util.Base64;

import java.io.*;

/**
 * Various convenience methods used throughout the app.
 *
 * @author Aidan Follestad
 */
public class Utils {

    public static Object deserialize(String input) {
        try {
            byte[] data = Base64.decode(input, Base64.DEFAULT);
            ObjectInputStream ois = new ObjectInputStream(new ByteArrayInputStream(data));
            Object o = ois.readObject();
            ois.close();
            return o;
        } catch (Exception e) {
            e.printStackTrace();
            return null;
        }
    }

    public static String serialize(Serializable tweet) {
        try {
            ByteArrayOutputStream baos = new ByteArrayOutputStream();
            ObjectOutputStream oos = new ObjectOutputStream(baos);
            oos.writeObject(tweet);
            oos.close();
            return Base64.encodeToString(baos.toByteArray(), Base64.DEFAULT);
        } catch (Exception e) {
            e.printStackTrace();
            return "";
        }
    }

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