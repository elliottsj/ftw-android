package com.afollestad.silk.fragments;

import android.app.Activity;
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

    protected boolean shouldRecreateCacheOnResume() {
        return false;
    }

    @Override
    public void onAttach(Activity activity) {
        super.onAttach(activity);
        /**
         * onAttach is called first in the fragment life cycle before anything else.
         * This tells the parent feed fragment whether or not the fragment will refresh every time it resumes,
         * or only the first time the fragment's views are created.
         */
        super.mInitialLoadOnResume = shouldRecreateCacheOnResume();
    }

    private void writeCache() {
        if (mCache == null || !mCache.isChanged()) return;
        mCache.commit(new SilkCache.SimpleCommitCallback() {
            @Override
            public void onError(Exception e) {
                e.printStackTrace();
                onError(e);
            }
        });
    }

    private void readCache() {
        new SilkCache<ItemType>(getActivity(), getCacheName(), new OnReadyCallback<ItemType>() {
            @Override
            public void onReady(SilkCache<ItemType> cache) {
                mCache = onCacheInitialized(cache);
                if (mCache == null)
                    throw new RuntimeException("onCacheInitialized() cannot return null.");
                if (mCache.size() == 0) {
                    SilkCachedFeedFragment.super.setLoadComplete(false);
                    onCacheEmpty();
                    return;
                }
                onPostLoadFromCache(mCache.read());
            }
        });
    }

    public final SilkCache<ItemType> getCache() {
        return mCache;
    }

    protected void onCacheEmpty() {
        super.performRefresh(true);
    }

    @Override
    public void onPause() {
        super.onPause();
        if (!isActuallyVisible())
            writeCache();
    }

    @Override
    protected void onPostLoad(List<ItemType> results, boolean paginated) {
        super.onPostLoad(results, paginated);
        if (mCache != null && results != null) {
            if (paginated) {
                if (getAddIndex() < 0) {
                    mCache.addAll(results);
                } else {
                    mCache.addAll(getAddIndex(), results);
                }
            } else {
                mCache.set(results);
            }
        }
    }

    protected void onPostLoadFromCache(List<ItemType> results) {
        super.onPostLoad(results, false);
    }

    protected SilkCache<ItemType> onCacheInitialized(SilkCache<ItemType> cache) {
        return cache;
    }

    /**
     * Overridden to initially load from the cache, if possible, instead of re-loading from the web.
     */
    @Override
    protected void onInitialRefresh() {
        if (getCacheName() == null) {
            super.onInitialRefresh();
            return;
        }
        setLoading(true);
        readCache();
    }
}