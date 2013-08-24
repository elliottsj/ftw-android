package com.afollestad.silk.cache;

import android.content.Context;
import android.content.SharedPreferences;
import android.os.Handler;
import com.afollestad.silk.adapters.SilkAdapter;
import com.afollestad.silk.fragments.SilkCachedFeedFragment;

import java.io.File;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

/**
 * Handles caching any item that implements {@link SilkComparable} locally in a file.
 *
 * @author Aidan Follestad (afollestad)
 */
public final class SilkCacheManager<T extends SilkComparable> extends SilkCacheManagerBase<T> {

    public interface InitializedCallback<T extends SilkComparable> {
        public void onInitialized(SilkCacheManager<T> manager);
    }

    public interface RemoveFilter<T> {
        public boolean shouldRemove(T item);
    }

    public interface FindCallback<T> {
        public void onFound(T item);

        public void onNothing();

        public void onError(Exception e);
    }

    public interface CommitCallback extends SimpleCommitCallback {
        public void onCommitted();
    }

    public interface SimpleCommitCallback {
        public void onError(Exception e);
    }


    /**
     * Initializes a new SilkCacheManager, using the default cache file and default cache directory.
     *
     * @param callback An optional callback that will cause the cache to initialize itself off the current thread, and post to a callback when it's ready.
     */
    public SilkCacheManager(Context context, InitializedCallback<T> callback) {
        super(context, null, null);
        initialize(callback);
    }

    /**
     * Initializes a new SilkCacheManager, using the default cache directory.
     *
     * @param cacheName The name of the cache, must be unique from other feed caches, but must also be valid for being in a file name.
     * @param callback  An optional callback that will cause the cache to initialize itself off the current thread, and post to a callback when it's ready.
     */
    public SilkCacheManager(Context context, String cacheName, InitializedCallback<T> callback) {
        super(context, cacheName, null);
        initialize(callback);
    }

    /**
     * Initializes a new SilkCacheManager.
     *
     * @param cacheName The name of the cache, must be unique from other feed caches, but must also be valid for being in a file name.
     * @param cacheDir  The directory that the cache file will be stored in, defaults to a folder called "Silk" in your external storage directory.
     * @param callback  An optional callback that will cause the cache to initialize itself off the current thread, and post to a callback when it's ready.
     */
    public SilkCacheManager(Context context, String cacheName, File cacheDir, InitializedCallback<T> callback) {
        super(context, cacheName, cacheDir);
        initialize(callback);
    }

    private boolean isInitialized;

    private void initialize(final SilkCacheManager.InitializedCallback<T> callback) {
        if (callback == null) {
            reloadIfNecessary();
            isInitialized = true;
            isChanged = false;
            log(getCacheFile().getName() + " successfully initialized!");
            return;
        }
        log("Initializing " + getCacheFile().getName() + "...");
        final Handler mHandler = new Handler();
        runPriorityThread(new Runnable() {
            @Override
            public void run() {
                reloadIfNecessary();
                mHandler.post(new Runnable() {
                    @Override
                    public void run() {
                        isInitialized = true;
                        isChanged = false;
                        callback.onInitialized(SilkCacheManager.this);
                        log(getCacheFile().getName() + " successfully initialized!");
                    }
                });
            }
        });
    }

    /**
     * Gets whether or not the manager has finished the initialization process.
     */
    public boolean isInitialized() {
        return isInitialized;
    }

    /**
     * Sets a size limiter to the cache, this only has to be done once as it gets saved in persistence.
     */
    public SilkCacheManager<T> setLimiter(CacheLimiter limiter) {
        SharedPreferences prefs = getContext().getSharedPreferences("[silk-cache-limiters]", Context.MODE_PRIVATE);
        if (limiter == null) {
            prefs.edit().remove(getCacheFile().getName()).commit();
        } else {
            prefs.edit().putString(getCacheFile().getName(), limiter.getSize() + ":" + limiter.getMode().intValue()).commit();
        }
        return this;
    }

    /**
     * Sets an expiration date for the cache; once the expiration is reached, the cache is automatically wiped. This only
     * has to be done once until the cache expires, as the expiration gets saved in persistence.
     */
    public SilkCacheManager<T> setExpiration(long dateTime) {
        SharedPreferences prefs = getContext().getSharedPreferences("[silk-cache-expirations]", Context.MODE_PRIVATE);
        if (dateTime <= 0) prefs.edit().remove(getCacheFile().getName()).commit();
        else prefs.edit().putLong(getCacheFile().getName(), dateTime).commit();
        return this;
    }

    /**
     * Sets the handler used when making callbacks from separate threads. This should be used if you didn't
     * instantiate the cache manager from the UI thread.
     */
    public SilkCacheManager<T> setHandler(Handler handler) {
        super.mHandler = handler;
        return this;
    }

    /**
     * Forces the cache manager to reload its buffer from the cache file.
     */
    public SilkCacheManager<T> forceReload() {
        super.buffer = null;
        super.isChanged = false;
        reloadIfNecessary();
        return this;
    }

    /**
     * Appends a single item to the cache.
     */
    public SilkCacheManager<T> append(T toAdd) {
        if (toAdd == null || toAdd.shouldIgnore()) {
            log("Item passed to append() was null or marked for ignoring.");
            return this;
        }
        super.buffer.add(toAdd);
        isChanged = true;
        log("Appended 1 item to the cache.");
        return this;
    }

    /**
     * Appends a collection of items to the cache.
     */
    public SilkCacheManager<T> append(List<T> toAppend) {
        if (toAppend == null || toAppend.size() == 0) {
            log("List passed to append() was null or empty.");
            return this;
        }
        int count = 0;
        for (T item : toAppend) {
            if (item.shouldIgnore()) continue;
            super.buffer.add(item);
            count++;
        }
        isChanged = true;
        log("Appended " + count + " items to the cache.");
        return this;
    }

    /**
     * Appends an array of items to the cache.
     */
    public SilkCacheManager<T> append(T[] toAppend) {
        if (toAppend == null || toAppend.length == 0) {
            log("Array passed to append() was null or empty.");
            return this;
        }
        isChanged = true;
        append(new ArrayList<T>(Arrays.asList(toAppend)));
        return this;
    }

    /**
     * Appends the contents of a {@link SilkAdapter} to the cache, and resets the adapter's changed state to unchanged.
     * If the adapter is marked as unchanged already, its contents will not be written.
     */
    public SilkCacheManager<T> append(SilkAdapter<T> adapter) {
        if (adapter == null || adapter.getCount() == 0) {
            log("Adapter passed to append() was null.");
            return this;
        }
        if (!adapter.isChanged()) {
            log("The adapter has not been changed, skipped writing to " + super.getCacheFile().getName());
            return this;
        }
        isChanged = true;
        adapter.resetChanged();
        append(adapter.getItems());
        return this;
    }

    /**
     * Updates an item in the cache, using isSameAs() from SilkComparable to find the item.
     *
     * @param appendIfNotFound Whether or not the item will be appended to the end of the cache if it's not found.
     */
    public SilkCacheManager<T> update(T toUpdate, boolean appendIfNotFound) {
        if (toUpdate == null || toUpdate.shouldIgnore()) {
            log("Item passed to update() was null or marked for ignoring.");
            return this;
        }
        if (super.buffer.size() == 0) {
            log("Cache buffer is empty.");
            return this;
        }
        boolean found = false;
        for (int i = 0; i < buffer.size(); i++) {
            if (buffer.get(i).isSameAs(toUpdate)) {
                buffer.set(i, toUpdate);
                found = true;
                break;
            }
        }
        if (found) {
            log("Updated 1 item in the cache.");
            isChanged = true;
        } else if (appendIfNotFound) {
            append(toUpdate);
            isChanged = true;
        }
        return this;
    }

    /**
     * Overwrites all items in the cache with a set of items from an array.
     * <p/>
     * This is equivalent to calling clear() and then append().
     */
    public SilkCacheManager<T> set(T[] toSet) {
        set(new ArrayList<T>(Arrays.asList(toSet)));
        return this;
    }

    /**
     * Overwrites all items in the cache with a set of items from a collection.
     * <p/>
     * This is equivalent to calling clear() and then append().
     */
    public SilkCacheManager<T> set(List<T> toSet) {
        clear();
        append(toSet);
        return this;
    }

    /**
     * Overwrites all items in the cache with a set of items from a collection.
     * <p/>
     * This is equivalent to calling clear() and then append().
     */
    public SilkCacheManager<T> set(SilkAdapter<T> adapter) {
        if (!adapter.isChanged()) {
            log("Adapter was not changed, cancelling call to set().");
            return this;
        }
        clear();
        append(adapter);
        return this;
    }

    /**
     * Removes an item from a specific index from the cache.
     */
    public SilkCacheManager<T> remove(int index) {
        super.buffer.remove(index);
        isChanged = true;
        log("Removed item at index " + index + " from " + super.getCacheFile().getName());
        return this;
    }

    /**
     * Removes a single item from the cache, uses isSameAs() from the {@link SilkComparable} to find the item.
     */
    public SilkCacheManager<T> remove(final T toRemove) {
        if (toRemove == null) {
            log("Item passed to remove() was null.");
            return this;
        }
        remove(new RemoveFilter<T>() {
            @Override
            public boolean shouldRemove(T item) {
                return item.isSameAs(toRemove);
            }
        }, true);
        return this;
    }

    /**
     * Removes items from the cache based on a filter that makes decisions. Returns a list of items that were removed.
     *
     * @param removeOne If true, it will remove one and stop searching, which can improve performance. Otherwise it'll search through the entire cache and remove multiple entries that match the filter.
     */
    public SilkCacheManager<T> remove(RemoveFilter<T> filter, boolean removeOne) {
        if (filter == null) throw new IllegalArgumentException("You must specify a RemoveFilter.");
        if (super.buffer.size() == 0) {
            log("Cache buffer is empty.");
            return this;
        }
        ArrayList<Integer> removeIndexes = new ArrayList<Integer>();
        for (int i = 0; i < super.buffer.size(); i++) {
            if (filter.shouldRemove(super.buffer.get(i))) {
                removeIndexes.add(i);
                if (removeOne) break;
            }
        }
        for (Integer i : removeIndexes)
            super.buffer.remove(i.intValue());
        if (removeIndexes.size() > 0)
            isChanged = true;
        log("Removed " + removeIndexes.size() + " items from the cache.");
        return this;
    }

    /**
     * Finds an item in the cache using isSameAs() from SilkComparable.
     *
     * @param query An item that will match up with another item using SilkComparable.isSameAs().
     */
    public T find(T query) {
        if (query == null) {
            log("Item passed to find() was null.");
            return null;
        }
        log("Searching " + super.buffer.size() + " items...");
        if (super.buffer.size() == 0) {
            log("Cache buffer is empty.");
            return null;
        }
        for (T item : super.buffer) {
            if (item.isSameAs(query)) return item;
        }
        return null;
    }

    /**
     * Clears all items from the cache.
     */
    public SilkCacheManager clear() {
        log("Cache was cleared.");
        if (super.buffer == null)
            super.buffer = new ArrayList<T>();
        else super.buffer.clear();
        isChanged = true;
        return this;
    }

    /**
     * Gets the total number of items in the cache.
     */
    public int size() {
        return super.buffer.size();
    }

    /**
     * Reads from the manager's cache file into a {@link SilkAdapter}, and notifies a {@link SilkCachedFeedFragment} when it's loading and done loading.
     *
     * @param adapter  The adapter that items will be added to.
     * @param fragment The optional fragment that will receive loading notifications.
     */
    public void readAsync(final SilkAdapter<T> adapter, final SilkCachedFeedFragment fragment) {
        if (adapter == null) throw new IllegalArgumentException("The adapter parameter cannot be null.");
        if (fragment != null) fragment.setLoading(false);
        final Handler handler = getHandler();
        runPriorityThread(new Runnable() {
            @Override
            public void run() {
                try {
                    if (buffer == null || buffer.isEmpty()) {
                        handler.post(new Runnable() {
                            @Override
                            public void run() {
                                adapter.clear();
                                if (fragment != null) {
                                    fragment.setLoadFromCacheComplete(false);
                                    fragment.onCacheEmpty();
                                }
                                adapter.resetChanged();
                            }
                        });
                        return;
                    }
                    handler.post(new Runnable() {
                        @Override
                        public void run() {
                            adapter.set(buffer);
                            if (fragment != null) fragment.setLoadFromCacheComplete(false);
                            adapter.resetChanged();
                        }
                    });
                } catch (RuntimeException e) {
                    e.printStackTrace();
                    handler.post(new Runnable() {
                        @Override
                        public void run() {
                            if (fragment != null) {
                                fragment.setLoadFromCacheComplete(true);
                                if (adapter.getCount() == 0) fragment.onCacheEmpty();
                            }
                            adapter.resetChanged();
                        }
                    });
                }
            }
        });
    }

    /**
     * Finds an item in the cache using isSameAs() from SilkComparable on a separate thread, and posts
     * results to a callback.
     *
     * @param query An item that will match up with another item via isSameAs().
     */
    public void findAsync(final T query, final FindCallback<T> callback) {
        if (callback == null) throw new IllegalArgumentException("You must specify a callback");
        final Handler handler = getHandler();
        runPriorityThread(new Runnable() {
            @Override
            public void run() {
                try {
                    final T result = find(query);
                    handler.post(new Runnable() {
                        @Override
                        public void run() {
                            if (result == null) callback.onNothing();
                            else callback.onFound(result);
                        }
                    });
                } catch (final Exception e) {
                    e.printStackTrace();
                    log("Cache find error: " + e.getMessage());
                    handler.post(new Runnable() {
                        @Override
                        public void run() {
                            callback.onError(e);
                        }
                    });
                }
            }
        });
    }
}