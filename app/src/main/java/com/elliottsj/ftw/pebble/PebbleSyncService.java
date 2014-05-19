package com.elliottsj.ftw.pebble;

import android.app.Service;
import android.content.Intent;
import android.database.Cursor;
import android.net.Uri;
import android.os.AsyncTask;
import android.os.IBinder;
import android.util.Log;

import com.elliottsj.ftw.loaders.PredictionsLoader;
import com.elliottsj.ftw.provider.NextbusProvider;
import com.elliottsj.ftw.provider.NextbusQueryHelper;
import com.elliottsj.ftw.utilities.Util;
import com.getpebble.android.kit.PebbleKit;
import com.getpebble.android.kit.util.PebbleDictionary;

import net.sf.nextbus.publicxmlfeed.domain.Prediction;
import net.sf.nextbus.publicxmlfeed.domain.PredictionGroup;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.SortedMap;
import java.util.TreeMap;

public class PebbleSyncService extends Service {

    private static final String TAG = PebbleSyncService.class.getSimpleName();

    /* Message fields */
    public static final String MESSAGE_TYPE = "message_type";
    public static final String SECTION_INDEX = "section_index";
    public static final String STOP_INDEX = "stop_index";
    public static final String ROUTE_TAG = "route_tag";
    public static final String STOP_TAG = "stop_tag";

    private ArrayList<StopSection> mSections;

    @Override
    public void onCreate() {
        mSections = new ArrayList<StopSection>();
    }

    @SuppressWarnings("unchecked")
    @Override
    public int onStartCommand(Intent intent, int flags, int startId) {
        int messageType = intent.getIntExtra(MESSAGE_TYPE, -1);
        if (messageType == -1) {
            Log.e(TAG, "Unknown message type: " + messageType);
        } else if (messageType == AppMessageConstants.MESSAGE_REQUEST_SECTIONS_METADATA) {
            Log.i(TAG, "Sending sections metadata...");
            sendSectionsMetadata();
        } else if (messageType == AppMessageConstants.MESSAGE_REQUEST_SECTION_DATA) {
            int sectionIndex = intent.getIntExtra(SECTION_INDEX, -1);
            Log.i(TAG, "Sending section data for sectionIndex == " + sectionIndex);
            sendSectionData(sectionIndex);
        } else if (messageType == AppMessageConstants.MESSAGE_REQUEST_STOP_DATA) {
            int sectionIndex = intent.getIntExtra(SECTION_INDEX, -1);
            int stopIndex = intent.getIntExtra(STOP_INDEX, -1);
            Log.i(TAG, "Sending stop data for sectionIndex == " + sectionIndex + ", stopIndex == " + stopIndex);
            sendStopData(sectionIndex, stopIndex);
        } else if (messageType == AppMessageConstants.MESSAGE_REQUEST_STOP_PREDICTION) {
            String routeTag = intent.getStringExtra(ROUTE_TAG);
            String stopTag = intent.getStringExtra(STOP_TAG);
            Log.i(TAG, "Sending prediction data for routeTag == " + routeTag + ", stopTag == " + stopTag);
            sendPredictionData(routeTag, stopTag);
        }

        // If we get killed, after returning from here, don't restart
        return START_NOT_STICKY;
    }

    @Override
    public IBinder onBind(Intent intent) {
        // We don't provide binding, so return null
        return null;
    }

    @Override
    public void onDestroy() {
    }

    private void sendSectionsMetadata() {
        // Retrieve stop data from the content provider
        Cursor savedStopsCursor = getContentResolver().query(
                Uri.withAppendedPath(NextbusProvider.CONTENT_URI, "saved-stops"),
                NextbusProvider.SAVED_STOPS_CURSOR_COLUMNS, null, null,
                NextbusProvider.SAVED_STOPS.COLUMN_STOP_TITLE);
        mSections = sectionsFromCursor(savedStopsCursor);

        // Prepare sections metadata dictionary
        PebbleDictionary data = new PebbleDictionary();
        data.addUint8(AppMessageConstants.MESSAGE_TYPE, AppMessageConstants.MESSAGE_SECTIONS_METADATA);
        data.addUint16(AppMessageConstants.SECTION_COUNT, (short) mSections.size());

        PebbleKit.sendDataToPebble(this, AppMessageConstants.WATCHAPP_UUID, data);
    }

    private void sendSectionData(int sectionIndex) {
        StopSection section = mSections.get(sectionIndex);

        PebbleDictionary data = new PebbleDictionary();
        data.addUint8(AppMessageConstants.MESSAGE_TYPE, AppMessageConstants.MESSAGE_SECTION_DATA);
        data.addUint16(AppMessageConstants.SECTION_INDEX, (short) sectionIndex);
        data.addString(AppMessageConstants.SECTION_STOP_TAG, section.getTag());
        data.addString(AppMessageConstants.SECTION_STOP_TITLE, section.getTitle());
        data.addUint16(AppMessageConstants.SECTION_STOP_COUNT, (short) section.getStops().size());

        PebbleKit.sendDataToPebble(this, AppMessageConstants.WATCHAPP_UUID, data);
    }

    private void sendStopData(int sectionIndex, int stopIndex) {
        Stop stop = mSections.get(sectionIndex).getStops().get(stopIndex);

        PebbleDictionary data = new PebbleDictionary();
        data.addUint8(AppMessageConstants.MESSAGE_TYPE, AppMessageConstants.MESSAGE_STOP_DATA);
        data.addUint16(AppMessageConstants.SECTION_INDEX, (short) sectionIndex);
        data.addUint16(AppMessageConstants.SECTION_STOP_INDEX, (short) stopIndex);
        data.addString(AppMessageConstants.STOP_ROUTE_TAG, stop.getRouteTag());
        data.addString(AppMessageConstants.STOP_ROUTE_TITLE, stop.getRouteTitle());
        data.addString(AppMessageConstants.STOP_DIRECTION_TAG, stop.getDirectionTag());
        data.addString(AppMessageConstants.STOP_DIRECTION_TITLE, stop.getDirectionTitle());

        PebbleKit.sendDataToPebble(this, AppMessageConstants.WATCHAPP_UUID, data);
    }

    private void sendPredictionData(final String routeTag, final String stopTag) {
        if (!Util.isNetworkConnected(this)) {
            Log.w(TAG, "Not connected to the internet");
            return;
        }

        // Load predictions asynchronously
        new AsyncTask<Void, Void, List<Integer>>() {
            @Override
            protected List<Integer> doInBackground(Void... params) {
                // Load predictions
                Map<String, List<String>> stopsMap = new HashMap<String, List<String>>();
                stopsMap.put(routeTag, Arrays.asList(stopTag));
                List<PredictionGroup> predictionGroups = new NextbusQueryHelper(PebbleSyncService.this).loadPredictions("ttc", stopsMap);
                List<Prediction> predictions = PredictionsLoader.predictionsAsMap(predictionGroups).get(routeTag).get(stopTag);
                if (predictions != null) {
                    List<Integer> predictionsMinutes = new ArrayList<Integer>(predictions.size());
                    for (Prediction prediction : predictions)
                        predictionsMinutes.add(prediction.getMinutes());
                    return predictionsMinutes;
                } else {
                    return new ArrayList<Integer>(0);
                }
            }

            @Override
            protected void onPostExecute(List<Integer> predictions) {
                // Send the predictions to the watchapp
                PebbleDictionary data = new PebbleDictionary();
                data.addUint8(AppMessageConstants.MESSAGE_TYPE, AppMessageConstants.MESSAGE_STOP_PREDICTION);
                data.addString(AppMessageConstants.STOP_PREDICTION, Stop.formatPrediction(predictions));
                data.addString(AppMessageConstants.STOP_MINUTES_LABEL, Stop.formatMinutesLabel(predictions));

                PebbleKit.sendDataToPebble(PebbleSyncService.this, AppMessageConstants.WATCHAPP_UUID, data);
            }
        }.execute();
    }

    private static ArrayList<StopSection> sectionsFromCursor(Cursor cursor) {
        SortedMap<StopSection, List<Stop>> sectionMap = new TreeMap<StopSection, List<Stop>>();

        cursor.moveToFirst();
        while (!cursor.isAfterLast()) {
            String stopTag = cursor.getString(cursor.getColumnIndexOrThrow(NextbusProvider.SAVED_STOPS.COLUMN_STOP_TAG));
            String stopTitle = cursor.getString(cursor.getColumnIndexOrThrow(NextbusProvider.SAVED_STOPS.COLUMN_STOP_TITLE));
            StopSection section = new StopSection(stopTag, stopTitle);
            if (!sectionMap.containsKey(section))
                sectionMap.put(section, new ArrayList<Stop>());

            sectionMap.get(section).add(Stop.fromCursor(cursor));
            cursor.moveToNext();
        }

        ArrayList<StopSection> result = new ArrayList<StopSection>();
        for (Map.Entry<StopSection, List<Stop>> section : sectionMap.entrySet())
            result.add(section.getKey().setStops(section.getValue()));

        return result;
    }

    /**
     * Get a map of stop tags from the given sections
     *
     * @param sections a list of stop sections
     * @return a map of (route tag -> list of stop tags)
     */
    @SuppressWarnings("SuspiciousMethodCalls")
    public static HashMap<String, List<String>> getStopsMap(List<StopSection> sections) {
        HashMap<String, List<String>> stopsMap = new HashMap<String, List<String>>();

        for (StopSection section : sections) {
            if (!section.getStops().isEmpty()) {
                // Create the ArrayList of stop tags if necessary
                Stop firstStop = section.getStops().get(0);
                if (!stopsMap.containsKey(firstStop))
                    stopsMap.put(firstStop.getRouteTag(), new ArrayList<String>());

                stopsMap.get(firstStop.getRouteTag()).add(section.getTag());
            }
        }

        return stopsMap;
    }

}
