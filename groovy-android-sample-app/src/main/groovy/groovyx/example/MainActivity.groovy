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

    imageService = GroovySampleApplication.get(this).groovyImageService
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
