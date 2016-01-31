package groovyx.grooid.functional

import spock.lang.Unroll

class CompilationSpec extends FunctionalSpec {

  @Unroll
  def "should compile android app with java:#javaVersion, android plugin:#androidPluginVersion"() {
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
        buildToolsVersion "21.1.2"

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
          sourceCompatibility $javaVersion
          targetCompatibility $javaVersion
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

    file('src/main/groovy/groovyx/grooid/test/MainActivity.groovy') << """
      package groovyx.grooid.test

      import android.app.Activity
      import android.os.Bundle
      import groovy.transform.CompileStatic

      @CompileStatic
      class MainActivity extends Activity {
        @Override void onCreate(Bundle savedInstanceState) {
          super.onCreate(savedInstanceState)
          contentView = R.layout.activity_main
        }
      }
    """

    file('src/androidTest/groovy/groovyx/grooid/test/AndroidTest.groovy') << """
      package groovyx.grooid.test

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

    file('src/test/groovy/groovyx/grooid/test/JvmTest.groovy') << """
      package groovyx.grooid.test

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
    file('build/intermediates/classes/debug/groovyx/grooid/test/MainActivity.class').exists()
    file('build/intermediates/classes/androidTest/debug/groovyx/grooid/test/AndroidTest.class').exists()
    file('build/intermediates/classes/test/debug/groovyx/grooid/test/JvmTest.class').exists()
    file('build/intermediates/classes/test/release/groovyx/grooid/test/JvmTest.class').exists()

    where:
    // test common configs that touches the different way to access the classpath
    javaVersion | androidPluginVersion | gradleVersion
    '1.6'       | '1.3.0' | '2.2' // android plugin requires 2.2.
    '1.6'       | '1.5.0' | '2.10'
    '1.7'       | '1.5.0' | '2.10'
  }

  @Unroll
  def "should compile android library with java:#javaVersion and android plugin:#androidPluginVersion"() {
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
          classpath 'org.codehaus.groovy:gradle-groovy-android-plugin:$PLUGIN_VERSION'
        }
      }

      apply plugin: 'com.android.library'
      apply plugin: 'groovyx.grooid.groovy-android'

      repositories {
        jcenter()
      }

      android {
        compileSdkVersion 23
        buildToolsVersion "21.1.2"

        defaultConfig {
          minSdkVersion 16
          targetSdkVersion 23

          versionCode 1
          versionName '1.0.0'
        }

        compileOptions {
          sourceCompatibility $javaVersion
          targetCompatibility $javaVersion
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

    file('src/main/AndroidManifest.xml') << '<manifest xmlns:android="http://schemas.android.com/apk/res/android" package="groovyx.grooid.test"/>'

    file('src/main/groovy/groovyx/grooid/test/Test.groovy') << """
      package groovyx.grooid.test

      import android.util.Log
      import groovy.transform.CompileStatic

      @CompileStatic
      class Test {
        static void testMethod() {
          Log.d(Test.name, 'Testing')
        }
      }
    """

    file('src/androidTest/groovy/groovyx/grooid/test/AndroidTest.groovy') << """
      package groovyx.grooid.test

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

    file('src/test/groovy/groovyx/grooid/test/JvmTest.groovy') << """
      package groovyx.grooid.test

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
    file('build/intermediates/classes/debug/groovyx/grooid/test/Test.class').exists()
    file('build/intermediates/classes/release/groovyx/grooid/test/Test.class').exists()
    file('build/intermediates/classes/androidTest/debug/groovyx/grooid/test/AndroidTest.class').exists()
    file('build/intermediates/classes/test/debug/groovyx/grooid/test/JvmTest.class').exists()
    file('build/intermediates/classes/test/release/groovyx/grooid/test/JvmTest.class').exists()

    where:
    // test common configs that touches the different way to access the classpath
    javaVersion | androidPluginVersion | gradleVersion
    '1.6'       | '1.3.0' | '2.2' // android plugin requires 2.2.
    '1.6'       | '1.5.0' | '2.10'
    '1.7'       | '1.5.0' | '2.10'
  }
}
