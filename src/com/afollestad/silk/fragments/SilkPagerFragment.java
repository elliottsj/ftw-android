package com.afollestad.silk.fragments;

import android.os.Bundle;
import android.support.v13.app.FragmentPagerAdapter;
import android.support.v4.view.ViewPager;
import android.view.View;
import com.afollestad.silk.R;

/**
 * A {@link com.afollestad.silk.fragments.SilkFragment} that displays a {@link android.support.v4.view.ViewPager}.
 *
 * @author Aidan Follestad (afollestad)
 */
public abstract class SilkPagerFragment extends SilkFragment {

    private ViewPager mViewPager;

    public abstract FragmentPagerAdapter getPagerAdapter();

    @Override
    public int getLayout() {
        return R.layout.fragment_pager;
    }

    @Override
    public void onViewCreated(View view, Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);
        mViewPager = (ViewPager) view.findViewById(R.id.pager);
        mViewPager.setAdapter(getPagerAdapter());
        mViewPager.setOffscreenPageLimit(5);
    }

    /**
     * Gets the fragment's view pager.
     */
    public final ViewPager getPager() {
        return mViewPager;
    }

    /**
     * Sets the currently visible page in the ViewPager.
     */
    public final void setCurrentPage(int page) {
        if (mViewPager == null) throw new IllegalStateException("View has not yet been created.");
        mViewPager.setCurrentItem(page);
    }

    /**
     * Gets the currently visible page in the ViewPager.
     */
    public final int getCurrentPage() {
        if (mViewPager == null) throw new IllegalStateException("View has not yet been created.");
        return mViewPager.getCurrentItem();
    }
}
