package com.elliottsj.ftw.receiver;

import android.content.Context;
import android.util.Log;

import com.getpebble.android.kit.PebbleKit;
import com.getpebble.android.kit.util.PebbleDictionary;

import java.util.UUID;

public class PebbleBroadcastReceiver extends PebbleKit.PebbleDataReceiver {

    private static final String TAG = PebbleBroadcastReceiver.class.getName();

    public static final int MESSAGE_TYPE = 0;

    public static final int MESSAGE_STOPS_METADATA = 0;
    public static final int MESSAGE_STOP_DATA = 1;

    public static final long APP_OPENED = 1;
    public static final long STOP_SELECTED = 2;

    public static final int STOP_INDEX = 3;

    private static final UUID WATCHAPP_UUID = UUID.fromString("f407f26b-2d3c-4de5-b7fb-71c9eeaaf261");

    public PebbleBroadcastReceiver() {
        super(WATCHAPP_UUID);
    }

    @Override
    public void receiveData(Context context, int transactionId, PebbleDictionary data) {
        Log.i(TAG, "Received broadcast from Pebble watchapp.");

        long messageType = data.getInteger(MESSAGE_TYPE);
        if (messageType == APP_OPENED) {
            PebbleKit.sendAckToPebble(context, transactionId);
            onAppOpened(context);
        } else if (messageType == STOP_SELECTED) {
            PebbleKit.sendAckToPebble(context, transactionId);
            onStopSelected(context, data.getUnsignedInteger(STOP_INDEX));
        } else {
            PebbleKit.sendNackToPebble(context, transactionId);
        }
    }

    /**
     * Send saved stop data to watchapp. First dictionary sent:
     *
     * 0: message type: MESSAGE_STOPS_METADATA == 0
     * 1: total number of stops n
     * 2: number of data fields per stop (currently 4)
     *
     * Next n dictionaries:
     * 0: message type: MESSAGE_STOP_DATA == 1
     * 1: route tag
     * 2: route title
     * 3: direction name
     * 4: stop title
     */
    private void onAppOpened(Context context) {
        // Prepare metadata dictionary
        PebbleDictionary first = new PebbleDictionary();
        int stopCount = 0;

        // Count saved stops
//        for (Direction direction : mPreferencesDataSource.getStops())
//            stopCount += direction.getStops().size();

        first.addUint32(MESSAGE_TYPE, MESSAGE_STOPS_METADATA);
        first.addUint32(1, stopCount);
        first.addUint32(2, 4);

        // Send initial dictionary
        PebbleKit.sendDataToPebbleWithTransactionId(context, WATCHAPP_UUID, first, MESSAGE_STOPS_METADATA);

        PebbleKit.registerReceivedAckHandler(context, new PebbleKit.PebbleAckReceiver(WATCHAPP_UUID) {
            /**
             * Runs when the watchapp acknowledges our sent data
             */
            @Override
            public void receiveAck(Context context, int transactionId) {
                switch (transactionId) {
                    case MESSAGE_STOPS_METADATA:

                        break;
                    case MESSAGE_STOP_DATA:
                        break;
                }
            }
        });

//        for (Direction direction : mPreferencesDataSource.getStops()) {
//            Route route = direction.getRoute();
//
//            String routeTag = route.getTag();
//            String routeTitle = route.getShortTitle() != null ? route.getShortTitle() : route.getTitle();
//            String directionName = direction.getName();
//
//            for (Stop stop : direction.getStops()) {
//                PebbleDictionary savedStop = new PebbleDictionary();
//                savedStop.addUint32(0, MESSAGE_STOP_DATA);
//
//                String stopTitle = stop.getShortTitle() != null ? stop.getShortTitle() : stop.getTitle();
//
//                savedStop.addString(1, routeTag);
//                savedStop.addString(2, routeTitle);
//                savedStop.addString(3, directionName);
//                savedStop.addString(4, stopTitle);
//
//                Log.i(TAG, "Sending data for stop: " + stopTitle);
//
//                try {
//                    Thread.sleep(1000L);
//                } catch (InterruptedException e) {
//                    e.printStackTrace();
//                }
//                PebbleKit.sendDataToPebble(context, WATCHAPP_UUID, savedStop);
//            }
//        }
    }

    private void onStopSelected(Context context, long stopIndex) {
        // Send vehicle predictions

    }

}
