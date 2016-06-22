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

package groovyx.example

import spock.lang.Specification

/**
 * Class to test the {@link GroovyImageService}.
 *
 * Note: There is no @CompileStatic because this is run on the JVM instead of Android, and there
 * for is not required.
 *
 * Note: this is not a good test, it's just a example of how to do JVM testing.
 */
class GroovyImageServiceTest extends Specification {

  def "should download groovy image"() {
    given:
    def imageService = new GroovyImageService()
    InputStream stream = imageService.groovyImageInputStream

    expect:
    assert stream != null
  }
}
