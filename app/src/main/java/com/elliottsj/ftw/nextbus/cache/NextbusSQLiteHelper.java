package com.elliottsj.ftw.nextbus.cache;

import android.content.Context;
import android.database.sqlite.SQLiteDatabase;
import android.database.sqlite.SQLiteOpenHelper;

public class NextbusSQLiteHelper extends SQLiteOpenHelper {

    private static final int DATABASE_VERSION = 1;
    private static final String DATABASE_NAME = "nextbus.db";

    public static final class AGENCIES {
        public static final String TABLE = "agencies";

        public static final String COLUMN_AUTO_ID = "_id";
        public static final String COLUMN_COPYRIGHT = "copyright";
        public static final String COLUMN_TIMESTAMP = "timestamp";
        public static final String COLUMN_TAG = "tag";
        public static final String COLUMN_TITLE = "title";
        public static final String COLUMN_SHORT_TITLE = "short_title";
        public static final String COLUMN_REGION_TITLE = "region_title";
    }

    public static final class ROUTES {
        public static final String TABLE = "routes";

        public static final String COLUMN_AUTO_ID = "_id";
        public static final String COLUMN_COPYRIGHT = "copyright";
        public static final String COLUMN_TIMESTAMP = "timestamp";
        public static final String COLUMN_AGENCY = "agency";
        public static final String COLUMN_TAG = "tag";
        public static final String COLUMN_TITLE = "title";
        public static final String COLUMN_SHORT_TITLE = "short_title";
    }

    public static final class ROUTE_CONFIGURATIONS {
        public static final String TABLE = "route_configurations";

        public static final String COLUMN_AUTO_ID = "_id";
        public static final String COLUMN_COPYRIGHT = "copyright";
        public static final String COLUMN_TIMESTAMP = "timestamp";
        public static final String COLUMN_ROUTE = "route";
        public static final String COLUMN_SERVICE_AREA = "service_area";
        public static final String COLUMN_UI_COLOR = "ui_color";
        public static final String COLUMN_UI_OPPOSITE_COLOR = "ui_opposite_color";
        public static final String COLUMN_STOPS = "stops";
        public static final String COLUMN_DIRECTIONS = "directions";
        public static final String COLUMN_PATHS = "paths";
    }

    public static final class VEHICLE_LOCATIONS {
        public static final String TABLE = "vehicle_locations";

        public static final String COLUMN_AUTO_ID = "_id";
        public static final String COLUMN_COPYRIGHT = "copyright";
        public static final String COLUMN_TIMESTAMP = "timestamp";
        public static final String COLUMN_VEHICLE = "vehicle";
        public static final String COLUMN_ROUTE = "route";
        public static final String COLUMN_DIRECTION_ID = "direction_id";
        public static final String COLUMN_PREDICTABLE = "predictable";
        public static final String COLUMN_LOCATION = "location";
        public static final String COLUMN_SPEED = "speed";
        public static final String COLUMN_HEADING = "heading";
    }

    private static final class SERVICE_AREAS {
        public static final String TABLE = "service_areas";

        public static final String COLUMN_AUTO_ID = "_id";
        public static final String COLUMN_LAT_MIN = "lat_min";
        public static final String COLUMN_LAT_MAX = "lat_max";
        public static final String COLUMN_LON_MIN = "lon_min";
        public static final String COLUMN_LON_MAX = "lon_max";
    }

    private static final class STOPS {
        public static final String TABLE = "stops";

        public static final String COLUMN_AUTO_ID = "_id";
        public static final String COLUMN_COPYRIGHT = "copyright";
        public static final String COLUMN_TIMESTAMP = "timestamp";
        public static final String COLUMN_AGENCY = "agency";
        public static final String COLUMN_TAG = "tag";
        public static final String COLUMN_TITLE = "title";
        public static final String COLUMN_SHORT_TITLE = "short_title";
        public static final String COLUMN_GEOLOCATION = "geolocation";
        public static final String COLUMN_ALTERNATE_STOP_ID = "alternate_stop_id";
    }

    private static final class DIRECTIONS {
        public static final String TABLE = "directions";

        public static final String COLUMN_AUTO_ID = "_id";
        public static final String COLUMN_COPYRIGHT = "copyright";
        public static final String COLUMN_TIMESTAMP = "timestamp";
        public static final String COLUMN_ROUTE = "route";
        public static final String COLUMN_TAG = "tag";
        public static final String COLUMN_TITLE = "title";
        public static final String COLUMN_NAME = "name";
        public static final String COLUMN_STOPS = "stops";
    }

    private static final class PATHS {
        public static final String TABLE = "paths";

        public static final String COLUMN_AUTO_ID = "_id";
        public static final String COLUMN_COPYRIGHT = "copyright";
        public static final String COLUMN_TIMESTAMP = "timestamp";
        public static final String COLUMN_ROUTE = "route";
        public static final String COLUMN_PATH_ID = "path_id";
        public static final String COLUMN_POINTS = "points";
    }

    private static final class POINTS {
        public static final String TABLE = "points";

        public static final String COLUMN_AUTO_ID = "_id";
        public static final String COLUMN_LAT = "lat";
        public static final String COLUMN_LON = "lon";
    }

    // CREATE TABLE statements
    private static final String CREATE_TABLE_AGENCIES =
            "CREATE TABLE " + AGENCIES.TABLE + " (" +
                AGENCIES.COLUMN_AUTO_ID + " INTEGER PRIMARY KEY AUTOINCREMENT, " +
                AGENCIES.COLUMN_COPYRIGHT + " TEXT NOT NULL" +
                AGENCIES.COLUMN_TIMESTAMP + " INTEGER NOT NULL" +
                AGENCIES.COLUMN_TAG + " TEXT NOT NULL" +
                AGENCIES.COLUMN_TITLE + " TEXT NOT NULL" +
                AGENCIES.COLUMN_SHORT_TITLE + " TEXT" +
                AGENCIES.COLUMN_REGION_TITLE + " TEXT NOT NULL" +
            ");";

    private static final String CREATE_TABLE_ROUTES =
            "CREATE TABLE " + ROUTES.TABLE + " (" +
                ROUTES.COLUMN_AUTO_ID + " INTEGER PRIMARY KEY AUTOINCREMENT, " +
                ROUTES.COLUMN_COPYRIGHT + " TEXT NOT NULL" +
                ROUTES.COLUMN_TIMESTAMP + " INTEGER NOT NULL" +
                ROUTES.COLUMN_TAG + " TEXT NOT NULL" +
                ROUTES.COLUMN_TITLE + " TEXT NOT NULL" +
                ROUTES.COLUMN_SHORT_TITLE + " TEXT" +
                "NOT NULL FOREIGN KEY(" + ROUTES.COLUMN_AGENCY + ") REFERENCES " + AGENCIES.TABLE + "(" + AGENCIES.COLUMN_AUTO_ID + ")" +
            ");";

    private static final String CREATE_TABLE_ROUTE_CONFIGURATIONS =
            "CREATE TABLE " + ROUTE_CONFIGURATIONS.TABLE + " (" +
                ROUTE_CONFIGURATIONS.COLUMN_AUTO_ID + " INTEGER PRIMARY KEY AUTOINCREMENT, " +
                ROUTE_CONFIGURATIONS.COLUMN_COPYRIGHT + " TEXT NOT NULL" +
                ROUTE_CONFIGURATIONS.COLUMN_TIMESTAMP + " INTEGER NOT NULL" +
                "NOT NULL FOREIGN KEY(" + ROUTE_CONFIGURATIONS.COLUMN_ROUTE + ") REFERENCES " + ROUTES.TABLE + "(" + ROUTES.COLUMN_AUTO_ID + ")" +
                "NOT NULL FOREIGN KEY(" + ROUTE_CONFIGURATIONS.COLUMN_SERVICE_AREA + ") REFERENCES " + SERVICE_AREAS.TABLE + "(" + SERVICE_AREAS.COLUMN_AUTO_ID + ")" +
                ROUTE_CONFIGURATIONS.COLUMN_UI_COLOR + " TEXT NOT NULL" + // TODO: SQL
                ROUTE_CONFIGURATIONS.COLUMN_UI_OPPOSITE_COLOR + " TEXT NOT NULL" + // TODO: SQL
                ROUTE_CONFIGURATIONS.COLUMN_STOPS + " TEXT NOT NULL" + // TODO: SQL
                ROUTE_CONFIGURATIONS.COLUMN_DIRECTIONS + " TEXT NOT NULL" + // TODO: SQL
                ROUTE_CONFIGURATIONS.COLUMN_PATHS + " TEXT NOT NULL" + // TODO: SQL
            ");";

    private static final String CREATE_TABLE_VEHICLE_LOCATIONS =
            "CREATE TABLE " + VEHICLE_LOCATIONS.TABLE + " (" +
                VEHICLE_LOCATIONS.COLUMN_AUTO_ID + " INTEGER PRIMARY KEY AUTOINCREMENT, " +
                VEHICLE_LOCATIONS.COLUMN_COPYRIGHT + " TEXT NOT NULL" +
                VEHICLE_LOCATIONS.COLUMN_TIMESTAMP + " INTEGER NOT NULL" +
                "NOT NULL FOREIGN KEY(" + VEHICLE_LOCATIONS.COLUMN_ROUTE + ") REFERENCES " + ROUTES.TABLE + "(" + ROUTES.COLUMN_AUTO_ID + ")" +
                VEHICLE_LOCATIONS.COLUMN_VEHICLE + " TEXT NOT NULL" + // TODO: SQL
                VEHICLE_LOCATIONS.COLUMN_DIRECTION_ID + " TEXT NOT NULL" + // TODO: SQL
                VEHICLE_LOCATIONS.COLUMN_PREDICTABLE + " TEXT NOT NULL" + // TODO: SQL
                VEHICLE_LOCATIONS.COLUMN_LOCATION + " TEXT NOT NULL" + // TODO: SQL
                VEHICLE_LOCATIONS.COLUMN_SPEED + " TEXT NOT NULL" + // TODO: SQL
                VEHICLE_LOCATIONS.COLUMN_HEADING + " TEXT NOT NULL" + // TODO: SQL
            ");";

    private static final String CREATE_TABLE_SERVICE_AREAS =
            "CREATE TABLE " + SERVICE_AREAS.TABLE + " (" +
                SERVICE_AREAS.COLUMN_AUTO_ID + " INTEGER PRIMARY KEY AUTOINCREMENT, " +
                SERVICE_AREAS.COLUMN_LAT_MIN + " REAL NOT NULL" +
                SERVICE_AREAS.COLUMN_LAT_MAX + " REAL NOT NULL" +
                SERVICE_AREAS.COLUMN_LON_MIN + " REAL NOT NULL" +
                SERVICE_AREAS.COLUMN_LON_MAX + " REAL NOT NULL" +
            ");";

    private static final String CREATE_TABLE_STOPS =
            "CREATE TABLE " + STOPS.TABLE + " (" +
                STOPS.COLUMN_AUTO_ID + " INTEGER PRIMARY KEY AUTOINCREMENT, " +
                STOPS.COLUMN_COPYRIGHT + " TEXT NOT NULL" +
                STOPS.COLUMN_TIMESTAMP + " INTEGER NOT NULL" +
                STOPS.COLUMN_AGENCY + " TEXT NOT NULL" + // TODO: SQL
                STOPS.COLUMN_TAG + " TEXT NOT NULL" + // TODO: SQL
                STOPS.COLUMN_TITLE+ " TEXT NOT NULL" + // TODO: SQL
                STOPS.COLUMN_SHORT_TITLE+ " TEXT NOT NULL" + // TODO: SQL
                STOPS.COLUMN_GEOLOCATION+ " TEXT NOT NULL" + // TODO: SQL
                STOPS.COLUMN_ALTERNATE_STOP_ID + " TEXT NOT NULL" + // TODO: SQL
            ");";

    private static final String CREATE_TABLE_DIRECTIONS =
            "CREATE TABLE " + DIRECTIONS.TABLE + " (" +
                DIRECTIONS.COLUMN_AUTO_ID + " INTEGER PRIMARY KEY AUTOINCREMENT, " +
                DIRECTIONS.COLUMN_COPYRIGHT + " TEXT NOT NULL" +
                DIRECTIONS.COLUMN_TIMESTAMP + " INTEGER NOT NULL" +
                DIRECTIONS.COLUMN_ROUTE + " TEXT NOT NULL" + // TODO: SQL
                DIRECTIONS.COLUMN_TAG + " TEXT NOT NULL" + // TODO: SQL
                DIRECTIONS.COLUMN_TITLE + " TEXT NOT NULL" + // TODO: SQL
                DIRECTIONS.COLUMN_NAME + " TEXT NOT NULL" + // TODO: SQL
                DIRECTIONS.COLUMN_STOPS + " TEXT NOT NULL" + // TODO: SQL
            ");";

    private static final String CREATE_TABLE_PATHS =
            "CREATE TABLE " + PATHS.TABLE + " (" +
                PATHS.COLUMN_AUTO_ID + " INTEGER PRIMARY KEY AUTOINCREMENT, " +
                PATHS.COLUMN_COPYRIGHT + " TEXT NOT NULL" +
                PATHS.COLUMN_TIMESTAMP + " INTEGER NOT NULL" +
                PATHS.COLUMN_ROUTE + " TEXT NOT NULL" + // TODO: SQL
                PATHS.COLUMN_PATH_ID + " TEXT NOT NULL" + // TODO: SQL
                PATHS.COLUMN_POINTS + " TEXT NOT NULL" + // TODO: SQL
            ");";

    private static final String CREATE_TABLE_POINTS =
            "CREATE TABLE " + POINTS.TABLE + " (" +
                POINTS.COLUMN_AUTO_ID + " INTEGER PRIMARY KEY AUTOINCREMENT, " +
                POINTS.COLUMN_LAT + " REAL NOT NULL" +
                POINTS.COLUMN_LON + " REAL NOT NULL" +
            ");";

    public NextbusSQLiteHelper(Context context) {
        super(context, DATABASE_NAME, null, DATABASE_VERSION);
    }

    @Override
    public void onCreate(SQLiteDatabase db) {
        db.execSQL(CREATE_TABLE_AGENCIES);
        db.execSQL(CREATE_TABLE_ROUTES);
        db.execSQL(CREATE_TABLE_ROUTE_CONFIGURATIONS);
        db.execSQL(CREATE_TABLE_VEHICLE_LOCATIONS);

        db.execSQL(CREATE_TABLE_SERVICE_AREAS);
        db.execSQL(CREATE_TABLE_STOPS);
        db.execSQL(CREATE_TABLE_DIRECTIONS);
        db.execSQL(CREATE_TABLE_PATHS);
        db.execSQL(CREATE_TABLE_POINTS);
    }

    @Override
    public void onUpgrade(SQLiteDatabase db, int oldVersion, int newVersion) {
        // Drop older tables
        db.execSQL("DROP TABLE IF EXISTS " + AGENCIES.TABLE);
        db.execSQL("DROP TABLE IF EXISTS " + ROUTES.TABLE);
        db.execSQL("DROP TABLE IF EXISTS " + ROUTE_CONFIGURATIONS.TABLE);
        db.execSQL("DROP TABLE IF EXISTS " + VEHICLE_LOCATIONS.TABLE);
        db.execSQL("DROP TABLE IF EXISTS " + SERVICE_AREAS.TABLE);
        db.execSQL("DROP TABLE IF EXISTS " + STOPS.TABLE);
        db.execSQL("DROP TABLE IF EXISTS " + DIRECTIONS.TABLE);
        db.execSQL("DROP TABLE IF EXISTS " + PATHS.TABLE);
        db.execSQL("DROP TABLE IF EXISTS " + POINTS.TABLE);

        // Create new tables
        onCreate(db);
    }

}
