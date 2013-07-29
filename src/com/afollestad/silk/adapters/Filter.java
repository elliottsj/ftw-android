package com.afollestad.silk.adapters;

public interface Filter<T> {
    public abstract boolean isSame(T one, T two);
}