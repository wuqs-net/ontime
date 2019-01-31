package net.wuqs.ontime.ui

import android.content.Intent
import androidx.test.ext.junit.runners.AndroidJUnit4
import androidx.test.rule.ActivityTestRule
import net.wuqs.ontime.feature.missedalarms.MissedAlarmsActivity
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