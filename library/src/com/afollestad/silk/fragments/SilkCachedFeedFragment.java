package com.afollestad.silk.fragments;

import android.os.Bundle;
import android.view.View;
import com.afollestad.silk.cache.CacheLimiter;
import com.afollestad.silk.cache.SilkCacheManager;
import com.afollestad.silk.cache.SilkComparable;

import java.io.File;
import java.util.List;

/**
 * A {@link SilkFeedFragment} that automatically caches loaded feeds locally and loads them again later.
 * <p/>
 * The class of type T must implement Serializable, otherwise errors will be thrown while attempting to cache.
 *
 * @author Aidan Follestad (afollestad)
 */
public abstract class SilkCachedFeedFragment<T extends SilkComparable> extends SilkFeedFragment<T> {

    public SilkCachedFeedFragment() {
    }

    private SilkCacheManager<T> cache;

    /**
     * Gets the cache name.
     */
    public abstract String getCacheTitle();

    /**
     * The directory set to the Fragment's {@link SilkCacheManager}, will be '/sdcard/Silk' by default.
     */
    protected File getCacheDirectory() {
        return null;
    }

    /**
     * Gets whether or not the fragment should re-create its cache when it resumes, causing a reload from the disk cache.
     * Defaults to false.
     */
    protected boolean shouldRecreateCacheOnResume() {
        return false;
    }

    /**
     * Called every time the cache is initialized or re-initialized. Allows you to modify properties of the fragment's cache,
     * such as a cache limiter.
     * <p/>
     * You must return the cache parameter.
     */
    protected SilkCacheManager<T> onCacheInitialized(SilkCacheManager<T> cache) {
        return cache;
    }

    /**
     * Gets the cache manager used by the fragment to read and write its cache.
     */
    protected final SilkCacheManager<T> getCacheManager() {
        return cache;
    }

    private void recreateCache(final SilkCacheManager.InitializedCallback<T> callback) {
        CacheLimiter limiter = null;
        if (cache != null) limiter = cache.getLimiter();
        cache = new SilkCacheManager<T>(getCacheTitle(), getCacheDirectory(), new SilkCacheManager.InitializedCallback<T>() {
            @Override
            public void onInitialized(SilkCacheManager<T> manager) {
                cache = onCacheInitialized(manager);
                callback.onInitialized(cache);
            }
        }).setLimiter(limiter);
    }

    /**
     * Performs the action done when the fragment wants to try loading itself from the cache, can be overridden to change behavior.
     */
    protected boolean onPerformCacheRead() {
        setLoading(true);
        if (cache == null) {
            recreateCache(new SilkCacheManager.InitializedCallback<T>() {
                @Override
                public void onInitialized(SilkCacheManager<T> manager) {
                    manager.readAsync(getAdapter(), SilkCachedFeedFragment.this);
                }
            });
            return true;
        } else if (cache.isInitialized()) {
            if (cache.isCommitted()) {
                recreateCache(new SilkCacheManager.InitializedCallback<T>() {
                    @Override
                    public void onInitialized(SilkCacheManager<T> manager) {
                        manager.readAsync(getAdapter(), SilkCachedFeedFragment.this);
                    }
                });
            } else cache.readAsync(getAdapter(), this);
            return true;
        }
        return false;
    }

    @Override
    public void onResume() {
        super.onResume();
        if (getCacheTitle() != null) {
            if (shouldRecreateCacheOnResume()) cache = null;
            onPerformCacheRead();
        } else onCacheEmpty();
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
    protected void onPostLoad(List<T> results) {
        super.onPostLoad(results);
        if (cache != null) {
            if (cache.isCommitted()) {
                recreateCache(new SilkCacheManager.InitializedCallback<T>() {
                    @Override
                    public void onInitialized(SilkCacheManager<T> manager) {
                        manager.set(getAdapter()).commitAsync(new SilkCacheManager.SimpleCommitCallback() {
                            @Override
                            public void onError(Exception e) {
                                e.printStackTrace();
                            }
                        });
                    }
                });
            } else cache.set(getAdapter()).commitAsync(new SilkCacheManager.SimpleCommitCallback() {
                @Override
                public void onError(Exception e) {
                    e.printStackTrace();
                }
            });
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
    protected void onVisibilityChange(boolean visible) {
        if (!visible && cache != null && !cache.isCommitted() && cache.isChanged()) {
            cache.set(getAdapter()).commitAsync(new SilkCacheManager.SimpleCommitCallback() {
                @Override
                public void onError(Exception e) {
                    e.printStackTrace();
                }
            });
        }
    }
}