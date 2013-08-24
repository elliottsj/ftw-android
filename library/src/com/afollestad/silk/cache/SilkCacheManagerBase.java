package com.afollestad.silk.cache;

import android.content.Context;
import android.content.SharedPreferences;
import android.os.Environment;
import android.os.Handler;
import android.util.Log;

import java.io.*;
import java.util.ArrayList;
import java.util.Calendar;
import java.util.List;

/**
 * @author Aidan Follestad (afollestad)
 */
class SilkCacheManagerBase<T extends SilkComparable> {

    protected SilkCacheManagerBase(Context context, String cacheName, File cacheDir) {
        mContext = context;
        if (cacheName == null || cacheName.trim().isEmpty())
            cacheName = "default";
        if (cacheDir == null)
            cacheDir = new File(Environment.getExternalStorageDirectory(), "Silk");
        if (!cacheDir.exists())
            cacheDir.mkdirs();
        cacheFile = new File(cacheDir, cacheName.toLowerCase() + ".cache");
    }

    private Context mContext;
    protected List<T> buffer;
    private final File cacheFile;
    protected Handler mHandler;
    protected boolean isChanged;

    protected void log(String message) {
        Log.d("SilkCacheManager", getCacheFile().getName() + ": " + message);
    }

    protected Context getContext() {
        return mContext;
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
        if (isExpired()) {
            log("The cache has expired, wiping...");
            buffer = new ArrayList<T>();
            getContext().getSharedPreferences("[silk-cache-expirations]", Context.MODE_PRIVATE)
                    .edit().remove(getCacheFile().getName()).commit();
            getCacheFile().delete();
            return;
        } else if (buffer != null) return;
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

    private boolean isExpired() {
        SharedPreferences prefs = mContext.getSharedPreferences("[silk-cache-expirations]", Context.MODE_PRIVATE);
        if (!hasExpiration()) return false;
        long dateTime = prefs.getLong(getCacheFile().getName(), 0);
        long now = Calendar.getInstance().getTimeInMillis();
        return dateTime <= now;
    }

    /**
     * Gets the {@link CacheLimiter} for the cache, if any has been set in the past.
     */
    public final CacheLimiter getLimiter() {
        SharedPreferences prefs = mContext.getSharedPreferences("[silk-cache-limiters]", Context.MODE_PRIVATE);
        if (!hasLimiter()) return null;
        return new CacheLimiter(prefs.getString(getCacheFile().getName(), null));
    }

    /**
     * Checks whether or not an expiration has been set to the cache.
     */
    public final boolean hasExpiration() {
        SharedPreferences prefs = mContext.getSharedPreferences("[silk-cache-expirations]", Context.MODE_PRIVATE);
        return prefs.contains(getCacheFile().getName());
    }

    /**
     * Checks whether or not an limiter has been set to the cache.
     */
    public final boolean hasLimiter() {
        SharedPreferences prefs = mContext.getSharedPreferences("[silk-cache-limiters]", Context.MODE_PRIVATE);
        return prefs.contains(getCacheFile().getName());
    }

    private List<T> loadItems() {
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

        // Trim off older items
        CacheLimiter mLimiter = getLimiter();
        if (mLimiter != null && buffer.size() > mLimiter.getSize()) {
            log("Cache (" + buffer.size() + ") is larger than size limit (" + mLimiter.getSize() + "), trimming...");
            while (buffer.size() > mLimiter.getSize()) {
                if (mLimiter.getMode() == CacheLimiter.TrimMode.TOP) buffer.remove(0);
                else buffer.remove(buffer.size() - 1);
            }
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