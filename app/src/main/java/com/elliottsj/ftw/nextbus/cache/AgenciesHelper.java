package com.elliottsj.ftw.nextbus.cache;

import android.content.ContentValues;
import android.database.Cursor;
import android.database.sqlite.SQLiteDatabase;

import net.sf.nextbus.publicxmlfeed.domain.Agency;
import net.sf.nextbus.publicxmlfeed.service.ServiceException;

import java.util.ArrayList;
import java.util.List;

public class AgenciesHelper extends CacheHelper {

    private static final String[] AGENCIES_COLUMNS =
            { NextbusSQLiteHelper.AGENCIES.COLUMN_AUTO_ID,
              NextbusSQLiteHelper.AGENCIES.COLUMN_COPYRIGHT,
              NextbusSQLiteHelper.AGENCIES.COLUMN_TIMESTAMP,
              NextbusSQLiteHelper.AGENCIES.COLUMN_TAG,
              NextbusSQLiteHelper.AGENCIES.COLUMN_TITLE,
              NextbusSQLiteHelper.AGENCIES.COLUMN_SHORT_TITLE,
              NextbusSQLiteHelper.AGENCIES.COLUMN_REGION_TITLE };

    protected AgenciesHelper(SQLiteDatabase database) {
        super(database);
    }

    public boolean isAgenciesCached() {
        Cursor cursor = mDatabase.query(NextbusSQLiteHelper.AGENCIES.TABLE,
                                        AGENCIES_COLUMNS, null, null, null, null, null, "1");

        int count = cursor.getCount();
        cursor.close();
        return count != 0;
    }

    public boolean isAgencyCached(Agency agency) {
        Cursor cursor = getAgencyCursor(mDatabase, agency.getTag());
        int count = cursor.getCount();
        cursor.close();
        return count != 0;
    }

    public long getAgenciesAge() {
        Cursor cursor = mDatabase.query(NextbusSQLiteHelper.AGENCIES.TABLE,
                                        AGENCIES_COLUMNS, null, null, null, null, null, "1");
        cursor.moveToFirst();
        Agency agency = getAgencyFromCursor(cursor);
        cursor.close();
        return agency.getObjectAge();
    }

    /**
     * Gets the agencies in this cache.
     *
     * @return the agencies in this cache
     */
    public List<Agency> getAgencies() {
        if (!isAgenciesCached())
            throw new ServiceException("Agencies are not cached");

        List<Agency> agencies = new ArrayList<Agency>();

        Cursor cursor = mDatabase.query(NextbusSQLiteHelper.AGENCIES.TABLE,
                                        AGENCIES_COLUMNS, null, null, null, null, null);

        cursor.moveToFirst();
        while (!cursor.isAfterLast()) {
            agencies.add(getAgencyFromCursor(cursor));
            cursor.moveToNext();
        }
        cursor.close();

        return agencies;
    }

    public Agency getAgency(String tag) {
        Cursor cursor = getAgencyCursor(mDatabase, tag);
        Agency agency = getAgencyFromCursor(cursor);
        cursor.close();

        return agency;
    }

    public void putAgencies(List<Agency> agencies) {
        // Delete all table entries
        mDatabase.execSQL("DELETE FROM " + NextbusSQLiteHelper.AGENCIES.TABLE);

        // Insert new entries
        for (Agency agency : agencies)
            putAgency(agency);
    }

    public void putAgency(Agency agency) {
        ContentValues values = new ContentValues();
        values.put(NextbusSQLiteHelper.AGENCIES.COLUMN_COPYRIGHT, agency.getCopyrightNotice());
        values.put(NextbusSQLiteHelper.AGENCIES.COLUMN_TIMESTAMP, agency.getObjectTimestamp());
        values.put(NextbusSQLiteHelper.AGENCIES.COLUMN_TAG, agency.getTag());
        values.put(NextbusSQLiteHelper.AGENCIES.COLUMN_TITLE, agency.getTitle());
        values.put(NextbusSQLiteHelper.AGENCIES.COLUMN_SHORT_TITLE, agency.getShortTitle());
        values.put(NextbusSQLiteHelper.AGENCIES.COLUMN_REGION_TITLE, agency.getRegionTitle());

        mDatabase.insert(NextbusSQLiteHelper.AGENCIES.TABLE, null, values);
    }

    /**
     * @param tag an agency tag
     * @return a cursor pointing at the row of the agency with the given tag
     */
    private static Cursor getAgencyCursor(SQLiteDatabase database, String tag) {
        Cursor cursor = database.query(NextbusSQLiteHelper.AGENCIES.TABLE,
                                       AGENCIES_COLUMNS,
                                       NextbusSQLiteHelper.AGENCIES.COLUMN_TAG + " = ?", new String[] { tag },
                                       null, null, null, "1");

        cursor.moveToFirst();
        return cursor;
    }

    /**
     * @param cursor a cursor pointing at a row of agency data
     * @return the agency on the current row of the given cursor
     */
    private Agency getAgencyFromCursor(Cursor cursor) {
        String copyright = cursor.getString(1);
        long timestamp = cursor.getLong(2);
        String tag = cursor.getString(3);
        String title = cursor.getString(4);
        String shortTitle = cursor.getString(5);
        String regionTitle = cursor.getString(6);

        return new Agency(tag, title, shortTitle, regionTitle, copyright, timestamp);
    }

    /**
     * @param agency an agency
     * @return the primary key AUTO_ID of the given agency
     */
    public static int getAgencyAutoId(SQLiteDatabase database, Agency agency) {
        Cursor agencyCursor = getAgencyCursor(database, agency.getTag());
        int agencyAutoId = agencyCursor.getInt(0);
        agencyCursor.close();
        return agencyAutoId;
    }

}
