Introduction
=============
Welcome! This library contains a collection of utility and convenience classes that make various things easier to do on Android.
This project is being implemented with one of my apps, but I decided to make it public as it would be useful to others.

## Importing

To use this library with your Android apps, you have to reference this project as a library (from Eclipse) or add it as a module (from IntelliJ).
It cannot be compiled as a JAR because this library contains resources such as layouts that are needed.

Classes
=============

## Adapters

#### SilkAdapter

A class that you can extend to create customizable list adapters without extending `BaseAdapter`. This class makes it
much easier and more consistent to create list adapters, and it handles things like recycling views on its own.

## Views

#### SilkDatePicker

A small, more compact version of the stock `DatePicker`. Made up of 3 horizontally-orientated spinners that represent the month,
day, and year.

## Utilities

#### SilkCacheManager

This class is used by the `SilkCachedFeedFragment` to cache items in the fragment's adapter. Allows you to easily manage your own
cache files using both synchronous and asynchronous methods; any class that implements `Serializable` can be written to and read
2from a cache (but remember to mark any fields of non-serializable types as `transient`).

#### TimeUtils

Provides convenience methods for converting Calendar/milliseconds into human readable strings. This is useful for almost any
app that needs to display a time to the user, but is especially useful for Twitter clients (see `TimeUtils.getFriendlyTimeShort()`).

## Fragments

#### SilkFragment

The base `Fragment` class extended by other library fragments. Contains various convenience methods that make setting up
fragments easier and more consistent, it also allows you to keep track of when the `Fragment` is actually visible to a user,
even when it's in a `ViewPager` (and onResume() is called when the Fragment is outside of the user's view).

#### SilkListFragment

A `SilkFragment` that contains a list, empty text, and progress view, allowing you to easily show progress while the list is loading.
Also makes attaching to a `SilkAdapter` very quick and easy and has callbacks for single/long taps of list items.

#### SilkFeedFragment

A `SilkListFragment` that pulls a feed from the network and automatically inserts the results into its own list. This makes
the networking part much easier as you don't have to handle the threading yourself, and it has callbacks for errors. All
that you have to do is override `refresh()` on `onError()`.

#### SilkCachedFeedFragment

A `SilkFeedFragment` that automatically caches its contents locally, and loads it again later without pulling from the network.

#### SilkLastUpdatedFragment

A `SilkCachedFeedFragment` that displays a frame on the top indicating the last time the fragment pulled from the network,
and allowing the user to invoke a refresh.

#### SilkPagerFragment

A `SilkFragment` that contains a `ViewPager` and makes interaction with it easy.