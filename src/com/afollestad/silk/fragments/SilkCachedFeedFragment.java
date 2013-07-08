package com.afollestad.silk.fragments;

import android.os.Bundle;
import android.view.View;
import com.afollestad.silk.cache.SilkCacheManager;

/**
 * A {@link SilkFeedFragment} that automatically caches loaded feeds locally and loads them again later.
 * <p/>
 * The class of type T must implement Serializable, otherwise errors will be thrown while attempting to cache.
 *
 * @author Aidan Follestad (afollestad)
 */
public abstract class SilkCachedFeedFragment<T> extends SilkFeedFragment<T> {

    private SilkCacheManager cache;

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        if (getCacheTitle() != null)
            cache = new SilkCacheManager<T>(getActivity(), getCacheTitle());
    }

    @Override
    public void onViewCreated(View view, Bundle savedInstanceState) {
        super.mCacheEnabled = true;
        super.onViewCreated(view, savedInstanceState);
        // Read from the cache and refresh from the internet when the fragment's view is created
        if (cache != null)
            cache.read(getAdapter());
        performRefresh();
    }

    @Override
    public void onPause() {
        super.onPause();
        // Write to the feed cache when the fragment is paused
        if (cache != null)
            cache.write(getAdapter());
    }

    /**
     * Gets the name of the fragment's cache, used by a {@link SilkCacheManager} instance and should be unique
     * from any other cached feed fragment,
     */
    public abstract String getCacheTitle();
}
