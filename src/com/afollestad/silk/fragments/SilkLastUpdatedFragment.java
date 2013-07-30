package com.afollestad.silk.fragments;

import android.content.SharedPreferences;
import android.os.Bundle;
import android.view.View;
import android.widget.ImageView;
import android.widget.TextView;
import android.widget.Toast;
import com.afollestad.silk.R;
import com.afollestad.silk.utilities.TimeUtils;
import com.afollestad.silk.cache.SilkComparable;

import java.io.File;
import java.util.Calendar;

/**
 * A {@link com.afollestad.silk.fragments.SilkCachedFeedFragment} that allows you to show a frame at the top of the list,
 * indicating the last time the fragment refreshed, and allowing the user to invoke a new refresh by pressing a button.
 *
 * @author Aidan Follestad (afollestad)
 */
public abstract class SilkLastUpdatedFragment<T extends SilkComparable> extends SilkCachedFeedFragment<T> {

    /**
     * Initializes a new SilkLastUpdatedFragment.
     *
     * @param cacheTitle The title to use for the Fragment's {@link com.afollestad.silk.cache.SilkCacheManager}.
     */
    public SilkLastUpdatedFragment(String cacheTitle) {
        super(cacheTitle);
    }

    /**
     * Initializes a new SilkLastUpdatedFragment.
     *
     * @param cacheTitle     The title to use for the Fragment's {@link com.afollestad.silk.cache.SilkCacheManager}.
     * @param cacheDirectory The directory set to the Fragment's {@link com.afollestad.silk.cache.SilkCacheManager}, will be '/sdcard/Silk' by default.
     */
    public SilkLastUpdatedFragment(String cacheTitle, File cacheDirectory) {
        super(cacheTitle, cacheDirectory);
    }

    private TextView mLastUpdateLabel;
    private ImageView mLastUpdateAction;

    private SharedPreferences getPrefs() {
        return getActivity().getSharedPreferences("feed_last_update", 0);
    }

    /**
     * Sets whether or not the last updated frame is visible.
     */
    public final void setLastUpdatedVisibile(boolean visible) {
        View v = getView();
        if (v == null) return;
        v.findViewById(R.id.lastUpdatedFrame).setVisibility(visible ? View.VISIBLE : View.GONE);
        v.findViewById(R.id.divider).setVisibility(visible ? View.VISIBLE : View.GONE);
    }

    /**
     * Gets the last time the fragment was updated (did a full refresh from the web).
     */
    public final Calendar getLastUpdatedTime() {
        SharedPreferences prefs = getPrefs();
        if (prefs.contains(mCacheTitle)) {
            Calendar cal = Calendar.getInstance();
            cal.setTimeInMillis(prefs.getLong(mCacheTitle, 0));
            return cal;
        }
        return null;
    }

    /**
     * Gets whether or not right now is a good time to show the last updated frame. Can be overridden to change behavior.
     */
    public boolean getShouldShowLastUpdated() {
        Calendar last = getLastUpdatedTime();
        if (last == null) return false;
        else if (getAdapter().getCount() == 0) return true;
        Calendar now = Calendar.getInstance();
        double lastHours = last.getTimeInMillis() / (1000 * 60 * 60);
        double nowHours = now.getTimeInMillis() / (1000 * 60 * 60);
        // An hour or more difference in last refresh time will return true
        return (nowHours - lastHours) >= 1;
    }

    /**
     * Invalidates the last updated frame. Decides whether or not the last updated frame should be visible based on
     * the return value of {#getShouldShowLastUpdated}, and updates the label if it's shown.
     */
    public final boolean invalidateLastUpdated() {
        boolean shouldShow = getShouldShowLastUpdated();
        setLastUpdatedVisibile(shouldShow);
        if (shouldShow) {
            mLastUpdateLabel.setText(getString(R.string.last_updated).replace("{date}",
                    TimeUtils.toString(getLastUpdatedTime(), false, true)));
        }
        return shouldShow;
    }


    @Override
    public int getLayout() {
        return R.layout.fragment_list_lastupdated;
    }

    @Override
    public void onViewCreated(View view, Bundle savedInstanceState) {
        mLastUpdateLabel = (TextView) view.findViewById(R.id.ptrLastUpdateLabel);
        mLastUpdateAction = (ImageView) view.findViewById(R.id.ptrLastUpdateAction);
        super.onViewCreated(view, savedInstanceState);
        invalidateLastUpdated();
        mLastUpdateAction.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                onUserRefresh();
            }
        });
        mLastUpdateAction.setOnLongClickListener(new View.OnLongClickListener() {
            @Override
            public boolean onLongClick(View v) {
                Toast.makeText(getActivity(), R.string.reload, Toast.LENGTH_SHORT).show();
                return true;
            }
        });
    }

    @Override
    public void performRefresh(boolean progress) {
        mLastUpdateAction.setEnabled(false);
        super.performRefresh(progress);
    }

    /**
     * Sets the last update time for the fragment to right now and updates the label.
     */
    public final void setLastUpdatedTime() {
        Calendar now = Calendar.getInstance();
        getPrefs().edit().putLong(mCacheTitle, now.getTimeInMillis()).commit();
        invalidateLastUpdated();
    }

    @Override
    public void setLoadComplete(boolean error) {
        super.setLoadComplete(error);
        mLastUpdateAction.setEnabled(true);
        if (!error) setLastUpdatedTime();
        invalidateLastUpdated();
    }

    @Override
    public void setLoadFromCacheComplete(boolean error) {
        // Prevent the setLoadComplete() code from this class from being called after a cache load
        super.setLoadComplete(error);
        invalidateLastUpdated();
    }

    @Override
    public void onCacheEmpty() {
        // Overriding the default behavior of refreshing immediately to show the last updated label if necessary
        Calendar last = getLastUpdatedTime();
        if (last == null || !invalidateLastUpdated()) super.onCacheEmpty();
    }

    /**
     * Called when the user presses the button in the last updated frame; invokes performRefresh() by default.
     */
    public void onUserRefresh() {
        performRefresh(true);
    }
}