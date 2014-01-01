package com.elliottsj.ttc_ftw.activities;


import android.app.Activity;
import android.app.Fragment;
import android.app.FragmentManager;
import android.content.Context;
import android.net.ConnectivityManager;
import android.net.NetworkInfo;
import android.os.Bundle;
import android.support.v13.app.FragmentPagerAdapter;
import android.support.v4.view.ViewPager;
import android.view.Menu;
import com.astuetz.PagerSlidingTabStrip;
import com.elliottsj.ttc_ftw.R;

public class StopsActivity extends Activity {

    PagerSlidingTabStrip mTabs;
    ViewPager mViewPager;
    StopsTabsAdapter mStopsTabsAdapter;

    private static final String TAG = "StopsActivity";

    /**
     * Returns true iff a network connection is available.
     */
    public boolean isNetworkConnected() {
        ConnectivityManager connMgr = (ConnectivityManager) getSystemService(Context.CONNECTIVITY_SERVICE);
        NetworkInfo networkInfo = connMgr.getActiveNetworkInfo();
        return networkInfo != null && networkInfo.isConnected();
    }

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_stops);

        mTabs = (PagerSlidingTabStrip) findViewById(R.id.tabs);
        mViewPager = (ViewPager) findViewById(R.id.pager);

        mStopsTabsAdapter = new StopsTabsAdapter(getFragmentManager());
        mViewPager.setAdapter(mStopsTabsAdapter);
        mTabs.setViewPager(mViewPager);
    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        getMenuInflater().inflate(R.menu.stops, menu);
        return super.onCreateOptionsMenu(menu);
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
