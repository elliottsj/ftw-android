package com.elliottsj.ftw.nextbus.data_store.helpers;

import android.database.sqlite.SQLiteDatabase;

public abstract class DataStoreHelper {

    protected SQLiteDatabase mDatabase;

    protected DataStoreHelper(SQLiteDatabase database) {
        this.mDatabase = database;
    }

    /**
     * Concatenates all of the given arrays in a new array
     *
     * @param arrays arrays to concatenate
     * @return the concatenation of all arrays
     */
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
