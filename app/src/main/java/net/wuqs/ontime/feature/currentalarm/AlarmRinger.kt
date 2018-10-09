package net.wuqs.ontime.feature.currentalarm

import android.content.Context
import android.media.AudioAttributes
import android.media.AudioManager
import android.media.MediaPlayer
import android.os.Build
import android.os.VibrationEffect
import android.os.Vibrator
import android.support.annotation.RequiresApi
import net.wuqs.ontime.db.Alarm
import net.wuqs.ontime.util.ApiUtil
import net.wuqs.ontime.util.LogUtils

object AlarmRinger {

    private val VIBRATE_TIMINGS = longArrayOf(500, 500)

    private var isStarted = false

    private var mediaPlayer: MediaPlayer? = null

    fun start(context: Context, alarm: Alarm) {
        if (isStarted) stop(context)
        mediaPlayer = MediaPlayer()
        if (ApiUtil.isLOrLater()) {
            val audioAttributes = AudioAttributes.Builder()
                    .setContentType(AudioAttributes.CONTENT_TYPE_MUSIC)
                    .setUsage(AudioAttributes.USAGE_ALARM)
                    .build()
            mediaPlayer!!.setAudioAttributes(audioAttributes)
        } else {
            @Suppress("DEPRECATION")
            mediaPlayer!!.setAudioStreamType(AudioManager.STREAM_ALARM)
        }
        mediaPlayer?.isLooping = true

        logger.v("start(), ringtone=${alarm.ringtoneUri}, vibrate=${alarm.vibrate}")
        isStarted = true

        mediaPlayer!!.setDataSource(context, alarm.ringtoneUri)
        mediaPlayer!!.prepare()
        mediaPlayer!!.start()

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
            mediaPlayer!!.run {
                stop()
                reset()
                release()
            }
            mediaPlayer = null
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