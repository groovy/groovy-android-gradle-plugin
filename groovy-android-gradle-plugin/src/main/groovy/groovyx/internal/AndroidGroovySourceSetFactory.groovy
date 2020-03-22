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
import org.gradle.api.NamedDomainObjectFactory
import org.gradle.api.model.ObjectFactory
import org.gradle.internal.reflect.Instantiator

/**
 * Factory to create {@link AndroidGroovySourceSet} object using an {@link Instantiator} to add
 * the DSL methods.
 */
class AndroidGroovySourceSetFactory implements NamedDomainObjectFactory<AndroidGroovySourceSet> {

  private final Instantiator instantiator
  private final ObjectFactory objects

  AndroidGroovySourceSetFactory(Instantiator instantiator, ObjectFactory objects) {
    this.instantiator = instantiator
    this.objects = objects
  }

  @Override AndroidGroovySourceSet create(String name) {
    return instantiator.newInstance(DefaultAndroidGroovySourceSet, name, objects)
  }
}
