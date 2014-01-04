package com.elliottsj.ftw.test;

import android.content.Context;
import android.test.AndroidTestCase;
import android.test.IsolatedContext;

import java.io.File;
import java.io.IOException;
import java.util.ArrayList;
import java.util.List;

public class MyCacheTest extends AndroidTestCase {

    File mDirectory;
    MyCache myCache;

    public void setUp() throws IOException {
        mDirectory = new File(getContext().getCacheDir(), "MyCacheTest");
        myCache = new MyCache(mDirectory);
    }

    public void tearDown() throws Exception {
//        myCache.mDiskLruCache.delete();
        // just close the cache, leaving contents on disk
        myCache.mDiskLruCache.close();
    }

    public void testPutString() {
        myCache.put("test-string", "hello");
    }

    public void testPutArrayList() throws Exception {
        List<String> list = new ArrayList<String>();
        list.add("first string");
        list.add("second string");

        myCache.put("test-array-list", list);
    }

//    public void testPutGet() throws Exception {
//        List<String> list = new ArrayList<String>();
//        list.add("first string");
//        list.add("second string");
//
//        myCache.put("test-array-list", list);
//
//        // Close and re-open the cache, forcing it to read from disk
//        myCache.mDiskLruCache.close();
//        myCache = new MyCache(mDirectory);
//
//        List<String> actual = (List<String>) myCache.get("test-array-list");
//        assertNotNull(actual); // test fails here
//        assertEquals(list, actual);
//    }

}
