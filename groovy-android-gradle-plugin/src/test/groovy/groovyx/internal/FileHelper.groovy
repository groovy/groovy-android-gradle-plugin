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

import org.gradle.testfixtures.ProjectBuilder
import org.junit.rules.TemporaryFolder

trait FileHelper {

  File getBuildFile() {
    return makeFile('build.gradle')
  }

  File makeFile(String path) {
    def f = file(path)
    if (!f.exists()) {
      def parts = path.split("/")
      if (parts.size() > 1) {
        dir.newFolder(*parts[0..-2])
      }
      dir.newFile(path)
    }
    return f
  }

  File file(String path) {
    def file = new File(dir.root, path)
    assert file.parentFile.mkdirs() || file.parentFile.exists()
    return file
  }

  /**
   * Helper method for copy a test project into home directory for debugging.
   */
  void copyTestDir() {
    def project = ProjectBuilder.builder().withProjectDir(dir.root).build()
    project.copy {
      from dir.root
      into new File(System.getProperty("user.home"), "testoutput-${new Date().format('yyyyMMddHHmm')}")
    }
  }

  abstract TemporaryFolder getDir()
}
