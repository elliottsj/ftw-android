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

    protected int getAddIndex() {
        return -1;
    }

    protected void onPostLoad(List<ItemType> results) {
        if (getAddIndex() == -1)
            getAdapter().add(results);
        else getAdapter().add(getAddIndex(), results);
        setLoadComplete(false);
    }

    protected abstract List<ItemType> refresh() throws Exception;

    protected abstract void onError(Exception e);

    public void performRefresh(boolean showProgress) {
        if (isLoading()) return;
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