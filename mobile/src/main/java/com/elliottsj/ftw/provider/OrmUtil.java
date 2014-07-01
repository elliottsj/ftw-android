package com.elliottsj.ftw.provider;

import android.database.Cursor;

import com.j256.ormlite.android.AndroidDatabaseResults;
import com.j256.ormlite.dao.CloseableIterator;
import com.j256.ormlite.stmt.QueryBuilder;
import com.j256.ormlite.stmt.SelectArg;

import java.sql.SQLException;

class OrmUtil {

    @SuppressWarnings("ConstantConditions")
    static SelectArg[] selectArgsFromStrings(String[] stringArgs) {
        SelectArg[] selectArgs = new SelectArg[stringArgs != null ? stringArgs.length : 0];
        for (int i = 0; i < selectArgs.length; i++)
            selectArgs[i] = new SelectArg(stringArgs[i]);
        return selectArgs;
    }

    static Cursor cursorFromQueryBuilder(QueryBuilder queryBuilder, String[] projection, String selection, String[] selectionArgs, String sortOrder) throws SQLException {
        if (projection != null)
            queryBuilder.selectColumns(projection);
        if (selection != null)
            queryBuilder.where().raw(selection, OrmUtil.selectArgsFromStrings(selectionArgs));
        if (sortOrder != null)
            queryBuilder.orderBy(sortOrder, true);

        CloseableIterator iterator = queryBuilder.iterator();
        AndroidDatabaseResults results = (AndroidDatabaseResults) iterator.getRawResults();
        return results.getRawCursor();
    }

}
