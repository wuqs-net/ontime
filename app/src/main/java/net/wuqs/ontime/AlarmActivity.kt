package net.wuqs.ontime

import android.annotation.TargetApi
import android.app.KeyguardManager
import android.content.Context
import android.media.*
import android.os.Build
import android.os.Bundle
import android.support.v7.app.AppCompatActivity
import android.view.WindowManager.LayoutParams
import kotlinx.android.synthetic.main.activity_alarm.*
import net.wuqs.ontime.alarm.ALARM_INSTANCE
import net.wuqs.ontime.alarm.getDateString
import net.wuqs.ontime.alarm.getNextAlarmOccurrence
import net.wuqs.ontime.alarm.updateAlarm
import net.wuqs.ontime.db.Alarm

class AlarmActivity : AppCompatActivity() {

    private lateinit var alarm: Alarm
    private lateinit var alarmRingtone: Ringtone
    private lateinit var mediaPlayer: MediaPlayer

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_alarm)
        volumeControlStream = AudioManager.STREAM_ALARM

//        GetAlarmTask().execute()

        // Wake up phone when this activity is shown
        if (ApiUtil.isOMR1OrLater()) turnScreenOnOMR1() else turnScreenOnPreOMR1()
        if (ApiUtil.isOOrLater()) dismissKeyguardO() else dismissKeyguardPreO()

        alarm = intent.getParcelableExtra(ALARM_INSTANCE)
        startAlarm()
    }

    override fun onDestroy() {
        alarmRingtone.stop()
        super.onDestroy()
    }

    @Suppress("DEPRECATION")
    fun startAlarm() {
        alarmRingtone = RingtoneManager.getRingtone(this, alarm.ringtoneUri)
        if (ApiUtil.isLOrLater()) {
            val audioAttributes = AudioAttributes.Builder()
                    .setContentType(AudioAttributes.CONTENT_TYPE_MUSIC)
                    .setUsage(AudioAttributes.USAGE_ALARM)
                    .build()
            alarmRingtone.audioAttributes = audioAttributes
        } else {
            alarmRingtone.streamType = RingtoneManager.TYPE_ALARM
        }
        alarmRingtone.play()
        if (alarm.repeatType == 0) {
            alarm.isEnabled = false
            textView2.text = "闹钟标题: ${alarm.title}\n正在播放: ${alarmRingtone.getTitle(this)}\n按返回键关闭闹钟"
        } else {
            alarm.nextOccurrence = getNextAlarmOccurrence(alarm)
            textView2.text = "闹钟标题: ${alarm.title}\n正在播放: ${alarmRingtone.getTitle(this)}\n按返回键关闭闹钟\n下次响铃${getDateString(alarm.nextOccurrence)}"
        }
        updateAlarm(this, alarm)
    }

    @TargetApi(Build.VERSION_CODES.O_MR1)
    private fun turnScreenOnOMR1() {
        setTurnScreenOn(true)
        setShowWhenLocked(true)
    }

    @Suppress("DEPRECATION")
    private fun turnScreenOnPreOMR1() {
        window.addFlags(LayoutParams.FLAG_TURN_SCREEN_ON)
        window.addFlags(LayoutParams.FLAG_SHOW_WHEN_LOCKED)
    }

    @TargetApi(Build.VERSION_CODES.O)
    private fun dismissKeyguardO() {
        val keyguardManager = getSystemService(Context.KEYGUARD_SERVICE) as KeyguardManager
        keyguardManager.requestDismissKeyguard(this, null)
    }

    @Suppress("DEPRECATION")
    private fun dismissKeyguardPreO() {
        window.addFlags(LayoutParams.FLAG_DISMISS_KEYGUARD)
    }

}
