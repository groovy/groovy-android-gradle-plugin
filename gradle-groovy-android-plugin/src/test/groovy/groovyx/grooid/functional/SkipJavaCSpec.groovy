/*
 * Copyright 2016 the original author or authors.
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *    http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */

package groovyx.grooid.functional

import spock.lang.IgnoreIf

import static groovyx.grooid.internal.TestProperties.allTests

@IgnoreIf({ !allTests })
class SkipJavaCSpec extends FunctionalSpec {

  def "should joint compile java files added to groovy sourceDirs"() {
    file("settings.gradle") << "rootProject.name = 'test-app'"

    buildFile << """
      buildscript {
        repositories {
          maven { url "${localRepo.toURI()}" }
          jcenter()
        }
        dependencies {
          classpath 'com.android.tools.build:gradle:1.5.0'
          classpath 'org.codehaus.groovy:gradle-groovy-android-plugin:$PLUGIN_VERSION'
        }
      }

      apply plugin: 'com.android.application'
      apply plugin: 'groovyx.grooid.groovy-android'

      repositories {
        jcenter()
      }

      android {
        compileSdkVersion 23
        buildToolsVersion '23.0.2'

        defaultConfig {
          minSdkVersion 16
          targetSdkVersion 23

          versionCode 1
          versionName '1.0.0'

          testInstrumentationRunner 'android.support.test.runner.AndroidJUnitRunner'
        }

        buildTypes {
          debug {
            applicationIdSuffix '.dev'
          }
        }

        compileOptions {
          sourceCompatibility '1.7'
          targetCompatibility '1.7'
        }
      }

      androidGroovy {
        skipJavaC = true
      }

      dependencies {
        compile 'org.codehaus.groovy:groovy:2.4.5:grooid'
      }
    """

    file('src/main/AndroidManifest.xml') << """
      <?xml version="1.0" encoding="utf-8"?>
      <manifest xmlns:android="http://schemas.android.com/apk/res/android"
          package="groovyx.grooid.test">

        <application
            android:allowBackup="true"
            android:label="Test App">
          <activity
              android:name=".MainActivity"
              android:label="Test App">
            <intent-filter>
              <action android:name="android.intent.action.MAIN"/>
              <category android:name="android.intent.category.LAUNCHER"/>
            </intent-filter>
          </activity>
        </application>

      </manifest>
    """.trim()

    file('src/main/res/layout/activity_main.xml') << """
      <?xml version="1.0" encoding="utf-8"?>
      <FrameLayout xmlns:android="http://schemas.android.com/apk/res/android"
          android:layout_width="match_parent"
          android:layout_height="match_parent"
      >

        <TextView
            android:layout_width="wrap_content"
            android:layout_height="wrap_content"
            android:text="Hello Groovy!"
            android:gravity="center"
            android:textAppearance="?android:textAppearanceLarge"
        />

      </FrameLayout>
    """.trim()

    file('src/main/java/groovyx/grooid/test/ExampleJava.java') << """
      package groovyx.grooid.test;

      class ExampleJava {
        String getCoolString() {
          return "this is a cool string";
        }

        // proof that join compilation can work
        // from java sources
        String getGroovyString() {
          return MainActivity.getCoolString();
        }
      }
    """

    file('src/main/groovy/groovyx/grooid/test/MainActivity.groovy') << """
      package groovyx.grooid.test

      import android.app.Activity
      import android.os.Bundle
      import groovy.transform.CompileStatic

      @CompileStatic
      class MainActivity extends Activity {

        static String getCoolString() {
          'cool string from groovy'
        }

        @Override void onCreate(Bundle savedInstanceState) {
          super.onCreate(savedInstanceState)
          contentView = R.layout.activity_main

          new ExampleJava().coolString
        }
      }
    """

    when:
    copyTestDir()
    run 'assemble'

    then:
    noExceptionThrown()
    file('build/outputs/apk/test-app-debug.apk').exists()
    file('build/intermediates/classes/debug/groovyx/grooid/test/MainActivity.class').exists()
    file('build/intermediates/classes/debug/groovyx/grooid/test/ExampleJava.class').exists()
  }
}
