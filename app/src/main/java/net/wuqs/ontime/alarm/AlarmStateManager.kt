package net.wuqs.ontime.alarm

import android.app.AlarmManager
import android.app.PendingIntent
import android.content.BroadcastReceiver
import android.content.Context
import android.content.Intent
import android.content.Intent.ACTION_BOOT_COMPLETED
import android.os.Build
import androidx.localbroadcastmanager.content.LocalBroadcastManager
import net.wuqs.ontime.db.Alarm
import net.wuqs.ontime.db.AppDatabase
import net.wuqs.ontime.db.updateAlarmToDb
import net.wuqs.ontime.util.AlarmWakeLock
import net.wuqs.ontime.util.AsyncHandler
import net.wuqs.ontime.util.Logger
import java.util.*
import kotlin.collections.ArrayList

const val ACTION_DISMISS_ALL_MISSED_ALARMS = "net.wuqs.ontime.action.DISMISS_ALL_MISSED_ALARMS"

const val ACTION_SCHEDULE_ALL_ALARMS = "net.wuqs.ontime.action.SCHEDULE_ALL_ALARMS"

const val ACTION_SHOW_MISSED_ALARMS = "net.wuqs.ontime.action.SHOW_MISSED_ALARMS"

const val EXTRA_MISSED_ALARMS = "net.wuqs.ontime.extra.MISSED_ALARMS"

private const val EXTRA_ON_BOOT = "net.wuqs.ontime.extra.ON_BOOT"

class AlarmStateManager : BroadcastReceiver() {

    override fun onReceive(context: Context, intent: Intent) {
        // This method is called when the BroadcastReceiver is receiving an Intent broadcast.
        logger.i("Received intent: ${intent.action}")
        val result = goAsync()
        val wl = AlarmWakeLock.createPartialWakeLock(context)
        wl.acquire()
        AsyncHandler.post {
            handleIntent(context, intent)
            result.finish()
            wl.release()
        }
    }

    companion object {

        fun createIntent(action: String, context: Context): Intent {
            return Intent(action, null, context, AlarmStateManager::class.java)
        }

        fun createScheduleAllAlarmsIntent(context: Context): Intent {
            return createIntent(ACTION_SCHEDULE_ALL_ALARMS, context)
        }

        /**
         * Creates a [PendingIntent] for an [Alarm].
         *
         * @param context to create the [PendingIntent].
         * @param alarm which the [PendingIntent] is to be created for.
         * @param flags for the [PendingIntent].
         * @return a [PendingIntent] for the specified [Alarm].
         */
        private fun createPendingIntent(
            context: Context,
            alarm: Alarm,
            flags: Int = PendingIntent.FLAG_UPDATE_CURRENT
        ): PendingIntent? {
            val startAlarmIntent = alarm.createAlarmStartIntent(context).apply {
                addFlags(Intent.FLAG_RECEIVER_FOREGROUND)
            }
            return if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
                PendingIntent.getForegroundService(context, alarm.hashCode(), startAlarmIntent,
                        flags)
            } else {
                PendingIntent.getService(context, alarm.hashCode(), startAlarmIntent,
                        flags)
            }
        }

        /**
         * Schedules an [Alarm] to so it will go off.
         *
         * @param context to schedule the [Alarm].
         * @param alarm to be scheduled.
         */
        fun scheduleAlarm(context: Context, alarm: Alarm) {
            val am = context.getSystemService(Context.ALARM_SERVICE) as AlarmManager

            val pendingIntent = createPendingIntent(context, alarm)

            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.M) {
                // Make sure the alarm fires even if the device is dozing.
                am.setExactAndAllowWhileIdle(
                        AlarmManager.RTC_WAKEUP,
                        alarm.nextTime!!.timeInMillis,
                        pendingIntent
                )
            } else {
                am.setExact(AlarmManager.RTC_WAKEUP, alarm.nextTime!!.timeInMillis, pendingIntent)
            }
            logger.d("Alarm registered: $alarm")
        }

        /**
         * Cancels an [Alarm] so it will not go off.
         *
         * @param context to cancel the [Alarm].
         * @param alarm to be cancelled.
         */
        fun cancelAlarm(context: Context, alarm: Alarm) {
            val pendingIntent = createPendingIntent(context, alarm,
                    PendingIntent.FLAG_NO_CREATE)
            if (pendingIntent == null) {
                logger.e("PendingIntent is null, alarm cannot be cancelled: $alarm")
                return
            }

            val am = context.getSystemService(Context.ALARM_SERVICE) as AlarmManager
            am.cancel(pendingIntent)
            logger.d("Alarm cancelled: $alarm")
        }

        /**
         * Handles various [Intent] received.
         *
         * @param context to handle the [Intent].
         * @param intent the [Intent] to be handled.
         */
        fun handleIntent(context: Context, intent: Intent) {
            when (intent.action) {
                ACTION_BOOT_COMPLETED -> scheduleAllAlarms(context, true)
                // TODO: Direct boot aware (long-term plan)
//                ACTION_LOCKED_BOOT_COMPLETED -> scheduleAllAlarms(context, true)
                ACTION_SCHEDULE_ALL_ALARMS -> scheduleAllAlarms(context, false)
                // ACTION_SHOW_MISSED_ALARMS -> {
                //     logger.i("Show missed alarms")
                //     val mIntent = Intent(context, MissedAlarmsActivity::class.java).apply {
                //         putExtras(intent)
                //     }
                //     context.startActivity(mIntent)
                // }
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
            val db = AppDatabase.getInstance(context)!!
            val alarms = db.alarmDao.alarmsHasNextTime
            val now = Calendar.getInstance()
            val (notMissed, missed) = alarms.partition { it.nextTime!!.after(now) }
            logger.d("Not missed: ${notMissed.joinToString()}")
            logger.d("Missed: ${missed.joinToString()}")
            notMissed.forEach { alarm ->
                if (alarm.snoozed == 0) {
                    alarm.nextTime = alarm.getNextOccurrence()
                } else if (alarm.nextTime == alarm.getNextOccurrence()) {
                    alarm.snoozed = 0
                }
                scheduleAlarm(context, alarm)
            }
            logger.i("Finished scheduling all alarms")

            if (missed.isEmpty()) return
            val (enabled, disabled) = missed.partition { it.isEnabled }
            dismissAllMissedAlarms(context, disabled)   // Ignore disabled alarms

            if (enabled.isEmpty()) return
            val intent = Intent(ACTION_SHOW_MISSED_ALARMS).apply {
                putParcelableArrayListExtra(EXTRA_MISSED_ALARMS, ArrayList(enabled))
                putExtra(EXTRA_ON_BOOT, onBoot)
            }
            LocalBroadcastManager.getInstance(context).sendBroadcast(intent)
        }

        private fun dismissAllMissedAlarms(context: Context, alarms: List<Alarm>) {
            val db = AppDatabase.getInstance(context)!!
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

        private val logger = Logger("AlarmStateManager")
    }
}