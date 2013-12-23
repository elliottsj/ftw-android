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

Tutorial
=======
Coming soon.

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
