package com.afollestad.silk.cache;

import android.os.Environment;
import android.os.Handler;
import android.util.Log;

import java.io.*;
import java.util.ArrayList;
import java.util.List;

/**
 * @author Aidan Follestad (afollestad)
 */
class SilkCacheManagerBase<T extends SilkComparable> {

    protected SilkCacheManagerBase(String cacheName, File cacheDir) {
        if (cacheName == null || cacheName.trim().isEmpty())
            cacheName = "default";
        if (cacheDir == null)
            cacheDir = new File(Environment.getExternalStorageDirectory(), "Silk");
        if (!cacheDir.exists())
            cacheDir.mkdirs();
        cacheFile = new File(cacheDir, cacheName.toLowerCase() + ".cache");
    }

    protected List<T> buffer;
    private final File cacheFile;
    protected Handler mHandler;
    protected boolean isChanged;

    protected void log(String message) {
        Log.d("SilkCacheManager", getCacheFile().getName() + ": " + message);
    }

    /**
     * Checks whether or not the manager has been commited.
     * <p/>
     * If it has, you cannot commit again until you re-initialize the manager
     * or make a call to {@link com.afollestad.silk.cache.SilkCacheManager#forceReload()}.
     */
    public final boolean isCommitted() {
        return buffer == null;
    }

    /**
     * Gets whether or not the cache has been changed since it was initialized.
     */
    public final boolean isChanged() {
        return isChanged;
    }

    protected void runPriorityThread(Runnable runnable) {
        Thread t = new Thread(runnable);
        t.setPriority(Thread.MAX_PRIORITY);
        t.start();
    }

    protected final Handler getHandler() {
        if (mHandler == null)
            mHandler = new Handler();
        return mHandler;
    }

    protected File getCacheFile() {
        return cacheFile;
    }

    protected void reloadIfNecessary() {
        if (buffer != null) return;
        buffer = loadItems();
    }

    /**
     * Gets the items currently stored in the cache manager's buffer; the buffer is loaded when the manager
     * is instantiated, cleared when it commits, and reloaded when needed.
     */
    public List<T> read() {
        reloadIfNecessary();
        return buffer;
    }

    private List<T> loadItems() {
        log("Reloading cache items to buffer.");
        try {
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
        } catch (Exception e) {
            e.printStackTrace();
            log("Error loading items: " + e.getMessage());
        }
        return null;
    }

    /**
     * Commits all changes to the cache file. This is from the calling thread.
     */
    public void commit() throws Exception {
        if (isCommitted()) {
            throw new IllegalStateException("The SilkCacheManager has already committed, you must re-initialize the manager or call forceReload().");
        } else if (!isChanged()) {
            throw new IllegalStateException("The SilkCacheManager has not been modified since initialization.");
        } else if (buffer.size() == 0) {
            if (cacheFile.exists()) {
                log("Deleting: " + cacheFile.getName());
                cacheFile.delete();
            }
            return;
        }
        int subtraction = 0;
        FileOutputStream fileOutputStream = new FileOutputStream(cacheFile);
        ObjectOutputStream objectOutputStream = new ObjectOutputStream(fileOutputStream);
        for (T item : buffer) {
            if (item.shouldIgnore()) {
                subtraction++;
                continue;
            }
            objectOutputStream.writeObject(item);
        }
        objectOutputStream.close();
        log("Committed " + (buffer.size() - subtraction) + " items to " + cacheFile.getName());
        buffer = null;
        isChanged = false;
    }

    /**
     * Commits all changes to the cache file. This is run on a separate thread and the results are posted to a callback.
     */
    public void commitAsync(final SilkCacheManager.SimpleCommitCallback callback) {
        final Handler handler = getHandler();
        runPriorityThread(new Runnable() {
            @Override
            public void run() {
                try {
                    commit();
                    if (callback != null) {
                        handler.post(new Runnable() {
                            @Override
                            public void run() {
                                if (callback instanceof SilkCacheManager.CommitCallback)
                                    ((SilkCacheManager.CommitCallback) callback).onCommitted();
                            }
                        });
                    }
                } catch (final Exception e) {
                    e.printStackTrace();
                    log("Cache commit error: " + e.getMessage());
                    if (callback != null) {
                        getHandler().post(new Runnable() {
                            @Override
                            public void run() {
                                callback.onError(e);
                            }
                        });
                    }
                }
            }
        });
    }
}