package com.elliottsj.ftw.loaders;

import android.content.AsyncTaskLoader;
import android.content.Context;

import com.elliottsj.ftw.provider.NextbusProvider;

import net.sf.nextbus.publicxmlfeed.domain.Prediction;
import net.sf.nextbus.publicxmlfeed.domain.PredictionGroup;

import java.util.HashMap;
import java.util.List;
import java.util.Map;

public class PredictionsLoader extends AsyncTaskLoader<List<PredictionGroup>> {

    private String mAgencyTag;
    private Map<String, List<String>> mStops;

    public PredictionsLoader(Context context, String agencyTag, Map<String, List<String>> stops) {
        super(context);
        mAgencyTag = agencyTag;
        mStops = stops;
    }

    @Override
    public List<PredictionGroup> loadInBackground() {
        return new NextbusProvider().loadPredictions(mAgencyTag, mStops);
    }

    /**
     * Get a map of predictions, indexed by agency tag, route tag, direction tag, and stop tag
     *
     * @param predictionGroups prediction groups as returned by the nextbus api
     * @return a map of (agency tag -> (route tag -> (direction tag -> (stop tag -> (integer of minutes)))))
     */
    public static Map<String, Map<String, Map<String, Map<String, Integer>>>> predictionsAsMap(List<PredictionGroup> predictionGroups) {
        // Each PredictionGroup represents a set of predictions for a particular route and stop, with possibly multiple directions

        Map<String, Map<String, Map<String, Map<String, Integer>>>> predictionMap = new HashMap<String, Map<String, Map<String, Map<String, Integer>>>>();

        for (PredictionGroup predictionGroup : predictionGroups) {
            String agencyTag = predictionGroup.getRoute().getAgency().getTag();
            String routeTag = predictionGroup.getRoute().getTag();
            String stopTag = predictionGroup.getStop().getTag();

            // Create map for agency if it doesn't exist
            if (!predictionMap.containsKey(agencyTag))
                predictionMap.put(agencyTag, new HashMap<String, Map<String, Map<String, Integer>>>());

            // Create map for route if it doesn't exist
            Map<String, Map<String, Map<String, Integer>>> routeMap = predictionMap.get(agencyTag);
            if (!routeMap.containsKey(routeTag))
                routeMap.put(routeTag, new HashMap<String, Map<String, Integer>>());

            for (PredictionGroup.PredictionDirection predictionDirection : predictionGroup.getDirections()) {
                for (Prediction prediction : predictionDirection.getPredictions()) {
                    String directionTag = prediction.getDirectionTag();

                    // Create map for direction if it doesn't exist
                    Map<String, Map<String, Integer>> directionMap = routeMap.get(routeTag);
                    if (!directionMap.containsKey(directionTag))
                        directionMap.put(directionTag, new HashMap<String, Integer>());

                    // Put prediction into the stop map
                    directionMap.get(directionTag).put(stopTag, prediction.getMinutes());
                }

            }
        }

        return predictionMap;
    }

}
