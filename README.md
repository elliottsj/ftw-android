Welcome
=============

![Screenshot](https://raw.github.com/afollestad/Cards-UI/master/images/device-2013-08-15-121417_framed.png)

Dependencies
=============
1. Silk (http://github.com/afollestad/Silk)

Getting Started
=============
Before the library will work, make sure you have `Silk` referenced as a dependency in both this library and your app. For best results, build Silk as a AAR file and install it to your local maven repository (see below).
Otherwise, you will need to copy the `assets` folder from `Silk` to your app due to the fact that this library uses font resources.

Installing as AAR (recommended)
===============================
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
Uploading: com/afollestad/cardsui/library/1.0-SNAPSHOT/library-1.0-20131007.010822-2.aar to repository remote at file:///home/<username>/.m2/repository
Transferring 24K from remote
Uploaded 24K
:library:install

BUILD SUCCESSFUL
```

If you are including Silk directly in your app:
* Find the `build.gradle` file used for your project application, which is usually either in the project root or in a subdirectory in the project root.
* Add `mavenLocal()` to the `repositories` block and `compile 'com.afollestad.cardsui:library:1.0-SNAPSHOT` to the `dependencies` block. Your file will look something like this:

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
    buildToolsVersion "17.0.0"

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
* You're done. You can now use Cards UI in your app.


Implementing
============
Implementing this library in your own apps is pretty simple. First, you need an XML layout that will contain the `ListView`
that displays your cards; it's recommended that you use the `CardListView` class instead of a stock `ListView`, as it
automates many things (such as notifying card headers that they were clicked and disabling the ListView divider and selector).

See the sample application for code details on how to start implementing the library into your own code.
