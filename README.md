Welcome
=============

![Screenshot](https://raw.github.com/afollestad/Cards-UI/master/images/device-2013-08-15-121417_framed.png)

Dependencies
=============
1. Silk (http://github.com/afollestad/Silk)

Getting Started
=============
Before the library will work, make sure you have `Silk` referenced as a dependency in both this library and your app.
You also need to copy the `assets` folder from `Silk` to your app due to the fact that this library uses font resources.

Implementing this library in your own apps is pretty simple. First, you need an XML layout that will contain the `ListView`
that displays your cards; it's recommended that you use the `CardListView` class instead of a stock `ListView`, as it
automates many things (such as notifying card headers that they were clicked and disabling the ListView divider and selector).

See the sample application for code details on how to start implementing the library into your own code.
