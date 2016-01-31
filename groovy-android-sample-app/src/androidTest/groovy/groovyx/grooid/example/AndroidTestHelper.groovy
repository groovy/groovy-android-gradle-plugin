package groovyx.grooid.example

import android.content.Context
import android.support.test.InstrumentationRegistry
import groovy.transform.CompileStatic

@CompileStatic
trait AndroidTestHelper {
  final Context context = InstrumentationRegistry.context
}
