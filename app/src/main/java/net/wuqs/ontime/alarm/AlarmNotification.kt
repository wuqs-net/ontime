package net.wuqs.ontime.alarm

import android.app.NotificationChannel
import android.app.NotificationManager
import android.app.PendingIntent
import android.app.Service
import android.content.Context
import android.content.Intent
import android.os.Build
import android.support.v4.app.NotificationCompat
import android.support.v4.content.ContextCompat
import net.wuqs.ontime.R
import net.wuqs.ontime.db.Alarm
import net.wuqs.ontime.feature.currentalarm.AlarmActivity

/** Channel ID for alarm notifications. */
const val CHANNEL_ALARM = "net.wuqs.ontime.channel.ALARM"

const val NOTIFICATION_ID_ALARM = 1

fun Context.createAlarmNotificationChannel() {
    if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
        val name = getString(R.string.channel_name_alarm)
        val importance = NotificationManager.IMPORTANCE_HIGH
        val channel = NotificationChannel(CHANNEL_ALARM, name, importance).apply {
            setSound(null, null)
            setShowBadge(false)
        }
        with(getSystemService(Context.NOTIFICATION_SERVICE) as NotificationManager) {
            createNotificationChannel(channel)
        }
    }
}

/**
 * Creates a notification for `this` firing alarm.
 *
 * @param service to build the notification
 * @return a notification indicating `this` alarm.
 */
fun showAlarmStartNotification(service: Service, alarm: Alarm) {
    val notification = NotificationCompat.Builder(service, CHANNEL_ALARM).apply {
        setSmallIcon(R.drawable.ic_stat_alarm)
        setContentTitle(alarm.getTitleOrDefault(service))
        setContentText(service.getString(R.string.msg_click_for_more_options,
                alarm.createTimeString(service)))
        setOngoing(true)
        setAutoCancel(false)
        setShowWhen(false)
        setCategory(NotificationCompat.CATEGORY_ALARM)
        setVisibility(NotificationCompat.VISIBILITY_PUBLIC)
        setLocalOnly(true)
        color = ContextCompat.getColor(service, R.color.colorPrimary)
        priority = NotificationCompat.PRIORITY_HIGH

        val contentIntent = Intent(service, AlarmActivity::class.java).apply {
            putExtra(EXTRA_ALARM_INSTANCE, alarm)
        }
        val contentPendingIntent = PendingIntent.getActivity(service, NOTIFICATION_ID_ALARM,
                contentIntent, PendingIntent.FLAG_UPDATE_CURRENT)
        setContentIntent(contentPendingIntent)

        val dismissIntent = Intent(service, AlarmService::class.java).apply {
            action = ACTION_ALARM_DISMISS
        }
        val dismissPendingIntent = PendingIntent.getService(service, NOTIFICATION_ID_ALARM,
                dismissIntent, PendingIntent.FLAG_UPDATE_CURRENT)
        addAction(R.drawable.ic_alarm_off_black_24dp,
                service.getString(R.string.action_dismiss_alarm), dismissPendingIntent)

//        val snoozeIntent = Intent(service, AlarmService::class.java).apply {
//            action = ACTION_ALARM_SNOOZE
//        }
//        val snoozePendingIntent = PendingIntent.getBroadcast(service, NOTIFICATION_ID_ALARM,
//                snoozeIntent, PendingIntent.FLAG_UPDATE_CURRENT)
//        addAction(R.drawable.ic_arrow_right,
//                service.getString(R.string.action_delay_alarm), snoozePendingIntent)

        val fullScreenIntent = Intent(service, AlarmActivity::class.java).apply {
            flags = Intent.FLAG_ACTIVITY_NEW_TASK or Intent.FLAG_ACTIVITY_NO_USER_ACTION
            putExtra(EXTRA_ALARM_INSTANCE, alarm)
        }
        setFullScreenIntent(PendingIntent.getActivity(service, NOTIFICATION_ID_ALARM,
                fullScreenIntent, PendingIntent.FLAG_UPDATE_CURRENT), true)
    }.build()

    service.startForeground(NOTIFICATION_ID_ALARM, notification)
}