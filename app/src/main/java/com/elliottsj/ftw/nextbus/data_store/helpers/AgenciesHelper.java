package com.elliottsj.ftw.nextbus.data_store.helpers;

import android.content.ContentValues;
import android.database.Cursor;
import android.database.sqlite.SQLiteDatabase;

import net.sf.nextbus.publicxmlfeed.domain.Agency;
import net.sf.nextbus.publicxmlfeed.service.ServiceException;

import java.util.ArrayList;
import java.util.List;

public class AgenciesHelper extends DataStoreHelper {

    public static final String[] AGENCIES_COLUMNS =
            { NextbusSQLiteHelper.AGENCIES.COLUMN_COPYRIGHT,
              NextbusSQLiteHelper.AGENCIES.COLUMN_TIMESTAMP,
              NextbusSQLiteHelper.AGENCIES.COLUMN_TAG,
              NextbusSQLiteHelper.AGENCIES.COLUMN_TITLE,
              NextbusSQLiteHelper.AGENCIES.COLUMN_SHORT_TITLE,
              NextbusSQLiteHelper.AGENCIES.COLUMN_REGION_TITLE };

    public AgenciesHelper(SQLiteDatabase database) {
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
        return agency.getAge();
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
        values.put(NextbusSQLiteHelper.AGENCIES.COLUMN_COPYRIGHT, agency.getCopyright());
        values.put(NextbusSQLiteHelper.AGENCIES.COLUMN_TIMESTAMP, agency.getTimestamp());
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
        String copyright = cursor.getString(0);
        long timestamp = cursor.getLong(1);
        String tag = cursor.getString(2);
        String title = cursor.getString(3);
        String shortTitle = cursor.getString(4);
        String regionTitle = cursor.getString(5);

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

    /**
     * Specifies which columns in a cursor correspond to each property of an {@link net.sf.nextbus.publicxmlfeed.domain.Agency}
     */
    protected static class AgencyCursorColumns {
        private final int tagColumn;
        private final int titleColumn;
        private final int shortTitleColumn;
        private final int regionTitleColumn;
        private final int copyrightColumn;
        private final int timestampColumn;

        private AgencyCursorColumns(int tagColumn, int titleColumn, int shortTitleColumn, int regionTitleColumn, int copyrightColumn, int timestampColumn) {
            this.tagColumn = tagColumn;
            this.titleColumn = titleColumn;
            this.shortTitleColumn = shortTitleColumn;
            this.regionTitleColumn = regionTitleColumn;
            this.copyrightColumn = copyrightColumn;
            this.timestampColumn = timestampColumn;
        }

        public static AgencyCursorColumns fromCursor(Cursor cursor) {
            return new AgencyCursorColumns(cursor.getColumnIndexOrThrow(NextbusSQLiteHelper.AGENCIES.COLUMN_TAG),
                                           cursor.getColumnIndexOrThrow(NextbusSQLiteHelper.AGENCIES.COLUMN_TITLE),
                                           cursor.getColumnIndexOrThrow(NextbusSQLiteHelper.AGENCIES.COLUMN_SHORT_TITLE),
                                           cursor.getColumnIndexOrThrow(NextbusSQLiteHelper.AGENCIES.COLUMN_REGION_TITLE),
                                           cursor.getColumnIndexOrThrow(NextbusSQLiteHelper.AGENCIES.COLUMN_COPYRIGHT),
                                           cursor.getColumnIndexOrThrow(NextbusSQLiteHelper.AGENCIES.COLUMN_TIMESTAMP));
        }

        public int getTagColumn() {
            return tagColumn;
        }

        public int getTitleColumn() {
            return titleColumn;
        }

        public int getShortTitleColumn() {
            return shortTitleColumn;
        }

        public int getRegionTitleColumn() {
            return regionTitleColumn;
        }

        public int getCopyrightColumn() {
            return copyrightColumn;
        }

        public int getTimestampColumn() {
            return timestampColumn;
        }
    }

    protected static Agency agencyFromCursor(Cursor cursor, AgencyCursorColumns agencyCursorColumns) {
        return new Agency(cursor.getString(agencyCursorColumns.getTagColumn()),
                          cursor.getString(agencyCursorColumns.getTitleColumn()),
                          cursor.getString(agencyCursorColumns.getShortTitleColumn()),
                          cursor.getString(agencyCursorColumns.getRegionTitleColumn()),
                          cursor.getString(agencyCursorColumns.getCopyrightColumn()),
                          cursor.getLong(agencyCursorColumns.getTimestampColumn()));
    }

}
