package com.elliottsj.ftw.loaders;

import android.content.AsyncTaskLoader;
import android.content.Context;

import com.elliottsj.ftw.provider.NextbusQueryHelper;

import net.sf.nextbus.publicxmlfeed.domain.Prediction;
import net.sf.nextbus.publicxmlfeed.domain.PredictionGroup;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

/**
 * @see <a href="http://www.androiddesignpatterns.com/2012/08/implementing-loaders.html">http://www.androiddesignpatterns.com/2012/08/implementing-loaders.html</a>
 */
public class PredictionsLoader extends AsyncTaskLoader<Map<String, Map<String, List<Prediction>>>> {

    private static final String TAG = PredictionsLoader.class.getSimpleName();

    private String mAgencyTag;
    private Map<String, List<String>> mStops;
    private Map<String, Map<String, List<Prediction>>> mPredictions;

    public PredictionsLoader(Context context, String agencyTag, Map<String, List<String>> stops) {
        super(context);
        mAgencyTag = agencyTag;
        mStops = stops;
    }

    /**
     * Load predictions in the background
     *
     * @return a map of (route tag -> (stop tag -> (list of prediction)))
     */
    @Override
    public Map<String, Map<String, List<Prediction>>> loadInBackground() {
        // Load predictions from the network, first loading existing models from the cache
        List<PredictionGroup> predictionGroups = new NextbusQueryHelper(getContext()).loadPredictions(mAgencyTag, mStops);
        return predictionsAsMap(predictionGroups);
    }

    /**
     * When there is new data to be delivered to the client, this is called on the main thread
     *
     * @param data newly-loaded data to be delivered
     */
    @Override
    public void deliverResult(Map<String, Map<String, List<Prediction>>> data) {
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

    /**
     * Handles a request to start the loader
     */
    @Override
    protected void onStartLoading() {
        if (mPredictions != null) {
            // Deliver previously-loaded data immediately
            deliverResult(mPredictions);
        } else {
            forceLoad();
        }
    }

    @Override
    protected void onStopLoading() {
        // The loader is in a stopped state, so we should attempt to cancel the current load (if there is one)
        cancelLoad();
    }

    @Override
    protected void onReset() {
        // Ensure that the loader has been stopped
        onStopLoading();

        // Release cached data
        mPredictions = null;
    }

    @Override
    public void onCanceled(Map<String, Map<String, List<Prediction>>> data) {
        // Attempt to cancel the current asynchronous load
        super.onCanceled(data);
    }

    /**
     * Get a map of predictions, indexed by route tag and stop tag
     *
     * @param predictionGroups prediction groups as returned by the nextbus api
     * @return a map of (route tag -> (stop tag -> (list of prediction)))
     */
    public static Map<String, Map<String, List<Prediction>>> predictionsAsMap(List<PredictionGroup> predictionGroups) {
        // Each PredictionGroup represents a set of predictions for a particular route and stop, with possibly multiple directions

        Map<String, Map<String, List<Prediction>>> predictionMap = new HashMap<String, Map<String, List<Prediction>>>();

        for (PredictionGroup predictionGroup : predictionGroups) {
            String routeTag = predictionGroup.getRoute().getTag();
            String stopTag = predictionGroup.getStop().getTag();

            // Create map for route if it doesn't exist
            if (!predictionMap.containsKey(routeTag))
                predictionMap.put(routeTag, new HashMap<String, List<Prediction>>());

            for (PredictionGroup.PredictionDirection predictionDirection : predictionGroup.getDirections()) {
                for (Prediction prediction : predictionDirection.getPredictions()) {
                    // Thanks to NextBus, the direction tag here doesn't always match a direction in routeConfig,
                    // so we'll ignore it for now
                    //String directionTag = prediction.getDirectionTag();

                    // Create a list of stop predictions if necessary
                    Map<String, List<Prediction>> stopMap = predictionMap.get(routeTag);
                    if (!stopMap.containsKey(stopTag))
                        stopMap.put(stopTag, new ArrayList<Prediction>());

                    // Add the prediction to the list of predictions for this stop
                    stopMap.get(stopTag).add(prediction);
                }

            }
        }

        return predictionMap;
    }

}
