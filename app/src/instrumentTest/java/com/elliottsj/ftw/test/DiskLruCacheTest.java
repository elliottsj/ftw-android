package com.elliottsj.ftw.test;

import android.content.Context;
import android.test.AndroidTestCase;
import android.test.IsolatedContext;

import com.jakewharton.disklrucache.DiskLruCache;

import junit.framework.Assert;

import java.io.BufferedInputStream;
import java.io.BufferedOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.ObjectInputStream;
import java.io.ObjectOutputStream;
import java.io.OutputStream;
import java.util.ArrayList;
import java.util.List;

public class DiskLruCacheTest extends AndroidTestCase {

    DiskLruCache mDiskLruCache;

    @Override
    public void setUp() throws IOException {
        // Not sure if I'm using IsolatedContext properly since it writes into the app's main cache directory,
        // but it works for now
        Context context = getContext();
        IsolatedContext isolatedContext = new IsolatedContext(context.getContentResolver(), context);
        mDiskLruCache = DiskLruCache.open(isolatedContext.getCacheDir(), 1, 1, (long) (20 * Math.pow(2, 20)));
    }

    @Override
    public void tearDown() throws Exception {
        mDiskLruCache.delete();
    }

    public void testStoreLong() throws IOException {
        // This test passes

        DiskLruCache.Editor editor = mDiskLruCache.edit("test-store-long");
        OutputStream os = editor.newOutputStream(0);
        OutputStream bos = new BufferedOutputStream(os);
        ObjectOutputStream oos = new ObjectOutputStream(bos);

        oos.writeLong(1122423543552L);

        oos.close();
        editor.commit();

        DiskLruCache.Snapshot snapshot = mDiskLruCache.get("test-store-long");
        assertNotNull("Snapshot is null", snapshot);
        InputStream is = snapshot.getInputStream(0);
        InputStream bis = new BufferedInputStream(is);
        ObjectInputStream ois = new ObjectInputStream(bis);

        Assert.assertEquals(1122423543552L, ois.readLong());

        ois.close();
    }

    public void testStoreString() throws IOException, ClassNotFoundException {
        // This test passes

        DiskLruCache.Editor editor = mDiskLruCache.edit("test-store-string");
        OutputStream os = editor.newOutputStream(0);
        OutputStream bos = new BufferedOutputStream(os);
        ObjectOutputStream oos = new ObjectOutputStream(bos);

        oos.writeObject("some string");

        oos.close();
        editor.commit();

        DiskLruCache.Snapshot snapshot = mDiskLruCache.get("test-store-string");
        assertNotNull("Snapshot is null", snapshot);
        InputStream is = snapshot.getInputStream(0);
        InputStream bis = new BufferedInputStream(is);
        ObjectInputStream ois = new ObjectInputStream(bis);

        Assert.assertEquals("some string", ois.readObject());

        ois.close();
    }

    public void testStoreArrayList() throws Exception {
        // This test passes

        DiskLruCache.Editor editor = mDiskLruCache.edit("test-store-array-list");
        OutputStream os = editor.newOutputStream(0);
        OutputStream bos = new BufferedOutputStream(os);
        ObjectOutputStream oos = new ObjectOutputStream(bos);

        List<String> list = new ArrayList<String>();
        list.add("first string");
        list.add("second string");

        oos.writeObject(list);

        oos.close();
        editor.commit();

        DiskLruCache.Snapshot snapshot = mDiskLruCache.get("test-store-array-list");
        assertNotNull("Snapshot is null", snapshot);
        InputStream is = snapshot.getInputStream(0);
        InputStream bis = new BufferedInputStream(is);
        ObjectInputStream ois = new ObjectInputStream(bis);

        assertEquals(list, ois.readObject());

        ois.close();
    }

    public void testPersistArrayListSnapshot() throws IOException, ClassNotFoundException, InterruptedException {
        // This test fails

        DiskLruCache.Editor editor = mDiskLruCache.edit("test-store-array-list");
        OutputStream os = editor.newOutputStream(0);
        OutputStream bos = new BufferedOutputStream(os);
        ObjectOutputStream oos = new ObjectOutputStream(bos);

        List<String> list = new ArrayList<String>();
        list.add("first string");
        list.add("second string");

        oos.writeObject(list);

        oos.close();
        editor.commit();

        // Close and re-open the cache, forcing it to read from disk
        mDiskLruCache.close();
        mDiskLruCache = DiskLruCache.open(mDiskLruCache.getDirectory(), 1, 1, 20 * 2^20);

        DiskLruCache.Snapshot snapshot = mDiskLruCache.get("test-store-array-list");
        // The below assertion fails
        assertNotNull("Snapshot is null", snapshot);
        InputStream is = snapshot.getInputStream(0);
        InputStream bis = new BufferedInputStream(is);
        ObjectInputStream ois = new ObjectInputStream(bis);

        assertEquals(list, ois.readObject());

        ois.close();
    }

    public void testPersistArrayListEditor() throws IOException, ClassNotFoundException {
        // This test fails

        DiskLruCache.Editor editor = mDiskLruCache.edit("test-store-array-list");
        OutputStream os = editor.newOutputStream(0);
        OutputStream bos = new BufferedOutputStream(os);
        ObjectOutputStream oos = new ObjectOutputStream(bos);

        List<String> list = new ArrayList<String>();
        list.add("first string");
        list.add("second string");

        oos.writeObject(list);

        oos.close();
        editor.commit();

        // Close and re-open the cache, forcing it to read from disk
        mDiskLruCache.close();
        mDiskLruCache = DiskLruCache.open(mDiskLruCache.getDirectory(), 1, 1, (long) (20 * Math.pow(2, 20)));

        editor = mDiskLruCache.edit("test-store-array-list");
        assertNotNull("Editor is null", editor);
        InputStream is = editor.newInputStream(0);
        // The below assertion fails
        assertNotNull("InputStream is null; no value found in the cache entry", is);
        InputStream bis = new BufferedInputStream(is);
        ObjectInputStream ois = new ObjectInputStream(bis);

        assertEquals(list, ois.readObject());

        ois.close();
    }

}
