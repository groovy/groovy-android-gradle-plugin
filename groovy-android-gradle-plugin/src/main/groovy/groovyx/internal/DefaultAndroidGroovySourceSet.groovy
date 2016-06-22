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
import org.gradle.api.file.SourceDirectorySet
import org.gradle.api.internal.file.DefaultSourceDirectorySet
import org.gradle.api.internal.file.FileResolver
import org.gradle.api.tasks.GroovySourceSet
import org.gradle.util.ConfigureUtil
import org.gradle.util.GradleVersion

class DefaultAndroidGroovySourceSet implements AndroidGroovySourceSet {
  final String name
  final SourceDirectorySet groovy
  final SourceDirectorySet allGroovy

  DefaultAndroidGroovySourceSet(String displayName, FileResolver fileResolver) {
    name = displayName

    if (GradleVersion.current().compareTo(GradleVersion.version('2.11')) > 0) {
      // do this for older version of gradle to be able to not have to worry about loading the class
      def directoryFileTreeFactoryClass = this.class.classLoader.loadClass('org.gradle.api.internal.file.collections.DefaultDirectoryFileTreeFactory')
      def directoryFileTreeFactory = directoryFileTreeFactoryClass.newInstance()
      groovy = new DefaultSourceDirectorySet("$displayName Groovy source", fileResolver, directoryFileTreeFactory)
      groovy.filter.include("**/*.java", "**/*.groovy")
      allGroovy = new DefaultSourceDirectorySet(String.format("%s Groovy source", displayName), fileResolver, directoryFileTreeFactory)
      allGroovy.source(groovy)
      allGroovy.filter.include("**/*.groovy")
    } else {
      groovy = new DefaultSourceDirectorySet("$displayName Groovy source", fileResolver)
      groovy.filter.include("**/*.java", "**/*.groovy")
      allGroovy = new DefaultSourceDirectorySet(String.format("%s Groovy source", displayName), fileResolver)
      allGroovy.source(groovy)
      allGroovy.filter.include("**/*.groovy")
    }
  }

  @Override GroovySourceSet groovy(Closure configureClosure) {
    ConfigureUtil.configure(configureClosure, groovy)
    return this
  }
}
