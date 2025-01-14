package openfoodfacts.github.scrachx.openfood

import androidx.test.ext.junit.runners.AndroidJUnit4
import openfoodfacts.github.scrachx.openfood.features.scan.ContinuousScanActivity
import openfoodfacts.github.scrachx.openfood.test.ScreenshotActivityTestRule
import org.junit.Rule
import org.junit.Test
import org.junit.runner.RunWith

/**
 * Take screenshots...
 */
@RunWith(AndroidJUnit4::class)
class TakeScreenshotScanActivityTest : AbstractScreenshotTest() {
    @Rule
    var activityRule = ScreenshotActivityTestRule(ContinuousScanActivity::class.java)
    @Test
    fun testTakeScreenshotScanActivity() {
        activityRule.afterActivityLaunchedAction = { screenshotActivityTestRule ->
            try {
                screenshotActivityTestRule.runOnUiThread {
                    val barcode = screenshotActivityTestRule.screenshotParameter!!.productCodes[0]
                    screenshotActivityTestRule.activity!!.showProduct(barcode)
                }
                Thread.sleep(MS_TO_WAIT_TO_DISPLAY_PRODUCT_IN_SCAN.toLong())
            } catch (throwable: Throwable) {
                throwable.printStackTrace()
            }
        }
        startForAllLocales(rules = listOf(activityRule))
    }

    companion object {
        const val MS_TO_WAIT_TO_DISPLAY_PRODUCT_IN_SCAN = 2000
    }
}