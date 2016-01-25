package groovyx.grooid.example

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

    def value = (0..childCount).find {
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
