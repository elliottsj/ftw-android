package com.elliottsj.ftw.nextbus.cache;

import android.content.Context;
import android.database.sqlite.SQLiteDatabase;
import android.database.sqlite.SQLiteOpenHelper;

public class AgenciesSQLiteHelper extends SQLiteOpenHelper {

    public static final String TABLE_AGENCIES = "agencies";
    public static final String COLUMN_AUTO_ID = "_id";
    public static final String COLUMN_COPYRIGHT = "copyright";
    public static final String COLUMN_TIMESTAMP = "timestamp";
    public static final String COLUMN_TAG = "tag";
    public static final String COLUMN_TITLE = "title";
    public static final String COLUMN_SHORT_TITLE = "short_title";
    public static final String COLUMN_REGION_TITLE = "region_title";

    private static final String DATABASE_NAME = "agencies.db";
    private static final int DATABASE_VERSION = 1;

    private static final String DATABASE_CREATE =
        "CREATE TABLE " + TABLE_AGENCIES + " (" +
            COLUMN_AUTO_ID + " INTEGER PRIMARY KEY AUTOINCREMENT, " +
            COLUMN_COPYRIGHT + " TEXT NOT NULL" +
            COLUMN_TIMESTAMP + " INTEGER NOT NULL" +
            COLUMN_TAG + " TEXT NOT NULL" +
            COLUMN_TITLE + " TEXT NOT NULL" +
            COLUMN_SHORT_TITLE + " TEXT" +
            COLUMN_REGION_TITLE + " TEXT NOT NULL" +
        ");";

    public AgenciesSQLiteHelper(Context context) {
        super(context, DATABASE_NAME, null, DATABASE_VERSION);
    }

    @Override
    public void onCreate(SQLiteDatabase db) {
        db.execSQL(DATABASE_CREATE);
    }

    @Override
    public void onUpgrade(SQLiteDatabase db, int oldVersion, int newVersion) {
        db.execSQL("DROP TABLE IF EXISTS " + TABLE_AGENCIES);
        onCreate(db);
    }

}
