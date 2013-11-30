package com.afollestad.silk.adapters;

import android.content.Context;
import android.database.Cursor;
import com.afollestad.silk.caching.SilkCursorItem;

import java.lang.reflect.Method;

/**
 * A CursorAdapter wrapper that makes creating list adapters easier. Contains various convenience methods and handles
 * recycling views on its own.
 *
 * @param <ItemType> The type of items held in the adapter.
 * @author Aidan Follestad (afollestad)
 */
public abstract class SilkCursorAdapter<ItemType extends SilkCursorItem> extends SilkAdapter<ItemType> implements ScrollStatePersister {

    private Cursor mCursor;
    private Class<? extends SilkCursorItem> mClass;

    public SilkCursorAdapter(Context context, Class<? extends SilkCursorItem> cls) {
        super(context);
        mClass = cls;
    }

    public final Cursor getCursor() {
        return mCursor;
    }

    public final void changeCursor(Cursor cursor) {
        mCursor = cursor;
        notifyDataSetChanged();
    }

    @Override
    public int getCount() {
        if (mCursor == null) return 0;
        return mCursor.getCount();
    }

    public final ItemType getItem(int position) {
        if (mCursor == null || mCursor.getCount() == 0) return null;
        mCursor.moveToPosition(position);
        return performConvert();
    }

    private ItemType performConvert() {
        try {
            Object o = mClass.newInstance();
            Method m = mClass.getDeclaredMethod("convert", Cursor.class);
            return (ItemType) m.invoke(o, getCursor());
        } catch (Exception e) {
            e.printStackTrace();
            throw new RuntimeException("An error occurred while invoking convert() of class " + mClass.getName() + ": " + e.getMessage());
        }
    }
}