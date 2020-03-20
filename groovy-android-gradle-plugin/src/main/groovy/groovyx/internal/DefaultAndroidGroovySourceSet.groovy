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

package groovyx.internal

import groovyx.api.AndroidGroovySourceSet
import org.gradle.api.Action
import org.gradle.api.file.SourceDirectorySet
import org.gradle.api.model.ObjectFactory
import org.gradle.api.tasks.GroovySourceSet
import org.gradle.util.ConfigureUtil

class DefaultAndroidGroovySourceSet implements AndroidGroovySourceSet {
  final String name
  final SourceDirectorySet groovy
  final SourceDirectorySet allGroovy

  DefaultAndroidGroovySourceSet(String displayName, ObjectFactory objects) {
    name = displayName

    groovy = objects.sourceDirectorySet("$displayName Groovy source", "$displayName Groovy source")
    groovy.filter.include("**/*.java", "**/*.groovy")
    allGroovy = objects.sourceDirectorySet(String.format("%s Groovy source", displayName), String.format("%s Groovy source", displayName))
    allGroovy.source(groovy)
    allGroovy.filter.include("**/*.groovy")
  }

  /**
   * Looks like this was added in Gradle 3.3 which was causing breaking issues. Leave Override off
   * to prevent issues.
   */
  GroovySourceSet groovy(Action<? super SourceDirectorySet> configureAction) {
    configureAction.execute(groovy)
    return this
  }

  @Override GroovySourceSet groovy(Closure configureClosure) {
    ConfigureUtil.configure(configureClosure, groovy)
    return this
  }
}
