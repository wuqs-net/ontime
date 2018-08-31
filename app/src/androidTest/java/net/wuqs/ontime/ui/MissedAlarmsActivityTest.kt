package net.wuqs.ontime.ui

import android.content.Intent
import android.support.test.rule.ActivityTestRule
import android.support.test.runner.AndroidJUnit4
import net.wuqs.ontime.ui.feature.missedalarms.MissedAlarmsActivity
import org.junit.Rule
import org.junit.Test
import org.junit.runner.RunWith

@RunWith(AndroidJUnit4::class)
class MissedAlarmsActivityTest {

    @get:Rule
    val rule = ActivityTestRule<MissedAlarmsActivity>(MissedAlarmsActivity::class.java, true, false)

    @Test
    fun intent() {
        val intent = Intent()
        rule.launchActivity(intent)
    }
}