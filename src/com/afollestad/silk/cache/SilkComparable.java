package com.afollestad.silk.cache;

import java.io.Serializable;

/**
 * A basic interface used by the cache manager for deciding whether or not two items are the same thing.
 *
 * @author Aidan Follestad (afollestad)
 */
public interface SilkComparable extends Serializable {

    public abstract long getId();
}
