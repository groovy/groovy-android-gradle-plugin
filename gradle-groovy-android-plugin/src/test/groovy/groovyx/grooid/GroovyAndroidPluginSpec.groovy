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

package groovyx.grooid

import com.android.build.gradle.AppPlugin
import com.android.build.gradle.LibraryPlugin
import com.android.build.gradle.api.AndroidSourceSet
import groovyx.grooid.internal.FileHelper
import org.gradle.api.Project
import org.gradle.api.tasks.GroovySourceSet
import org.gradle.testfixtures.ProjectBuilder
import org.junit.Rule
import org.junit.rules.TemporaryFolder
import spock.lang.Specification
import spock.lang.Unroll

import static groovyx.grooid.GroovyAndroidPlugin.ANDROID_GROOVY_EXTENSION_NAME

class GroovyAndroidPluginSpec extends Specification implements FileHelper {

  @Rule TemporaryFolder dir

  Project project

  def setup() {
    project = ProjectBuilder.builder().withProjectDir(dir.root).build()
  }

  def "should apply groovy plugin on top of app plugin"() {
    when:
    applyAppPlugin()

    then:
    noExceptionThrown()
    project.plugins.hasPlugin(GroovyAndroidPlugin)
  }

  def "should apply groovy plugin on top of library plugin"() {
    when:
    applyLibraryPlugin()

    then:
    noExceptionThrown()
    project.plugins.hasPlugin(GroovyAndroidPlugin)
  }

  def "should add groovy extension"() {
    given:
    applyAppPlugin()

    expect:
    project.extensions.getByName(ANDROID_GROOVY_EXTENSION_NAME) != null
  }

  def "should add groovy source sets"() {
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

  def "should add groovy compile tasks"() {
    given:
    applyAppPlugin()
    project.android {
      buildToolsVersion '21.1.2'
      compileSdkVersion 23
    }

    // Android Plugin Reqires this file to exist with parsable XML
    file('src/main/AndroidManifest.xml') << """
     <?xml version="1.0" encoding="utf-8"?>
      <manifest xmlns:android="http://schemas.android.com/apk/res/android"
          package="groovyx.grooid.test" />
    """.trim()

    when:
    project.evaluate()
    def groovyTasks = project.tasks.findAll { it.name.contains('Groovy') }
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
  def "should add groovy sourceCompatibility=#version and targetCompatibility=#version automatically"() {
    given:
    applyAppPlugin()
    project.android {
      buildToolsVersion '21.1.2'
      compileSdkVersion 23

      compileOptions {
        sourceCompatibility version
        targetCompatibility version
      }
    }

    // Android Plugin Reqires this file to exist with parsable XML
    file('src/main/AndroidManifest.xml') << """
     <?xml version="1.0" encoding="utf-8"?>
      <manifest xmlns:android="http://schemas.android.com/apk/res/android"
          package="groovyx.grooid.test" />
    """.trim()

    when:
    project.evaluate()
    def groovyTasks = project.tasks.findAll { it.name.contains('Groovy') }

    then:
    groovyTasks.each { task ->
      assert task.sourceCompatibility == version
      assert task.targetCompatibility == version
    }

    where:
    version | _
    '1.6'   | _
    '1.7'   | _
  }

  private void applyAppPlugin() {
    project.pluginManager.apply(AppPlugin)
    project.pluginManager.apply(GroovyAndroidPlugin)
  }

  private void applyLibraryPlugin() {
    project.pluginManager.apply(LibraryPlugin)
    project.pluginManager.apply(GroovyAndroidPlugin)
  }
}

