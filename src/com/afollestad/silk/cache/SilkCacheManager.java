package com.afollestad.silk.cache;

import android.app.Activity;
import android.os.Environment;
import android.util.Log;
import com.afollestad.silk.SilkAdapter;
import com.afollestad.silk.fragments.SilkCachedFeedFragment;

import java.io.*;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

/**
 * Handles caching feeds locally.
 *
 * @author Aidan Follestad (afollestad)
 */
public final class SilkCacheManager<T> {

    public interface RemoveFilter<T> {
        public boolean shouldRemove(T item);
    }

    public interface ReadCallback<T> {

        public void onRead(List<T> results);

        public void onError(String msg);

        public void onCacheEmpty();
    }

    /**
     * Initializes a new SilkCacheManager.
     *
     * @param context   The context used for retrieving the cache directory and running methods on the UI thread.
     * @param cacheName The name of the cache, must be unique from other feed caches, but must also be valid for being in a file name.
     */
    public SilkCacheManager(Activity context, String cacheName) {
        this.context = context;
        cacheFile = new File(CACHE_DIRECTORY, cacheName + ".cache");
        if (!CACHE_DIRECTORY.exists()) CACHE_DIRECTORY.mkdirs();
    }

    public final File CACHE_DIRECTORY = new File(Environment.getExternalStorageDirectory(), "Silk Cache");

    private final Activity context;
    private final File cacheFile;

    private void log(String message) {
        Log.d("SilkCacheManager", message);
    }

    /**
     * Writes a single object to the cache, without overwriting previous entries.
     */
    public void add(T toAdd) throws Exception {
        List<T> cache = read();
        cache.add(toAdd);
        write(cache);
        log("Wrote 1 item to " + cacheFile.getName());
    }

    /**
     * Writes a list of items to the cache from the calling thread, overwriting all current entries in the cache.
     */
    public void write(List<T> items) throws Exception {
        if (items == null || items.size() == 0) {
            if (cacheFile.exists()) {
                log("Adapter for " + cacheFile.getName() + " is empty, deleting file...");
                cacheFile.delete();
            }
        }
        FileOutputStream fileOutputStream = new FileOutputStream(cacheFile);
        ObjectOutputStream objectOutputStream = new ObjectOutputStream(fileOutputStream);
        for (T item : items) objectOutputStream.writeObject(item);
        objectOutputStream.close();
        log("Wrote " + items.size() + " items to " + cacheFile.getName());
    }

    /**
     * Writes an array of items to the cache from the calling thread, overwriting all current entries in the cache.
     */
    public void write(T[] items) throws Exception {
        write(Arrays.asList(items));
    }

    /**
     * Writes the contents of a {@link SilkAdapter} to the cache file from the calling thread, overwriting all current entries in the cache.
     */
    public void write(SilkAdapter<T> adapter) throws Exception {
        if (adapter == null) throw new IllegalArgumentException("The adapter cannot be null.");
        write(adapter.getItems());
    }

    /**
     * Writes a list of items to the cache from a separate thread, overwriting all current entries in the cache.
     */
    public void writeAsync(final List<T> items) {
        Thread t = new Thread(new Runnable() {
            @Override
            public void run() {
                try {
                    write(items);
                } catch (Exception e) {
                    log("Cache write error: " + e.getMessage());
                    e.printStackTrace();
                }
            }
        });
        t.setPriority(Thread.MAX_PRIORITY);
        t.start();
    }

    /**
     * Writes an array of items to the cache from a separate thread, overwriting all current entries in the cache.
     */
    public void writeAsync(T[] items) {
        writeAsync(Arrays.asList(items));
    }

    /**
     * Writes the contents of a {@link SilkAdapter} to the cache file from a separate thread, overwriting all current entries in the cache.
     */
    public void writeAsync(SilkAdapter<T> adapter) {
        if (adapter == null) throw new IllegalArgumentException("The adapter cannot be null.");
        writeAsync(adapter.getItems());
    }

    /**
     * Reads from the cache file on the calling thread and returns the results.
     */
    public List<T> read() throws Exception {
        FileInputStream fileInputStream = new FileInputStream(cacheFile);
        ObjectInputStream objectInputStream = new ObjectInputStream(fileInputStream);
        final List<T> results = new ArrayList<T>();
        while (true) {
            try {
                final T item = (T) objectInputStream.readObject();
                if (item != null) results.add(item);
            } catch (EOFException eof) {
                break;
            }
        }
        objectInputStream.close();
        log("Read " + results.size() + " items from " + cacheFile.getName());
        return results;
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
        Thread t = new Thread(new Runnable() {
            @Override
            public void run() {
                try {
                    final List<T> results = read();
                    context.runOnUiThread(new Runnable() {
                        @Override
                        public void run() {
                            if (results.size() == 0) callback.onCacheEmpty();
                            else callback.onRead(results);
                        }
                    });
                } catch (final Exception e) {
                    e.printStackTrace();
                    log("Cache read error: " + e.getMessage());
                    context.runOnUiThread(new Runnable() {
                        @Override
                        public void run() {
                            callback.onError(e.getMessage());
                        }
                    });
                }
            }
        });
        t.setPriority(Thread.MAX_PRIORITY);
        t.start();
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
        else if (fragment.isLoading())
            return;
        fragment.setLoading(false);
        readAsync(new ReadCallback<T>() {
            @Override
            public void onRead(List<T> results) {
                adapter.clear();
                for (T item : results) adapter.add(item);
                fragment.setLoadFromCacheComplete(false);
            }

            @Override
            public void onError(String msg) {
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
     * Removes items from the cache file based on the passed filter.
     */
    public void remove(RemoveFilter<T> filter) throws Exception {
        if (filter == null) throw new IllegalArgumentException("Remove filter cannot be null.");
        List<T> cache = read();
        if (cache.size() == 0) return;
        int removed = 0;
        for (int i = 0; i < cache.size(); i++) {
            if (filter.shouldRemove(cache.get(i))) {
                removed++;
                cache.remove(i);
            }
        }
        write(cache);
        log("Removed " + removed + " items from " + cacheFile.getName());
    }
}