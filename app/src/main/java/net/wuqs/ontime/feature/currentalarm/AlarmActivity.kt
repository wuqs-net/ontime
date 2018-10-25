package net.wuqs.ontime.feature.currentalarm

import android.annotation.TargetApi
import android.app.KeyguardManager
import android.content.BroadcastReceiver
import android.content.Context
import android.content.Intent
import android.content.IntentFilter
import android.media.AudioManager
import android.os.Build
import android.os.Bundle
import android.support.v4.content.LocalBroadcastManager
import android.support.v7.app.AppCompatActivity
import android.view.View
import android.view.WindowManager.LayoutParams
import kotlinx.android.synthetic.main.activity_alarm.*
import net.wuqs.ontime.R
import net.wuqs.ontime.alarm.*
import net.wuqs.ontime.db.Alarm
import net.wuqs.ontime.util.Logger
import net.wuqs.ontime.util.changeTaskDescription
import java.util.*

class AlarmActivity : AppCompatActivity(), DelayOptionFragment.DelayOptionListener {

    private lateinit var alarm: Alarm

    private val logger = Logger("AlarmActivity")

    private val receiver = object : BroadcastReceiver() {
        override fun onReceive(context: Context?, intent: Intent?) {
            logger.v("onReceive(action=${intent?.action})")
            when (intent?.action) {
                ACTION_ALARM_DONE -> finishAlarm()
            }
        }
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_alarm)

        changeTaskDescription()

        volumeControlStream = AudioManager.STREAM_ALARM

        // Wake up phone when this activity is launched
//        if (ApiUtil.isOMR1OrLater()) turnScreenOnOMR1() else turnScreenOnPreOMR1()
//        if (ApiUtil.isOOrLater()) dismissKeyguardO() else dismissKeyguardPreO()
        window.addFlags(LayoutParams.FLAG_KEEP_SCREEN_ON
                or LayoutParams.FLAG_TURN_SCREEN_ON
                or LayoutParams.FLAG_SHOW_WHEN_LOCKED
                or LayoutParams.FLAG_DISMISS_KEYGUARD
                or LayoutParams.FLAG_ALLOW_LOCK_WHILE_SCREEN_ON)

        alarm = intent.getParcelableExtra(EXTRA_ALARM_INSTANCE)

        btn_delay.setOnClickListener { delayAlarm() }
        btn_dismiss.setOnClickListener { dismissAlarm() }
        tv_alarm_title.text = alarm.title
        tv_alarm_notes.text = alarm.notes

        alarm.nextTime.let {
            if (it == null) {
                tv_next_date.visibility = View.GONE
            } else {
                tv_next_date.visibility = View.VISIBLE
                tv_next_date.text = getString(R.string.msg_next_date, it.createDateString(false))
            }
        }

        val intentFilter = IntentFilter(ACTION_ALARM_DONE)
        LocalBroadcastManager.getInstance(this).registerReceiver(receiver, intentFilter)
    }

    override fun onDelayOptionClick(quantity: Int, unit: Int) {
        val interval = if (unit == Calendar.WEEK_OF_YEAR) {
            intArrayOf((7 * quantity), Calendar.DATE)
        } else {
            intArrayOf(quantity, unit)
        }
        val intent = Intent(ACTION_ALARM_SNOOZE).apply {
            putExtra(EXTRA_SNOOZE_INTERVAL, interval)
        }
        LocalBroadcastManager.getInstance(this).sendBroadcast(intent)
    }

    private fun delayAlarm() {
        DelayOptionFragment.newInstance(alarm.nextTime).show(supportFragmentManager, null)
    }

    private fun dismissAlarm() {
        val intent = Intent(ACTION_ALARM_DISMISS)
        LocalBroadcastManager.getInstance(this).sendBroadcast(intent)
    }

    private fun finishAlarm() {
        // TODO: Add animations.
        finish()
    }

    /**
     * Turns on the screen. Used on Android versions from O MR1.
     */
    @TargetApi(Build.VERSION_CODES.O_MR1)
    private fun turnScreenOnOMR1() {
        window.addFlags(LayoutParams.FLAG_KEEP_SCREEN_ON
                or LayoutParams.FLAG_ALLOW_LOCK_WHILE_SCREEN_ON)
        setTurnScreenOn(true)
        setShowWhenLocked(true)
    }

    /**
     * Turns on the screen. Used on Android versions prior to O_MR1.
     */
    @Suppress("DEPRECATION")
    private fun turnScreenOnPreOMR1() {
        window.addFlags(LayoutParams.FLAG_TURN_SCREEN_ON
                or LayoutParams.FLAG_KEEP_SCREEN_ON
                or LayoutParams.FLAG_SHOW_WHEN_LOCKED
                or LayoutParams.FLAG_ALLOW_LOCK_WHILE_SCREEN_ON)
    }

    @TargetApi(Build.VERSION_CODES.O)
    private fun dismissKeyguardO() {
        val km = getSystemService(Context.KEYGUARD_SERVICE) as KeyguardManager
        km.requestDismissKeyguard(this, null)
    }


    @Suppress("DEPRECATION")
    private fun dismissKeyguardPreO() {
        window.addFlags(LayoutParams.FLAG_DISMISS_KEYGUARD)
    }

    // Prevent quit by pressing Back
    override fun onBackPressed() {
    }

    override fun onDestroy() {
        super.onDestroy()

        LocalBroadcastManager.getInstance(this).unregisterReceiver(receiver)
    }
}
