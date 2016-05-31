# Lorem Contacts

[![Build Status](https://travis-ci.org/milosmns/contacts-generator-android.svg?branch=master)](https://travis-ci.org/milosmns/contacts-generator-android)
[![Codacy Badge](https://api.codacy.com/project/badge/Grade/496513ff12ae42cbbfae805077744698)](https://www.codacy.com/app/milosmns/contacts-generator-android?utm_source=github.com&amp;utm_medium=referral&amp;utm_content=milosmns/contacts-generator-android&amp;utm_campaign=Badge_Grade)

A simple contacts generating app (with contact photos)

How it works
------------

- Choose a number of contacts to generate
- Pick a gender (you can also pick _both_)
- Choose whether to use contact photos or not
- Let it work in background or watch as it happens
- See the stats page with some interesting information

App Demo
--------

You can install the app from [here](https://play.google.com/store/apps/details?id=me.angrybyte.contactsgenerator) and check it out. Here are some layout captures from the app.

Landing page on big tablets

![Big_Tablet_1](https://raw.githubusercontent.com/milosmns/contacts-generator-android/master/resources/github-listing/bigtablet-main-landscape.png)

Stats page on big tablets

![Big_Tablet_2](https://raw.githubusercontent.com/milosmns/contacts-generator-android/master/resources/github-listing/bigtablet-stats-landscape.png)

Landing page on phones

![Phone](https://raw.githubusercontent.com/milosmns/contacts-generator-android/master/resources/github-listing/phone-stats-portrait.png)

Landing page on small tablets

![Small_Tablet_1](https://raw.githubusercontent.com/milosmns/contacts-generator-android/master/resources/github-listing/smalltablet-main-landscape.png)

Stats page on small tablets

![Small_Tablet_2](https://raw.githubusercontent.com/milosmns/contacts-generator-android/master/resources/github-listing/smalltablet-stats-landscape.png)

Requirements
------------

To run the app, you will need at least **Android 4.0**, but to build the source you will need:

- Android SDK 4.0 or later (minimum SDK level is 14)
- Android Studio (to compile and run), **Eclipse is not supported**
- An emulator or a physical device to run the app and tests

Testing
-------

You should be able to either test from AndroidStudio (```right click -> Run Test```) or from Gradle CLI (```gradlew connectedCheck```). Some tests require private user permissions, so you need to enable those manually from device settings in order for the tests to pass.

You can also check the code quality analysis [here](https://www.codacy.com/app/milosmns/contacts-generator-android), and automated build status with available Unit and Integration tests [here](https://travis-ci.org/milosmns/contacts-generator-android).

Additional information, credits and contribution
------------------------------------------------

- Persons are fetched from a dedicated REST API on [Random User](https://randomuser.me/), which is a part of [Random API](https://randomapi.com/) service. Big thanks to [Arron](https://twitter.com/arronhunt) and [Keith](https://twitter.com/solewolf1993)!
- API key stored in **raw** folder is a public one. We keep the private key locally.
When modifying the project, be sure to exclude that file either in your **local** version
of the **.gitignore** file, or using the following **git** commands:

    To stop tracking files: 
    
        git update-index --assume-unchanged FILENAME
    
    To start tracking files: 
    
        git update-index --no-assume-unchanged FILENAME
        
- If you found a bug while using the app, please [file an issue](https://github.com/milosmns/contacts-generator-android/issues/new). We will be tracking further developent through GitHub issues as well.
- All patches are encouraged, and may be submitted by [forking this project](https://github.com/milosmns/contacts-generator-android/fork) and
submitting a pull request through GitHub. I promise I will look into it as soon as possible ☺.
Some more help can be found through [Stack Overflow](http://stackoverflow.com/questions/tagged/lorem-contacts) or somewhere [on my blog](http://angrybyte.me).
