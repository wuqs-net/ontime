package net.wuqs.ontime.ui.alarmscreen

import android.annotation.TargetApi
import android.app.KeyguardManager
import android.content.Context
import android.media.*
import android.os.Build
import android.os.Bundle
import android.support.v7.app.AppCompatActivity
import android.view.View
import android.view.WindowManager.LayoutParams
import kotlinx.android.synthetic.main.activity_alarm.*
import net.wuqs.ontime.R
import net.wuqs.ontime.alarm.*
import net.wuqs.ontime.db.Alarm
import net.wuqs.ontime.util.ApiUtil
import net.wuqs.ontime.util.LogUtils

class AlarmActivity : AppCompatActivity(), View.OnClickListener {

    private lateinit var alarm: Alarm

    private lateinit var mediaPlayer: MediaPlayer

    private lateinit var mAlarmUpdateHandler: AlarmUpdateHandler

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_alarm)
        volumeControlStream = AudioManager.STREAM_ALARM

        mAlarmUpdateHandler = AlarmUpdateHandler(applicationContext)
        mediaPlayer = MediaPlayer()

        // Wake up phone when this activity is launched
        if (ApiUtil.isOMR1OrLater()) turnScreenOnOMR1() else turnScreenOnPreOMR1()
        if (ApiUtil.isOOrLater()) dismissKeyguardO() else dismissKeyguardPreO()

        alarm = intent.getParcelableExtra(ALARM_INSTANCE)
        startAlarm()

        btnDismiss.setOnClickListener(this)
        alarmTitle.text = alarm.title
    }

    override fun onClick(v: View?) {
        when (v) {
            btnDismiss -> stopAlarm()
        }
    }

    // Prevent quit by pressing Back
    override fun onBackPressed() {
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

        alarm.getNextOccurrence().let {
            if (it == null) {
                alarm.isEnabled = false
                next_time.visibility = View.GONE
            } else {
                alarm.nextTime = it
                next_time.visibility = View.VISIBLE
                next_time.text = getString(R.string.msg_next_date, getDateString(it, false))
            }
        }

//        mAlarmUpdateHandler.asyncUpdateAlarm(alarm)
    }

    private fun stopAlarm() {
        mediaPlayer.stop()
        mediaPlayer.release()
        mLogger.i("Alarm finished: $alarm")
        mAlarmUpdateHandler.asyncUpdateAlarm(alarm)
        finish()
    }

    /**
     * Turns on the screen. Used on Android versions from O_MR1.
     */
    @TargetApi(Build.VERSION_CODES.O_MR1)
    private fun turnScreenOnOMR1() {
        setTurnScreenOn(true)
        setShowWhenLocked(true)
    }

    /**
     * Turns on the screen. Used on Android versions prior to O_MR1.
     */
    @Suppress("DEPRECATION")
    private fun turnScreenOnPreOMR1() = window.run {
        addFlags(LayoutParams.FLAG_TURN_SCREEN_ON)
        addFlags(LayoutParams.FLAG_SHOW_WHEN_LOCKED)
    }

    @TargetApi(Build.VERSION_CODES.O)
    private fun dismissKeyguardO() {
        val keyguardManager = getSystemService(Context.KEYGUARD_SERVICE) as KeyguardManager
        keyguardManager.requestDismissKeyguard(this, null)
    }

    @Suppress("DEPRECATION")
    private fun dismissKeyguardPreO() = window.run {
        addFlags(LayoutParams.FLAG_DISMISS_KEYGUARD)
    }

    private val mLogger = LogUtils.Logger("AlarmActivity")
}
