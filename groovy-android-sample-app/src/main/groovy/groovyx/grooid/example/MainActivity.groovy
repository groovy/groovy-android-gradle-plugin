package groovyx.grooid.example

import android.app.Activity
import android.os.Bundle
import groovy.transform.CompileStatic

@CompileStatic
class MainActivity extends Activity {
  @Override void onCreate(Bundle savedInstanceState) {
    super.onCreate(savedInstanceState)
    contentView = R.layout.activity_main
  }
}
