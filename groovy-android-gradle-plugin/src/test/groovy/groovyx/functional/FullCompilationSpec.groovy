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

package groovyx.functional

import spock.lang.IgnoreIf
import spock.lang.Unroll

import static groovyx.internal.TestProperties.allTests
import static groovyx.internal.TestProperties.buildToolsVersion
import static groovyx.internal.TestProperties.compileSdkVersion

/**
 * Complete test suite to ensure the plugin works with the different versions of android gradle plugin.
 * This will only be run if the system property of 'allTests' is set to true
 */
@IgnoreIf({ !allTests })
class FullCompilationSpec extends FunctionalSpec {

  @Unroll
  def "should compile android app with java:#javaVersion, android plugin:#androidPluginVersion, gradle version: #gradleVersion"() {
    given:
    file("settings.gradle") << "rootProject.name = 'test-app'"

    buildFile << """
      buildscript {
        repositories {
          maven { url "${localRepo.toURI()}" }
          jcenter()
        }
        dependencies {
          classpath 'com.android.tools.build:gradle:$androidPluginVersion'
          classpath 'org.codehaus.groovy:groovy-android-gradle-plugin:$PLUGIN_VERSION'
        }
      }

      apply plugin: 'com.android.application'
      apply plugin: 'groovyx.android'

      repositories {
        jcenter()
      }

      android {
        compileSdkVersion $compileSdkVersion
        buildToolsVersion '$buildToolsVersion'

        defaultConfig {
          minSdkVersion 16
          targetSdkVersion $compileSdkVersion

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
          sourceCompatibility '$javaVersion'
          targetCompatibility '$javaVersion'
        }
      }

      androidGroovy {
        options {
          configure(groovyOptions) {
            encoding = 'UTF-8'
            forkOptions.jvmArgs = ['-noverify']
          }
          sourceCompatibility = '$javaVersion'
          targetCompatibility = '$javaVersion'
         }
      }

      dependencies {
        compile 'org.codehaus.groovy:groovy:2.4.5:grooid'

        androidTestCompile 'com.android.support.test:runner:0.4.1'
        androidTestCompile 'com.android.support.test:rules:0.4.1'

        testCompile 'junit:junit:4.12'
      }

      // force unit test types to be assembled too
      android.testVariants.all { variant ->
        tasks.getByName('assemble').dependsOn variant.assemble
      }
    """

    file('src/main/AndroidManifest.xml') << """
      <?xml version="1.0" encoding="utf-8"?>
      <manifest xmlns:android="http://schemas.android.com/apk/res/android"
          package="groovyx.test">

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

    // create Java class to ensure this compile correctly along with groovy classes
    file('src/main/java/groovyx/test/SimpleJava.java') << """
      package groovyx.test;

      public class SimpleJava {
        public static int getInt() {
          return 1337;
        }
      }
    """

    // create Java class in groovy folder to ensure this compile correctly along with groovy classes
    file('src/main/groovy/groovyx/test/SimpleJavaGroovy.java') << """
      package groovyx.test;

      public class SimpleJavaGroovy {
        public static int getInt() {
          return 2;
        }
      }
    """

    file('src/main/groovy/groovyx/test/MainActivity.groovy') << """
      package groovyx.test

      import android.app.Activity
      import android.os.Bundle
      import groovy.transform.CompileStatic

      @CompileStatic
      class MainActivity extends Activity {
        @Override void onCreate(Bundle savedInstanceState) {
          super.onCreate(savedInstanceState)
          contentView = R.layout.activity_main

          def someValue = SimpleJava.int
          def result = someValue * SimpleJavaGroovy.int
        }
      }
    """

    file('src/androidTest/groovy/groovyx/test/AndroidTest.groovy') << """
      package groovyx.test

      import android.support.test.runner.AndroidJUnit4
      import android.test.suitebuilder.annotation.SmallTest
      import groovy.transform.CompileStatic
      import org.junit.Before
      import org.junit.Test
      import org.junit.runner.RunWith

      @RunWith(AndroidJUnit4)
      @SmallTest
      @CompileStatic
      class AndroidTest {
        @Test void shouldCompile() {
          assert 5 * 2 == 10
        }
      }
    """

    file('src/test/groovy/groovyx/test/JvmTest.groovy') << """
      package groovyx.test

      import org.junit.Test

      class JvmTest {
        @Test void shouldCompile() {
          assert 10 * 2 == 20
        }
      }
    """

    when:
    runWithVersion gradleVersion, 'assemble', 'test'

    then:
    noExceptionThrown()
    file('build/outputs/apk/test-app-debug.apk').exists()
    file('build/intermediates/classes/debug/groovyx/test/MainActivity.class').exists()
    file('build/intermediates/classes/androidTest/debug/groovyx/test/AndroidTest.class').exists()
    file('build/intermediates/classes/test/debug/groovyx/test/JvmTest.class').exists()
    file('build/intermediates/classes/test/release/groovyx/test/JvmTest.class').exists()

    where:
    // test common configs that touches the different way to access the classpath
    javaVersion | androidPluginVersion | gradleVersion
    '1.6'       | '1.1.0'              | '2.10'
    '1.6'       | '1.3.0'              | '2.10'
    '1.6'       | '1.5.0'              | '2.10'
    '1.7'       | '1.5.0'              | '2.11'
    '1.7'       | '1.5.0'              | '2.12' // added due to breaking changes in gradle 2.12
    '1.7'       | '2.0.0'              | '2.13'
    '1.7'       | '2.1.2'              | '2.14'
  }

  @Unroll
  def "should compile android library with java:#javaVersion and android plugin:#androidPluginVersion, gradle version:#gradleVersion"() {
    given:
    file("settings.gradle") << "rootProject.name = 'test-lib'"

    buildFile << """
      buildscript {
        repositories {
          maven { url "${localRepo.toURI()}" }
          jcenter()
        }
        dependencies {
          classpath 'com.android.tools.build:gradle:$androidPluginVersion'
          classpath 'org.codehaus.groovy:groovy-android-gradle-plugin:$PLUGIN_VERSION'
        }
      }

      apply plugin: 'com.android.library'
      apply plugin: 'groovyx.android'

      repositories {
        jcenter()
      }

      android {
        compileSdkVersion $compileSdkVersion
        buildToolsVersion '$buildToolsVersion'

        defaultConfig {
          minSdkVersion 16
          targetSdkVersion $compileSdkVersion

          versionCode 1
          versionName '1.0.0'
        }

        compileOptions {
          sourceCompatibility '$javaVersion'
          targetCompatibility '$javaVersion'
        }
      }

      dependencies {
        compile 'org.codehaus.groovy:groovy:2.4.5:grooid'

        androidTestCompile 'com.android.support.test:runner:0.4.1'
        androidTestCompile 'com.android.support.test:rules:0.4.1'

        testCompile 'junit:junit:4.12'
      }

      // force unit test types to be assembled too
      android.testVariants.all { variant ->
        tasks.getByName('assemble').dependsOn variant.assemble
      }
    """

    file('src/main/AndroidManifest.xml') << """
      <manifest xmlns:android="http://schemas.android.com/apk/res/android"
        package="groovyx.test"/>
    """

    // create Java class to ensure this compiles correctly along with groovy classes
    file('src/main/java/groovyx/test/SimpleJava.java') << """
      package groovyx.test;

      public class SimpleJava {
        public static int getInt() {
          return 1;
        }
      }
    """

    // create Java class in groovy folder to ensure this compile correctly along with groovy classes
    file('src/main/groovy/groovyx/test/SimpleJavaGroovy.java') << """
      package groovyx.test;

      public class SimpleJavaGroovy {
        public static int getInt() {
          return 2;
        }
      }
    """

    file('src/main/groovy/groovyx/test/Test.groovy') << """
      package groovyx.test

      import android.util.Log
      import groovy.transform.CompileStatic

      @CompileStatic
      class Test {
        static void testMethod() {
          Log.d(Test.name, "Testing \${SimpleJava.int} \${SimpleJavaGroovy.int}")
        }
      }
    """

    file('src/androidTest/groovy/groovyx/test/AndroidTest.groovy') << """
      package groovyx.test

      import android.support.test.runner.AndroidJUnit4
      import android.test.suitebuilder.annotation.SmallTest
      import groovy.transform.CompileStatic
      import org.junit.Before
      import org.junit.Test
      import org.junit.runner.RunWith

      @RunWith(AndroidJUnit4)
      @SmallTest
      @CompileStatic
      class AndroidTest {
        @Test
        void shouldCompile() {
          assert 5 == 5
        }
      }
    """

    file('src/test/groovy/groovyx/test/JvmTest.groovy') << """
      package groovyx.test

      import org.junit.Test

      class JvmTest {
        @Test void shouldCompile() {
          assert 10 * 2 == 20
        }
      }
    """

    when:
    runWithVersion gradleVersion, 'assemble', 'test'

    then:
    noExceptionThrown()
    file('build/outputs/aar/test-lib-debug.aar').exists()
    file('build/outputs/aar/test-lib-release.aar').exists()
    file('build/intermediates/classes/debug/groovyx/test/Test.class').exists()
    file('build/intermediates/classes/release/groovyx/test/Test.class').exists()
    file('build/intermediates/classes/androidTest/debug/groovyx/test/AndroidTest.class').exists()
    file('build/intermediates/classes/test/debug/groovyx/test/JvmTest.class').exists()
    file('build/intermediates/classes/test/release/groovyx/test/JvmTest.class').exists()

    where:
    // test common configs that touches the different way to access the classpath
    javaVersion | androidPluginVersion | gradleVersion
    '1.6'       | '1.1.0'              | '2.10'
    '1.6'       | '1.3.0'              | '2.10'
    '1.6'       | '1.5.0'              | '2.10'
    '1.7'       | '1.5.0'              | '2.10'
    '1.7'       | '1.5.0'              | '2.11'
    '1.7'       | '1.5.0'              | '2.12'
    '1.7'       | '2.0.0'              | '2.13'
    '1.7'       | '2.1.2'              | '2.14'
  }
}
