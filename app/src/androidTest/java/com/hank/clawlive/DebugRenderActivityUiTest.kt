package com.hank.clawlive

import android.view.View
import androidx.core.view.ViewCompat
import androidx.core.view.WindowInsetsCompat
import androidx.test.core.app.ActivityScenario
import androidx.test.ext.junit.runners.AndroidJUnit4
import org.junit.Assert.*
import org.junit.Test
import org.junit.runner.RunWith

/**
 * UI/UX tests for DebugRenderActivity (Layout Editor)
 * Validates edge-to-edge display and safe insets for system bars
 */
@RunWith(AndroidJUnit4::class)
class DebugRenderActivityUiTest {

    @Test
    fun testTopBarHasSafeInsetFromStatusBar() {
        ActivityScenario.launch(DebugRenderActivity::class.java).use { scenario ->
            scenario.onActivity { activity ->
                val topBar = activity.findViewById<View>(R.id.topBar)
                val rootView = activity.findViewById<View>(android.R.id.content)

                assertNotNull("Top bar should exist", topBar)

                val windowInsets = ViewCompat.getRootWindowInsets(rootView)
                assertNotNull("Window insets should be available", windowInsets)

                val statusBarHeight = windowInsets!!.getInsets(
                    WindowInsetsCompat.Type.statusBars()
                ).top

                val topPadding = topBar.paddingTop
                assertTrue(
                    "Top bar padding ($topPadding) should be >= status bar height ($statusBarHeight)",
                    topPadding >= statusBarHeight
                )
            }
        }
    }

    @Test
    fun testBottomControlsHasSafeInsetFromNavigationBar() {
        ActivityScenario.launch(DebugRenderActivity::class.java).use { scenario ->
            scenario.onActivity { activity ->
                val bottomControls = activity.findViewById<View>(R.id.bottomControls)
                val rootView = activity.findViewById<View>(android.R.id.content)

                assertNotNull("Bottom controls should exist", bottomControls)

                val windowInsets = ViewCompat.getRootWindowInsets(rootView)
                assertNotNull("Window insets should be available", windowInsets)

                val navBarHeight = windowInsets!!.getInsets(
                    WindowInsetsCompat.Type.navigationBars()
                ).bottom

                val bottomPadding = bottomControls.paddingBottom
                assertTrue(
                    "Bottom padding ($bottomPadding) should be >= nav bar height ($navBarHeight)",
                    bottomPadding >= navBarHeight
                )
            }
        }
    }

    @Test
    fun testBackButtonIsAccessible() {
        ActivityScenario.launch(DebugRenderActivity::class.java).use { scenario ->
            scenario.onActivity { activity ->
                val btnBack = activity.findViewById<View>(R.id.btnBack)
                val rootView = activity.findViewById<View>(android.R.id.content)

                assertNotNull("Back button should exist", btnBack)
                assertTrue("Back button should be clickable", btnBack.isClickable)

                val location = IntArray(2)
                btnBack.getLocationOnScreen(location)

                val windowInsets = ViewCompat.getRootWindowInsets(rootView)
                val statusBarHeight = windowInsets?.getInsets(
                    WindowInsetsCompat.Type.statusBars()
                )?.top ?: 0

                assertTrue(
                    "Back button top (${location[1]}) should be below status bar ($statusBarHeight)",
                    location[1] >= statusBarHeight
                )
            }
        }
    }

    @Test
    fun testDoneButtonIsAccessible() {
        ActivityScenario.launch(DebugRenderActivity::class.java).use { scenario ->
            scenario.onActivity { activity ->
                val btnDone = activity.findViewById<View>(R.id.btnDone)
                val rootView = activity.findViewById<View>(android.R.id.content)

                assertNotNull("Done button should exist", btnDone)
                assertTrue("Done button should be clickable", btnDone.isClickable)

                val screenHeight = activity.resources.displayMetrics.heightPixels
                val location = IntArray(2)
                btnDone.getLocationOnScreen(location)
                val buttonBottom = location[1] + btnDone.height

                val windowInsets = ViewCompat.getRootWindowInsets(rootView)
                val navBarHeight = windowInsets?.getInsets(
                    WindowInsetsCompat.Type.navigationBars()
                )?.bottom ?: 0

                val maxBottomY = screenHeight - navBarHeight + 50
                assertTrue(
                    "Done button bottom ($buttonBottom) should be above nav bar area",
                    buttonBottom <= maxBottomY || navBarHeight == 0
                )
            }
        }
    }

    @Test
    fun testSafeInsetsAppliedCorrectly() {
        ActivityScenario.launch(DebugRenderActivity::class.java).use { scenario ->
            scenario.onActivity { activity ->
                val topBar = activity.findViewById<View>(R.id.topBar)
                val bottomControls = activity.findViewById<View>(R.id.bottomControls)
                val rootView = activity.findViewById<View>(android.R.id.content)

                val windowInsets = ViewCompat.getRootWindowInsets(rootView)
                assertNotNull("Window insets should be available", windowInsets)

                val systemBarsInsets = windowInsets!!.getInsets(
                    WindowInsetsCompat.Type.systemBars()
                )

                println("=== DebugRenderActivity Safe Insets Test ===")
                println("Status bar height: ${systemBarsInsets.top}")
                println("Navigation bar height: ${systemBarsInsets.bottom}")
                println("Top bar padding: top=${topBar.paddingTop}")
                println("Bottom controls padding: bottom=${bottomControls.paddingBottom}")

                assertTrue(
                    "Top bar should have top padding >= status bar",
                    topBar.paddingTop >= systemBarsInsets.top
                )

                assertTrue(
                    "Bottom controls should have bottom padding >= nav bar",
                    bottomControls.paddingBottom >= systemBarsInsets.bottom
                )
            }
        }
    }
}
