package com.elliottsj.ftw.provider.model;

import com.j256.ormlite.field.DatabaseField;
import com.j256.ormlite.table.DatabaseTable;

import com.elliottsj.nextbus.domain.Direction;
import com.elliottsj.nextbus.domain.Stop;

@DatabaseTable(tableName = "saved_stops")
public class SavedStop {

    public static final String FIELD_ID = "_id";
    public static final String FIELD_STOP_ID = "stop_id";
    public static final String FIELD_DIRECTION_ID = "direction_id";

    @DatabaseField(columnName = FIELD_ID, generatedId = true)
    private int _id;

    @DatabaseField(columnName = FIELD_STOP_ID, canBeNull = false, foreign = true)
    private Stop stop;

    @DatabaseField(columnName = FIELD_DIRECTION_ID, canBeNull = false, foreign = true)
    private Direction direction;

    /**
     * Empty constructor for OrmLite
     */
    SavedStop() {}

    public SavedStop(Stop stop, Direction direction) {
        this.stop = stop;
        this.direction = direction;
    }

    public int getId() {
        return _id;
    }

    public Stop getStop() {
        return stop;
    }

    public Direction getDirection() {
        return direction;
    }

}
