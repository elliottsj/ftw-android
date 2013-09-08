package com.afollestad.silk.caching;

public interface OnReadyCallback<ItemType extends SilkComparable> {

    public void onReady(SilkCache<ItemType> cache);
}
