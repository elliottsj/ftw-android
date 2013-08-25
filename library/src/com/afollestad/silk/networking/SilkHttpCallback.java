package com.afollestad.silk.networking;

/**
 * @author Aidan Follestad (afollestad)
 */
public interface SilkHttpCallback {

    public void onComplete(SilkHttpResponse response);

    public void onError(SilkHttpException e);
}
