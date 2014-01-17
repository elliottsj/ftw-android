package com.elliottsj.ftw.nextbus;

import android.os.AsyncTask;

import net.sf.nextbus.publicxmlfeed.domain.Route;
import net.sf.nextbus.publicxmlfeed.domain.RouteConfiguration;

public class FetchRouteConfigurationTask extends AsyncTask<Route, Void, RouteConfiguration> {

    private Callbacks mCallbacks;

    public FetchRouteConfigurationTask(Callbacks callbacks) {
        this.mCallbacks = callbacks;
    }

    @Override
    protected RouteConfiguration doInBackground(Route... params) {
        return null;
    }

    @Override
    protected void onPostExecute(RouteConfiguration routeConfiguration) {
        mCallbacks.onRouteConfigurationFetched(routeConfiguration);
        super.onPostExecute(routeConfiguration);
    }

    public static interface Callbacks {

        public abstract void onRouteConfigurationFetched(RouteConfiguration routeConfiguration);

    }

}
