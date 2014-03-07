package com.elliottsj.ftw.sync;

import android.accounts.Account;
import android.content.AbstractThreadedSyncAdapter;
import android.content.ContentProviderClient;
import android.content.Context;
import android.content.SyncResult;
import android.os.Bundle;
import android.util.Log;

import com.elliottsj.ftw.nextbus.CachedNextbusServiceAdapter;
import com.elliottsj.ftw.nextbus.ICachedNextbusService;

import net.sf.nextbus.publicxmlfeed.domain.Agency;
import net.sf.nextbus.publicxmlfeed.domain.Route;

public class SyncAdapter extends AbstractThreadedSyncAdapter implements CachedNextbusServiceAdapter.Callbacks {

    private static final String TAG = SyncAdapter.class.getSimpleName();

    public static final String ACCOUNT_TYPE = "com.elliottsj.ftw";
    public static final String ACCOUNT = "account";


    public SyncAdapter(Context context, boolean autoInitialize) {
        this(context, autoInitialize, false);
    }

    public SyncAdapter(Context context, boolean autoInitialize, boolean allowParallelSyncs) {
        super(context, autoInitialize, allowParallelSyncs);
        // Do initialization here
    }

    @Override
    public void onPerformSync(Account account, Bundle extras, String authority, ContentProviderClient provider, SyncResult syncResult) {
        Log.i(TAG, "FTW performing sync");

        ICachedNextbusService nextbusService = new CachedNextbusServiceAdapter(getContext(), provider, this);

        // TODO: Implement multiple agencies
        Agency agency = nextbusService.getAgency("ttc");

        // Download and cache route configurations; blocks until finished
        // Progress is reported in onRouteConfigurationCached()
        nextbusService.cacheRouteConfigurations(agency);
    }

    @Override
    public void onRouteConfigurationCached(Route route) {
        // Report progress
        Log.i(TAG, "Configuration cached for route " + route.getTag());
    }

}
