package net.wuqs.ontime.feature.currentalarm

import android.annotation.TargetApi
import android.app.KeyguardManager
import android.content.Context
import android.media.AudioAttributes
import android.media.AudioManager
import android.media.MediaPlayer
import android.os.Build
import android.os.Bundle
import android.support.v7.app.AppCompatActivity
import android.view.View
import android.view.WindowManager.LayoutParams
import kotlinx.android.synthetic.main.activity_alarm.*
import net.wuqs.ontime.R
import net.wuqs.ontime.alarm.ALARM_INSTANCE
import net.wuqs.ontime.alarm.AlarmUpdateHandler
import net.wuqs.ontime.alarm.getDateString
import net.wuqs.ontime.db.Alarm
import net.wuqs.ontime.util.AlarmWakeLock
import net.wuqs.ontime.util.ApiUtil
import net.wuqs.ontime.util.LogUtils
import net.wuqs.ontime.util.getCustomTaskDescription
import java.util.*

class AlarmActivity : AppCompatActivity(), DelayOptionFragment.DelayOptionPickListener {

    private lateinit var alarm: Alarm

    private lateinit var mediaPlayer: MediaPlayer

    private lateinit var mAlarmUpdateHandler: AlarmUpdateHandler

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_alarm)

        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.LOLLIPOP) {
            setTaskDescription(getCustomTaskDescription())
        }

        mLogger.v("onCreate")

        AlarmWakeLock.acquireCpuWakeLock(this)
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

    override fun onDelayOptionPick(quantity: Int, unit: Int) {
        val newQuantity: Int
        val newUnit: Int
        if (unit == Calendar.WEEK_OF_YEAR) {
            newQuantity = 7 * quantity
            newUnit = Calendar.DATE
        } else {
            newQuantity = quantity
            newUnit = unit
        }
        alarm.nextTime = Calendar.getInstance().apply { add(newUnit, newQuantity) }
        alarm.snoozed += 1
        alarm.isEnabled = true
        stopAlarm()
    }

    private fun startAlarm() {
        if (ApiUtil.isLOrLater()) {
            val audioAttributes = AudioAttributes.Builder()
                    .setContentType(AudioAttributes.CONTENT_TYPE_MUSIC)
                    .setUsage(AudioAttributes.USAGE_ALARM)
                    .build()
            mediaPlayer.setAudioAttributes(audioAttributes)
        } else {
            @Suppress("DEPRECATION")
            mediaPlayer.setAudioStreamType(AudioManager.STREAM_ALARM)
        }
        mediaPlayer.setDataSource(applicationContext, alarm.ringtoneUri)
        mediaPlayer.isLooping = true
        mediaPlayer.prepare()
        mediaPlayer.start()
        AlarmRinger.start(this, alarm)

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
        alarm.snoozed = 0
        stopAlarm()
    }

    private fun stopAlarm() {
        AlarmRinger.stop(this)
        mediaPlayer.stop()
        mediaPlayer.reset()
        mediaPlayer.release()
        mLogger.i("Alarm finished: $alarm")
        mAlarmUpdateHandler.asyncUpdateAlarm(alarm)
        AlarmWakeLock.releaseCpuLock()
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
