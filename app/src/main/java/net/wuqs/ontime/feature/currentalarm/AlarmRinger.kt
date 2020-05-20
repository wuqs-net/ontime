package net.wuqs.ontime.feature.currentalarm

import android.content.Context
import android.media.AudioAttributes
import android.media.AudioManager
import android.media.MediaPlayer
import android.media.RingtoneManager
import android.os.Build
import android.os.VibrationEffect
import android.os.Vibrator
import androidx.annotation.RequiresApi
import net.wuqs.ontime.db.Alarm
import net.wuqs.ontime.util.AsyncHandler
import net.wuqs.ontime.util.Logger
import java.io.IOException

object AlarmRinger {

    private val VIBRATE_TIMINGS = longArrayOf(500, 500)

    private var isStarted = false

    private var mediaPlayer: MediaPlayer? = null

    private val logger = Logger("AlarmRinger")

    /**
     * Start the ringtone and vibration of an Alarm.
     *
     * @param context to start the ringtone and vibration
     * @param alarm for which to play the ringtone and vibrate
     */
    @JvmStatic
    fun start(context: Context, alarm: Alarm): Int? {
        if (isStarted) stop(context)

        logger.v("start(), ringtone=${alarm.ringtoneUri}, vibrate=${alarm.vibrate}")
        isStarted = true

        // Start MediaPlayer only if ringtone is not null.
        alarm.ringtoneUri?.let {
            if (mediaPlayer == null) mediaPlayer = MediaPlayer()
            logger.v("MediaPlayer is created")
            val mp = this.mediaPlayer!!
            try {
                mp.setDataSource(context, it)
            } catch (e: IOException) {
                mp.setDataSource(context, RingtoneManager.getDefaultUri(RingtoneManager.TYPE_ALARM))
                logger.e("Fail to play ringtone: $it")
            }
            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.LOLLIPOP) {
                val audioAttributes = AudioAttributes.Builder()
                        .setContentType(AudioAttributes.CONTENT_TYPE_MUSIC)
                        .setUsage(AudioAttributes.USAGE_ALARM)
                        .build()
                mp.setAudioAttributes(audioAttributes)
            } else {
                @Suppress("DEPRECATION")
                mp.setAudioStreamType(AudioManager.STREAM_ALARM)
            }
            mp.prepare()
            if (alarm.silenceAfter == -2) {
                AsyncHandler.postDelayed(mp.duration.toLong() + 200) {
                    stop(context)
                }
            } else if (alarm.silenceAfter > 0) {
                AsyncHandler.postDelayed(alarm.silenceAfter.toLong()) {
                    stop(context)
                }
            }
            mp.isLooping = alarm.silenceAfter != -2
            mp.start()
        }

        if (alarm.vibrate) {
            val vibrator = context.getSystemService(Context.VIBRATOR_SERVICE) as Vibrator
            when {
                Build.VERSION.SDK_INT >= Build.VERSION_CODES.O -> vibrateO(vibrator)
                Build.VERSION.SDK_INT >= Build.VERSION_CODES.LOLLIPOP -> vibrateL(vibrator)
                else -> vibrateK(vibrator)
            }
        }
        return mediaPlayer?.duration
        // TODO: Null ringtone
    }

    /**
     * Stop the ringtone and vibration of an Alarm.
     *
     * @param context to stop the ringtone and vibration
     */
    @JvmStatic
    fun stop(context: Context) {
        if (isStarted) {
            logger.v("stop()")
            mediaPlayer?.run {
                if (isPlaying) stop()
                reset()
                release()
                logger.v("MediaPlayer is released")
            }
            mediaPlayer = null
            val vibrator = context.getSystemService(Context.VIBRATOR_SERVICE) as Vibrator
            vibrator.cancel()
            isStarted = false
        }
    }

    @JvmStatic
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
    @JvmStatic
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
    @JvmStatic
    private fun vibrateK(vibrator: Vibrator) {
        vibrator.vibrate(VIBRATE_TIMINGS, 0)
    }
}