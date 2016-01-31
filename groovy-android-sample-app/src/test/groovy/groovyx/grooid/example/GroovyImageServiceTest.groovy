package groovyx.grooid.example

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
