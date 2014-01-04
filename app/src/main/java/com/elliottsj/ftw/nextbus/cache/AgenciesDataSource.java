package com.elliottsj.ftw.nextbus.cache;

import android.content.ContentValues;
import android.content.Context;
import android.database.Cursor;
import android.database.SQLException;
import android.database.sqlite.SQLiteDatabase;

import net.sf.nextbus.publicxmlfeed.domain.Agency;

import java.util.ArrayList;
import java.util.List;

public class AgenciesDataSource {

    private SQLiteDatabase mDatabase;
    private AgenciesSQLiteHelper mDbHelper;
    private String[] mAllColumns = { AgenciesSQLiteHelper.COLUMN_AUTO_ID,
                                     AgenciesSQLiteHelper.COLUMN_COPYRIGHT,
                                     AgenciesSQLiteHelper.COLUMN_TIMESTAMP,
                                     AgenciesSQLiteHelper.COLUMN_TAG,
                                     AgenciesSQLiteHelper.COLUMN_TITLE,
                                     AgenciesSQLiteHelper.COLUMN_SHORT_TITLE,
                                     AgenciesSQLiteHelper.COLUMN_REGION_TITLE };

    public AgenciesDataSource(Context context) {
        mDbHelper = new AgenciesSQLiteHelper(context);
    }

    public void open() throws SQLException {
        mDatabase = mDbHelper.getWritableDatabase();
    }

    public void close() {
        mDbHelper.close();
    }

    public void insertAgency(Agency agency) {
        ContentValues values = new ContentValues();
        values.put(AgenciesSQLiteHelper.COLUMN_COPYRIGHT, agency.getRegionTitle());
        values.put(AgenciesSQLiteHelper.COLUMN_TIMESTAMP, agency.getObjectTimestamp());
        values.put(AgenciesSQLiteHelper.COLUMN_TAG, agency.getTag());
        values.put(AgenciesSQLiteHelper.COLUMN_TITLE, agency.getTitle());
        values.put(AgenciesSQLiteHelper.COLUMN_SHORT_TITLE, agency.getShortTitle());
        values.put(AgenciesSQLiteHelper.COLUMN_REGION_TITLE, agency.getRegionTitle());
        mDatabase.insert(AgenciesSQLiteHelper.TABLE_AGENCIES, null, values);
    }

    public void deleteAgency(Agency agency) {
        mDatabase.delete(AgenciesSQLiteHelper.TABLE_AGENCIES,
                         AgenciesSQLiteHelper.COLUMN_TAG + " = " + agency.getTag(), null);
    }

    /**
     * @return an arbitrarily-selected agency from this data source, or null if there are no agencies
     */
    public Agency getAnAgency() {
        if (isEmpty())
            return null;
        Cursor cursor = mDatabase.query(AgenciesSQLiteHelper.TABLE_AGENCIES,
                                        mAllColumns, null, null, null, null, null, "1");
        cursor.moveToFirst();
        Agency agency = agencyFromCursor(cursor);
        cursor.close();
        return agency;
    }

    public List<Agency> getAllAgencies() {
        List<Agency> agencies = new ArrayList<Agency>();

        Cursor cursor = mDatabase.query(AgenciesSQLiteHelper.TABLE_AGENCIES,
                                        mAllColumns, null, null, null, null, null);

        cursor.moveToFirst();
        while (!cursor.isAfterLast()) {
            agencies.add(agencyFromCursor(cursor));
            cursor.moveToNext();
        }

        cursor.close();
        return agencies;
    }

    public void putAgencies(List<Agency> agencies) {
        for (Agency agency : agencies)
            insertAgency(agency);
    }

    public boolean isEmpty() {
        Cursor cursor = mDatabase.rawQuery("SELECT COUNT(*) FROM " + AgenciesSQLiteHelper.TABLE_AGENCIES, null);
        cursor.moveToFirst();
        int rowCount = cursor.getInt(0);
        cursor.close();
        return rowCount == 0;
    }

    private Agency agencyFromCursor(Cursor cursor) {
        String copyright = cursor.getString(1);
        long timestamp = cursor.getLong(2);
        String tag = cursor.getString(3);
        String title = cursor.getString(4);
        String shortTitle = cursor.getString(5);
        String regionTitle = cursor.getString(6);

        return new Agency(tag, title, shortTitle, regionTitle, copyright, timestamp);
    }

}
