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
        classpath 'com.android.tools.build:gradle:1.5.0'
        classpath 'org.codehaus.groovy:gradle-groovy-android-plugin:0.3.8'
    }
}

apply plugin: 'groovyx.grooid.groovy-android'
```

Then you must choose which version and modules of Groovy you use. So far, Android support is available in
the 2.4.0 release and beyond, so you need to add the following repository to your `build.gradle` file:

```groovy

repositories {
   jcenter() // or mavenCentral()
}
```

Then you can start using Groovy by adding a dependency on the `grooid` classifier:

```groovy

dependencies {
    compile 'org.codehaus.groovy:groovy:2.4.5:grooid'
}

```

then use the `assembleDebug` task to test.

Should you want to test development versions of the plugin, you can add the snapshot repository and depend on a SNAPSHOT:

```groovy

buildscript {
    repositories {
        jcenter()
        maven {
            url = 'http://oss.jfrog.org/artifactory/oss-snapshot-local'
        }
    }
    dependencies {
        classpath 'com.android.tools.build:gradle:1.5.0'
        classpath 'org.codehaus.groovy:gradle-groovy-android-plugin:0.3.9-SNAPSHOT'
    }
}

```

Where to put sources?
---------------------

Groovy sources **must** be found inside `src/main/groovy`. Note that the plugin follows the behavior of the Groovy plugin
in Gradle, which means that if you want to be able to have Groovy classes referencing Java classes that themselves reference
Groovy classes, then those Java classes must be found inside `src/main/groovy` too (this is called *joint compilation*).

Writing Groovy code
-------------------

This plugin has been successfully tested with Android Studio. It will let you write an application in Groovy. It is
recommended, for performance, memory and battery life, that you use `@CompileStatic` wherever possible.

Details can be found on my [blog](http://melix.github.io/blog/2014/06/grooid.html) and [here for more technical details](http://melix.github.io/blog/2014/06/grooid2.html)

Configuring the Groovy compilation options
------------------------------------------

The default language level for both source and target is `1.6`. You can change it by giving some options. Following example changes it to `1.7`. Note that you have to change Android Studio project settings as well if you use the IDE.

```groovy

project.androidGroovy {
    options {
        sourceCompatibility = '1.7'
        targetCompatibility = '1.7'
    }
}

```

Similarily, the Groovy compilation tasks can be configured in the `androidGroovy` block using the `options` block:

```groovy

project.androidGroovy {
    options {
        configure(groovyOptions) {
            encoding = 'UTF-8'
            forkOptions.jvmArgs = ['-noverify'] // maybe necessary if you use Google Play Services
        }
        sourceCompatibility = '1.7'
        targetCompatibility = '1.7'
    }
}

```

Android `packagingOptions`
--------------------------

Groovy Extension Modules and Global transformations both need a file
descriptor in order to work. Android packaging has a restriction
related to files having the same name located in the same path.

If you are using several Groovy libraries containing extension modules
or/and global transformations, Android may complain about those files.

You can simply add the following rule:

```groovy

packagingOptions {
    exclude 'META-INF/services/org.codehaus.groovy.transform.ASTTransformation'
    exclude 'META-INF/services/org.codehaus.groovy.runtime.ExtensionModule'
}

```

There is no problem excluding global transformation descriptors because
those are only used at compile time, never at runtime.

The problem comes with module extensions. Unless you statically
compile classes using extension modules with `@CompileStatic` they won't
be available at runtime and you'll have a runtime exception.

There is an alternative. The [emerger](https://github.com/kaleidos/emerger) gradle plugin add
those excludes for you and merges all extension module descriptors in
a single file which will be available at runtime.
