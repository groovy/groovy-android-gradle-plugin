package groovyx.functional

import static groovyx.internal.TestProperties.androidPluginVersion
import static groovyx.internal.TestProperties.buildToolsVersion
import static groovyx.internal.TestProperties.compileSdkVersion

class AnnotationProcessingSpec extends FunctionalSpec {
  def "should compile android app with annotation processing"() {
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
          sourceCompatibility '1.7'
          targetCompatibility '1.7'
        }
      }

      dependencies {
        compile 'org.codehaus.groovy:groovy:2.4.11:grooid'

        compile 'com.squareup.moshi:moshi:1.5.0'
        annotationProcessor 'com.ryanharter.auto.value:auto-value-moshi:0.4.3'
        provided 'com.ryanharter.auto.value:auto-value-moshi-annotations:0.4.3'

        annotationProcessor 'com.google.auto.value:auto-value:1.4.1'
        provided 'com.jakewharton.auto.value:auto-value-annotations:1.4'

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

    file('src/main/groovy/groovyx/test/MainActivity.groovy') << """
      package groovyx.test

      import android.app.Activity
      import android.os.Bundle
      import groovy.transform.CompileStatic
      import com.google.auto.value.AutoValue

      @CompileStatic
      class MainActivity extends Activity {
        @Override void onCreate(Bundle savedInstanceState) {
          super.onCreate(savedInstanceState)
          contentView = R.layout.activity_main
          
          def dog = Animal.create 'dog', 4
          println dog
          
          def car = Automobile.create 'car', 4
          println car
        }
      }
      
      @AutoValue
      @CompileStatic
      abstract class Temp {

        static Temp create(String name) {
          new AutoValue_Temp(name)
        }

        abstract String name()
      }
    """

    file('src/main/groovy/groovyx/test/Automobile.java') << """
      package groovyx.test;
      
      import com.google.auto.value.AutoValue;
      
      @AutoValue
      abstract class Automobile {
        static Automobile create(String name, int numberOfWheels) {
          return new AutoValue_Automobile(name, numberOfWheels);
        }
        abstract String name();
        abstract int numberOfWheels();
      }
    """

    file('src/main/java/groovyx/test/Animal.java') << """
      package groovyx.test;
      
      import com.google.auto.value.AutoValue;
      
      @AutoValue
      abstract class Animal {
        static Animal create(String name, int numberOfLegs) {
          return new AutoValue_Animal(name, numberOfLegs);
        }
      
        abstract String name();
        abstract int numberOfLegs();
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

    when:
    run 'assemble', 'test'

    then:
    noExceptionThrown()
    file('build/outputs/apk/test-app-debug.apk').exists()
    file('build/intermediates/classes/debug/groovyx/test/MainActivity.class').exists()

    file('build/intermediates/classes/debug/groovyx/test/Animal.class').exists()
    file('build/generated/source/apt/debug/groovyx/test/AutoValue_Animal.java').exists()
    file('build/generated/source/apt/release/groovyx/test/AutoValue_Animal.java').exists()
    file('build/intermediates/classes/debug/groovyx/test/AutoValue_Animal.class').exists()

    file('build/intermediates/classes/debug/groovyx/test/Automobile.class').exists()
    file('build/generated/source/apt/debug/groovyx/test/AutoValue_Automobile.java').exists()
    file('build/generated/source/apt/release/groovyx/test/AutoValue_Automobile.java').exists()
    file('build/intermediates/classes/debug/groovyx/test/AutoValue_Automobile.class').exists()

    file('build/intermediates/classes/debug/groovyx/test/Temp.class').exists()
    file('build/generated/source/apt/debug/groovyx/test/AutoValue_Temp.java').exists()
    file('build/generated/source/apt/release/groovyx/test/AutoValue_Temp.java').exists()
    file('build/intermediates/classes/debug/groovyx/test/AutoValue_Temp.class').exists()

    file('build/intermediates/classes/androidTest/debug/groovyx/test/AndroidTest.class').exists()
    file('build/intermediates/classes/test/debug/groovyx/test/JvmTest.class').exists()
    file('build/intermediates/classes/test/release/groovyx/test/JvmTest.class').exists()
  }
}
