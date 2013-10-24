package com.afollestad.silk.adapters;

import android.app.Activity;
import android.content.Context;
import android.database.Cursor;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.AbsListView;
import android.widget.SimpleCursorAdapter;
import com.afollestad.silk.caching.SilkComparable;

/**
 * A CursorAdapter wrapper that makes creating list adapters easier. Contains various convenience methods and handles
 * recycling views on its own.
 *
 * @param <ItemType> The type of items held in the adapter.
 * @author Aidan Follestad (afollestad)
 */
public abstract class SilkCursorAdapter<ItemType extends SilkComparable> extends SimpleCursorAdapter implements ScrollStatePersister {

    private final Context context;
    private final int mLayout;
    private CursorConverter<ItemType> mConverter;
    private int mScrollState = AbsListView.OnScrollListener.SCROLL_STATE_IDLE;

    public SilkCursorAdapter(Activity context, int layout, CursorConverter<ItemType> converter) {
        this(context, layout, null, converter);
    }

    public SilkCursorAdapter(Activity context, int layout, Cursor c, CursorConverter<ItemType> converter) {
        this(context, layout, c, new String[]{}, new int[]{}, 0, converter);
    }

    public SilkCursorAdapter(Activity context, int layout, Cursor c, String[] from, int[] to, int flags, CursorConverter<ItemType> converter) {
        super(context, layout, c, from, to, flags);
        this.context = context;
        this.mLayout = layout;
        this.mConverter = converter;
    }

    public void setConverter(CursorConverter<ItemType> converter) {
        mConverter = converter;
    }

    /**
     * Called when a list item view is inflated and the inheriting adapter must fill in views in the inflated layout.
     * The second parameter ('recycled') should be returned at the end of the method.
     *
     * @param index    The index of the inflated view.
     * @param recycled The layout with views to be filled (e.g. text views).
     * @param item     The item at the current index of the adapter.
     */
    public abstract View onViewCreated(int index, View recycled, ItemType item);

    /**
     * Gets the context passed in the constructor, that's used for inflating views.
     */
    protected final Context getContext() {
        return context;
    }

    @Override
    public final View newView(Context context, Cursor cursor, ViewGroup parent) {
        return LayoutInflater.from(context).inflate(mLayout, null);
    }

    /**
     * @deprecated Override {@link #onViewCreated(int, android.view.View, SilkComparable)} instead.
     */
    @Override
    public View getView(int i, View convertView, ViewGroup viewGroup) {
        View view = super.getView(i, convertView, viewGroup);
        if (view == null) view = convertView;
        return onViewCreated(i, view, getItem(i));
    }

    public ItemType getItem(int position) {
        getCursor().moveToPosition(position);
        return mConverter.convert(getCursor());
    }

    /**
     * Gets the scroll state set by a {@link com.afollestad.silk.views.list.SilkListView}.
     */
    @Override
    public final int getScrollState() {
        return mScrollState;
    }

    /**
     * Used by the {@link com.afollestad.silk.views.list.SilkListView} to update the adapter with its scroll state.
     */
    @Override
    public final void setScrollState(int state) {
        mScrollState = state;
    }

    public interface CursorConverter<T extends SilkComparable> {
        public T convert(Cursor cursor);
    }
}