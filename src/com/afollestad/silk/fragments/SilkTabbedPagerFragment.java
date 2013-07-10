package com.afollestad.silk.fragments;

import android.app.ActionBar;
import android.app.FragmentTransaction;
import android.os.Bundle;
import android.support.v4.app.FragmentPagerAdapter;
import android.support.v4.view.ViewPager;

/**
 * A {@link SilkPagerFragment} that sets the containing activity's action bar navigation mode to tabs,
 * adds action bar tabs based on its pager adapter when it's created, and connects the view pager and action bar tabs
 * to each other.
 *
 * @author Aidan Follestad
 */
public abstract class SilkTabbedPagerFragment extends SilkPagerFragment {

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        final ActionBar ab = getActivity().getActionBar();
        ab.setNavigationMode(ActionBar.NAVIGATION_MODE_TABS);

        // Create a tab listener that will move the view pager when a tab is selected
        ActionBar.TabListener tabListener = new ActionBar.TabListener() {

            @Override
            public void onTabSelected(ActionBar.Tab tab, FragmentTransaction ft) {
                setCurrentPage(tab.getPosition());
            }

            public void onTabUnselected(ActionBar.Tab tab, FragmentTransaction ft) {
            }

            public void onTabReselected(ActionBar.Tab tab, FragmentTransaction ft) {
            }
        };

        // Copy the view pager adapter's page titles over to action bar tabs
        FragmentPagerAdapter adapter = getPagerAdapter();
        for (int i = 0; i < adapter.getCount(); i++) {
            ab.addTab(ab.newTab().setText(adapter.getPageTitle(i)).setTabListener(tabListener));
        }

        // Setup the view pager listener to change the selected action bar tab when it's visible page is changed
        getPager().setOnPageChangeListener(new ViewPager.OnPageChangeListener() {
            @Override
            public void onPageScrolled(int i, float v, int i2) {
            }

            @Override
            public void onPageSelected(int i) {
                if (getActivity() == null)
                    return;
                getActivity().getActionBar().setSelectedNavigationItem(i);
            }

            @Override
            public void onPageScrollStateChanged(int i) {
            }
        });
    }
}
