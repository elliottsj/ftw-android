package com.elliottsj.ftw.pebble;

import android.content.Context;
import android.content.Intent;
import android.util.Log;

import com.getpebble.android.kit.PebbleKit;
import com.getpebble.android.kit.util.PebbleDictionary;

/**
 * Broadcast receiver which receives com.getpebble.action.app.RECEIVE broadcasts and forwards valid
 * messages to {@link com.elliottsj.ftw.pebble.PebbleSyncService}
 */
public class PebbleDataReceiver extends PebbleKit.PebbleDataReceiver {

    private static final String TAG = PebbleDataReceiver.class.getName();

    public PebbleDataReceiver() {
        super(AppMessageConstants.WATCHAPP_UUID);
    }

    @Override
    public void receiveData(Context context, int transactionId, PebbleDictionary data) {
        int messageType = data.getInteger(AppMessageConstants.MESSAGE_TYPE).intValue();
        switch (messageType) {
            case AppMessageConstants.MESSAGE_REQUEST_SECTIONS_METADATA: {
                // Watchapp requests data about all sections
                Log.i(TAG, "Received message with type: MESSAGE_REQUEST_SECTIONS_METADATA");
                PebbleKit.sendAckToPebble(context, transactionId);
                onSectionsMetadataRequested(context, messageType);
                break;
            }
            case AppMessageConstants.MESSAGE_REQUEST_SECTION_DATA: {
                Log.i(TAG, "Received message with type: MESSAGE_REQUEST_SECTION_DATA");
                // Watchapp requests data about a section
                PebbleKit.sendAckToPebble(context, transactionId);
                int sectionIndex = data.getInteger(AppMessageConstants.SECTION_INDEX).intValue();
                onSectionRequested(context, messageType, sectionIndex);
                break;
            }
            case AppMessageConstants.MESSAGE_REQUEST_STOP_DATA: {
                Log.i(TAG, "Received message with type: MESSAGE_REQUEST_STOP_DATA");
                // Watchapp requests stop data
                PebbleKit.sendAckToPebble(context, transactionId);
                int sectionIndex = data.getInteger(AppMessageConstants.SECTION_INDEX).intValue();
                int stopIndex = data.getInteger(AppMessageConstants.SECTION_STOP_INDEX).intValue();
                onStopRequested(context, messageType, sectionIndex, stopIndex);
                break;
            }
            case AppMessageConstants.MESSAGE_REQUEST_STOP_PREDICTION: {
                Log.i(TAG, "Received message with type: MESSAGE_REQUEST_STOP_PREDICTION");
                // User selected a stop on the watchapp
                PebbleKit.sendAckToPebble(context, transactionId);
                String routeTag = data.getString(AppMessageConstants.STOP_ROUTE_TAG);
                String stopTag = data.getString(AppMessageConstants.SECTION_STOP_TAG);
                onPredictionRequested(context, messageType, routeTag, stopTag);
                break;
            }
            default: {
                Log.i(TAG, "Received unknown message type");
                // Unknown message
                PebbleKit.sendNackToPebble(context, transactionId);
                break;
            }
        }
    }

    private void onSectionsMetadataRequested(Context context, int messageType) {
        Intent intent = new Intent(context, PebbleSyncService.class);
        intent.putExtra(PebbleSyncService.MESSAGE_TYPE, messageType);
        context.startService(intent);
    }

    private void onSectionRequested(Context context, int messageType, int sectionIndex) {
        Intent intent = new Intent(context, PebbleSyncService.class);
        intent.putExtra(PebbleSyncService.MESSAGE_TYPE, messageType);
        intent.putExtra(PebbleSyncService.SECTION_INDEX, sectionIndex);
        context.startService(intent);
    }

    private void onStopRequested(Context context, int messageType, int sectionIndex, int stopIndex) {
        Intent intent = new Intent(context, PebbleSyncService.class);
        intent.putExtra(PebbleSyncService.MESSAGE_TYPE, messageType);
        intent.putExtra(PebbleSyncService.SECTION_INDEX, sectionIndex);
        intent.putExtra(PebbleSyncService.STOP_INDEX, stopIndex);
        context.startService(intent);
    }

    private void onPredictionRequested(Context context, int messageType, String routeTag, String stopTag) {
        Intent intent = new Intent(context, PebbleSyncService.class);
        intent.putExtra(PebbleSyncService.MESSAGE_TYPE, messageType);
        intent.putExtra(PebbleSyncService.ROUTE_TAG, routeTag);
        intent.putExtra(PebbleSyncService.STOP_TAG, stopTag);
        context.startService(intent);
    }

}
