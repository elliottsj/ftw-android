package com.afollestad.silk.cache;

import android.content.Context;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.util.Log;

import java.io.File;
import java.io.FileOutputStream;

/**
 * Handles writing/reading images to and from the external disk cache.
 *
 * @author Aidan Follestad
 */
public class DiskCache {

    public DiskCache(Context context) {
        this.context = context;
        setCacheDirectory(null);
    }

    private Context context;
    private static File CACHE_DIR;

    public void put(String key, Bitmap image) throws Exception {
        String path = getFilePath(key);
        FileOutputStream os = new FileOutputStream(path);
        image.compress(Bitmap.CompressFormat.JPEG, 100, os);
        Log.d("SilkImageManager.DiskCache", "Wrote image to " + path);
    }

    public Bitmap get(String key) throws Exception {
        File fi = new File(CACHE_DIR, key + ".jpeg");
        if (!fi.exists()) {
            return null;
        }
        return BitmapFactory.decodeFile(fi.getAbsolutePath());
    }

    public String getFilePath(String key) {
        File fi = new File(CACHE_DIR, key + ".jpeg");
        return fi.getPath();
    }

    public void setCacheDirectory(File dir) {
        if (dir == null)
            CACHE_DIR = context.getExternalCacheDir();
        else
            CACHE_DIR = dir;
        CACHE_DIR.mkdirs();
    }
}
