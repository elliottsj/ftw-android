package com.afollestad.silk.fragments;

import android.os.Bundle;
import android.view.View;
import com.afollestad.silk.cache.SilkCacheManager;
import com.afollestad.silk.cache.SilkComparable;

import java.io.File;

/**
 * A {@link SilkFeedFragment} that automatically caches loaded feeds locally and loads them again later.
 * <p/>
 * The class of type T must implement Serializable, otherwise errors will be thrown while attempting to cache.
 *
 * @author Aidan Follestad (afollestad)
 */
public abstract class SilkCachedFeedFragment<T extends SilkComparable> extends SilkFeedFragment<T> {

    /**
     * Initializes a new SilkCachedFeedFragment.
     *
     * @param cacheTitle The title to use for the Fragment's {@link SilkCacheManager}.
     */
    public SilkCachedFeedFragment(String cacheTitle) {
        mCacheTitle = cacheTitle;
    }

    /**
     * Initializes a new SilkCachedFeedFragment.
     *
     * @param cacheTitle     The title to use for the Fragment's {@link SilkCacheManager}.
     * @param cacheDirectory The directory set to the Fragment's {@link SilkCacheManager}, will be '/sdcard/Silk' by default.
     */
    public SilkCachedFeedFragment(String cacheTitle, File cacheDirectory) {
        this(cacheTitle);
        mCacheDir = cacheDirectory;
    }

    protected final String mCacheTitle;
    private File mCacheDir;
    private SilkCacheManager<T> cache;

    /**
     * Gets the cache manager used by the fragment to read and write its cache.
     */
    protected final SilkCacheManager<T> getCacheManager() {
        return cache;
    }

    /**
     * Performs the action done when the fragment wants to try loading itself from the cache, can be overridden to change behavior.
     */
    protected boolean onPerformCacheRead() {
        if (!isLoading()) {
            cache.readAsync(getAdapter(), this);
            return true;
        }
        return false;
    }

    @Override
    public void onResume() {
        super.onResume();
        onPerformCacheRead();
    }

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        cache = new SilkCacheManager<T>(mCacheTitle, mCacheDir);
    }

    @Override
    public void onViewCreated(View view, Bundle savedInstanceState) {
        super.mCacheEnabled = true;
        super.onViewCreated(view, savedInstanceState);
    }

    /**
     * Fired from the {@link SilkCacheManager} when the cache was found to be empty during view creation. By default,
     * causes a refresh, but this can be overridden.
     */
    public void onCacheEmpty() {
        performRefresh(true);
    }

    @Override
    protected void onPostLoad(T[] results) {
        super.onPostLoad(results);
        try {
            cache.set(getAdapter()).commit();
        } catch (Exception e) {
            e.printStackTrace();
        }
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
        if (!visible) {
            cache.set(getAdapter()).commitAsync(new SilkCacheManager.SimpleCommitCallback() {
                @Override
                public void onError(Exception e) {
                    e.printStackTrace();
                }
            });
        }
    }
}