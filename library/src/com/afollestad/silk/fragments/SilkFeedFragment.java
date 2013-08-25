package com.afollestad.silk.fragments;

import android.os.Bundle;
import android.view.View;
import com.afollestad.silk.R;
import com.afollestad.silk.Silk;
import com.afollestad.silk.cache.SilkComparable;

import java.util.List;

/**
 * A {@link com.afollestad.silk.fragments.SilkListFragment} that pulls data from the network, and automatically puts the retrieved data in its list.
 *
 * @author Aidan Follestad (afollestad)
 */
public abstract class SilkFeedFragment<T extends SilkComparable> extends SilkListFragment<T> {

    protected boolean mCacheEnabled = false;

    /**
     * Returns whether or not loading progress is displayed when the fragment refreshes from the web.
     */
    protected boolean shouldShowLoadingProgress() {
        return true;
    }

    @Override
    public void onViewCreated(View view, Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);
        // If caching is enabled, the SilkCachedFeedFragment will handle this instead. super.onViewCreated() still must be called though.
        if (!mCacheEnabled) {
            // Immediately load the fragment's feed
            performRefresh(shouldShowLoadingProgress());
        }
    }

    /**
     * Called when inheriting classes must load their feed. This is called from a separate thread so you don't
     * need to worry about threading on your own.
     */
    protected abstract List<T> refresh() throws Exception;

    /**
     * Called when an error occurs while refreshing.
     */
    protected abstract void onError(Exception message);

    /**
     * Stuff that's done right before refresh() starts, return false here to cancel refreshing.
     */
    protected boolean onPreLoad() {
        if (isLoading()) return false;
        else if (!Silk.isOnline(getActivity())) {
            onError(new Exception(getString(R.string.offline_error)));
            setLoadComplete(true);
            return false;
        }
        return true;
    }

    /**
     * Called from a separate thread (not the UI thread) when refresh() has returned results. Can
     * be overridden to do something with the results before being added to the adapter.
     */
    protected void onPostLoad(final List<T> results) {
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
        if (!onPreLoad()) return;
        setLoading(progress);
        new Thread(new Runnable() {
            @Override
            public void run() {
                try {
                    final List<T> results = refresh();
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
                                onError(new Exception(getString(R.string.offline_error)));
                            } else {
                                onError(e);
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
