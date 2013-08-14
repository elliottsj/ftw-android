package com.afollestad.silk.cache;

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

    public interface RemoveFilter<T> {
        public boolean shouldRemove(T item);
    }

    public interface FindCallback<T> {
        public void onFound(T item);

        public void onNothing();

        public void onError(Exception e);
    }

    public interface CommitCallback extends SimpleCommitCallback {
        public void onCommitted(boolean returnValue);
    }

    public interface SimpleCommitCallback {
        public void onError(Exception e);
    }


    /**
     * Initializes a new SilkCacheManager, using the default cache file and default cache directory.
     */
    public SilkCacheManager() {
        super(null, null);
    }

    /**
     * Initializes a new SilkCacheManager, using the default cache directory.
     *
     * @param cacheName The name of the cache, must be unique from other feed caches, but must also be valid for being in a file name.
     */
    public SilkCacheManager(String cacheName) {
        super(cacheName, null);
    }

    /**
     * Initializes a new SilkCacheManager.
     *
     * @param cacheName The name of the cache, must be unique from other feed caches, but must also be valid for being in a file name.
     * @param cacheDir  The directory that the cache file will be stored in, defaults to a folder called "Silk" in your external storage directory.
     */
    public SilkCacheManager(String cacheName, File cacheDir) {
        super(cacheName, cacheDir);
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
        } else if (appendIfNotFound) {
            append(toUpdate);
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
        log("Removed item at index " + index + " from " + super.getCacheFile().getName());
        return this;
    }

    /**
     * Removes a single item from the cache, uses isSameAs() from the {@link SilkComparable} to find the item.
     */
    public SilkCacheManager<T> remove(final T toRemove) throws Exception {
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
        else if (fragment != null && fragment.isLoading()) return;
        if (fragment != null) fragment.setLoading(false);
        runPriorityThread(new Runnable() {
            @Override
            public void run() {
                try {
                    if (buffer.isEmpty()) {
                        runOnUiThread(new Runnable() {
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
                    runOnUiThread(new Runnable() {
                        @Override
                        public void run() {
                            adapter.set(buffer);
                            if (fragment != null) fragment.setLoadFromCacheComplete(false);
                            adapter.resetChanged();
                        }
                    });
                } catch (RuntimeException e) {
                    e.printStackTrace();
                    runOnUiThread(new Runnable() {
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
        runPriorityThread(new Runnable() {
            @Override
            public void run() {
                try {
                    final T result = find(query);
                    mHandler.post(new Runnable() {
                        @Override
                        public void run() {
                            if (result == null) callback.onNothing();
                            else callback.onFound(result);
                        }
                    });
                } catch (final Exception e) {
                    e.printStackTrace();
                    log("Cache find error: " + e.getMessage());
                    mHandler.post(new Runnable() {
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