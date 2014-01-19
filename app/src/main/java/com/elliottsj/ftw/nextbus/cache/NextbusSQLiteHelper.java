package com.elliottsj.ftw.nextbus.cache;

import android.content.Context;
import android.database.sqlite.SQLiteDatabase;
import android.database.sqlite.SQLiteOpenHelper;

public class NextbusSQLiteHelper extends SQLiteOpenHelper {

    protected static final int DATABASE_VERSION = 1;
    protected static final String DATABASE_NAME = "nextbus.db";

    public static final class AGENCIES {
        public static final String TABLE = "agencies";

        public static final String COLUMN_AUTO_ID = "_ID";
        public static final String COLUMN_COPYRIGHT = "agency_copyright";
        public static final String COLUMN_TIMESTAMP = "agency_timestamp";
        public static final String COLUMN_TAG = "agency_tag";
        public static final String COLUMN_TITLE = "agency_title";
        public static final String COLUMN_SHORT_TITLE = "agency_short_title";
        public static final String COLUMN_REGION_TITLE = "agency_region_title";
    }

    public static final class ROUTES {
        public static final String TABLE = "routes";

        public static final String COLUMN_AUTO_ID = "_ID";
        public static final String COLUMN_COPYRIGHT = "route_copyright";
        public static final String COLUMN_TIMESTAMP = "route_timestamp";
        public static final String COLUMN_AGENCY = "route_agency";
        public static final String COLUMN_TAG = "route_tag";
        public static final String COLUMN_TITLE = "route_title";
        public static final String COLUMN_SHORT_TITLE = "route_short_title";
    }

    public static final class ROUTE_CONFIGURATIONS {
        public static final String TABLE = "route_configurations";

        public static final String COLUMN_AUTO_ID = "_ID";
        public static final String COLUMN_COPYRIGHT = "route_configuration_copyright";
        public static final String COLUMN_TIMESTAMP = "route_configuration_timestamp";
        public static final String COLUMN_ROUTE = "route_configuration_route";
        public static final String COLUMN_UI_COLOR = "route_configuration_ui_color";
        public static final String COLUMN_UI_OPPOSITE_COLOR = "route_configuration_ui_opposite_color";
    }

    public static final class SERVICE_AREAS {
        public static final String TABLE = "service_areas";

        public static final String COLUMN_AUTO_ID = "_ID";
        public static final String COLUMN_LAT_MIN = "lat_min";
        public static final String COLUMN_LAT_MAX = "lat_max";
        public static final String COLUMN_LON_MIN = "lon_min";
        public static final String COLUMN_LON_MAX = "lon_max";
        public static final String COLUMN_ROUTE_CONFIGURATION = "route_configuration";
    }

    public static final class STOPS {
        public static final String TABLE = "stops";

        public static final String COLUMN_AUTO_ID = "_ID";
        public static final String COLUMN_COPYRIGHT = "stop_copyright";
        public static final String COLUMN_TIMESTAMP = "stop_timestamp";
        public static final String COLUMN_AGENCY = "stop_agency";
        public static final String COLUMN_TAG = "stop_tag";
        public static final String COLUMN_TITLE = "stop_title";
        public static final String COLUMN_SHORT_TITLE = "stop_short_title";
        public static final String COLUMN_STOP_ID = "stop_id";
    }

    /**
     * Locations for stops
     */
    public static final class GEOLOCATIONS {
        public static final String TABLE = "geolocations";

        public static final String COLUMN_AUTO_ID = "_ID";
        public static final String COLUMN_LAT = "lat";
        public static final String COLUMN_LON = "lon";
        public static final String COLUMN_STOP = "geolocation_stop";
    }

    public static final class ROUTE_CONFIGURATIONS_STOPS {
        public static final String TABLE = "route_configurations_stops";

        public static final String COLUMN_AUTO_ID = "_ID";
        public static final String COLUMN_ROUTE_CONFIGURATION = "route_configuration";
        public static final String COLUMN_STOP = "stop";
    }

    public static final class DIRECTIONS {
        public static final String TABLE = "directions";

        public static final String COLUMN_AUTO_ID = "_ID";
        public static final String COLUMN_COPYRIGHT = "direction_copyright";
        public static final String COLUMN_TIMESTAMP = "direction_timestamp";
        public static final String COLUMN_ROUTE = "direction_route";
        public static final String COLUMN_TAG = "direction_tag";
        public static final String COLUMN_TITLE = "direction_title";
        public static final String COLUMN_NAME = "direction_name";
        public static final String COLUMN_ROUTE_CONFIGURATION = "direction_route_configuration";
    }

    public static final class DIRECTIONS_STOPS {
        public static final String TABLE = "directions_stops";

        public static final String COLUMN_AUTO_ID = "_ID";
        public static final String COLUMN_DIRECTION = "direction";
        public static final String COLUMN_STOP = "stop";
    }

    public static final class PATHS {
        public static final String TABLE = "paths";

        public static final String COLUMN_AUTO_ID = "_ID";
        public static final String COLUMN_COPYRIGHT = "path_copyright";
        public static final String COLUMN_ROUTE = "path_route";
        public static final String COLUMN_PATH_ID = "path_id";
        public static final String COLUMN_ROUTE_CONFIGURATION = "path_route_configuration";
    }

    /**
     * Locations for points on paths
     */
    public static final class POINTS {
        public static final String TABLE = "points";

        public static final String COLUMN_AUTO_ID = "_ID";
        public static final String COLUMN_LAT = "lat";
        public static final String COLUMN_LON = "lon";
        public static final String COLUMN_PATH = "point_path";
    }

    public static final class VEHICLE_LOCATIONS {
        public static final String TABLE = "vehicle_locations";

        public static final String COLUMN_AUTO_ID = "_ID";
        public static final String COLUMN_COPYRIGHT = "vehicle_location_copyright";
        public static final String COLUMN_TIMESTAMP = "vehicle_location_timestamp";
        public static final String COLUMN_VEHICLE = "vehicle_location_vehicle";
        public static final String COLUMN_ROUTE = "vehicle_location_route";
        public static final String COLUMN_DIRECTION_ID = "vehicle_location_direction_id";
        public static final String COLUMN_PREDICTABLE = "vehicle_location_predictable";
        public static final String COLUMN_LOCATION = "vehicle_location_location";
        public static final String COLUMN_SPEED = "vehicle_location_speed";
        public static final String COLUMN_HEADING = "vehicle_location_heading";
    }

    // CREATE TABLE statements

    private static final String CREATE_TABLE_AGENCIES =
            "CREATE TABLE " + AGENCIES.TABLE + " (" +
                AGENCIES.COLUMN_AUTO_ID + " INTEGER PRIMARY KEY AUTOINCREMENT, " +
                AGENCIES.COLUMN_COPYRIGHT + " TEXT NOT NULL, " +
                AGENCIES.COLUMN_TIMESTAMP + " INTEGER NOT NULL, " +
                AGENCIES.COLUMN_TAG + " TEXT NOT NULL UNIQUE, " +
                AGENCIES.COLUMN_TITLE + " TEXT NOT NULL, " +
                AGENCIES.COLUMN_SHORT_TITLE + " TEXT, " +
                AGENCIES.COLUMN_REGION_TITLE + " TEXT NOT NULL" +
            ");";

    private static final String CREATE_TABLE_ROUTES =
            "CREATE TABLE " + ROUTES.TABLE + " (" +
                ROUTES.COLUMN_AUTO_ID + " INTEGER PRIMARY KEY AUTOINCREMENT, " +
                ROUTES.COLUMN_COPYRIGHT + " TEXT NOT NULL, " +
                ROUTES.COLUMN_TIMESTAMP + " INTEGER NOT NULL, " +
                ROUTES.COLUMN_AGENCY + " INTEGER NOT NULL, " +
                ROUTES.COLUMN_TAG + " TEXT NOT NULL UNIQUE, " +
                ROUTES.COLUMN_TITLE + " TEXT NOT NULL, " +
                ROUTES.COLUMN_SHORT_TITLE + " TEXT, " +
                "FOREIGN KEY(" + ROUTES.COLUMN_AGENCY + ") REFERENCES " + AGENCIES.TABLE + "(" + AGENCIES.COLUMN_AUTO_ID + ")" +
            ");";

    private static final String CREATE_TABLE_ROUTE_CONFIGURATIONS =
            "CREATE TABLE " + ROUTE_CONFIGURATIONS.TABLE + " (" +
                ROUTE_CONFIGURATIONS.COLUMN_AUTO_ID + " INTEGER PRIMARY KEY AUTOINCREMENT, " +
                ROUTE_CONFIGURATIONS.COLUMN_COPYRIGHT + " TEXT NOT NULL, " +
                ROUTE_CONFIGURATIONS.COLUMN_TIMESTAMP + " INTEGER NOT NULL, " +
                ROUTE_CONFIGURATIONS.COLUMN_ROUTE + " INTEGER NOT NULL UNIQUE, " +
                ROUTE_CONFIGURATIONS.COLUMN_UI_COLOR + " TEXT NOT NULL, " +
                ROUTE_CONFIGURATIONS.COLUMN_UI_OPPOSITE_COLOR + " TEXT NOT NULL, " +
                "FOREIGN KEY(" + ROUTE_CONFIGURATIONS.COLUMN_ROUTE + ") REFERENCES " + ROUTES.TABLE + "(" + ROUTES.COLUMN_AUTO_ID + ")" +
            ");";

    private static final String CREATE_TABLE_SERVICE_AREAS =
            "CREATE TABLE " + SERVICE_AREAS.TABLE + " (" +
                SERVICE_AREAS.COLUMN_AUTO_ID + " INTEGER PRIMARY KEY AUTOINCREMENT, " +
                SERVICE_AREAS.COLUMN_LAT_MIN + " REAL NOT NULL, " +
                SERVICE_AREAS.COLUMN_LAT_MAX + " REAL NOT NULL, " +
                SERVICE_AREAS.COLUMN_LON_MIN + " REAL NOT NULL, " +
                SERVICE_AREAS.COLUMN_LON_MAX + " REAL NOT NULL, " +
                SERVICE_AREAS.COLUMN_ROUTE_CONFIGURATION + " INTEGER NOT NULL, " +
                "FOREIGN KEY(" + SERVICE_AREAS.COLUMN_ROUTE_CONFIGURATION + ") REFERENCES " + ROUTE_CONFIGURATIONS.TABLE + "(" + ROUTE_CONFIGURATIONS.COLUMN_AUTO_ID + ")" +
            ");";

    private static final String CREATE_TABLE_STOPS =
            "CREATE TABLE " + STOPS.TABLE + " (" +
                STOPS.COLUMN_AUTO_ID + " INTEGER PRIMARY KEY AUTOINCREMENT, " +
                STOPS.COLUMN_COPYRIGHT + " TEXT NOT NULL, " +
                STOPS.COLUMN_TIMESTAMP + " INTEGER NOT NULL, " +
                STOPS.COLUMN_AGENCY + " INTEGER NOT NULL, " +
                STOPS.COLUMN_TAG + " TEXT NOT NULL, " +
                STOPS.COLUMN_TITLE + " TEXT NOT NULL, " +
                STOPS.COLUMN_SHORT_TITLE + " TEXT, " +
                STOPS.COLUMN_STOP_ID + " TEXT, " +
                "FOREIGN KEY(" + STOPS.COLUMN_AGENCY + ") REFERENCES " + AGENCIES.TABLE + "(" + AGENCIES.COLUMN_AUTO_ID + ")" +
            ");";

    private static final String CREATE_TABLE_GEOLOCATIONS =
            "CREATE TABLE " + GEOLOCATIONS.TABLE + " (" +
                GEOLOCATIONS.COLUMN_AUTO_ID + " INTEGER PRIMARY KEY AUTOINCREMENT, " +
                GEOLOCATIONS.COLUMN_LAT + " REAL NOT NULL, " +
                GEOLOCATIONS.COLUMN_LON + " REAL NOT NULL, " +
                GEOLOCATIONS.COLUMN_STOP + " INTEGER NOT NULL, " +
                "FOREIGN KEY(" + GEOLOCATIONS.COLUMN_STOP + ") REFERENCES " + STOPS.TABLE + "(" + STOPS.COLUMN_AUTO_ID + ")" +
            ");";

    private static final String CREATE_TABLE_ROUTE_CONFIGURATIONS_STOPS =
            "CREATE TABLE " + ROUTE_CONFIGURATIONS_STOPS.TABLE + " (" +
                ROUTE_CONFIGURATIONS_STOPS.COLUMN_AUTO_ID + " INTEGER PRIMARY KEY AUTOINCREMENT, " +
                ROUTE_CONFIGURATIONS_STOPS.COLUMN_ROUTE_CONFIGURATION + " INTEGER NOT NULL, " +
                ROUTE_CONFIGURATIONS_STOPS.COLUMN_STOP + " INTEGER NOT NULL, " +
                "FOREIGN KEY(" + ROUTE_CONFIGURATIONS_STOPS.COLUMN_ROUTE_CONFIGURATION + ") REFERENCES " + ROUTE_CONFIGURATIONS.TABLE + "(" + ROUTE_CONFIGURATIONS.COLUMN_AUTO_ID + "), " +
                "FOREIGN KEY(" + ROUTE_CONFIGURATIONS_STOPS.COLUMN_STOP + ") REFERENCES " + STOPS.TABLE + "(" + STOPS.COLUMN_AUTO_ID + ")" +
            ");";

    private static final String CREATE_TABLE_DIRECTIONS =
            "CREATE TABLE " + DIRECTIONS.TABLE + " (" +
                DIRECTIONS.COLUMN_AUTO_ID + " INTEGER PRIMARY KEY AUTOINCREMENT, " +
                DIRECTIONS.COLUMN_COPYRIGHT + " TEXT NOT NULL, " +
                DIRECTIONS.COLUMN_TIMESTAMP + " INTEGER NOT NULL, " +
                DIRECTIONS.COLUMN_ROUTE + " INTEGER NOT NULL, " +
                DIRECTIONS.COLUMN_TAG + " TEXT NOT NULL, " +
                DIRECTIONS.COLUMN_TITLE + " TEXT NOT NULL, " +
                DIRECTIONS.COLUMN_NAME + " TEXT NOT NULL, " +
                DIRECTIONS.COLUMN_ROUTE_CONFIGURATION + " INTEGER NOT NULL, " +
                "FOREIGN KEY(" + DIRECTIONS.COLUMN_ROUTE + ") REFERENCES " + ROUTES.TABLE + "(" + ROUTES.COLUMN_AUTO_ID + "), " +
                "FOREIGN KEY(" + DIRECTIONS.COLUMN_ROUTE_CONFIGURATION + ") REFERENCES " + ROUTE_CONFIGURATIONS.TABLE + "(" + ROUTE_CONFIGURATIONS.COLUMN_AUTO_ID + ")" +
            ");";

    private static final String CREATE_TABLE_DIRECTIONS_STOPS =
            "CREATE TABLE " + DIRECTIONS_STOPS.TABLE + " (" +
            DIRECTIONS_STOPS.COLUMN_AUTO_ID + " INTEGER PRIMARY KEY AUTOINCREMENT, " +
            DIRECTIONS_STOPS.COLUMN_DIRECTION + " INTEGER NOT NULL, " +
            DIRECTIONS_STOPS.COLUMN_STOP + " INTEGER NOT NULL, " +
            "FOREIGN KEY(" + DIRECTIONS_STOPS.COLUMN_DIRECTION + ") REFERENCES " + DIRECTIONS.TABLE + "(" + DIRECTIONS.COLUMN_AUTO_ID + "), " +
            "FOREIGN KEY(" + DIRECTIONS_STOPS.COLUMN_STOP + ") REFERENCES " + STOPS.TABLE + "(" + STOPS.COLUMN_AUTO_ID + ")" +
            ");";

    private static final String CREATE_TABLE_PATHS =
            "CREATE TABLE " + PATHS.TABLE + " (" +
                PATHS.COLUMN_AUTO_ID + " INTEGER PRIMARY KEY AUTOINCREMENT, " +
                PATHS.COLUMN_COPYRIGHT + " TEXT NOT NULL, " +
                PATHS.COLUMN_ROUTE + " INTEGER NOT NULL, " +
                PATHS.COLUMN_PATH_ID + " TEXT NOT NULL, " +
                PATHS.COLUMN_ROUTE_CONFIGURATION + " INTEGER NOT NULL, " +
                "FOREIGN KEY(" + PATHS.COLUMN_ROUTE + ") REFERENCES " + ROUTES.TABLE + "(" + ROUTES.COLUMN_AUTO_ID + "), " +
                "FOREIGN KEY(" + PATHS.COLUMN_ROUTE_CONFIGURATION + ") REFERENCES " + ROUTE_CONFIGURATIONS.TABLE + "(" + ROUTE_CONFIGURATIONS.COLUMN_AUTO_ID + ")" +
            ");";

    private static final String CREATE_TABLE_POINTS =
            "CREATE TABLE " + POINTS.TABLE + " (" +
                POINTS.COLUMN_AUTO_ID + " INTEGER PRIMARY KEY AUTOINCREMENT, " +
                POINTS.COLUMN_LAT + " REAL NOT NULL, " +
                POINTS.COLUMN_LON + " REAL NOT NULL, " +
                POINTS.COLUMN_PATH + " INTEGER NOT NULL, " +
                "FOREIGN KEY(" + POINTS.COLUMN_PATH + ") REFERENCES " + PATHS.TABLE + "(" + PATHS.COLUMN_AUTO_ID + ")" +
            ");";

    private static final String CREATE_TABLE_VEHICLE_LOCATIONS =
            "CREATE TABLE " + VEHICLE_LOCATIONS.TABLE + " (" +
                VEHICLE_LOCATIONS.COLUMN_AUTO_ID + " INTEGER PRIMARY KEY AUTOINCREMENT, " +
                VEHICLE_LOCATIONS.COLUMN_COPYRIGHT + " TEXT NOT NULL, " +
                VEHICLE_LOCATIONS.COLUMN_TIMESTAMP + " INTEGER NOT NULL, " +
                VEHICLE_LOCATIONS.COLUMN_ROUTE + " INTEGER NOT NULL, " +
                VEHICLE_LOCATIONS.COLUMN_VEHICLE + " INTEGER NOT NULL, " + // TODO: SQL
                VEHICLE_LOCATIONS.COLUMN_DIRECTION_ID + " TEXT NOT NULL, " +
                VEHICLE_LOCATIONS.COLUMN_PREDICTABLE + " INTEGER NOT NULL, " +
                VEHICLE_LOCATIONS.COLUMN_LOCATION + " TEXT NOT NULL, " + // TODO: SQL
                VEHICLE_LOCATIONS.COLUMN_SPEED + " REAL NOT NULL, " +
                VEHICLE_LOCATIONS.COLUMN_HEADING + " REAL NOT NULL, " +
                "FOREIGN KEY(" + VEHICLE_LOCATIONS.COLUMN_ROUTE + ") REFERENCES " + ROUTES.TABLE + "(" + ROUTES.COLUMN_AUTO_ID + ")" +
            ");";

    public NextbusSQLiteHelper(Context context) {
        super(context, DATABASE_NAME, null, DATABASE_VERSION);
    }

    @Override
    public void onCreate(SQLiteDatabase db) {
        db.execSQL(CREATE_TABLE_AGENCIES);
        db.execSQL(CREATE_TABLE_ROUTES);
        db.execSQL(CREATE_TABLE_ROUTE_CONFIGURATIONS);
        db.execSQL(CREATE_TABLE_SERVICE_AREAS);
        db.execSQL(CREATE_TABLE_STOPS);
        db.execSQL(CREATE_TABLE_GEOLOCATIONS);
        db.execSQL(CREATE_TABLE_ROUTE_CONFIGURATIONS_STOPS);
        db.execSQL(CREATE_TABLE_DIRECTIONS);
        db.execSQL(CREATE_TABLE_DIRECTIONS_STOPS);
        db.execSQL(CREATE_TABLE_PATHS);
        db.execSQL(CREATE_TABLE_POINTS);
        db.execSQL(CREATE_TABLE_VEHICLE_LOCATIONS);
    }

    @Override
    public void onUpgrade(SQLiteDatabase db, int oldVersion, int newVersion) {
        // Drop older tables
        empty(db);

        // Create new tables
        onCreate(db);
    }

    /**
     * Drops all tables in this database
     */
    public void empty(SQLiteDatabase db) {
        db.execSQL("DROP TABLE IF EXISTS " + AGENCIES.TABLE);
        db.execSQL("DROP TABLE IF EXISTS " + ROUTES.TABLE);
        db.execSQL("DROP TABLE IF EXISTS " + ROUTE_CONFIGURATIONS.TABLE);
        db.execSQL("DROP TABLE IF EXISTS " + VEHICLE_LOCATIONS.TABLE);
        db.execSQL("DROP TABLE IF EXISTS " + SERVICE_AREAS.TABLE);
        db.execSQL("DROP TABLE IF EXISTS " + STOPS.TABLE);
        db.execSQL("DROP TABLE IF EXISTS " + DIRECTIONS.TABLE);
        db.execSQL("DROP TABLE IF EXISTS " + PATHS.TABLE);
        db.execSQL("DROP TABLE IF EXISTS " + POINTS.TABLE);
    }

}
