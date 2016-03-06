Groovy language support for Android
===================================

[![Build Status](https://travis-ci.org/groovy/groovy-android-gradle-plugin.svg?branch=master)](https://travis-ci.org/groovy/groovy-android-gradle-plugin)

This plugin adds support for writing Android applications using the [Groovy language](http://groovy-lang.org).

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

Groovy sources may be placed in `src/main/groovy`, `src/test/groovy`, `src/androidTest/groovy` and any `src/${buildVariant}/groovy` 
configured by default. A default project will have the `release` and `debug` variants but these can be configured with build
types and flavors. See the [android plugin docs](https://sites.google.com/a/android.com/tools/tech-docs/new-build-system/user-guide#TOC-Build-Types)
for more about configuring different build variants.

Extra groovy sources may be added in a similar fashion as the [android plugin](https://sites.google.com/a/android.com/tools/tech-docs/new-build-system/user-guide#TOC-Sourcesets-and-Dependencies)
using the `androidGroovy.sourceSets` block. This is especially useful for sharing code between the different test types, and also 
allows you to add Groovy to a previous project. For example

```groovy
androidGroovy {
  sourceSets {
    main {
      groovy {
        srcDirs += 'src/main/java'
      }
    }
  }
}
```

would add all of the Java files in `src/main/java` directory to the Groovy compile task. These files will be removed
from the Java compile task, and will allow for the Java code to make references to the Groovy code (joint compilation).
Please note, that you may need to also add these extra directories to the Java source sets in the android plugin 
for Android Studio to recognize the Groovy files as source.

Writing Groovy code
-------------------

This plugin has been successfully tested with Android Studio and will make no attempts to add support for other IDEs.
This plugin will let you write an application in Groovy but it is recommended, for performance, memory and battery life, 
that you use `@CompileStatic` wherever possible.

Details can be found on Melix's [blog](http://melix.github.io/blog/2014/06/grooid.html) and [here for more technical details](http://melix.github.io/blog/2014/06/grooid2.html)


Configuring the Groovy compilation options
------------------------------------------

The Groovy compilation tasks can be configured in the `androidGroovy` block using the `options` block:

```groovy
androidGroovy {
  options {
    configure(groovyOptions) {
      encoding = 'UTF-8'
      forkOptions.jvmArgs = ['-noverify'] // maybe necessary if you use Google Play Services
    }
    sourceCompatibility	= '1.7' // as of 0.3.9 these are automatically set based off the android plugin's
    targetCompatibility = '1.7'
  }
}
```

See [GroovyCompile](https://docs.gradle.org/current/dsl/org.gradle.api.tasks.compile.GroovyCompile.html) for more options.

Only Use GroovyC
----------------

For integration with previous projects or for working with generated files (such as BuildConfg)
it may be desirable to only have GroovyC run in order to have Java files reference Groovy files.
In order to do this the flag `skipJavaC` in the androidGroovy block should be set to true.

```groovy
androidGroovy {
  skipJavaC = true
}
```

This will remove all the files from the JavaC tasks and them all to GroovyC.

Android `packagingOptions`
--------------------------

Groovy Extension Modules and Global transformations both need a file
descriptor in order to work. Android packaging has a restriction
related to files having the same name located in the same path.

If you are using several Groovy libraries containing extension modules
or/and global transformations, Android may complain about those files.

You can simply add the following rule:

```groovy
android {
  packagingOptions {
      exclude 'META-INF/services/org.codehaus.groovy.transform.ASTTransformation'
      exclude 'META-INF/services/org.codehaus.groovy.runtime.ExtensionModule'
  }
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
