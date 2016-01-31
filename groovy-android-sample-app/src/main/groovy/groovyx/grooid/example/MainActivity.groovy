package groovyx.grooid.example

import android.app.Activity
import android.graphics.drawable.BitmapDrawable
import android.graphics.drawable.Drawable
import android.os.Bundle
import android.widget.ImageView
import com.arasthel.swissknife.SwissKnife
import com.arasthel.swissknife.annotations.InjectView
import com.arasthel.swissknife.annotations.OnBackground
import com.arasthel.swissknife.annotations.OnClick
import com.arasthel.swissknife.annotations.OnUIThread
import groovy.transform.CompileStatic

@CompileStatic
class MainActivity extends Activity {

  @InjectView(R.id.main_content) BetterViewAnimator mainContent
  @InjectView(R.id.main_image) ImageView image

  private GroovyImageService imageService

  @Override void onCreate(Bundle savedInstanceState) {
    super.onCreate(savedInstanceState)
    contentView = R.layout.activity_main
    SwissKnife.inject(this)

    mainContent.displayedChildId = R.id.main_load_data

    GroovySampleApplication.get(this).groovyImageService
  }

  @OnClick(R.id.main_load_data) void loadDataButtonClick() {
    mainContent.displayedChildId = R.id.main_progress
    loadImage()
  }

  @OnBackground
  private void loadImage() {
    def drawable = new BitmapDrawable(imageService.groovyImageInputStream)
    displayImage(drawable)
  }

  @OnUIThread
  private void displayImage(Drawable drawable) {
    image.imageDrawable = drawable
    mainContent.displayedChildId = R.id.main_image
  }
}
