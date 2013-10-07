Introduction
=============
Welcome! This library contains a collection of utility and convenience classes that make various things easier to do on Android.
This project is being implemented with one of my apps, but I decided to make it public as it would be useful to others.

#### Dependencies

The JARs in the `libs` folder are the only dependency. The HTTP JAR was generated using [this](https://github.com/afollestad/httpclientandroidlib).

#### Importing

To use this library with your Android apps, you have to reference this project as a library (from Eclipse) or add it as a module (from IntelliJ). It now has Gradle support, too. It cannot be compiled as a JAR because this library contains resources such as layouts that are needed.

##### Including Silk as .aar in your project

This process was written with the assumption that you are using Android Studio 0.2.11, gradle 1.8+, and you have recent Android Build Tools 18.0.1. Other versions may work with some adaptations.

You do not need to open this library in Android Studio to build and include it in your project.

Steps:
* Clone a copy of this repository.
* Determine the location of your copy of the Android SDK. (See [Installing Android Studio](http://developer.android.com/sdk/installing/studio.html) to learn where the SDK might be installed.)
* Create a file at the root of the repository called `local.properties`. Add the following line, replacing "/path/to/sdk" with the actual path as determined above:

```
sdk.dir=/path/to/sdk
```
* At the root of the repository, run `./gradlew install`. You should see some ":library:..." lines in the output, and at the end something like this:

```
Uploading: com/afollestad/silk/library/1.0-SNAPSHOT/library-1.0-20131007.005624-2.aar to repository remote at file:///home/<username>/.m2/repository
Transferring 1228K from remote
Uploaded 1228K
:library:install

BUILD SUCCESSFUL
```

If you are including Silk directly in your app:
* Find the `build.gradle` file used for your project application, which is usually either in the project root or in a subdirectory in the project root.
* Add `mavenLocal()` to the `repositories` block and `compile 'com.afollestad.silk:library:1.0-SNAPSHOT` to the `dependencies` block. Your file will look something like this:

```
buildscript {
    repositories {
        mavenCentral()
    }
    dependencies {
        classpath 'com.android.tools.build:gradle:0.5.+'
    }
}
apply plugin: 'android'

repositories {
    mavenLocal()
    mavenCentral()
}

android {
    compileSdkVersion 18
    buildToolsVersion "18.0.1"

    defaultConfig {
        minSdkVersion 15
        targetSdkVersion 18
    }
}

dependencies {
	compile 'com.afollestad.cardsui:library:1.0-SNAPSHOT'
}

```
* At the root of your project directory, run `./gradlew build`. You should see `:<ProjectName>:prepareComAfollestadSilkLibrary10SNAPSHOTLibrary` or something like it in the output and `BUILD SUCCESSFUL` at the end.
* You're done. You can now use Silk in your app.

Adapters
=============

#### SilkAdapter

A class that you can extend to create customizable list adapters without extending `BaseAdapter`. This class makes it
much easier and more consistent to create list adapters, and it handles things like recycling views on its own.

Views
=============

#### 
DatePicker

A small, more compact version of the stock `DatePicker`. Made up of 3 horizontally-orientated spinners that represent the month,
day, and year.

#### SilkListView/SilkGridView

Connects to a `SilkAdapter` and notifies it of scroll state changes using `SilkAdapter.setScrollState()`.
Whenever you stop scrolling/flinging the list and the scroll state becomes idle, the list invokes `notifyDataSetChanged()` to cause a redraw of currently visible list items.
You can use `SilkAdapter.getScrollState()` to get the current scroll state from within the adapter. This is useful for
only loading images when the list is not being scrolled.

#### SilkImageView

Has a `setImageURL()` method, uses the `SilkImageManager` to quickly load images from various source types into the view. Images are automatically cached in memory and on disk for quick loading later.

#### SilkAspectImageView

A `SilkImageView` that automatically adjusts its height to keep aspect ratio with the width (even in a `RelativeLayout` where `MATCH_CONTENT/WRAP_CONTENT` type dimensions are used).

#### SilkSquareImageView

A `SilkImageView` that automatically adjusts its height to match the width of the view.

#### SilkSquareHeightImageView

A `SilkImageView` that automatically adjusts its width to match the height of the view.

#### `SilkRoundedImageView`

A `SilkImageView` that displays an image in a circle instead of a square, similar to what Google+ does.

#### SilkTextView

A `TextView` that automatically sets its typeface to Roboto Light. It loads
from the assets folder so it will work on any version of Android.

NOTE: You MUST copy the `assets` folder from this library to your application. The assets folder does not get compiled
in your app when it's only in the library.

#### SilkCondensedTextView

Same as the `SilkTextView` but uses Roboto Condensed instead of light.

NOTE: You MUST copy the `assets` folder from this library to your application. The assets folder does not get compiled
in your app when it's only in the library

#### SilkEditText

Same as the `SilkTextView` but it's an `EditText`.

NOTE: You MUST copy the `assets` folder from this library to your application. The assets folder does not get compiled
in your app when it's only in the library

Activities
=============

#### SilkDrawerActivity

Makes interacting with a `DrawerLayout` in your Activity's layout easier, handles mostly everything related to it on
its own.

Utilities
=============

#### Silk

A misc. utility class.

#### SilkCache

This class is used by the `SilkCachedFeedFragment` to cache items in the fragment's adapter, it allows you to easily manage your own
cache files using a class similar to the stock `SharedPreferences` class. You can write/read any class that implements `SilkComparable<T>` to/from a cache file (but remember to mark any fields of non-serializable types as `transient`, so they're ignored during serialization).

#### SilkImageManager

Allows you to easily load images from the disk, web, content provider, etc. and automatically cache them in memory (and on disk if necessary). Used by all variations of the `SilkImageView`.

#### TimeUtils

Provides convenience methods for converting Calendar/milliseconds into human readable strings. This is useful for almost any
app that needs to display a time to the user, but is especially useful for Twitter clients (see `TimeUtils.toStringShort()`).

Networking
=============

#### SilkHttpClient

Basically a wrapper of the Apache `HttpClient` that makes HTTP networking much easier, supports ayncronous methods with callbacks.

Fragments
=============

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

License
=======

    Copyright 2013 Aidan Follestad

    Licensed under the Apache License, Version 2.0 (the "License");
    you may not use this file except in compliance with the License.
    You may obtain a copy of the License at

       http://www.apache.org/licenses/LICENSE-2.0

    Unless required by applicable law or agreed to in writing, software
    distributed under the License is distributed on an "AS IS" BASIS,
    WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
    See the License for the specific language governing permissions and
    limitations under the License.
