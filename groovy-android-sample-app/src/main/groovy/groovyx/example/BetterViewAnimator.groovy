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

import android.content.Context
import android.support.annotation.IdRes
import android.util.AttributeSet
import android.widget.ViewAnimator
import groovy.transform.CompileStatic

@CompileStatic
class BetterViewAnimator extends ViewAnimator {
  BetterViewAnimator(Context context, AttributeSet attrs) {
    super(context, attrs)
  }

  /** Displays the view of the id passed in */
  public void setDisplayedChildId(@IdRes int id) {
    if (displayedChildId == id) return

    def value = (0..(childCount - 1)).find {
      getChildAt(it as int).id == id
    }

    if (value != null) {
      displayedChild = value as int
      return
    }

    throw new IllegalArgumentException("No view with ID $id")
  }

  /** Get the id this ViewAnimator is currently displaying */
  @IdRes public int getDisplayedChildId() {
    return getChildAt(displayedChild).id
  }
}
