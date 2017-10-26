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

package groovyx

import groovyx.internal.AndroidGroovySourceSetFactory
import org.gradle.api.Action
import org.gradle.api.NamedDomainObjectContainer
import org.gradle.api.Project
import org.gradle.api.internal.file.FileResolver
import org.gradle.api.tasks.GroovySourceSet
import org.gradle.api.tasks.compile.GroovyCompile
import org.gradle.internal.reflect.Instantiator
import org.gradle.util.ConfigureUtil

/**
 * Configuration specific to the Groovy+Android plugin.
 */
class GroovyAndroidExtension {

  /**
   * Setting this flag to true will have only groovyc run instead of javac then groovyc run
   * This will effectively have all code (java and groovy) be joint compiled. This is
   * useful for adding groovy into older projects, and for having generated
   * code be able to utilize groovy code.
   *
   * @param skipJavaC
   */
  boolean skipJavaC

  final NamedDomainObjectContainer<GroovySourceSet> sourceSetsContainer

  private Closure<Void> configClosure

  GroovyAndroidExtension(Project project, Instantiator instantiator, FileResolver fileResolver) {
    sourceSetsContainer = project.container(GroovySourceSet, new AndroidGroovySourceSetFactory(instantiator, fileResolver))

    sourceSetsContainer.whenObjectAdded { GroovySourceSet sourceSet ->
      sourceSet.groovy
    }
  }

  /**
   * Configure {@link GroovyCompile} options
   * <p>
   * Here is an example of setting compiler customizers for compilation, turning on the
   * annotation processing, and setting the source and target compilation to java 1.7:
   * <pre>
   * androidGroovy {
   *   options {
   *     configure(groovyOptions) {
   *       javaAnnotationProcessing = true
   *       configurationScript = file("$projectDir/config/groovy-compile-options.groovy")
   *     }
   *     sourceCompatibility = '1.7'
   *     targetCompatibility = '1.7'
   *   }
   * }
   * </pre>
   * <p>
   * Please see <a href="//docs.gradle.org/current/dsl/org.gradle.api.tasks.compile.GroovyCompile.html">GroovyCompile</a>
   * for more information.
   *
   * @param config configuration closure that will be applied to all {@link GroovyCompile} tasks for the
   * android project.
   */

  void options(Closure<Void> config) {
    configClosure = config
  }

  /**
   * Configure the sources for Groovy in the same way Java sources can be configured for Android.
   * <p>
   * For example:
   * <pre>
   * androidGroovy {
   *   sourceSets {
   *     main {
   *       groovy {
   *         srcDirs = ['src/main/groovy', 'src/main/java', 'src/shared/groovy']
   *       }
   *     }
   *   }
   * }
   * </pre>
   * Would include both Groovy and Java source to be compiled by groovyc as well as included
   * the shared sources in 'src/shared/groovy'
   *
   * @param configClosure the configuration block that configures
   */
  void sourceSets(Action<NamedDomainObjectContainer<GroovySourceSet>> configClosure) {
    configClosure.execute(sourceSetsContainer)
  }

  void configure(GroovyCompile task) {
    if (configClosure != null) {
      ConfigureUtil.configure(configClosure, task)
    }
  }
}
