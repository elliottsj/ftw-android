package com.afollestad.silk.cache;

import android.app.Activity;
import android.os.Environment;
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
public final class SilkCacheManager<T> {

    public interface RemoveFilter<T> {
        public boolean shouldRemove(T item);
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
        Log.d("FeedCacheManager", message);
    }

    private List<T> readEntireCache() throws Exception {
        FileInputStream fileInputStream = new FileInputStream(cacheFile);
        ObjectInputStream objectInputStream = new ObjectInputStream(fileInputStream);
        final List<T> toAdd = new ArrayList<T>();
        while (true) {
            try {
                final T item = (T) objectInputStream.readObject();
                if (item != null) toAdd.add(item);
            } catch (EOFException eof) {
                break;
            }
        }
        objectInputStream.close();
        return toAdd;
    }

    /**
     * Writes a single object to the cache.
     */
    public void add(T toAdd) throws Exception {
        boolean shouldAppend = cacheFile.exists();
        FileOutputStream fileOutputStream = new FileOutputStream(cacheFile);
        ObjectOutputStream objectOutputStream;
        if (shouldAppend) objectOutputStream = new AppendableObjectOutputStream(fileOutputStream);
        else objectOutputStream = new ObjectOutputStream(fileOutputStream);
        objectOutputStream.writeObject(toAdd);
        objectOutputStream.close();
        log("Wrote 1 item to " + cacheFile.getName());
    }

    /**
     * Removes items from the cache based on the passed filter.
     */
    public void remove(RemoveFilter filter) throws Exception {
        if (filter == null) throw new IllegalArgumentException("Remove filter cannot be null.");
        List<T> cache = readEntireCache();
        if (cache.size() == 0) return;
        for (int i = 0; i < cache.size(); i++) {
            if (filter.shouldRemove(cache.get(i)))
                cache.remove(i);
        }
        FileOutputStream fileOutputStream = new FileOutputStream(cacheFile);
        ObjectOutputStream objectOutputStream = new ObjectOutputStream(fileOutputStream);
        for (T item : cache) objectOutputStream.writeObject(item);
        objectOutputStream.close();
    }

    /**
     * Caches the contents of a SilkAdapter the manager's cache file.
     */
    public void write(final SilkAdapter<T> adapter) {
        if (adapter == null || adapter.getCount() == 0) {
            if (cacheFile.exists()) {
                log("Adapter for " + cacheFile.getName() + " is empty, deleting file...");
                cacheFile.delete();
            }
            return;
        }
        final List<T> items = adapter.getItems();
        Thread t = new Thread(new Runnable() {
            @Override
            public void run() {
                try {
                    FileOutputStream fileOutputStream = new FileOutputStream(cacheFile);
                    ObjectOutputStream objectOutputStream = new ObjectOutputStream(fileOutputStream);
                    for (T item : items) objectOutputStream.writeObject(item);
                    objectOutputStream.close();
                    log("Wrote " + items.size() + " items to " + cacheFile.getName());
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
     * Reads from the manager's cache file into a SilkAdapter.
     */
    public void read(final SilkAdapter<T> adapter, final SilkCachedFeedFragment fragment) {
        if (!cacheFile.exists()) {
            log("No cache for " + cacheFile.getName());
            fragment.onCacheEmpty();
            return;
        }
        fragment.setLoading(true);
        Thread t = new Thread(new Runnable() {
            @Override
            public void run() {
                try {
                    final List<T> toAdd = readEntireCache();
                    context.runOnUiThread(new Runnable() {
                        @Override
                        public void run() {
                            adapter.clear();
                            for (T item : toAdd) adapter.add(item);
                        }
                    });
                    log("Read " + adapter.getCount() + " items from " + cacheFile.getName());
                    fragment.runOnUiThread(new Runnable() {
                        @Override
                        public void run() {
                            if (adapter.getCount() == 0)
                                fragment.onCacheEmpty();
                            fragment.setLoadFromCacheComplete(false);
                        }
                    });
                } catch (Exception e) {
                    log("Cache read error: " + e.getMessage());
                    e.printStackTrace();
                    fragment.runOnUiThread(new Runnable() {
                        @Override
                        public void run() {
                            if (adapter.getCount() == 0)
                                fragment.onCacheEmpty();
                            fragment.setLoadFromCacheComplete(true);
                        }
                    });
                }
            }
        });
        t.setPriority(Thread.MAX_PRIORITY);
        t.start();
    }
}