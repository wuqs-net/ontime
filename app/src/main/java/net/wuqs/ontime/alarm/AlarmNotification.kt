package net.wuqs.ontime.alarm

import android.app.Notification
import android.app.NotificationChannel
import android.app.NotificationManager
import android.app.PendingIntent
import android.content.Context
import android.content.Intent
import android.os.Build
import androidx.core.app.NotificationCompat
import androidx.core.content.ContextCompat
import net.wuqs.ontime.R
import net.wuqs.ontime.db.Alarm
import net.wuqs.ontime.feature.currentalarm.AlarmActivity

/** Notification channel ID for starting alarm. */
const val CHANNEL_ALARM = "net.wuqs.ontime.channel.ALARM"

/** Notification channel ID for current alarm. */
const val CHANNEL_CURRENT_ALARM = "net.wuqs.ontime.channel.ONGOING_ALARM"

const val NOTIFICATION_ID_ALARM = 1

fun Context.createAlarmNotificationChannels() {
    if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
        val alarmChannel = NotificationChannel(
                CHANNEL_ALARM,
                getString(R.string.channel_name_alarm),
                NotificationManager.IMPORTANCE_HIGH
        ).apply {
            setSound(null, null)
            setShowBadge(false)
        }
        val currentAlarmChannel = NotificationChannel(
                CHANNEL_CURRENT_ALARM,
                getString(R.string.channel_name_current_alarm),
                NotificationManager.IMPORTANCE_DEFAULT
        ).apply {
            setSound(null, null)
            setShowBadge(false)
        }
        with(getSystemService(Context.NOTIFICATION_SERVICE) as NotificationManager) {
            createNotificationChannel(alarmChannel)
            createNotificationChannel(currentAlarmChannel)
        }
    }
}

/**
 * Builds a notification for a current alarm.
 *
 * @param context the Context to build the notification
 * @return a notification indicating the current alarm
 */
fun buildAlarmNotification(
    context: Context,
    channelId: String,
    alarm: Alarm,
    fullScreen: Boolean = false
): Notification {
    return NotificationCompat.Builder(context, channelId).apply {
        setSmallIcon(R.drawable.ic_stat_alarm)
        setContentTitle(alarm.getTitleOrDefault(context))
        setContentText(context.getString(R.string.msg_click_for_more_options,
                alarm.createTimeString(context)))
        setOngoing(true)
        setAutoCancel(false)
        setShowWhen(false)
        setCategory(NotificationCompat.CATEGORY_ALARM)
        setVisibility(NotificationCompat.VISIBILITY_PUBLIC)
        setLocalOnly(true)
        color = ContextCompat.getColor(context, R.color.colorPrimary)
        priority = NotificationCompat.PRIORITY_HIGH

        val contentIntent = Intent(context, AlarmActivity::class.java).apply {
            putExtra(EXTRA_ALARM_INSTANCE, alarm)
            flags = Intent.FLAG_ACTIVITY_NEW_TASK or Intent.FLAG_ACTIVITY_CLEAR_TASK
        }
        val contentPendingIntent = PendingIntent.getActivity(context, NOTIFICATION_ID_ALARM,
                contentIntent, PendingIntent.FLAG_UPDATE_CURRENT)
        setContentIntent(contentPendingIntent)

        val dismissIntent = Intent(context, AlarmService::class.java).apply {
            action = ACTION_ALARM_DISMISS
        }
        val dismissPendingIntent = PendingIntent.getService(context, NOTIFICATION_ID_ALARM,
                dismissIntent, PendingIntent.FLAG_UPDATE_CURRENT)
        addAction(R.drawable.ic_alarm_off_black_24dp,
                context.getString(R.string.action_dismiss_alarm), dismissPendingIntent)

        val snoozeIntent = Intent(context, AlarmService::class.java).apply {
            action = ACTION_ALARM_SHOW_SNOOZE_OPTIONS
//            putExtra(EXTRA_ALARM_INSTANCE, alarm)
        }
        val snoozePendingIntent = PendingIntent.getService(context, NOTIFICATION_ID_ALARM,
                snoozeIntent, PendingIntent.FLAG_UPDATE_CURRENT)
        addAction(R.drawable.ic_arrow_right,
                context.getString(R.string.action_delay_alarm), snoozePendingIntent)

        if (fullScreen) {
            val fullScreenIntent = Intent(context, AlarmActivity::class.java).apply {
                flags = (Intent.FLAG_ACTIVITY_NEW_TASK or Intent.FLAG_ACTIVITY_CLEAR_TASK
                        or Intent.FLAG_ACTIVITY_NO_USER_ACTION)
                putExtra(EXTRA_ALARM_INSTANCE, alarm)
            }
            setFullScreenIntent(PendingIntent.getActivity(context, NOTIFICATION_ID_ALARM,
                    fullScreenIntent, PendingIntent.FLAG_UPDATE_CURRENT), true)
        }
    }.build()
}