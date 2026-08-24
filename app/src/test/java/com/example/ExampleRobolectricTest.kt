package com.example

import android.content.Context
import androidx.lifecycle.Lifecycle
import androidx.test.core.app.ActivityScenario
import androidx.test.core.app.ApplicationProvider
import org.junit.Assert.assertEquals
import org.junit.Assert.assertTrue
import org.junit.Test
import org.junit.runner.RunWith
import org.robolectric.RobolectricTestRunner
import org.robolectric.annotation.Config

@RunWith(RobolectricTestRunner::class)
@Config(sdk = [36])
class ExampleRobolectricTest {

  @Test
  fun `read string from context`() {
    val context = ApplicationProvider.getApplicationContext<Context>()
    val appName = context.getString(R.string.app_name)
    assertEquals("AI StudyMate", appName)
  }

  @Test
  fun `launch MainActivity successfully`() {
    val scenario = ActivityScenario.launch(MainActivity::class.java)
    scenario.moveToState(Lifecycle.State.RESUMED)
    assertTrue(scenario.state.isAtLeast(Lifecycle.State.RESUMED))
    scenario.close()
  }
}
