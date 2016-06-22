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
