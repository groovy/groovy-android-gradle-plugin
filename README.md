Groovy language support for Android
===================================

[![Build Status](https://travis-ci.org/melix/groovy-android-gradle-plugin.svg?branch=master)](https://travis-ci.org/melix/groovy-android-gradle-plugin)

This plugin adds support for writing Android applications using the [Groovy language](http://groovy.codehaus.org).

Usage
-----

Edit your `build.gradle` file with the following:

```groovy
buildscript {
    repositories {
        jcenter()
    }
    dependencies {
        classpath 'me.champeau.gradle:gradle-groovy-android-plugin:0.2.1'
    }
}

apply plugin: 'me.champeau.gradle.groovy-android'

```

Then you must choose which version and modules of Groovy you use. So far, Android support is only available in
snapshots, so you need to add the following repository to your `build.gradle` file:

```groovy

repository {
   maven {
     url='https://oss.jfrog.org/oss-snapshot-local/'
   }
}
```

Then you can start using Groovy by adding a dependency on the `grooid` classifier:

```groovy

dependencies {
    compile 'org.codehaus.groovy:groovy:2.4.0-SNAPSHOT:grooid'
}

```

then use the `assembleDebug` task to test.

Writing Groovy code
-------------------

This plugin has been successfully tested with Android Studio. It will let you write an application in Groovy. It is
recommanded, for performance, memory and battery life, that you use @CompileStatic wherever possible.

Details can be found on my [blog](http://melix.github.io/blog/2014/06/grooid.html)


