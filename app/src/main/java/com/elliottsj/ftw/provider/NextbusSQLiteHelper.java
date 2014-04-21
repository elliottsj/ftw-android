package com.elliottsj.ftw.provider;

import android.content.Context;
import android.database.sqlite.SQLiteDatabase;
import android.util.Log;

import com.elliottsj.ftw.provider.model.SavedStop;
import com.j256.ormlite.android.apptools.OrmLiteSqliteOpenHelper;
import com.j256.ormlite.dao.BaseDaoImpl;
import com.j256.ormlite.dao.Dao;
import com.j256.ormlite.support.ConnectionSource;
import com.j256.ormlite.table.TableUtils;

import net.sf.nextbus.publicxmlfeed.domain.Agency;
import net.sf.nextbus.publicxmlfeed.domain.Geolocation;
import net.sf.nextbus.publicxmlfeed.domain.Route;
import net.sf.nextbus.publicxmlfeed.domain.Stop;

import java.sql.SQLException;

class NextbusSQLiteHelper extends OrmLiteSqliteOpenHelper {

    private static final String TAG = NextbusSQLiteHelper.class.getName();

    protected static final int DATABASE_VERSION = 1;
    protected static final String DATABASE_NAME = "nextbus.db";

    private BaseDaoImpl<Agency, Integer> agencyDao = null;

    NextbusSQLiteHelper(Context context) {
        super(context, DATABASE_NAME, null, DATABASE_VERSION);
    }

    @Override
    public void onCreate(SQLiteDatabase database, ConnectionSource connectionSource) {
        try {
            Log.i(TAG, "Creating Nextbus tables...");
            TableUtils.createTable(connectionSource, Agency.class);
            TableUtils.createTable(connectionSource, Route.class);
            TableUtils.createTable(connectionSource, Stop.class);
            TableUtils.createTable(connectionSource, Geolocation.class);
            TableUtils.createTable(connectionSource, SavedStop.class);
        } catch (SQLException e) {
            Log.e(TAG, "Can't create database", e);
            throw new RuntimeException(e);
        }
    }

    @Override
    public void onUpgrade(SQLiteDatabase database, ConnectionSource connectionSource, int oldVersion, int newVersion) {
        try {
            Log.i(TAG, "Upgrading Nextbus tables...");
            TableUtils.dropTable(connectionSource, SavedStop.class, false);
            TableUtils.dropTable(connectionSource, Stop.class, false);
            TableUtils.dropTable(connectionSource, Geolocation.class, false);
            TableUtils.dropTable(connectionSource, Route.class, false);
            TableUtils.dropTable(connectionSource, Agency.class, false);

            onCreate(database, connectionSource);
        } catch (SQLException e) {
            Log.e(TAG, "Can't drop databases", e);
            throw new RuntimeException(e);
        }
    }

    /**
     * Close the database connections and clear any cached DAOs.
     */
    @Override
    public void close() {
        super.close();
        agencyDao = null;
    }

    public Dao<Agency, Integer> getAgencyDao() throws SQLException {
        if (agencyDao == null) {
            agencyDao = getDao(Agency.class);
        }
        return agencyDao;
    }

}
