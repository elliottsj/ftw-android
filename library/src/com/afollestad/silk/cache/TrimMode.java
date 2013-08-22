package com.afollestad.silk.cache;

/**
 * Used to instruct a {@link SilkCacheManager} how to trim items when its size limit is reached.
 *
 * @author Aidan Follestad (afollestad)
 */
public enum TrimMode {
    /**
     * Items are trimmed from the top of the cache list when the size limit is reached (index 0).
     */
    TOP,
    /**
     * Items are trimmed from the bottom of the cache list when the size limit is reached (index size - 1).
     */
    BOTTOM
}