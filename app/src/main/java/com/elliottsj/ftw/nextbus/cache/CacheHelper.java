package com.elliottsj.ftw.nextbus.cache;

import android.database.sqlite.SQLiteDatabase;

public abstract class CacheHelper {

    protected SQLiteDatabase mDatabase;

    protected CacheHelper(SQLiteDatabase database) {
        this.mDatabase = database;
    }

    protected static String toCommaDelimited(String prefix, String[] strings) {
        String result = "";
        for (int i = 0; i < strings.length; i++)
            result += i == strings.length - 1 ? (prefix + strings[i]) : (prefix + strings[i] + ", ");
        return result;
    }

    protected static String[] stringsWithPrefix(String prefix, String[] strings) {
        String[] result = new String[strings.length];
        for (int i = 0; i < strings.length; i++)
            result[i] = prefix + strings[i];
        return result;
    }

}
