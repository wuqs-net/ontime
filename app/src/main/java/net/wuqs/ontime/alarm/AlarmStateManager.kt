package net.wuqs.ontime.alarm

import android.app.AlarmManager
import android.app.PendingIntent
import android.content.BroadcastReceiver
import android.content.Context
import android.content.Intent
import net.wuqs.ontime.AlarmActivity
import net.wuqs.ontime.MainActivity
import net.wuqs.ontime.db.Alarm
import net.wuqs.ontime.db.AppDatabase
import net.wuqs.ontime.utils.ApiUtil
import net.wuqs.ontime.utils.AsyncHandler
import net.wuqs.ontime.utils.LogUtils
import java.util.*


class AlarmStateManager : BroadcastReceiver() {

    override fun onReceive(context: Context, intent: Intent) {
        // This method is called when the BroadcastReceiver is receiving an Intent broadcast.
        LOGGER.i("Received intent: ${intent.action}")
        when (intent.action) {
            Intent.ACTION_BOOT_COMPLETED,
            Intent.ACTION_LOCKED_BOOT_COMPLETED,
            ACTION_SCHEDULE_ALL_ALARMS -> {
                val result = goAsync()
                AsyncHandler.post {
                    scheduleAllAlarms(context)
                    result.finish()
                }
            }
            ACTION_ALARM_START -> {
                val alarm = intent.getBundleExtra(ALARM_INSTANCE).getParcelable<Alarm>(ALARM_INSTANCE)
                LOGGER.i("Alarm started: $alarm")
                val myIntent = Intent(context, AlarmActivity::class.java).apply {
                    addFlags(Intent.FLAG_ACTIVITY_NEW_TASK)
                    putExtra(ALARM_INSTANCE, alarm)
                }
                context.startActivity(myIntent)
            }
        }
    }

    /**
     * Schedules all alarms in AlarmManager.
     */
    private fun scheduleAllAlarms(context: Context) {
        val db = AppDatabase[context]!!
        val alarms = db.alarmDAO.alarmsSync
        val now = Calendar.getInstance()
        val missedAlarms = mutableListOf<Alarm>()
        val needUpdate = alarms.filter { it.isEnabled }
        needUpdate.forEach {
            if (it.nextTime?.before(now) == true) {
                missedAlarms.add(Alarm(it))
            }
            it.nextTime = it.getNextOccurrence()
            if (it.nextTime != null) {
                scheduleAlarm(context, it)
            } else {
                it.isEnabled = false
            }
            Alarm.updateAlarm(db, it)
        }
        LOGGER.i("Finished scheduling all alarms")
    }

    companion object {
        const val ACTION_ALARM_START = "net.wuqs.ontime.action.ALARM_START"
        const val ACTION_SCHEDULE_ALL_ALARMS = "net.wuqs.ontime.action.SCHEDULE_ALL_ALARMS"

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