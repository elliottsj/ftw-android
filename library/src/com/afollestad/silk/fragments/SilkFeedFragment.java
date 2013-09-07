package com.afollestad.silk.fragments;

import android.os.Bundle;
import android.view.View;
import com.afollestad.silk.Silk;
import com.afollestad.silk.caching.SilkComparable;

import java.util.List;

/**
 * @author Aidan Follestad (afollestad)
 */
public abstract class SilkFeedFragment<ItemType extends SilkComparable<ItemType>> extends SilkListFragment<ItemType> {

    public static class OfflineException extends Exception {
        public OfflineException() {
            super("You are currently offline.");
        }
    }

    protected void onPostLoad(List<ItemType> results) {
        getAdapter().set(results);
    }

    protected abstract List<ItemType> refresh() throws Exception;

    protected abstract void onError(Exception e);

    public void performRefresh(boolean showProgress) {
        if (showProgress) setLoading(true);
        Thread t = new Thread(new Runnable() {
            @Override
            public void run() {
                try {
                    if (!Silk.isOnline(getActivity())) throw new OfflineException();
                    final List<ItemType> items = refresh();
                    runOnUiThread(new Runnable() {
                        @Override
                        public void run() {
                            onPostLoad(items);
                            setLoadComplete(false);
                        }
                    });
                } catch (final Exception e) {
                    e.printStackTrace();
                    runOnUiThread(new Runnable() {
                        @Override
                        public void run() {
                            onError(e);
                            setLoadComplete(true);
                        }
                    });
                }
            }
        });
        t.setPriority(Thread.MAX_PRIORITY);
        t.start();
    }

    @Override
    public void onViewCreated(View view, Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);
        performRefresh(true);
    }
}