package com.elliottsj.ftw.provider;

import android.database.Cursor;

import com.j256.ormlite.android.AndroidDatabaseResults;
import com.j256.ormlite.dao.CloseableIterator;
import com.j256.ormlite.dao.Dao;
import com.j256.ormlite.stmt.SelectArg;

import java.sql.SQLException;

class OrmUtil {

    static SelectArg[] selectArgsFromStrings(String[] stringArgs) {
        SelectArg[] selectArgs = new SelectArg[stringArgs.length];
        for (int i = 0; i < stringArgs.length; i++)
            selectArgs[i] = new SelectArg(stringArgs[i]);
        return selectArgs;
    }

    static Cursor cursorFromDao(Dao dao, String[] projection, String selection, String[] selectionArgs, String sortOrder) throws SQLException {
        CloseableIterator iterator = dao.queryBuilder()
                .selectColumns(projection)
                .orderByRaw(sortOrder)
                .where()
                .raw(selection, OrmUtil.selectArgsFromStrings(selectionArgs))
                .iterator();
        AndroidDatabaseResults results = (AndroidDatabaseResults) iterator.getRawResults();
        Cursor cursor = results.getRawCursor();
        iterator.closeQuietly();
        return cursor;
    }

}
