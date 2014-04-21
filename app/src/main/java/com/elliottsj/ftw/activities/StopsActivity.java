package com.elliottsj.ftw.activities;

import android.accounts.Account;
import android.accounts.AccountManager;
import android.app.Fragment;
import android.app.FragmentManager;
import android.content.Context;
import android.os.Bundle;
import android.support.v13.app.FragmentPagerAdapter;
import android.support.v4.view.ViewPager;
import android.util.Log;
import android.view.Menu;
import android.view.MenuItem;

import com.astuetz.PagerSlidingTabStrip;
import com.elliottsj.ftw.R;
import com.elliottsj.ftw.provider.NextbusProvider;
import com.elliottsj.ftw.sync.SyncAdapter;

public class StopsActivity extends TintedStatusBarActivity {

    private static final String TAG = StopsActivity.class.getSimpleName();

    public static final String AUTHORITY = NextbusProvider.AUTHORITY;
    public static final String ACCOUNT_TYPE = SyncAdapter.ACCOUNT_TYPE;
    public static final String ACCOUNT = SyncAdapter.ACCOUNT;

    private Account mAccount;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_stops);

        Log.i(TAG, "onCreate called");

        // Initialize tab navigation
        PagerSlidingTabStrip tabs = (PagerSlidingTabStrip) findViewById(R.id.tabs);
        ViewPager viewPager = (ViewPager) findViewById(R.id.pager);
        StopsTabsAdapter stopsTabsAdapter = new StopsTabsAdapter(getFragmentManager());
        viewPager.setAdapter(stopsTabsAdapter);
        tabs.setViewPager(viewPager);

        // Account needed to request syncs
        mAccount = getSyncAccount(this);
    }

    @Override
    protected void onDestroy() {
        Log.i(TAG, "onDestroy called");
        super.onDestroy();
    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        getMenuInflater().inflate(R.menu.stops, menu);
        return super.onCreateOptionsMenu(menu);
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        switch (item.getItemId()) {
            case R.id.settings:
                return true;
//            case R.id.sync:
//                // Force manual sync
//                Bundle bundle = new Bundle();
//                bundle.putBoolean(ContentResolver.SYNC_EXTRAS_MANUAL, true);
//                bundle.putBoolean(ContentResolver.SYNC_EXTRAS_EXPEDITED, true);
//                ContentResolver.requestSync(mAccount, AUTHORITY, bundle);
//                return true;
            case R.id.about:
                return true;
            default:
                return super.onOptionsItemSelected(item);
        }
    }

    /**
     * Create a new dummy account for the sync adapter
     *
     * @param context The application context
     */
    public static Account getSyncAccount(Context context) {
        // Get an instance of the Android account manager
        AccountManager accountManager = (AccountManager) context.getSystemService(ACCOUNT_SERVICE);

        // Return the existing account if it exists
        for (Account account : accountManager.getAccountsByType(ACCOUNT_TYPE))
            if (account.name.equals(ACCOUNT))
                return account;

        // No account exists; create the default account
        Account newAccount = new Account(ACCOUNT, ACCOUNT_TYPE);

        if (accountManager.addAccountExplicitly(newAccount, null, null)) {
            return newAccount;
        } else {
            // The account exists or some other error occurred
            Log.e(TAG, "Error while creating sync account");
            return null;
        }
    }

    public static class StopsTabsAdapter extends FragmentPagerAdapter {

        private final String[] TITLES = { "Nearby", "Saved" };

        public StopsTabsAdapter(FragmentManager fm) {
            super(fm);
        }

        @Override
        public CharSequence getPageTitle(int position) {
            return TITLES[position];
        }

        @Override
        public int getCount() {
            return TITLES.length;
        }

        @Override
        public Fragment getItem(int position) {
            switch (position) {
                case 0:
                    return new NearbyStopsFragment();
                case 1:
                default:
                    return new SavedStopsFragment();
            }
        }
    }

}
