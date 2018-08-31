package net.wuqs.ontime.ui.feature.alarm

import android.content.Context
import android.media.AudioAttributes
import android.os.Build
import android.os.VibrationEffect
import android.os.Vibrator
import android.support.annotation.RequiresApi
import net.wuqs.ontime.db.Alarm
import net.wuqs.ontime.util.LogUtils

object AlarmRinger {

    private val VIBRATE_TIMINGS = longArrayOf(500, 500)

    private var isStarted = false

    fun start(context: Context, alarm: Alarm) {
        stop(context)
        logger.v("start(), ringtone=${alarm.ringtoneUri}, vibrate=${alarm.vibrate}")
        isStarted = true

        if (alarm.vibrate) {
            val vibrator = context.getSystemService(Context.VIBRATOR_SERVICE) as Vibrator
            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
                vibrateO(vibrator)
            } else if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.LOLLIPOP) {
                vibrateL(vibrator)
            } else {
                vibrateK(vibrator)
            }
        }
    }

    fun stop(context: Context) {
        if (isStarted) {
            logger.v("stop()")
            val vibrator = context.getSystemService(Context.VIBRATOR_SERVICE) as Vibrator
            vibrator.cancel()
            isStarted = false
        }
    }

    @RequiresApi(Build.VERSION_CODES.O)
    private fun vibrateO(vibrator: Vibrator) {
        val vibe = VibrationEffect.createWaveform(VIBRATE_TIMINGS, 0)
        val attrs = AudioAttributes.Builder().run {
            setContentType(AudioAttributes.CONTENT_TYPE_SONIFICATION)
            setUsage(AudioAttributes.USAGE_ALARM)
            build()
        }
        vibrator.vibrate(vibe, attrs)
    }

    @Suppress("DEPRECATION")
    @RequiresApi(Build.VERSION_CODES.LOLLIPOP)
    private fun vibrateL(vibrator: Vibrator) {
        val attrs = AudioAttributes.Builder().run {
            setContentType(AudioAttributes.CONTENT_TYPE_SONIFICATION)
            setUsage(AudioAttributes.USAGE_ALARM)
            build()
        }
        vibrator.vibrate(VIBRATE_TIMINGS, 0, attrs)
    }

    @Suppress("DEPRECATION")
    private fun vibrateK(vibrator: Vibrator) {
        vibrator.vibrate(VIBRATE_TIMINGS, 0)
    }

    private val logger = LogUtils.Logger("AlarmRinger")
}