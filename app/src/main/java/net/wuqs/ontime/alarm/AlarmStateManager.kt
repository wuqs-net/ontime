package net.wuqs.ontime.alarm

import android.app.AlarmManager
import android.app.PendingIntent
import android.content.BroadcastReceiver
import android.content.Context
import android.content.Intent
import net.wuqs.ontime.db.Alarm
import net.wuqs.ontime.db.AppDatabase
import net.wuqs.ontime.ui.alarmscreen.AlarmActivity
import net.wuqs.ontime.ui.mainscreen.MainActivity
import net.wuqs.ontime.util.ApiUtil
import net.wuqs.ontime.util.AsyncHandler
import net.wuqs.ontime.util.LogUtils
import java.util.*


class AlarmStateManager : BroadcastReceiver() {

    override fun onReceive(context: Context, intent: Intent) {
        // This method is called when the BroadcastReceiver is receiving an Intent broadcast.
        LOGGER.i("Received intent: ${intent.action}")
        when (intent.action) {
            Intent.ACTION_BOOT_COMPLETED,
//            Intent.ACTION_LOCKED_BOOT_COMPLETED,
            ACTION_SCHEDULE_ALL_ALARMS -> {
                val result = goAsync()
                AsyncHandler.post {
                    scheduleAllAlarms(context)
                    result.finish()
                }
            }
            ACTION_ALARM_START -> {
                val alarm = intent.getBundleExtra(ALARM_INSTANCE).getParcelable<Alarm>(ALARM_INSTANCE)
                if (alarm.isEnabled) {
                    LOGGER.i("Alarm started: $alarm")
                    val myIntent = Intent(context, AlarmActivity::class.java).apply {
                        addFlags(Intent.FLAG_ACTIVITY_NEW_TASK)
                        putExtra(ALARM_INSTANCE, alarm)
                    }
                    context.startActivity(myIntent)
                } else {
                    LOGGER.i("Disabled alarm triggered: $alarm")
                    alarm.snoozed = 0
                    alarm.nextTime = alarm.getNextOccurrence()
                    AlarmUpdateHandler(context).asyncUpdateAlarm(alarm)
                }
            }
            ACTION_SHOW_MISSED_ALARMS -> {
                LOGGER.i("Show missed alarms")
            }
        }
    }

    /**
     * Schedules all alarms in AlarmManager.
     */
    private fun scheduleAllAlarms(context: Context) {
        val db = AppDatabase[context]!!
        val alarms = db.alarmDAO.allSync
        val alarmsInUse = alarms.filter { it.nextTime != null }
        val missedAlarms = arrayListOf<Alarm>()
        val now = Calendar.getInstance()
        alarmsInUse.forEach {
            if (it.nextTime!!.before(now)) {
                missedAlarms += Alarm(it)
                it.nextTime = it.getNextOccurrence()
            }
            if (it.nextTime != null) {
                scheduleAlarm(context, it)
            } else {
                it.isEnabled = false
            }
            Alarm.updateAlarm(db, it)
        }
        LOGGER.i("Finished scheduling all alarms")

//        if (missedAlarms.isEmpty()) return
        val intent = createIntent(ACTION_SHOW_MISSED_ALARMS, context).apply {
            putExtra(EXTRA_MISSED_ALARMS, missedAlarms)
        }
        context.sendBroadcast(intent)
    }

    companion object {
        const val ACTION_ALARM_START = "net.wuqs.ontime.action.ALARM_START"
        const val ACTION_SCHEDULE_ALL_ALARMS = "net.wuqs.ontime.action.SCHEDULE_ALL_ALARMS"
        private const val ACTION_SHOW_MISSED_ALARMS = "net.wuqs.ontime.action.SHOW_MISSED_ALARMS"

        fun createIntent(action: String, context: Context) =
                Intent(action, null, context, AlarmStateManager::class.java)

        fun createScheduleAllAlarmsIntent(context: Context) =
                createIntent(AlarmStateManager.ACTION_SCHEDULE_ALL_ALARMS, context)

        fun scheduleAlarm(context: Context, alarm: Alarm) {
            val am = context.getSystemService(Context.ALARM_SERVICE) as AlarmManager

            val startAlarmIntent = alarm.createAlarmStartIntent(context)
//                    .apply {
//                        addFlags(Intent.FLAG_RECEIVER_FOREGROUND)
//                    }
            val operation = PendingIntent.getBroadcast(context, alarm.hashCode(),
                    startAlarmIntent, PendingIntent.FLAG_UPDATE_CURRENT)

            if (ApiUtil.isLOrLater()) {
                val viewIntent = PendingIntent.getActivity(context, alarm.hashCode(),
                        alarm.createIntent(context, MainActivity::class.java), 0)
                val info = AlarmManager.AlarmClockInfo(alarm.nextTime!!.timeInMillis, viewIntent)
                am.setAlarmClock(info, operation)
            } else {
                am.setExact(AlarmManager.RTC_WAKEUP, alarm.nextTime!!.timeInMillis, operation)
            }
            LOGGER.d("Alarm registered: $alarm")
        }

        fun cancelAlarm(context: Context, alarm: Alarm) {
            val operation = PendingIntent.getBroadcast(context, alarm.hashCode(),
                    alarm.createAlarmStartIntent(context), PendingIntent.FLAG_NO_CREATE)
            operation?.let {
                val am = context.getSystemService(Context.ALARM_SERVICE) as AlarmManager
                am.cancel(it)
                it.cancel()
                LOGGER.d("Alarm cancelled: $alarm")
            }
        }

        private val LOGGER = LogUtils.Logger("AlarmStateManager")
    }
}

private const val EXTRA_MISSED_ALARMS = "net.wuqs.ontime.extra.MISSED_ALARMS"