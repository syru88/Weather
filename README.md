Weather application
=======

This application was created as test project for STRV. It is weather application which can show information about
current weather and forecast. It is possible to look for city by name or by current position.


Features
========

Search by city name or current location.

Saving cities (I don't think that the users will add or remove locations very often, so there is no list for
removing cities. It is possible to add or remove city within optional menu. That is reason why I changed the
information in navigation drawer (there is a list with cities now instead of today and forecast menu items)))

Database with information about weather - it is prepared for background synchronization which can be quickly and
easily done (e.g. for widget).

Swipe refresh function.

Basic layout for landscape orientation.


Building project
================

'gradlew assemble'

'gradlew test' for tests (There is just

I've left signing configuration for certificate in weather.properties


WeatherConfig.java
------------------

Constants for accuracy, network timeout, receiving position timeout and how long are data valid (according to
OpenWeatherMap it should be 10 minutes. For testing purpose it is just 2 minutes)


Dependencies
============

* [Android Support Library v4](http://developer.android.com/tools/extras/support-library.html)
* [AppCompat](https://developer.android.com/reference/android/support/v7/appcompat/package-summary.html)
* [Android Design Support Library] (http://developer.android.com/tools/support-library/features.html#design)
* [Google Play Services](http://developer.android.com/google/play-services/index.html)
* [GSON](http://code.google.com/p/google-gson/)
* [OkHttp](https://github.com/square/okhttp)
* [Otto](https://github.com/square/otto)
* [Volley](https://android.googlesource.com/platform/frameworks/volley/)

Libraries for testing
* [dspec] (https://github.com/lucasr/dspec)
* [Stetho] (https://github.com/facebook/stetho)
* [Testing Support Library] (https://developer.android.com/tools/testing-support-library/index.html)
