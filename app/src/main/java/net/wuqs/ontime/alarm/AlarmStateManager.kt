package net.wuqs.ontime.alarm

import android.app.AlarmManager
import android.app.PendingIntent
import android.content.BroadcastReceiver
import android.content.Context
import android.content.Intent
import android.support.v4.content.LocalBroadcastManager
import net.wuqs.ontime.db.Alarm
import net.wuqs.ontime.db.AppDatabase
import net.wuqs.ontime.db.updateAlarmToDb
import net.wuqs.ontime.feature.currentalarm.AlarmActivity
import net.wuqs.ontime.util.AlarmWakeLock
import net.wuqs.ontime.util.ApiUtil
import net.wuqs.ontime.util.AsyncHandler
import net.wuqs.ontime.util.LogUtils
import java.util.*
import kotlin.collections.ArrayList


class AlarmStateManager : BroadcastReceiver() {

    private val ACTION_BOOT_COMPLETED = if (ApiUtil.isNOrLater()) {
        Intent.ACTION_LOCKED_BOOT_COMPLETED
    } else {
        Intent.ACTION_BOOT_COMPLETED
    }

    override fun onReceive(context: Context, intent: Intent) {
        // This method is called when the BroadcastReceiver is receiving an Intent broadcast.
        LOGGER.i("Received intent: ${intent.action}")
        val result = goAsync()
        val wl = AlarmWakeLock.createPartialWakeLock(context)
        wl.acquire()
        AsyncHandler.post {
            handleIntent(context, intent)
            result.finish()
            wl.release()
        }
    }

    private fun handleIntent(context: Context, intent: Intent) {
        when (intent.action) {
            ACTION_BOOT_COMPLETED -> scheduleAllAlarms(context, true)
            ACTION_SCHEDULE_ALL_ALARMS -> scheduleAllAlarms(context, false)
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
//            ACTION_SHOW_MISSED_ALARMS -> {
//                LOGGER.i("Show missed alarms")
//                val mIntent = Intent(context, MissedAlarmsActivity::class.java).apply {
//                    putExtras(intent)
//                }
//                context.startActivity(mIntent)
//            }
            ACTION_DISMISS_ALL_MISSED_ALARMS -> {
                dismissAllMissedAlarms(context,
                        intent.getParcelableArrayListExtra(EXTRA_MISSED_ALARMS))
            }
        }
    }

    /**
     * Schedules all alarms in AlarmManager.
     */
    private fun scheduleAllAlarms(context: Context, onBoot: Boolean) {
        val db = AppDatabase[context]!!
        val alarms = db.alarmDao.alarmsHasNextTime
        val now = Calendar.getInstance()
        val (notMissed, missed) = alarms.partition { it.nextTime!!.after(now) }
        LOGGER.d("Not missed: ${notMissed.joinToString()}")
        LOGGER.d("Missed: ${missed.joinToString()}")
        notMissed.forEach {
            if (it.snoozed != 0 && it.nextTime == it.getNextOccurrence()) it.snoozed = 0
            scheduleAlarm(context, it)
        }
        LOGGER.i("Finished scheduling all alarms")

        if (missed.isEmpty()) return
        val intent = Intent(ACTION_SHOW_MISSED_ALARMS).apply {
            putParcelableArrayListExtra(EXTRA_MISSED_ALARMS, ArrayList(missed))
            putExtra(EXTRA_ON_BOOT, onBoot)
        }
        val lbm = LocalBroadcastManager.getInstance(context)
        lbm.sendBroadcast(intent)
    }

    private fun dismissAllMissedAlarms(context: Context, alarms: List<Alarm>) {
        val db = AppDatabase[context]!!
        val now = Calendar.getInstance()
        alarms.forEach {
            it.snoozed = 0
            it.nextTime = it.getNextOccurrence(now)
            if (it.nextTime != null) {
                scheduleAlarm(context, it)
            } else {
                it.isEnabled = false
            }
            updateAlarmToDb(db, it)
        }
    }

    companion object {

        fun createIntent(action: String, context: Context) =
                Intent(action, null, context, AlarmStateManager::class.java)

        fun createScheduleAllAlarmsIntent(context: Context) =
                createIntent(ACTION_SCHEDULE_ALL_ALARMS, context)

        fun scheduleAlarm(context: Context, alarm: Alarm) {
            val am = context.getSystemService(Context.ALARM_SERVICE) as AlarmManager

            val startAlarmIntent = alarm.createAlarmStartIntent(context)
//                    .apply {
//                        addFlags(Intent.FLAG_RECEIVER_FOREGROUND)
//                    }
            val operation = PendingIntent.getBroadcast(context, alarm.hashCode(),
                    startAlarmIntent, PendingIntent.FLAG_UPDATE_CURRENT)

//            if (ApiUtil.isLOrLater()) {
//                val viewIntent = PendingIntent.getActivity(context, alarm.hashCode(),
//                        alarm.createIntent(context, MainActivity::class.java), 0)
//                val info = AlarmManager.AlarmClockInfo(alarm.nextTime!!.timeInMillis, viewIntent)
//                am.setAlarmClock(info, operation)
//            } else {
            am.setExact(AlarmManager.RTC_WAKEUP, alarm.nextTime!!.timeInMillis, operation)
//            }
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

private const val EXTRA_ON_BOOT = "net.wuqs.ontime.extra.ON_BOOT"
const val EXTRA_MISSED_ALARMS = "net.wuqs.ontime.extra.MISSED_ALARMS"
const val ACTION_ALARM_START = "net.wuqs.ontime.action.ALARM_START"
const val ACTION_DISMISS_ALL_MISSED_ALARMS = "net.wuqs.ontime.action.DISMISS_ALL_MISSED_ALARMS"
const val ACTION_SCHEDULE_ALL_ALARMS = "net.wuqs.ontime.action.SCHEDULE_ALL_ALARMS"
const val ACTION_SHOW_MISSED_ALARMS = "net.wuqs.ontime.action.SHOW_MISSED_ALARMS"