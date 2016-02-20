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

import groovyx.grooid.internal.AndroidPluginHelper
import org.gradle.api.JavaVersion
import org.gradle.api.Project
import org.gradle.api.tasks.compile.GroovyCompile
import org.gradle.testfixtures.ProjectBuilder
import org.junit.Rule
import org.junit.rules.TemporaryFolder
import spock.lang.Specification


class GroovyAndroidExtensionSpec extends Specification implements AndroidPluginHelper {
  @Rule TemporaryFolder dir

  GroovyCompile groovyTask
  Project project

  def setup() {
    project = ProjectBuilder.builder().withProjectDir(dir.root).build()
    applyAppPlugin()
  }

  def "should set options on groovy compile"() {
    given:
    groovyTask = project.tasks.create('Test Groovy Compile', GroovyCompile)

    project.androidGroovy {
      options { // must be explicit here as spock does not resolve like gradle
        project.configure(groovyTask.groovyOptions) {
          encoding = 'UTF-8'
          forkOptions.jvmArgs = ['-noverify']
        }
        sourceCompatibility = JavaVersion.VERSION_1_7
        targetCompatibility = '1.7'
      }
    }

    when:
    extension.configure(groovyTask)

    then:
    groovyTask.sourceCompatibility == '1.7'
    groovyTask.targetCompatibility == '1.7'
    groovyTask.groovyOptions.encoding == 'UTF-8'
    groovyTask.groovyOptions.forkOptions.jvmArgs == ['-noverify']
  }

  def "should add groovy source directories to groovy extension"() {
    given:
    def expectedSourceDirs = ['test/groovy/test', 'src/main/groovy']

    when:
    project.androidGroovy {
      sourceSets {
        main {
          groovy {
            srcDirs = expectedSourceDirs
          }
        }
      }
    }

    def srcDirs = extension.sourceSetsContainer.getByName('main').groovy.srcDirs

    // this is to get around a weird bug where absolute path of dir.root does not actually
    // return the fill path missing the /private folder at the beginning (OSX).
    def sourcesDirStrings = srcDirs.collect{ it.path.split('/')[-3..-1].join('/') }

    then:
    noExceptionThrown()
    sourcesDirStrings == expectedSourceDirs
  }
}
