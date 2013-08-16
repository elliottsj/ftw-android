package com.afollestad.silk.cache;

import java.io.Serializable;

/**
 * A basic interface used by the cache manager for deciding whether or not two items are the same thing, and whether
 * or not an item belongs in the cache.
 *
 * @author Aidan Follestad (afollestad)
 */
public interface SilkComparable<T> extends Serializable {

    /**
     * Whether or not this item is the same as another.
     */
    public abstract boolean isSameAs(T another);

    /**
     * Whether or not this item should be put into the cache.
     */
    public abstract boolean shouldIgnore();
}
