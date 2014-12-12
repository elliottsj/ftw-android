package com.elliottsj.ftw.activities

import android.app.Activity
import android.app.Fragment
import android.app.FragmentManager
import android.os.Bundle
import android.support.v13.app.FragmentPagerAdapter
import android.support.v4.view.ViewPager
import android.util.Log
import android.view.Menu
import android.view.MenuItem

import com.elliottsj.ftw.R
import com.elliottsj.ftw.views.SlidingTabLayout

class StopListActivity extends Activity {

    private static final String TAG = StopListActivity.class.getSimpleName()

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate savedInstanceState
        setContentView R.layout.activity_stop_list

        Log.i TAG, "onCreate called"

        // Get the ViewPager and set it's PagerAdapter so that it can display items
        def viewPager = findViewById(R.id.pager) as ViewPager
        viewPager.adapter = new StopsTabsAdapter(fragmentManager)

        // Give the SlidingTabLayout the ViewPager
        def slidingTabLayout = findViewById(R.id.sliding_tabs) as SlidingTabLayout
        slidingTabLayout.viewPager = viewPager
    }

    @Override
    protected void onDestroy() {
        super.onDestroy()
        Log.i TAG, "onDestroy"
    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        menuInflater.inflate R.menu.stops, menu
        super.onCreateOptionsMenu menu
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        [
                (R.id.settings): { true },
                (R.id.sync): {
                    // Force manual sync
//                    Bundle bundle = new Bundle()
//                    bundle.putBoolean(ContentResolver.SYNC_EXTRAS_MANUAL, true)
//                    bundle.putBoolean(ContentResolver.SYNC_EXTRAS_EXPEDITED, true)
//                    ContentResolver.requestSync(mAccount, AUTHORITY, bundle)
//                    return true
                },
                (R.id.about): { true }
        ][item.itemId]()
    }

    static class StopsTabsAdapter extends FragmentPagerAdapter {

        private final TITLES = ["Nearby", "Saved"]

        StopsTabsAdapter(FragmentManager fm) {
            super(fm)
        }

        @Override
        CharSequence getPageTitle(int position) {
            TITLES[position]
        }

        @Override
        int getCount() {
            TITLES.size()
        }

        @Override
        Fragment getItem(int position) {
            [{ new SavedStopListFragment() }, { new SavedStopListFragment() }][position]()
        }
    }

}
