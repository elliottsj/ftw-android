package com.elliottsj.ftw.provider;

import android.database.Cursor;

import com.elliottsj.nextbus.domain.Agency;

public class NextbusFactory {

    public Agency fromCursor(Cursor cursor) {
        String tag = cursor.getString(cursor.getColumnIndexOrThrow(Agency.FIELD_TAG));
        String title = cursor.getString(cursor.getColumnIndexOrThrow(Agency.FIELD_TITLE));
        String shortTitle = cursor.getString(cursor.getColumnIndexOrThrow(Agency.FIELD_SHORT_TITLE));
        String region = cursor.getString(cursor.getColumnIndexOrThrow(Agency.FIELD_REGION_TITLE));
        String copyright = cursor.getString(cursor.getColumnIndexOrThrow(Agency.FIELD_COPYRIGHT));
        Long timestamp = cursor.getLong(cursor.getColumnIndexOrThrow(Agency.FIELD_TIMESTAMP));
        return new Agency(tag, title, shortTitle, region, copyright, timestamp);
    }

}
