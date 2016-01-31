package groovyx.grooid.example

import android.support.test.runner.AndroidJUnit4
import android.test.suitebuilder.annotation.SmallTest
import android.view.View
import groovy.transform.CompileStatic
import org.junit.Before
import org.junit.Test
import org.junit.runner.RunWith

@RunWith(AndroidJUnit4)
@SmallTest
@CompileStatic
class BetterViewAnimatorTest implements AndroidTestHelper {

  private BetterViewAnimator betterViewAnimator

  @Before
  void setup() {
    betterViewAnimator = new BetterViewAnimator(context, null)
  }

  @Test(expected = IllegalArgumentException)
  void shouldThrowExceptionOnNonExistentId() {
    given: // in groovy we can put labels where ever!
    def view = new View(context)
    betterViewAnimator.addView(view)

    then:
    betterViewAnimator.displayedChildId = 1989
  }

  @Test
  void shouldSetChildWithIdToDisplayed() {
    given:
    def views = [new View(context), new View(context), new View(context)]
    views.eachWithIndex { View view, Integer i ->
      view.id = i
      betterViewAnimator.addView(view)
    }


    when:
    betterViewAnimator.displayedChildId = 2

    then:
    assert betterViewAnimator.displayedChild == 2 // Index and ids are the same for this test
  }
}
