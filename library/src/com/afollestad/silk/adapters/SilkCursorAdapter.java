package com.afollestad.silk.adapters;

import android.app.Activity;
import android.content.Context;
import android.database.Cursor;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.AbsListView;
import android.widget.SimpleCursorAdapter;
import com.afollestad.silk.caching.SilkCursorItem;

import java.lang.reflect.Method;

/**
 * A CursorAdapter wrapper that makes creating list adapters easier. Contains various convenience methods and handles
 * recycling views on its own.
 *
 * @param <ItemType> The type of items held in the adapter.
 * @author Aidan Follestad (afollestad)
 */
public abstract class SilkCursorAdapter<ItemType extends SilkCursorItem> extends SimpleCursorAdapter implements ScrollStatePersister {

    private final Context context;
    private final Class<ItemType> mClass;
    private final int mLayout;
    private int mScrollState = AbsListView.OnScrollListener.SCROLL_STATE_IDLE;

    public SilkCursorAdapter(Activity context, Class<ItemType> cls, int layout) {
        this(context, cls, layout, null);
    }

    public SilkCursorAdapter(Activity context, Class<ItemType> cls, int layout, Cursor c) {
        this(context, cls, layout, c, new String[]{}, new int[]{}, 0);
    }

    public SilkCursorAdapter(Activity context, Class<ItemType> cls, int layout, Cursor c, String[] from, int[] to, int flags) {
        super(context, layout, c, from, to, flags);
        this.mClass = cls;
        this.context = context;
        this.mLayout = layout;
    }

    public int getLayout() {
        return mLayout;
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
     * Called to get the layout of a view being inflated by the SilkAdapter. The inheriting adapter class must return
     * the layout for list items, this should always be the same value unless you have multiple view types.
     * <p/>
     * If you override {#getItemViewType} and/or {#getViewTypeCount}, the parameter to this method will be filled with
     * the item type at the index of the item view being inflated. Otherwise, it can be ignored.
     */
    protected abstract int getLayout(int index, int type);

    /**
     * @deprecated Override {@link #onViewCreated(int, android.view.View, SilkCursorItem)} instead.
     */
    @Override
    public View getView(int i, View view, ViewGroup viewGroup) {
        if (view == null) {
            int type = getItemViewType(i);
            view = LayoutInflater.from(context).inflate(getLayout(i, type), null);
        }
        return onViewCreated(i, view, getItem(i));
    }

    public ItemType getItem(int position) {
        getCursor().moveToPosition(position);
        return performConvert();
    }

    private ItemType performConvert() {
        try {
            Object o = mClass.newInstance();
            Method m = mClass.getDeclaredMethod("convert", Cursor.class);
            return (ItemType) m.invoke(o, getCursor());
        } catch (Exception e) {
            throw new RuntimeException("An error occurred while invoking convert() of class " + mClass.getName());
        }
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
}