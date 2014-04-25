package com.elliottsj.ftw.loaders;

import android.content.AsyncTaskLoader;
import android.content.Context;
import android.content.CursorLoader;
import android.util.Log;

import com.elliottsj.ftw.provider.NextbusProvider;
import com.elliottsj.ftw.provider.NextbusQueryHelper;

import net.sf.nextbus.publicxmlfeed.domain.Prediction;
import net.sf.nextbus.publicxmlfeed.domain.PredictionGroup;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

public class PredictionsLoader extends AsyncTaskLoader<List<PredictionGroup>> {

    private static final String TAG = PredictionsLoader.class.getSimpleName();

    private String mAgencyTag;
    private Map<String, List<String>> mStops;
    private List<PredictionGroup> mPredictions;

    public PredictionsLoader(Context context, String agencyTag, Map<String, List<String>> stops) {
        super(context);
        mAgencyTag = agencyTag;
        mStops = stops;
    }

    @Override
    public List<PredictionGroup> loadInBackground() {
        Log.i(TAG, "loadInBackground");
        return new NextbusQueryHelper(getContext()).loadPredictions(mAgencyTag, mStops);
    }

    @Override
    public void deliverResult(List<PredictionGroup> data) {
        Log.i(TAG, "deliverResult");
        if (isReset()) {
            // The loader has been reset; ignore the result
            return;
        }

        mPredictions = data;

        if (isStarted()) {
            // If the loader is in a started state, deliver the results to the client.
            // The superclass method does this for us.
            super.deliverResult(data);
        }
    }

    @Override
    protected void onStartLoading() {
        Log.i(TAG, "onStartLoading");

        if (mPredictions != null) {
            // Deliver previously-loaded data immediately
            deliverResult(mPredictions);
        } else {
            forceLoad();
        }
    }

    @Override
    protected void onStopLoading() {
        Log.i(TAG, "onStopLoading");
        // The loader is in a stopped state, so we should attempt to cancel the current load (if there is one)
        cancelLoad();
    }

    @Override
    protected void onReset() {
        Log.i(TAG, "onReset");

        // Ensure that the loader has been stopped
        onStopLoading();

        // Release cached data
        mPredictions = null;
    }

    @Override
    public void onCanceled(List<PredictionGroup> data) {
        Log.i(TAG, "onCanceled");
        // Attempt to cancel the current asynchronous load
        super.onCanceled(data);
    }

    /**
     * Get a map of predictions, indexed by agency tag, route tag, direction tag, and stop tag
     *
     * @param predictionGroups prediction groups as returned by the nextbus api
     * @return a map of (agency tag -> (route tag -> (direction tag -> (stop tag -> (list of integer)))))
     */
    public static Map<String, Map<String, Map<String, Map<String, List<Integer>>>>> predictionsAsMap(List<PredictionGroup> predictionGroups) {
        // Each PredictionGroup represents a set of predictions for a particular route and stop, with possibly multiple directions

        Map<String, Map<String, Map<String, Map<String, List<Integer>>>>> predictionMap = new HashMap<String, Map<String, Map<String, Map<String, List<Integer>>>>>();

        for (PredictionGroup predictionGroup : predictionGroups) {
            String agencyTag = predictionGroup.getRoute().getAgency().getTag();
            String routeTag = predictionGroup.getRoute().getTag();
            String stopTag = predictionGroup.getStop().getTag();

            // Create map for agency if it doesn't exist
            if (!predictionMap.containsKey(agencyTag))
                predictionMap.put(agencyTag, new HashMap<String, Map<String, Map<String, List<Integer>>>>());

            // Create map for route if it doesn't exist
            Map<String, Map<String, Map<String, List<Integer>>>> routeMap = predictionMap.get(agencyTag);
            if (!routeMap.containsKey(routeTag))
                routeMap.put(routeTag, new HashMap<String, Map<String, List<Integer>>>());

            for (PredictionGroup.PredictionDirection predictionDirection : predictionGroup.getDirections()) {
                for (Prediction prediction : predictionDirection.getPredictions()) {
                    String directionTag = prediction.getDirectionTag();

                    // Create map for direction if it doesn't exist
                    Map<String, Map<String, List<Integer>>> directionMap = routeMap.get(routeTag);
                    if (!directionMap.containsKey(directionTag))
                        directionMap.put(directionTag, new HashMap<String, List<Integer>>());

                    // Create a list of stop predictions if necessary
                    Map<String, List<Integer>> stopMap = directionMap.get(directionTag);
                    if (!stopMap.containsKey(stopTag))
                        stopMap.put(stopTag, new ArrayList<Integer>());

                    // Add the prediction to the list of predictions for this stop
                    stopMap.get(stopTag).add(prediction.getMinutes());
                }

            }
        }

        return predictionMap;
    }

}
