package com.afollestad.silk.fragments;

import android.os.Bundle;
import android.view.View;
import com.afollestad.silk.R;
import com.afollestad.silk.Utils;

/**
 * A {@link com.afollestad.silk.fragments.SilkListFragment} that pulls data from the network, and automatically puts the retrieved data in its list.
 *
 * @author Aidan Follestad (afollestad)
 */
public abstract class SilkFeedFragment<T> extends SilkListFragment<T> {

    protected boolean mCacheEnabled = false;

    @Override
    public void onViewCreated(View view, Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);
        // If caching is enabled, the SilkCachedFeedFragment will handle this instead
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
     * Causes sub-fragments to pull from the network, and adds the results to the list.
     */
    public void performRefresh(boolean progress) {
        if (isLoading()) {
            return;
        } else if (!Utils.isOnline(getActivity())) {
            onError(getString(R.string.offline_error));
            setLoadComplete();
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
                                getAdapter().set(results);
                            }
                        });
                    }
                } catch (final Exception e) {
                    e.printStackTrace();
                    runOnUiThread(new Runnable() {
                        @Override
                        public void run() {
                            setLoadComplete();
                            if (!Utils.isOnline(getActivity())) {
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
                        setLoadComplete();
                    }
                });
            }
        }).start();
    }
}
