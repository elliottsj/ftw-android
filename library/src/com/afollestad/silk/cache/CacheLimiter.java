package com.afollestad.silk.cache;

/**
 * Used to set a limit to a {@link SilkCacheManager}.
 *
 * @author Aidan Follestad (afollestad)
 */
public class CacheLimiter {

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

    /**
     * Initializes a new CacheLimiter instance.
     *
     * @param size The size limit for the cache before items are trimmed.
     */
    public CacheLimiter(int size) {
        if (size < 1) throw new IllegalArgumentException("The size limit cannot be less than 1.");
        mSize = size;
        mMode = TrimMode.TOP;
    }

    /**
     * Initializes a new CacheLimiter instance.
     *
     * @param size The size limit for the cache before items are trimmed.
     * @param mode The trim mode that instructs the manager how to trim items when the cache limit is exceeded.
     */
    public CacheLimiter(int size, TrimMode mode) {
        this(size);
        mMode = mode;
    }

    private int mSize;
    private TrimMode mMode;

    public int getSize() {
        return mSize;
    }

    public TrimMode getMode() {
        return mMode;
    }
}
