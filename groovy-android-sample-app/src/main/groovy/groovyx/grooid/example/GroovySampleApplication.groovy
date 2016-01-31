package groovyx.grooid.example

import android.app.Application
import android.content.Context
import groovy.transform.CompileStatic

@CompileStatic
class GroovySampleApplication extends Application {

  final GroovyImageService groovyImageService = new GroovyImageService()

  static GroovySampleApplication get(Context context) {
    return context.getApplicationContext() as GroovySampleApplication
  }
}
