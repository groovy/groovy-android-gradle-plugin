Groovy language support for Android
===================================

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
        classpath 'me.champeau.gradle:gradle-groovy-android-plugin:0.1'
    }
}

apply plugin: 'groovy-android'

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

then use the `packageDebug` task to test. If you see an error like:

```
Duplicate files copied in APK META-INF/LICENSE.txt
```

Then add the following block to your `android` configuration section:

```groovy

packagingOptions {
   // workaround for http://stackoverflow.com/questions/20673625/android-gradle-plugin-0-7-0-duplicate-files-during-packaging-of-apk
   exclude 'META-INF/LICENSE.txt'
   exclude 'META-INF/groovy-release-info.properties'
}
```

Writing Groovy code
-------------------

This plugin has been successfully tested with Android Studio. It will let you write an application in Groovy. It is
recommanded, for performance, memory and battery life, that you use @CompileStatic wherever possible.

Details can be found on my [blog](http://melix.github.io/blog/2014/06/grooid.html)


