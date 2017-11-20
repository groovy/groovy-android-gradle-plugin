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

import groovyx.functional.internal.AndroidFunctionalSpec
import groovyx.internal.AndroidFileHelper
import spock.lang.IgnoreIf
import spock.lang.Unroll

import java.lang.Void as Should

import static groovyx.internal.TestProperties.allTests

/**
 * Complete test suite to ensure the plugin works with the different versions of android gradle plugin.
 * This will only be run if the system property of 'allTests' is set to true
 */
@IgnoreIf({ !allTests })
class FullCompilationSpec extends AndroidFunctionalSpec implements AndroidFileHelper {

  @Unroll
  Should "compile android app with java:#javaVersion, android plugin:#androidPluginVersion, gradle version: #gradleVersion"() {
    given:
    file("settings.gradle") << "rootProject.name = 'test-app'"

    createBuildFileForApplication(_androidPluginVersion, javaVersion)
    createAndroidManifest()
    createMainActivityLayoutFile()

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
    runWithVersion(gradleVersion, *args)

    then:
    noExceptionThrown()
    file('build/outputs/apk/debug/test-app-debug.apk').exists()
    file('build/intermediates/classes/debug/groovyx/test/MainActivity.class').exists()
    file('build/intermediates/classes/androidTest/debug/groovyx/test/AndroidTest.class').exists()
    if (args.contains('test')) {
      assert file('build/intermediates/classes/test/debug/groovyx/test/JvmTest.class').exists()
      assert file('build/intermediates/classes/test/release/groovyx/test/JvmTest.class').exists()
    }

    where:
    // test common configs that touches the different way to access the classpath
    // Skipping tests on Gradle 4.1 and 4.2.1 due to a bug.
    // > Could not write message [ChannelMessage channel: org.gradle.process.internal.worker.request.RequestProtocol, payload: [MethodInvocation method: run(execute, [Ljava.lang.Class;@61128295, [Ljava.lang.Object;@652d0a6f, 166)]] to '/0:0:0:0:0:0:0:1:57024'.
    // > org.gradle.api.internal.artifacts.configurations.DefaultConfiguration$ConfigurationFileCollection
    // Stack trace shows Caused by: java.io.NotSerializableException: org.gradle.api.internal.artifacts.configurations.DefaultConfiguration$ConfigurationFileCollection
    javaVersion               | _androidPluginVersion | gradleVersion | args
    'JavaVersion.VERSION_1_6' | '3.0.0'               | '4.1'         | ['assemble']
    'JavaVersion.VERSION_1_7' | '3.0.0'               | '4.2.1'       | ['assemble']
    'JavaVersion.VERSION_1_8' | '3.0.0'               | '4.3'         | ['assemble', 'test']
  }

  @Unroll
  Should "compile android library with java:#javaVersion and android plugin:#androidPluginVersion, gradle version:#gradleVersion"() {
    given:
    file("settings.gradle") << "rootProject.name = 'test-lib'"

    createBuildFileForLibrary()
    createSimpleAndroidManifest()

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
    runWithVersion(gradleVersion, *args)

    then:
    noExceptionThrown()
    file('build/outputs/aar/test-lib-debug.aar').exists()
    file('build/outputs/aar/test-lib-release.aar').exists()
    file('build/intermediates/classes/debug/groovyx/test/Test.class').exists()
    file('build/intermediates/classes/release/groovyx/test/Test.class').exists()
    file('build/intermediates/classes/androidTest/debug/groovyx/test/AndroidTest.class').exists()
    if (args.contains('test')) {
      assert file('build/intermediates/classes/test/debug/groovyx/test/JvmTest.class').exists()
      assert file('build/intermediates/classes/test/release/groovyx/test/JvmTest.class').exists()
    }

    where:
    // test common configs that touches the different way to access the classpath
    // Skipping tests on Gradle 4.1 and 4.2.1 due to a bug.
    // > Could not write message [ChannelMessage channel: org.gradle.process.internal.worker.request.RequestProtocol, payload: [MethodInvocation method: run(execute, [Ljava.lang.Class;@61128295, [Ljava.lang.Object;@652d0a6f, 166)]] to '/0:0:0:0:0:0:0:1:57024'.
    // > org.gradle.api.internal.artifacts.configurations.DefaultConfiguration$ConfigurationFileCollection
    // Stack trace shows Caused by: java.io.NotSerializableException: org.gradle.api.internal.artifacts.configurations.DefaultConfiguration$ConfigurationFileCollection
    javaVersion               | _androidPluginVersion | gradleVersion | args
    'JavaVersion.VERSION_1_6' | '3.0.0'               | '4.1'         | ['assemble']
    'JavaVersion.VERSION_1_7' | '3.0.0'               | '4.2.1'       | ['assemble']
    'JavaVersion.VERSION_1_8' | '3.0.0'               | '4.3'         | ['assemble', 'test']
  }
}
