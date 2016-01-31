package groovyx.grooid.example

import groovy.transform.CompileStatic

@CompileStatic
class GroovyImageService {
  private static final String GROOVY_IMAGE_URL = 'https://raw.githubusercontent.com/apache/groovy/master/xdocs/images/groovy-logo.png'

  /**
   * @return Input stream that when consumed will retrieve the groovy logo.
   */
  InputStream getGroovyImageInputStream() {
    return new URL(GROOVY_IMAGE_URL).newInputStream()
  }
}
