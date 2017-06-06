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

trait AndroidFileHelper implements FileHelper {
  /**
   * Creates a simple android manifest that will make the Android Plugin happy.
   */
  void createSimpleAndroidManifest() {
    file('src/main/AndroidManifest.xml') << """
     <?xml version="1.0" encoding="utf-8"?>
      <manifest xmlns:android="http://schemas.android.com/apk/res/android"
          package="groovyx.test" />
    """.trim()
  }

  void createSimpleGroovyFile() {
    file('src/main/groovy/groovyx/Simple.groovy') << """
      package groovyx
      
      import groovy.transform.CompileStatic
      
      @CompileStatic
      class Simple {
        void doWork() {
          'Hello World'
        }
      }
    """
  }
}
