package groovyx.grooid.functional

import com.google.common.base.StandardSystemProperty
import org.gradle.testkit.jarjar.org.gradle.util.GradleVersion
import org.gradle.testkit.runner.BuildResult
import org.gradle.testkit.runner.GradleRunner
import org.junit.Rule
import org.junit.rules.TemporaryFolder
import spock.lang.Specification

class FunctionalSpec extends Specification {

  static final String PLUGIN_VERSION = FunctionalSpec.classLoader.getResource("groovyx/gradle-groovy-android-plugin-version.txt").text.trim()

  @Rule TemporaryFolder dir

  GradleRunner runner(String gradleVersion, String... args) {
    return GradleRunner.create()
        .withProjectDir(dir.root)
        .withDebug(true) // always run inline to save memory, especially on CI
        .forwardOutput()
        .withTestKitDir(getTestKitDir())
        .withArguments(args.toList())
        .withGradleVersion(gradleVersion?:GradleVersion.current().version)
  }

  BuildResult runWithVersion(String gradleVersion, String... args) {
    runner(gradleVersion, args).build()
  }

  BuildResult run(String... args) {
    runner(null, args).build()
  }

  BuildResult fail(String... args) {
    runner(null, args).buildAndFail()
  }

  private static File getTestKitDir() {
    def gradleUserHome = System.getenv("GRADLE_USER_HOME")
    if (!gradleUserHome) {
      gradleUserHome = new File(System.getProperty("user.home"), ".gradle").absolutePath
    }
    return new File(gradleUserHome, "testkit")
  }

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

  File getLocalRepo() {
    def rootRelative = new File('build/localrepo')
    rootRelative.directory ? rootRelative : new File(new File(StandardSystemProperty.USER_DIR.value()).parentFile, 'build/localrepo')
  }
}
