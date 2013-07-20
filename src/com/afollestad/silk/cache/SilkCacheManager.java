package com.afollestad.silk.cache;

import android.os.Handler;
import android.util.Log;
import com.afollestad.silk.SilkAdapter;
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

        public void onError(Exception e);
    }

    public interface UpdateCallback<T> {
        public void onUpdated(T item, boolean added);

        public void onError(Exception e);
    }

    public interface WriteCallback<T> {
        public void onWrite(List<T> items);

        public void onError(Exception e);
    }

    public interface ReadCallback<T> {
        public void onRead(List<T> results);

        public void onError(Exception e);

        public void onCacheEmpty();
    }

    public interface RemoveCallback {
        public void onRemoved(int index, long itemId);

        public void onError(Exception e);
    }


    /**
     * Initializes a new SilkCacheManager.
     *
     * @param cacheName The name of the cache, must be unique from other feed caches, but must also be valid for being in a file name.
     */
    public SilkCacheManager(String cacheName, File cacheDir) {
        mHandler = new Handler();
        cacheFile = new File(cacheDir, cacheName.toLowerCase() + ".cache");
        if (!cacheDir.exists()) cacheDir.mkdirs();
    }

    /**
     * Initializes a new SilkCacheManager.
     *
     * @param cacheName The name of the cache, must be unique from other feed caches, but must also be valid for being in a file name.
     * @param handler   If the manager isn't being created on the UI thread, a handler that was.
     */
    public SilkCacheManager(String cacheName, File cacheDir, Handler handler) {
        mHandler = handler;
        cacheFile = new File(cacheDir, cacheName.toLowerCase() + ".cache");
        if (!cacheDir.exists()) cacheDir.mkdirs();
    }

    private final Handler mHandler;
    private final File cacheFile;

    private void log(String message) {
        Log.d("SilkCacheManager", message);
    }

    /**
     * Writes a single object to the cache, without overwriting previous entries.
     *
     * @param toAdd the item to add to the cache.
     */
    public void add(T toAdd) throws Exception {
        List<T> temp = new ArrayList<T>();
        temp.add(toAdd);
        write(temp, true);
    }

    /**
     * Updates an item in the cache. If it's not found, it's added.
     *
     * @param toUpdate The item to update, or add.
     */
    public boolean update(T toUpdate) throws Exception {
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

    private void write(List<T> items, boolean append) throws Exception {
        if (items == null || items.size() == 0) {
            if (cacheFile.exists()) {
                log("Adapter for " + cacheFile.getName() + " is empty, deleting file...");
                cacheFile.delete();
            }
            return;
        }
        FileOutputStream fileOutputStream = new FileOutputStream(cacheFile, append);
        ObjectOutputStream objectOutputStream = new ObjectOutputStream(fileOutputStream);
        for (T item : items) objectOutputStream.writeObject(item);
        objectOutputStream.close();
        log("Wrote " + items.size() + " items to " + cacheFile.getName());
    }

    /**
     * Writes an list of items to the cache from the calling thread, overwriting all current entries in the cache.
     */
    public void write(List<T> items) throws Exception {
        write(items, false);
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
     * Removes an item from the cache.
     */
    public int remove(long itemId) throws Exception {
        List<T> cache = read();
        if (cache.size() == 0) return -1;
        int index = -1;
        for (int i = 0; i < cache.size(); i++) {
            if (cache.get(i).isSameAs(itemId)) {
                cache.remove(i);
                index = i;
                break;
            }
        }
        write(cache, false);
        log("Removed item at index " + index + " from " + cacheFile.getName());
        return index;
    }

    private void runPriorityThread(Runnable runnable) {
        Thread t = new Thread(runnable);
        t.setPriority(Thread.MAX_PRIORITY);
        t.start();
    }

    /**
     * Writes a single object to the cache from a separate thread, posting results to a callback.
     */
    public void addAsync(final T toAdd, final AddCallback callback) throws Exception {
        if (callback == null) throw new IllegalArgumentException("You must specify a callback");
        runPriorityThread(new Runnable() {
            @Override
            public void run() {
                try {
                    add(toAdd);
                    callback.onAdded(toAdd);
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
    public void updateAsync(final T toUpdate, final UpdateCallback callback) {
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
     * Writes a list of items to the cache from a separate thread, overwriting all current entries in the cache.
     */
    public void writeAsync(final List<T> items, final WriteCallback callback) {
        if (callback == null) throw new IllegalArgumentException("You must specify a callback");
        runPriorityThread(new Runnable() {
            @Override
            public void run() {
                try {
                    write(items, false);
                    mHandler.post(new Runnable() {
                        @Override
                        public void run() {
                            callback.onWrite(items);
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
    public void readAsync(final ReadCallback callback) {
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
     * @param adapter      The adapter that items will be added to.
     * @param fragment     The fragment that will receive loading notifications.
     * @param clearIfEmpty Whether or not the adapter will be cleared if the cache is empty.
     */
    public void readAsync(final SilkAdapter<T> adapter, final SilkCachedFeedFragment fragment, final boolean clearIfEmpty) {
        if (adapter == null || fragment == null)
            throw new IllegalArgumentException("The adapter and fragment parameters cannot be null.");
        else if (fragment.isLoading()) return;
        readAsync(new ReadCallback<T>() {
            @Override
            public void onRead(List<T> results) {
                adapter.clear();
                for (T item : results) adapter.add(item);
                fragment.setLoadFromCacheComplete(false);
            }

            @Override
            public void onError(Exception e) {
                if (adapter.getCount() == 0)
                    fragment.onCacheEmpty();
                fragment.setLoadFromCacheComplete(true);
            }

            @Override
            public void onCacheEmpty() {
                if (clearIfEmpty) adapter.clear();
                fragment.onCacheEmpty();
                fragment.setLoadFromCacheComplete(false);
            }
        });
    }

    /**
     * Removes an item from the cache, results are posted to a callback.
     */
    public void removeAsync(final long itemId, final RemoveCallback callback) {
        if (callback == null) throw new IllegalArgumentException("You must specify a callback");
        runPriorityThread(new Runnable() {
            @Override
            public void run() {
                try {
                    final int removeIndex = remove(itemId);
                    mHandler.post(new Runnable() {
                        @Override
                        public void run() {
                            callback.onRemoved(removeIndex, itemId);
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
}