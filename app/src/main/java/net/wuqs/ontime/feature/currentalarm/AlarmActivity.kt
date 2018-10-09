package net.wuqs.ontime.feature.currentalarm

import android.annotation.TargetApi
import android.app.KeyguardManager
import android.content.BroadcastReceiver
import android.content.Context
import android.content.Intent
import android.content.IntentFilter
import android.media.AudioManager
import android.media.MediaPlayer
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
import net.wuqs.ontime.util.LogUtils
import net.wuqs.ontime.util.getCustomTaskDescription
import java.util.*

class AlarmActivity : AppCompatActivity(), DelayOptionFragment.DelayOptionPickListener {

    private lateinit var alarm: Alarm

    private lateinit var mediaPlayer: MediaPlayer

    private lateinit var mAlarmUpdateHandler: AlarmUpdateHandler

    private val receiver = object : BroadcastReceiver() {
        override fun onReceive(context: Context?, intent: Intent?) {
            if (intent == null) return
            mLogger.v("onReceive() ${intent.action}")
            when (intent.action) {
                ACTION_ALARM_SNOOZE -> stopAlarm()
            }
        }
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_alarm)

        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.LOLLIPOP) {
            setTaskDescription(getCustomTaskDescription())
        }

        mLogger.v("onCreate")

        val filter = IntentFilter().apply {
            addAction(ACTION_ALARM_DISMISS)
            addAction(ACTION_ALARM_SNOOZE)
        }
        LocalBroadcastManager.getInstance(this).registerReceiver(receiver, filter)

        volumeControlStream = AudioManager.STREAM_ALARM

        mAlarmUpdateHandler = AlarmUpdateHandler(applicationContext)
        mediaPlayer = MediaPlayer()

        // Wake up phone when this activity is launched
//        if (ApiUtil.isOMR1OrLater()) turnScreenOnOMR1() else turnScreenOnPreOMR1()
//        if (ApiUtil.isOOrLater()) dismissKeyguardO() else dismissKeyguardPreO()
        window.addFlags(LayoutParams.FLAG_KEEP_SCREEN_ON
                or LayoutParams.FLAG_TURN_SCREEN_ON
                or LayoutParams.FLAG_SHOW_WHEN_LOCKED
                or LayoutParams.FLAG_DISMISS_KEYGUARD
                or LayoutParams.FLAG_ALLOW_LOCK_WHILE_SCREEN_ON)

        alarm = intent.getParcelableExtra(ALARM_INSTANCE)
        startAlarm()

        btn_delay.setOnClickListener { delayAlarm() }
        btn_dismiss.setOnClickListener { dismissAlarm() }
        tv_alarm_title.text = alarm.title
        tv_alarm_notes.text = alarm.notes
    }

    // Prevent quit by pressing Back
    override fun onBackPressed() {
    }

    override fun onDestroy() {
        super.onDestroy()

        LocalBroadcastManager.getInstance(this).unregisterReceiver(receiver)
    }

    override fun onDelayOptionPick(quantity: Int, unit: Int) {
        alarm.nextTime = Calendar.getInstance().apply { add(quantity, unit) }
        alarm.snoozed += 1
        alarm.isEnabled = true
        stopAlarm()
    }

    private fun startAlarm() {

        alarm.getNextOccurrence().let {
            alarm.nextTime = it
            if (it == null) {
                alarm.isEnabled = false
                tv_next_date.visibility = View.GONE
            } else {
                tv_next_date.visibility = View.VISIBLE
                tv_next_date.text = getString(R.string.msg_next_date, getDateString(it, false))
            }
        }

//        mAlarmUpdateHandler.asyncUpdateAlarm(alarm)
    }

    private fun delayAlarm() {
        DelayOptionFragment.newInstance(alarm.nextTime).show(supportFragmentManager, null)
    }

    private fun dismissAlarm() {
        stopAlarm()
        LocalBroadcastManager.getInstance(this).sendBroadcast(Intent(ACTION_ALARM_DISMISS))
    }

    private fun stopAlarm() {
        mLogger.i("Alarm finished: $alarm")
        mAlarmUpdateHandler.asyncUpdateAlarm(alarm)
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.LOLLIPOP) {
            finishAndRemoveTask()
        } else {
            finish()
        }
    }

    /**
     * Turns on the screen. Used on Android versions from O_MR1.
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

    private val mLogger = LogUtils.Logger("AlarmActivity")
}
