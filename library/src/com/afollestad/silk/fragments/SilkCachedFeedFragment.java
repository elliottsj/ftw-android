package com.afollestad.silk.fragments;

import android.util.Log;
import com.afollestad.silk.caching.OnReadyCallback;
import com.afollestad.silk.caching.SilkCache;
import com.afollestad.silk.caching.SilkComparable;

import java.util.List;

/**
 * @author Aidan Follestad (afollestad)
 */
public abstract class SilkCachedFeedFragment<ItemType extends SilkComparable<ItemType>> extends SilkFeedFragment<ItemType> {

    private SilkCache<ItemType> mCache;

    public abstract String getCacheName();

    public final SilkCache<ItemType> getCache() {
        return mCache;
    }

    protected void onCacheEmpty() {
        super.performRefresh(true);
    }

    @Override
    protected void onPostLoad(List<ItemType> results) {
        super.onPostLoad(results);
        if (mCache != null) mCache.addAll(0, results);
    }

    protected SilkCache<ItemType> onCacheInitialized(SilkCache<ItemType> cache) {
        return cache;
    }

    @Override
    public void performRefresh(boolean showProgress) {
        if (getCacheName() == null) {
            super.performRefresh(showProgress);
            return;
        }
        setLoading(showProgress);
        new SilkCache<ItemType>(getActivity(), getCacheName(), new OnReadyCallback<ItemType>() {
            @Override
            public void onReady(SilkCache<ItemType> cache) {
                mCache = onCacheInitialized(cache);
                if (mCache == null)
                    throw new RuntimeException("onCacheInitialized() cannot return null.");
                if (mCache.size() == 0) {
                    onCacheEmpty();
                    return;
                }
                SilkCachedFeedFragment.super.onPostLoad(mCache.read());
            }
        });
    }

    @Override
    protected void onVisibilityChanged(boolean visible) {
        super.onVisibilityChanged(visible);
        if (!visible && mCache != null && mCache.isChanged()) {
            mCache.commit(new SilkCache.SimpleCommitCallback() {
                @Override
                public void onError(Exception e) {
                    e.printStackTrace();
                    onError(e);
                }
            });
        }
    }
}