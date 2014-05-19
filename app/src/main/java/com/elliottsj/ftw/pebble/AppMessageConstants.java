package com.elliottsj.ftw.pebble;

import java.util.UUID;

public class AppMessageConstants {

    public static final UUID WATCHAPP_UUID = UUID.fromString("7be0d6de-b124-4322-8fcc-eeb6ed039a62");

    /* Message fields */
    public static final int MESSAGE_TYPE = 0;
    // Sections
    public static final int SECTION_INDEX = 1;
    public static final int SECTION_COUNT = 2;
    public static final int SECTION_STOP_TAG = 3;
    public static final int SECTION_STOP_TITLE = 4;
    public static final int SECTION_STOP_COUNT = 5;
    // Stops
    public static final int SECTION_STOP_INDEX = 6;
    public static final int STOP_ROUTE_TAG = 7;
    public static final int STOP_ROUTE_TITLE = 8;
    public static final int STOP_DIRECTION_TAG = 9;
    public static final int STOP_DIRECTION_TITLE = 10;
    public static final int STOP_PREDICTION = 11;
    public static final int STOP_MINUTES_LABEL = 12;

    /* Incoming message types */
    public static final int MESSAGE_REQUEST_SECTIONS_METADATA = 0;
    public static final int MESSAGE_REQUEST_SECTION_DATA = 1;
    public static final int MESSAGE_REQUEST_STOP_DATA = 2;
    public static final int MESSAGE_REQUEST_STOP_PREDICTION = 3;

    /* Outgoing messages types */
    public static final byte MESSAGE_SECTIONS_METADATA = 4;
    public static final byte MESSAGE_SECTION_DATA = 5;
    public static final byte MESSAGE_STOP_DATA = 6;
    public static final byte MESSAGE_STOP_PREDICTION = 7;

}
