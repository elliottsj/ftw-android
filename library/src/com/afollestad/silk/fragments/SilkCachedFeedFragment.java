package com.afollestad.silk.fragments;

import com.afollestad.silk.caching.OnReadyCallback;
import com.afollestad.silk.caching.SilkCache;
import com.afollestad.silk.caching.SilkComparable;

/**
 * @author Aidan Follestad (afollestad)
 */
public abstract class SilkCachedFeedFragment<ItemType extends SilkComparable<ItemType>> extends SilkFeedFragment<ItemType> {

    private SilkCache<ItemType> mCache;

    public abstract String getCacheName();

    protected void onCacheEmpty() {
        super.performRefresh(true);
    }

    protected SilkCache<ItemType> onCacheInitialized(SilkCache<ItemType> cache) {
        return cache;
    }

    @Override
    public void performRefresh(boolean showProgress) {
        new SilkCache<ItemType>(getActivity(), getCacheName(), new OnReadyCallback<ItemType>() {
            @Override
            public void onReady(SilkCache<ItemType> cache) {
                mCache = onCacheInitialized(cache);
                if (cache.size() == 0) {
                    onCacheEmpty();
                    return;
                }
                getAdapter().set(cache.read());
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
