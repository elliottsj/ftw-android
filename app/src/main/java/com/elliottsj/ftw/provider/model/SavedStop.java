package com.elliottsj.ftw.provider.model;

import com.j256.ormlite.field.DatabaseField;
import com.j256.ormlite.table.DatabaseTable;

import net.sf.nextbus.publicxmlfeed.domain.Stop;

@DatabaseTable(tableName = "saved_stops")
public class SavedStop {

    public static final String FIELD_ID = "_id";
    public static final String FIELD_STOP_ID = "stop_id";

    @DatabaseField(columnName = FIELD_ID, canBeNull = false)
    private int _id;

    @DatabaseField(columnName = FIELD_STOP_ID, canBeNull = false, foreign = true)
    private Stop stop;

    /**
     * Empty constructor for OrmLite
     */
    SavedStop() {}

    /**
     * Create a new SavedStop
     *
     * @param stop Nextbus Stop contained in this SavedStop
     */
    public SavedStop(Stop stop) {
        this.stop = stop;
    }

    public Stop getStop() {
        return stop;
    }

}
