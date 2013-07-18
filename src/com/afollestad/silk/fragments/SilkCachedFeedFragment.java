package com.afollestad.silk.fragments;

import android.os.Bundle;
import android.view.View;
import com.afollestad.silk.cache.SilkCacheManager;
import com.afollestad.silk.cache.SilkComparable;

/**
 * A {@link SilkFeedFragment} that automatically caches loaded feeds locally and loads them again later.
 * <p/>
 * The class of type T must implement Serializable, otherwise errors will be thrown while attempting to cache.
 *
 * @author Aidan Follestad (afollestad)
 */
public abstract class SilkCachedFeedFragment<T extends SilkComparable> extends SilkFeedFragment<T> {

    private SilkCacheManager<T> cache;

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        if (getCacheTitle() != null)
            cache = new SilkCacheManager<T>(getCacheTitle());
    }

    @Override
    public void onViewCreated(View view, Bundle savedInstanceState) {
        super.mCacheEnabled = true;
        super.onViewCreated(view, savedInstanceState);
        if (cache != null) cache.readAsync(getAdapter(), this, true);
        else performRefresh(true);
    }

    /**
     * Gets the name of the fragment's cache, used by a {@link SilkCacheManager} instance and should be unique
     * from any other cached feed fragment,
     */
    public abstract String getCacheTitle();

    /**
     * Fired from the {@link SilkCacheManager} when the cache was found to be empty during view creation. By default,
     * causes a refresh, but this can be overridden.
     */
    public void onCacheEmpty() {
        performRefresh(true);
    }

    /**
     * Notifies the fragment that it is done loading data from the cache. This causes the progress view to become invisible, and the list
     * or empty text become visible again.
     * <p/>
     * This is equivalent to {#setLoadComplete} by default, but can be overridden.
     * <p/>
     * * @param error Whether or not an error occurred while loading. This value can be used by overriding classes.
     */
    public void setLoadFromCacheComplete(boolean error) {
        setLoadComplete(error);
    }

    @Override
    public void onVisibilityChange(boolean visible) {
        if (cache != null) {
            if (visible)
                cache.readAsync(getAdapter(), this, true);
            else
                cache.writeAsync(getAdapter());
        }
    }
}