package com.elliottsj.ftw.nextbus.data_store.helpers;

import android.database.sqlite.SQLiteDatabase;

public class SavedStopsHelper extends DataStoreHelper {

    public static final String[] SAVED_COLUMNS =
            { NextbusSQLiteHelper.SAVED_STOPS.COLUMN_AUTO_ID,
              NextbusSQLiteHelper.SAVED_STOPS.COLUMN_STOP };


    public SavedStopsHelper(SQLiteDatabase database) {
        super(database);
    }



}
