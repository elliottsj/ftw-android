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

    private Class<? extends SilkCursorItem> mClass;

    public SilkCursorAdapter(Context context, Class<? extends SilkCursorItem> cls) {
        super(context);
        mClass = cls;
    }

    public final void changeCursor(Cursor cursor) {
        clear();
        while (cursor.moveToNext()) {
            ItemType item = performConvert(cursor);
            if (item != null) add(item);
        }
        notifyDataSetChanged();
    }

    private ItemType performConvert(Cursor cursor) {
        try {
            Object o = mClass.newInstance();
            Method m = mClass.getDeclaredMethod("convert", Cursor.class);
            return (ItemType) m.invoke(o, cursor);
        } catch (Exception e) {
            e.printStackTrace();
            throw new RuntimeException("An error occurred while invoking convert() of class " + mClass.getName() + ": " + e.getMessage());
        }
    }
}