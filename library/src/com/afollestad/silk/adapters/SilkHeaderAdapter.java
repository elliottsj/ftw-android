package com.afollestad.silk.adapters;

import android.content.Context;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import com.afollestad.silk.cache.SilkComparable;

/**
 * Exactly the same as a {@link SilkAdapter}, but puts a header view at the top of the list.
 */
public abstract class SilkHeaderAdapter<T extends SilkComparable> extends SilkAdapter<T> {

    public SilkHeaderAdapter(Context context, int headerLayout) {
        super(context);
        mHeaderLayout = headerLayout;
    }

    private int mHeaderLayout;

    @Override
    public final View getView(int i, View view, ViewGroup viewGroup) {
        if (view == null) {
            int type = getItemViewType(i);
            int layout;
            if (type == 0) layout = mHeaderLayout;
            else layout = getLayout(i - 1, type);
            view = LayoutInflater.from(getContext()).inflate(layout, null);
        }
        return onViewCreated(i, view, getItem(i));
    }

    @Override
    public View onViewCreated(int index, View recycled, T item) {
        return null;
    }

    @Override
    public int getCount() {
        return super.getCount() + 1;
    }

    @Override
    public long getItemId(int i) {
        return super.getItemId(i - 1);
    }

    @Override
    public boolean isEnabled(int position) {
        return position > 0;
    }

    @Override
    public int getItemViewType(int position) {
        if (position == 0) return 0;
        return super.getItemViewType(position) + 1;
    }

    @Override
    public int getViewTypeCount() {
        return super.getViewTypeCount() + 1;
    }

    @Override
    public boolean isEmpty() {
        return getCount() <= 1;
    }
}