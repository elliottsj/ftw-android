package com.afollestad.silk.adapters;

import android.content.Context;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.AbsListView;
import android.widget.BaseAdapter;
import com.afollestad.silk.cache.SilkComparable;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

/**
 * A BaseAdapter wrapper that makes creating list adapters easier. Contains various convenience methods and handles
 * recycling views on its own.
 *
 * @param <T> The type of items held in the adapter.
 * @author Aidan Follestad (afollestad)
 */
public abstract class SilkAdapter<T extends SilkComparable> extends BaseAdapter {

    public SilkAdapter(Context context) {
        this.context = context;
        this.items = new ArrayList<T>();
    }

    private final Context context;
    private final List<T> items;
    private boolean isChanged = false;
    private int mScrollState = AbsListView.OnScrollListener.SCROLL_STATE_IDLE;

    /**
     * Called to get the layout of a view being inflated by the SilkAdapter. The inheriting adapter class must return
     * the layout for list items, this should always be the same value unless you have multiple view types.
     * <p/>
     * If you override {#getItemViewType} and/or {#getViewTypeCount}, the parameter to this method will be filled with
     * the item type at the index of the item view being inflated. Otherwise, it can be ignored.
     */
    public abstract int getLayout(int type);

    /**
     * Called when a list item view is inflated and the inheriting adapter must fill in views in the inflated layout.
     * The second parameter ('recycled') should be returned at the end of the method.
     *
     * @param index    The index of the inflated view.
     * @param recycled The layout with views to be filled (e.g. text views).
     * @param item     The item at the current index of the adapter.
     */
    public abstract View onViewCreated(int index, View recycled, T item);

    /**
     * Gets the context passed in the constructor, that's used for inflating views.
     */
    public final Context getContext() {
        return context;
    }

    public void add(int index, T toAdd) {
        isChanged = true;
        this.items.add(index, toAdd);
        notifyDataSetChanged();
    }

    /**
     * Adds a single item to the adapter and notifies the attached ListView.
     */
    public void add(T toAdd) {
        isChanged = true;
        this.items.add(toAdd);
        notifyDataSetChanged();
    }

    /**
     * Adds an array of items to the adapter and notifies the attached ListView.
     */
    public final void add(T[] toAdd) {
        isChanged = true;
        for (T item : toAdd)
            add(item);
    }


    /**
     * Updates a single item in the adapter using isSame() from SilkComparable. Once the filter finds the item, the loop is broken
     * so you cannot update multiple items with a single call.
     * <p/>
     * If the item is not found, it will be added to the adapter.
     *
     * @return True if the item was updated.
     */
    public boolean update(T toUpdate) {
        return update(toUpdate, true);
    }

    /**
     * Updates a single item in the adapter using isSame() from SilkComparable. Once the filter finds the item, the loop is broken
     * so you cannot update multiple items with a single call.
     *
     * @param addIfNotFound Whether or not the item will be added if it's not found.
     * @return True if the item was updated or added.
     */
    public boolean update(T toUpdate, boolean addIfNotFound) {
        boolean found = false;
        for (int i = 0; i < items.size(); i++) {
            if (toUpdate.isSameAs(items.get(i))) {
                items.set(i, toUpdate);
                found = true;
                break;
            }
        }
        if (found) return true;
        else if (addIfNotFound && !found) {
            add(toUpdate);
            return true;
        }
        return false;
    }

    /**
     * Sets the items in the adapter (clears any previous ones before adding) and notifies the attached ListView.
     */
    public final void set(T[] toSet) {
        set(new ArrayList<T>(Arrays.asList(toSet)));
    }

    /**
     * Sets the items in the adapter (clears any previous ones before adding) and notifies the attached ListView.
     */
    public void set(List<T> toSet) {
        isChanged = true;
        this.items.clear();
        for (T item : toSet) this.add(item);
        notifyDataSetChanged();
    }

    /**
     * Checks whether or not the adapter contains an item based on the adapter's inherited Filter.
     */
    public final boolean contains(T item) {
        for (int i = 0; i < getCount(); i++) {
            T curItem = getItem(i);
            if (item.isSameAs(curItem)) return true;
        }
        return false;
    }

    /**
     * Removes an item from the list by its index.
     */
    public void remove(int index) {
        isChanged = true;
        this.items.remove(index);
        notifyDataSetChanged();
    }

    /**
     * Removes a single item in the adapter using isSame() from SilkComparable. Once the filter finds the item, the loop is broken
     * so you cannot remove multiple items with a single call.
     */
    public void remove(T toRemove) {
        for (int i = 0; i < items.size(); i++) {
            if (toRemove.isSameAs(items.get(i))) {
                this.remove(i);
                break;
            }
        }
    }

    /**
     * Removes an array of items from the adapter, uses isSame() from SilkComparable to find the items.
     */
    public final void remove(T[] toRemove) {
        for (T item : toRemove) remove(item);
    }

    /**
     * Clears all items from the adapter and notifies the attached ListView.
     */
    public void clear() {
        isChanged = true;
        this.items.clear();
        notifyDataSetChanged();
    }

    /**
     * Gets a list of all items in the adapter.
     */
    public final List<T> getItems() {
        return items;
    }

    @Override
    public int getCount() {
        return items.size();
    }

    @Override
    public T getItem(int i) {
        return items.get(i);
    }

    @Override
    public long getItemId(int i) {
        return i;
    }

    @Override
    public View getView(int i, View view, ViewGroup viewGroup) {
        if (view == null) {
            int type = getItemViewType(i);
            view = LayoutInflater.from(context).inflate(getLayout(type), null);
        }
        return onViewCreated(i, view, getItem(i));
    }

    /**
     * Resets the changed state of the adapter, indicating that the adapter has not been changed. Every call
     * to a mutator method (e.g. add, set, remove, clear) will set it back to true.
     */
    public void resetChanged() {
        isChanged = false;
    }

    /**
     * Gets whether or not the adapter has been changed since the last time {#resetChanged} was called.
     */
    public boolean isChanged() {
        return isChanged;
    }

    /**
     * Used by the {@link com.afollestad.silk.views.list.SilkListView} to update the adapter with its scroll state.
     */
    public final void setScrollState(int state) {
        mScrollState = state;
    }

    /**
     * Gets the scroll state set by a {@link com.afollestad.silk.views.list.SilkListView}.
     */
    public final int getScrollState() {
        return mScrollState;
    }
}