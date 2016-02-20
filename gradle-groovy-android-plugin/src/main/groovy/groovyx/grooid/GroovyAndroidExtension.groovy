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

import groovy.transform.PackageScope
import groovyx.grooid.internal.AndroidGroovySourceSetFactory
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

  private Closure<Void> configClosure

  final NamedDomainObjectContainer<GroovySourceSet> sourceSetsContainer

  GroovyAndroidExtension(Project project, Instantiator instantiator, FileResolver fileResolver) {
    sourceSetsContainer = project.container(GroovySourceSet, new AndroidGroovySourceSetFactory(instantiator, fileResolver))

    sourceSetsContainer.whenObjectAdded { GroovySourceSet sourceSet ->
      sourceSet.groovy
    }
  }

  void options(Closure<Void> config) {
    configClosure = config
  }

  void sourceSets(Action<NamedDomainObjectContainer<GroovySourceSet>> configClosure) {
    configClosure.execute(sourceSetsContainer)
  }

  @PackageScope void configure(GroovyCompile task) {
    if (configClosure != null) {
      ConfigureUtil.configure(configClosure, task)
    }
  }
}
