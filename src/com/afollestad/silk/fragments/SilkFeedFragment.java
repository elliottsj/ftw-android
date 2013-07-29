package com.afollestad.silk.fragments;

import android.os.Bundle;
import android.view.View;
import com.afollestad.silk.R;
import com.afollestad.silk.Silk;
import com.afollestad.silk.cache.SilkComparable;

/**
 * A {@link com.afollestad.silk.fragments.SilkListFragment} that pulls data from the network, and automatically puts the retrieved data in its list.
 *
 * @author Aidan Follestad (afollestad)
 */
public abstract class SilkFeedFragment<T extends SilkComparable> extends SilkListFragment<T> {

    protected boolean mCacheEnabled = false;

    @Override
    public void onViewCreated(View view, Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);
        // If caching is enabled, the SilkCachedFeedFragment will handle this instead. super.onViewCreated() still must be called though.
        if (!mCacheEnabled) {
            // Immediately load the fragment's feed
            performRefresh(true);
        }
    }

    /**
     * Called when inheriting classes must load their feed. This is called from a separate thread so you don't
     * need to worry about threading on your own.
     */
    protected abstract T[] refresh() throws Exception;

    /**
     * Called when an error occurs while refreshing.
     */
    public abstract void onError(String message);

    /**
     * Called from a separate thread (not the UI thread) when refresh() has returned results. Can
     * be overridden to do something with the results before being added to the adapter.
     */
    protected void onPostLoad(final T[] results) {
        runOnUiThread(new Runnable() {
            @Override
            public void run() {
                getAdapter().set(results);
            }
        });
    }

    /**
     * Causes sub-fragments to pull from the network, and adds the results to the list.
     */
    public void performRefresh(boolean progress) {
        if (isLoading()) return;
        else if (!Silk.isOnline(getActivity())) {
            onError(getString(R.string.offline_error));
            setLoadComplete(true);
            return;
        }

        setLoading(progress);
        new Thread(new Runnable() {
            @Override
            public void run() {
                try {
                    final T[] results = refresh();
                    if (results != null) {
                        runOnUiThread(new Runnable() {
                            @Override
                            public void run() {
                                onPostLoad(results);
                            }
                        });
                    }
                } catch (final Exception e) {
                    e.printStackTrace();
                    runOnUiThread(new Runnable() {
                        @Override
                        public void run() {
                            setLoadComplete(true);
                            if (!Silk.isOnline(getActivity())) {
                                onError(getString(R.string.offline_error));
                            } else {
                                onError(e.getMessage());
                            }
                        }
                    });
                    return;
                }
                runOnUiThread(new Runnable() {
                    @Override
                    public void run() {
                        setLoadComplete(false);
                    }
                });
            }
        }).start();
    }
}
