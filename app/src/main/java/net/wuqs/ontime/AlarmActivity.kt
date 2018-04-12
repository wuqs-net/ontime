package net.wuqs.ontime

import android.annotation.TargetApi
import android.app.KeyguardManager
import android.content.Context
import android.media.AudioAttributes
import android.media.AudioManager
import android.media.Ringtone
import android.media.RingtoneManager
import android.os.AsyncTask
import android.os.Build
import android.os.Bundle
import android.support.v7.app.AppCompatActivity
import android.util.Log
import android.view.WindowManager.LayoutParams
import kotlinx.android.synthetic.main.activity_alarm.*
import net.wuqs.ontime.alarm.ALARM_ID
import net.wuqs.ontime.alarm.ALARM_INSTANCE
import net.wuqs.ontime.db.Alarm
import net.wuqs.ontime.db.AppDatabase

class AlarmActivity : AppCompatActivity() {

    private lateinit var alarm: Alarm
    private lateinit var alarmRingtone: Ringtone

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_alarm)
        volumeControlStream = AudioManager.STREAM_ALARM

//        GetAlarmTask().execute()

        // Wake up phone when this activity is shown
        if (isOMR1OrLater()) turnScreenOnOMR1() else turnScreenOnPreOMR1()
        if (isOOrLater()) dismissKeyguardO() else dismissKeyguardPreO()

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
        if (isLOrLater()) {
            val audioAttributes = AudioAttributes.Builder()
                    .setContentType(AudioAttributes.CONTENT_TYPE_MUSIC)
                    .setUsage(AudioAttributes.USAGE_ALARM)
                    .build()
            alarmRingtone.audioAttributes = audioAttributes
        } else {
            alarmRingtone.streamType = RingtoneManager.TYPE_ALARM
        }
        alarmRingtone.play()
        textView2.text = "闹钟标题: ${alarm.title}\n正在播放: ${alarmRingtone.getTitle(this)}\n按返回键关闭闹钟"

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

//    private inner class GetAlarmTask : AsyncTask<Unit, Unit, Unit>() {
//        override fun doInBackground(vararg params: Unit?) {
//            val db = AppDatabase.getInstance(this@AlarmActivity)
//            val alarmId = intent.getIntExtra(ALARM_ID, -1)
//            Log.i("AlarmActivity", "Alarm id: " + alarmId.toString())
//            alarm = db.alarmDAO().loadAlarm(alarmId)
//        }
//
//        override fun onPostExecute(result: Unit?) {
//            startAlarm()
//        }
//    }
}
