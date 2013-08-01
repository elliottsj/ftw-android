package com.afollestad.silk.views;

import android.content.Context;
import android.util.AttributeSet;
import android.widget.AbsListView;
import android.widget.ListAdapter;
import android.widget.ListView;
import com.afollestad.silk.adapters.SilkAdapter;

/**
 * @author Aidan Follestad (afollestad)
 */
public class SilkListView extends ListView {

    public SilkListView(Context context) {
        super(context);
        init();
    }

    public SilkListView(Context context, AttributeSet attrs) {
        super(context, attrs);
        init();
    }

    public SilkListView(Context context, AttributeSet attrs, int defStyle) {
        super(context, attrs, defStyle);
        init();
    }

    private void init() {
        setOnScrollListener(new OnScrollListener() {
            @Override
            public void onScrollStateChanged(AbsListView view, int scrollState) {
                SilkAdapter adapter = (SilkAdapter) getAdapter();
                adapter.setScrollState(scrollState);
                if (scrollState == SCROLL_STATE_IDLE) {
                    // When the list is idle, notify the adapter to update (causing images to load)
                    adapter.notifyDataSetChanged();
                }
            }

            @Override
            public void onScroll(AbsListView view, int firstVisibleItem, int visibleItemCount, int totalItemCount) {
            }
        });
    }

    /**
     * @deprecated Use {@link #setSilkAdapter(com.afollestad.silk.adapters.SilkAdapter)} instead.
     */
    @Override
    public void setAdapter(ListAdapter adapter) {
        throw new RuntimeException("Please use setSilkAdapter() instead of setAdapter() on the SilkListView.");
    }

    /**
     * Sets the list's adapter, enforces the use of only a SilkAdapter, not any other type of adapter
     */
    public final void setSilkAdapter(SilkAdapter adapter) {
        super.setAdapter(adapter);
    }
}