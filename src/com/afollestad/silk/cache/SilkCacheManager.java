package com.afollestad.silk.cache;

import android.os.Environment;
import android.os.Handler;
import android.util.Log;
import com.afollestad.silk.adapters.SilkAdapter;
import com.afollestad.silk.fragments.SilkCachedFeedFragment;

import java.io.*;
import java.util.ArrayList;
import java.util.List;

/**
 * Handles caching feeds locally.
 *
 * @author Aidan Follestad (afollestad)
 */
public final class SilkCacheManager<T extends SilkComparable> {

    public interface AddCallback<T> {
        public void onAdded(T item);

        public void onIgnored(T item);

        public void onError(Exception e);
    }

    public interface UpdateCallback<T> {
        public void onUpdated(T item, boolean added);

        public void onError(Exception e);
    }

    public interface WriteCallback<T> {
        public void onWrite(List<T> items, boolean isAppended);

        public void onError(Exception e);
    }

    public interface ReadCallback<T> {
        public void onRead(List<T> results);

        public void onError(Exception e);

        public void onCacheEmpty();
    }

    public interface RemoveCallback<T> {
        public void onRemoved(List<T> items);

        public void onError(Exception e);
    }

    public interface RemoveFilter<T> {
        public boolean shouldRemove(T item);
    }


    /**
     * Initializes a new SilkCacheManager, using the default cache file and default cache directory.
     */
    public SilkCacheManager() {
        init(null, null, null);
    }

    /**
     * Initializes a new SilkCacheManager, using the default cache directory.
     *
     * @param cacheName The name of the cache, must be unique from other feed caches, but must also be valid for being in a file name.
     */
    public SilkCacheManager(String cacheName) {
        init(cacheName, null, null);
    }

    /**
     * Initializes a new SilkCacheManager.
     *
     * @param cacheName The name of the cache, must be unique from other feed caches, but must also be valid for being in a file name.
     * @param cacheDir  The directory that the cache file will be stored in, defaults to "/sdcard/Silk".
     */
    public SilkCacheManager(String cacheName, File cacheDir) {
        init(cacheName, cacheDir, null);
    }

    /**
     * Initializes a new SilkCacheManager.
     *
     * @param cacheName The name of the cache, must be unique from other feed caches, but must also be valid for being in a file name.
     * @param cacheDir  The directory that the cache file will be stored in, defaults to "/sdcard/Silk".
     * @param handler   If the manager isn't being created on the UI thread, a handler that was.
     */
    public SilkCacheManager(String cacheName, File cacheDir, Handler handler) {
        init(cacheName, cacheDir, handler);
    }

    private Handler mHandler;
    private File cacheFile;

    private void init(String cacheName, File cacheDir, Handler handler) {
        if (cacheName == null || cacheName.trim().isEmpty())
            cacheName = "default";
        if (handler == null)
            mHandler = new Handler();
        else mHandler = handler;
        if (cacheDir == null)
            cacheDir = new File(Environment.getExternalStorageDirectory(), "Silk");
        if (!cacheDir.exists())
            cacheDir.mkdirs();
        cacheFile = new File(cacheDir, cacheName.toLowerCase() + ".cache");
    }

    private void log(String message) {
        Log.d("SilkCacheManager", message);
    }

    /**
     * Writes a single object to the cache, without overwriting previous entries.
     *
     * @param toAdd the item to add to the cache.
     */
    public boolean add(T toAdd) throws Exception {
        if (toAdd.shouldIgnore()) return false;
        List<T> temp = new ArrayList<T>();
        temp.add(toAdd);
        write(temp, true);
        return true;
    }

    /**
     * Updates an item in the cache. If it's not found, it's added.
     *
     * @param toUpdate The item to update, or add.
     */
    public boolean update(T toUpdate) throws Exception {
        if (toUpdate.shouldIgnore()) return false;
        List<T> cache = read();
        boolean found = false;
        for (int i = 0; i < cache.size(); i++) {
            if (cache.get(i).isSameAs(toUpdate)) {
                cache.set(i, toUpdate);
                found = true;
                break;
            }
        }
        if (found) write(cache);
        else add(toUpdate);
        return found;
    }

    /**
     * Writes an list of items to the cache from the calling thread. Allows you set whether to overwrite the cache
     * or append to it.
     */
    public void write(List<T> items, boolean append) throws Exception {
        if (items == null || items.size() == 0) {
            if (cacheFile.exists() && !append) {
                log(cacheFile.getName() + " is empty, deleting file...");
                cacheFile.delete();
            }
            return;
        }
        int subtraction = 0;
        FileOutputStream fileOutputStream = new FileOutputStream(cacheFile, append);
        ObjectOutputStream objectOutputStream = new ObjectOutputStream(fileOutputStream);
        for (T item : items) {
            if (item.shouldIgnore()) {
                subtraction++;
                continue;
            }
            objectOutputStream.writeObject(item);
        }
        objectOutputStream.close();
        log("Wrote " + (items.size() - subtraction) + " items to " + cacheFile.getName());
    }

    /**
     * Writes an list of items to the cache from the calling thread, overwriting all current entries in the cache.
     */
    public void write(List<T> items) throws Exception {
        write(items, false);
    }

    /**
     * Writes an adapter to the cache. The adapter will not be written if its isChanged() method returns false.
     * The adapter's isChanged() is reset to false every time the cache reads to it or if it's reset elsewhere by you.
     */
    public void write(SilkAdapter<T> adapter) throws Exception {
        if (!adapter.isChanged()) {
            log("The adapter has not been changed, skipped writing to " + cacheFile.getName());
            return;
        }
        adapter.resetChanged();
        write(adapter.getItems(), false);
    }

    /**
     * Reads from the cache file on the calling thread and returns the results.
     */
    public List<T> read() throws Exception {
        final List<T> results = new ArrayList<T>();
        if (cacheFile.exists()) {
            FileInputStream fileInputStream = new FileInputStream(cacheFile);
            ObjectInputStream objectInputStream = new ObjectInputStream(fileInputStream);
            while (true) {
                try {
                    final T item = (T) objectInputStream.readObject();
                    if (item != null) results.add(item);
                } catch (EOFException eof) {
                    break;
                }
            }
            objectInputStream.close();
        }
        log("Read " + results.size() + " items from " + cacheFile.getName());
        return results;
    }

    /**
     * Removes a single item from the cache, uses isSameAs() from the {@link SilkComparable} to find the item.
     */
    public void remove(final T toRemove) throws Exception {
        remove(new RemoveFilter<T>() {
            @Override
            public boolean shouldRemove(T item) {
                return item.isSameAs(toRemove);
            }
        });
    }

    /**
     * Removes multiple items from the cache, uses isSameAs() from the {@link SilkComparable} to find the item.
     */
    public void remove(final T... toRemove) throws Exception {
        remove(new RemoveFilter<T>() {
            @Override
            public boolean shouldRemove(T item) {
                boolean found = false;
                for (int i = 0; i < toRemove.length; i++) {
                    if (toRemove[i].isSameAs(item)) {
                        found = true;
                        break;
                    }
                }
                return found;
            }
        });
    }

    /**
     * Removes items from the cache based on a filter that makes decisions. Returns a list of items that were removed.
     */
    public List<T> remove(RemoveFilter<T> filter) throws Exception {
        if (filter == null) throw new IllegalArgumentException("You must specify a filter");
        List<T> toReturn = new ArrayList<T>();
        List<T> cache = read();
        if (cache.size() == 0) return toReturn;

        ArrayList<Integer> removeIndexes = new ArrayList<Integer>();
        for (int i = 0; i < cache.size(); i++) {
            if (filter.shouldRemove(cache.get(i))) {
                toReturn.add(cache.get(i));
                removeIndexes.add(i);
            }
        }
        for (Integer i : removeIndexes) {
            cache.remove(i.intValue());
        }

        write(cache, false);
        log("Removed " + toReturn.size() + " items from " + cacheFile.getName());
        return toReturn;
    }

    private void runPriorityThread(Runnable runnable) {
        Thread t = new Thread(runnable);
        t.setPriority(Thread.MAX_PRIORITY);
        t.start();
    }

    /**
     * Writes a single object to the cache from a separate thread, posting results to a callback.
     */
    public void addAsync(final T toAdd, final AddCallback<T> callback) {
        if (callback == null) throw new IllegalArgumentException("You must specify a callback");
        runPriorityThread(new Runnable() {
            @Override
            public void run() {
                try {
                    if (add(toAdd)) callback.onAdded(toAdd);
                    else callback.onIgnored(toAdd);
                } catch (Exception e) {
                    log("Cache write error: " + e.getMessage());
                    callback.onError(e);
                }
            }
        });
    }

    /**
     * Updates an item in the cache. If it's not found, it's added. This is done on a separate thread and results are
     * posted to a callback.
     */
    public void updateAsync(final T toUpdate, final UpdateCallback<T> callback) {
        if (callback == null) throw new IllegalArgumentException("You must specify a callback");
        runPriorityThread(new Runnable() {
            @Override
            public void run() {
                try {
                    boolean isAdded = update(toUpdate);
                    callback.onUpdated(toUpdate, isAdded);
                } catch (Exception e) {
                    log("Cache write error: " + e.getMessage());
                    callback.onError(e);
                }
            }
        });
    }

    /**
     * Writes a list of items to the cache from a separate thread. Posts results to a callback.
     */
    public void writeAsync(final List<T> items, final WriteCallback<T> callback) {
        writeAsync(items, false, callback);
    }

    /**
     * Writes an adapter to the cache from a separate thread. The adapter will not be written if its
     * isChanged() method returns false. The adapter's isChanged() is reset to false every time the cache reads
     * to it or if it's reset elsewhere by you.
     */
    public void writeAsync(final SilkAdapter<T> adapter, final WriteCallback<T> callback) {
        if (!adapter.isChanged()) {
            log("The adapter has not been changed, skipped writing to " + cacheFile.getName());
            return;
        }
        adapter.resetChanged();
        writeAsync(adapter.getItems(), false, callback);
    }

    /**
     * Writes a list of items to the cache from a separate thread. Allows you set whether to overwrite the cache
     * or append to it. Posts results to a callback.
     */
    public void writeAsync(final List<T> items, final boolean append, final WriteCallback<T> callback) {
        if (callback == null) throw new IllegalArgumentException("You must specify a callback");
        runPriorityThread(new Runnable() {
            @Override
            public void run() {
                try {
                    write(items, append);
                    mHandler.post(new Runnable() {
                        @Override
                        public void run() {
                            callback.onWrite(items, append);
                        }
                    });
                } catch (final Exception e) {
                    log("Cache write error: " + e.getMessage());
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

    /**
     * Reads from the cache file on a separate thread and posts results to a callback.
     */
    public void readAsync(final ReadCallback<T> callback) {
        if (callback == null) throw new IllegalArgumentException("You must specify a callback");
        if (!cacheFile.exists()) {
            log("No cache for " + cacheFile.getName());
            callback.onCacheEmpty();
            return;
        }
        runPriorityThread(new Runnable() {
            @Override
            public void run() {
                try {
                    final List<T> results = read();
                    mHandler.post(new Runnable() {
                        @Override
                        public void run() {
                            if (results.size() == 0) callback.onCacheEmpty();
                            else callback.onRead(results);
                        }
                    });
                } catch (final Exception e) {
                    log("Cache read error: " + e.getMessage());
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

    /**
     * Reads from the manager's cache file into a {@link SilkAdapter}, and notifies a {@link SilkCachedFeedFragment} when it's loading and done loading.
     *
     * @param adapter  The adapter that items will be added to.
     * @param fragment The optional fragment that will receive loading notifications.
     */
    public void readAsync(final SilkAdapter<T> adapter, final SilkCachedFeedFragment fragment) {
        if (adapter == null)
            throw new IllegalArgumentException("The adapter parameter cannot be null.");
        else if (fragment != null && fragment.isLoading()) return;
        if (fragment != null) fragment.setLoading(false);
        readAsync(new ReadCallback<T>() {
            @Override
            public void onRead(List<T> results) {
                adapter.clear();
                for (T item : results) adapter.add(item);
                if (fragment != null) fragment.setLoadFromCacheComplete(false);
                adapter.resetChanged();
            }

            @Override
            public void onError(Exception e) {
                if (fragment != null) {
                    fragment.setLoadFromCacheComplete(true);
                    if (adapter.getCount() == 0) fragment.onCacheEmpty();
                }
                adapter.resetChanged();
            }

            @Override
            public void onCacheEmpty() {
                adapter.clear();
                if (fragment != null) {
                    fragment.setLoadFromCacheComplete(false);
                    fragment.onCacheEmpty();
                }
                adapter.resetChanged();
            }
        });
    }

    /**
     * Removes a single item from the cache, uses isSameAs() remove the {@link RemoveFilter} to find the item. Results are
     * posted to a callback.
     */
    public void removeAsync(final T toRemove, final RemoveCallback<T> callback) {
        if (callback == null) throw new IllegalArgumentException("You must specify a callback");
        runPriorityThread(new Runnable() {
            @Override
            public void run() {
                try {
                    final List<T> results = remove(new RemoveFilter<T>() {
                        @Override
                        public boolean shouldRemove(T item) {
                            return item.isSameAs(toRemove);
                        }
                    });
                    mHandler.post(new Runnable() {
                        @Override
                        public void run() {
                            callback.onRemoved(results);
                        }
                    });
                } catch (final Exception e) {
                    log("Cache remove error: " + e.getMessage());
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

    /**
     * Removes items from the cache based on a filter that makes decisions. Results are posted to a callback.
     */
    public void removeAsync(final RemoveFilter<T> filter, final RemoveCallback<T> callback) {
        if (filter == null) throw new IllegalArgumentException("You must specify a filter");
        else if (callback == null) throw new IllegalArgumentException("You must specify a callback");
        runPriorityThread(new Runnable() {
            @Override
            public void run() {
                try {
                    final List<T> results = remove(filter);
                    mHandler.post(new Runnable() {
                        @Override
                        public void run() {
                            callback.onRemoved(results);
                        }
                    });
                } catch (final Exception e) {
                    log("Cache remove error: " + e.getMessage());
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