package com.afollestad.silk.cache;

import java.io.Serializable;

/**
 * A basic interface used by the cache manager for deciding whether or not two items are the same thing.
 *
 * @author Aidan Follestad (afollestad)
 */
public interface SilkComparable<T> extends Serializable {

    public abstract boolean isSameAs(T another);
}
