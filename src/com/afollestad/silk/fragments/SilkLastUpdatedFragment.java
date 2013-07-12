package com.afollestad.silk.fragments;

import android.content.SharedPreferences;
import android.os.Bundle;
import android.view.View;
import android.widget.ImageButton;
import android.widget.TextView;
import com.afollestad.silk.R;
import com.afollestad.silk.TimeUtils;

import java.util.Calendar;

/**
 * A {@link com.afollestad.silk.fragments.SilkCachedFeedFragment} that allows you to show a frame at the top of the list,
 * indicating the last time the fragment refreshed, and allowing the user to invoke a new refresh by pressing a button.
 *
 * @author Aidan Follestad (afollestad)
 */
public abstract class SilkLastUpdatedFragment<T> extends SilkCachedFeedFragment<T> {

    private TextView mLastUpdateLabel;
    private ImageButton mLastUpdateAction;


    public final void setLastUpdateVisibile(boolean visible) {
        View v = getView();
        if (v == null)
            return;
        v.findViewById(R.id.lastUpdatedFrame).setVisibility(visible ? View.VISIBLE : View.GONE);
        v.findViewById(R.id.divider).setVisibility(visible ? View.VISIBLE : View.GONE);
    }

    public final Calendar getLastUpdateTime() {
        SharedPreferences prefs = getActivity().getSharedPreferences("feed_last_update", 0);
        if (prefs.contains(getCacheTitle())) {
            Calendar cal = Calendar.getInstance();
            cal.setTimeInMillis(prefs.getLong("feed_last_update", 0));
            return cal;
        }
        return null;
    }

    public void invalidateLastUpdateLabel() {
        Calendar lastUpdate = getLastUpdateTime();
        if (lastUpdate != null) {
            mLastUpdateLabel.setText(getString(R.string.last_updated).replace("{date}", TimeUtils.getFriendlyTimeLong(lastUpdate)));
            setLastUpdateVisibile(true);
        } else setLastUpdateVisibile(false);
    }


    @Override
    public int getLayout() {
        return R.layout.fragment_list_lastupdated;
    }

    @Override
    public void onViewCreated(View view, Bundle savedInstanceState) {
        mLastUpdateLabel = (TextView) view.findViewById(R.id.ptrLastUpdateLabel);
        mLastUpdateAction = (ImageButton) view.findViewById(R.id.ptrLastUpdateAction);
        super.onViewCreated(view, savedInstanceState);
        invalidateLastUpdateLabel();
        mLastUpdateAction.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                onUserRefresh();
            }
        });
    }

    @Override
    public void setLoadComplete() {
        super.setLoadComplete();
        // Save the update time in shared preferences and hide the last updated frame
        Calendar now = Calendar.getInstance();
        SharedPreferences prefs = getActivity().getSharedPreferences("feed_last_update", 0);
        prefs.edit().putLong(getCacheTitle(), now.getTimeInMillis()).commit();
        setLastUpdateVisibile(false);
    }

    @Override
    public void performRefresh(boolean progress) {
        // The last updated frame is hidden when refresh starts, by default.
        setLastUpdateVisibile(false);
        super.performRefresh(progress);
    }

    @Override
    public void onCacheEmpty() {
        super.onCacheEmpty();
        // Overriding the default behavior of refreshing immediately to show the last updated label
        if (getLastUpdateTime() != null) {
            // This isn't the first time the fragment has refreshed, just show the last update label
            setLastUpdateVisibile(true);
        } else {
            // The fragment has never been refreshed, invoke the default behavior in this case
            super.onCacheEmpty();
        }
    }

    /**
     * Called when the user presses the button in the last updated frame; invokes performRefresh() by default.
     */
    public void onUserRefresh() {
        performRefresh(true);
    }
}
