package com.elliottsj.ftw.loaders;

import android.content.AsyncTaskLoader;
import android.content.Context;

import net.sf.nextbus.publicxmlfeed.domain.PredictionGroup;

import java.util.List;

public class PredictionsLoader extends AsyncTaskLoader<List<PredictionGroup>> {

    public PredictionsLoader(Context context) {
        super(context);
    }

    @Override
    public List<PredictionGroup> loadInBackground() {

        return null;
    }

}
