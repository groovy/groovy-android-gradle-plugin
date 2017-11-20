/*
 * Copyright 2017 the original author or authors.
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

package groovyx

import com.android.build.gradle.AppPlugin
import com.android.build.gradle.api.AndroidSourceSet
import groovyx.internal.AndroidFileHelper
import groovyx.internal.AndroidPluginHelper
import groovyx.internal.TestProperties
import org.gradle.api.Project
import org.gradle.api.tasks.GroovySourceSet
import org.gradle.api.tasks.compile.GroovyCompile
import org.gradle.testfixtures.ProjectBuilder
import org.junit.Rule
import org.junit.rules.TemporaryFolder
import spock.lang.Specification
import spock.lang.Unroll

import java.lang.Void as Should

import static groovyx.GroovyAndroidPlugin.ANDROID_GROOVY_EXTENSION_NAME

class GroovyAndroidPluginSpec extends Specification implements AndroidFileHelper, AndroidPluginHelper {

  @Rule TemporaryFolder dir

  Project project

  void setup() {
    project = ProjectBuilder.builder().withProjectDir(dir.root).build()
  }

  @Unroll
  Should "apply groovy plugin on top of #projectPlugin"() {
    given:
    project.apply plugin: projectPlugin

    when:
    project.apply plugin: 'groovyx.android'

    then:
    noExceptionThrown()
    project.plugins.hasPlugin(GroovyAndroidPlugin)

    where:
    projectPlugin << ['android', 'com.android.application', 'android-library', 'com.android.library',
                      'com.android.test', 'com.android.feature']
  }

  Should "add groovy extension"() {
    given:
    applyAppPlugin()

    expect:
    project.extensions.getByName(ANDROID_GROOVY_EXTENSION_NAME) != null
  }

  Should "add groovy source sets"() {
    given:
    applyAppPlugin()
    def extension = project.extensions.getByName('android')

    expect:
    extension.sourceSets.all { AndroidSourceSet sourceSet ->
      def groovySourceSet = sourceSet.convention.plugins.get('groovy')

      assert groovySourceSet != null
      assert groovySourceSet instanceof GroovySourceSet
      assert groovySourceSet.groovy.srcDirs.contains(project.file("src/$sourceSet.name/groovy"))
    }
  }

  Should "add groovy compile tasks"() {
    given:
    applyAppPlugin()
    project.android {
      buildToolsVersion TestProperties.getBuildToolsVersion()
      compileSdkVersion TestProperties.getCompileSdkVersion()
    }

    // Android Plugin Requires this file to exist with parsable XML
    createSimpleAndroidManifest()
    createSimpleGroovyFile()
    file('src/androidTest/groovy/groovyx/SimpleAndroidTest.groovy') << """
      package groovyx
      class SimpleAndroidTest { }
    """
    file('src/test/groovy/groovyx/SimpleTest.groovy') << """
      package groovyx 
      class SimpleTest { }
    """

    when:
    project.evaluate()
    def groovyTasks = project.tasks.withType(GroovyCompile)
    def taskNames = groovyTasks.collect { it.name }

    then:
    groovyTasks.size() == 5
    taskNames.contains('compileDebugAndroidTestGroovyWithGroovyc')
    taskNames.contains('compileDebugGroovyWithGroovyc')
    taskNames.contains('compileDebugUnitTestGroovyWithGroovyc')
    taskNames.contains('compileReleaseGroovyWithGroovyc')
    taskNames.contains('compileReleaseUnitTestGroovyWithGroovyc')
  }

  @Unroll
  Should "add groovy sourceCompatibility=#version and targetCompatibility=#version automatically"() {
    given:
    applyAppPlugin()
    project.android {
      buildToolsVersion TestProperties.getBuildToolsVersion()
      compileSdkVersion TestProperties.getCompileSdkVersion()

      compileOptions {
        sourceCompatibility version
        targetCompatibility version
      }
    }

    // Android Plugin Requires this file to exist with parsable XML
    createSimpleAndroidManifest()
    createSimpleGroovyFile()

    when:
    project.evaluate()
    def groovyTasks = project.tasks.withType(GroovyCompile)

    then:
    groovyTasks.size() == 2
    groovyTasks.each { task ->
      assert task.sourceCompatibility == version
      assert task.targetCompatibility == version
    }

    where:
    version | _
    '1.6'   | _
    '1.7'   | _
    '1.8'   | _
  }

  Should "not enable groovy tasks if no source set"() {
    given:
    project.with {
      pluginManager.apply(AppPlugin)
      pluginManager.apply(GroovyAndroidPlugin)
      android {
        buildToolsVersion TestProperties.getBuildToolsVersion()
        compileSdkVersion TestProperties.getCompileSdkVersion()
      }
    }

    // Android Plugin Requires this file to exist with parsable XML
    createSimpleAndroidManifest()

    when:
    project.evaluate()

    then:
    project.tasks.withType(GroovyCompile).size() == 0
  }
}

