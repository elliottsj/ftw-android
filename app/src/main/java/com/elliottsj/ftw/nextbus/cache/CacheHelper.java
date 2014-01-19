package com.elliottsj.ftw.nextbus.cache;

import android.database.sqlite.SQLiteDatabase;

public abstract class CacheHelper {

    protected SQLiteDatabase mDatabase;

    protected CacheHelper(SQLiteDatabase database) {
        this.mDatabase = database;
    }

    protected static String[] concatAll(String[]... arrays) {
        int len = 0;
        for (final String[] array : arrays)
            len += array.length;

        final String[] result = new String[len];

        int currentPos = 0;
        for (final String[] array : arrays) {
            System.arraycopy(array, 0, result, currentPos, array.length);
            currentPos += array.length;
        }

        return result;
    }

    protected static String[] stringsWithPrefix(String prefix, String[] strings) {
        String[] result = new String[strings.length];
        for (int i = 0; i < strings.length; i++)
            result[i] = prefix + strings[i];
        return result;
    }

}
