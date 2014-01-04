package com.elliottsj.ftw.test;

import com.jakewharton.disklrucache.DiskLruCache;

import java.io.File;
import java.io.IOException;
import java.io.ObjectInputStream;
import java.io.ObjectOutputStream;

public class MyCache {

    DiskLruCache mDiskLruCache;

    public MyCache(File directory) throws IOException {
        mDiskLruCache = DiskLruCache.open(directory, 1, 1, 20 * 2^20);
    }

    public void put(String key, Object object) {
        DiskLruCache.Editor editor = null;
        try {
            editor = mDiskLruCache.edit(key);
            if (editor == null) {
                return;
            }

            ObjectOutputStream out = new ObjectOutputStream(editor.newOutputStream(0));
            out.writeObject(object);
            editor.commit();
            out.close();
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    public Object get(String key) {
        DiskLruCache.Snapshot snapshot;

        try {
            snapshot = mDiskLruCache.get(key);
            ObjectInputStream in = new ObjectInputStream(snapshot.getInputStream(0));
            return in.readObject();
        } catch (IOException e) {
            e.printStackTrace();
        } catch (ClassNotFoundException ex) {
            ex.printStackTrace();
        }
        return null;
    }

}
