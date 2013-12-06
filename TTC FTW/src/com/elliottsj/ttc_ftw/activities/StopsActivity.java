package com.elliottsj.ttc_ftw.activities;


import android.app.Activity;
import android.app.Fragment;
import android.app.FragmentManager;
import android.os.Bundle;
import android.support.v13.app.FragmentPagerAdapter;
import android.support.v4.view.ViewPager;
import com.astuetz.viewpager.extensions.PagerSlidingTabStrip;
import com.elliottsj.ttc_ftw.R;

public class StopsActivity extends Activity {

    PagerSlidingTabStrip mTabs;
    ViewPager mViewPager;
    StopsTabsAdapter mStopsTabsAdapter;

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
