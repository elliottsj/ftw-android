package com.afollestad.silk;

import android.content.Context;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.BaseAdapter;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collections;
import java.util.List;

/**
 * A BaseAdapter wrapper that makes creating list adapters easier. Contains various convenience methods and handles
 * recycling views on its own.
 *
 * @param <T> The type of items held in the adapter.
 * @author Aidan Follestad (afollestad)
 */
public abstract class SilkAdapter<T> extends BaseAdapter {

    public SilkAdapter(Context context) {
        this.context = context;
        this.items = new ArrayList<T>();
    }

    private final Context context;
    private final List<T> items;

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

    /**
     * Adds a single item to the adapter and notifies the attached ListView.
     */
    public void add(T toAdd) {
        this.items.add(toAdd);
        notifyDataSetChanged();
    }

    /**
     * Adds an array of items to the adapter and notifies the attached ListView.
     */
    public void add(T[] toAdd) {
        Collections.addAll(this.items, toAdd);
        notifyDataSetChanged();
    }

    /**
     * Sets the items in the adapter (clears any previous ones before adding) and notifies the attached ListView.
     */
    public void set(T[] toSet) {
        this.items.clear();
        this.items.addAll(Arrays.asList(toSet));
        notifyDataSetChanged();
    }

    /**
     * Checks whether or not the adapter contains an item yet.
     */
    public abstract boolean contains(T item);

    /**
     * Removes an item from the list by its index.
     */
    public void remove(int index) {
        this.items.remove(index);
        notifyDataSetChanged();
    }

    /**
     * Clears all items from the adapter and notifies the attached ListView.
     */
    public void clear() {
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
    public final int getCount() {
        return items.size();
    }

    @Override
    public final T getItem(int i) {
        return items.get(i);
    }

    @Override
    public long getItemId(int i) {
        return i;
    }

    @Override
    public final View getView(int i, View view, ViewGroup viewGroup) {
        if (view == null) {
            int type = getItemViewType(i);
            view = LayoutInflater.from(context).inflate(getLayout(type), null);
        }
        return onViewCreated(i, view, getItem(i));
    }
}
