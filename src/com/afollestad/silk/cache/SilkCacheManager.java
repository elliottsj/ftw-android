package com.afollestad.silk.cache;

import android.app.Activity;
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

    /**
     * Initializes a new SilkCacheManager.
     *
     * @param context   The context used for retrieving the cache directory and running methods on the UI thread.
     * @param cacheName The name of the cache, must be unique from other feed caches, but must also be valid for being in a file name.
     */
    public SilkCacheManager(Activity context, String cacheName) {
        this.context = context;
        cacheFile = new File(context.getCacheDir(), cacheName + ".cache");
    }

    private final Activity context;
    private final File cacheFile;

    private void log(String message) {
        Log.d("FeedCacheManager", message);
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
                    for (T item : items) {
                        objectOutputStream.writeObject(item);
                    }
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
            fragment.performRefresh(true);
            return;
        }
        fragment.setLoading(true);
        adapter.clear();
        Thread t = new Thread(new Runnable() {
            @Override
            public void run() {
                try {
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
                    context.runOnUiThread(new Runnable() {
                        @Override
                        public void run() {
                            for (T item : toAdd) adapter.add(item);
                        }
                    });
                    objectInputStream.close();
                    log("Read " + adapter.getCount() + " items from " + cacheFile.getName());
                } catch (Exception e) {
                    log("Cache read error: " + e.getMessage());
                    e.printStackTrace();
                }

                fragment.runOnUiThread(new Runnable() {
                    @Override
                    public void run() {
                        adapter.notifyDataSetChanged();
                        fragment.setLoadFromCacheComplete();
                        if (adapter.getCount() == 0)
                            fragment.onCacheEmpty();
                    }
                });
            }
        });
        t.setPriority(Thread.MAX_PRIORITY);
        t.start();
    }
}

