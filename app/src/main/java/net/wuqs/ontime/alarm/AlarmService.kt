package net.wuqs.ontime.alarm

import android.app.Service
import android.content.BroadcastReceiver
import android.content.Context
import android.content.Intent
import android.content.IntentFilter
import android.os.Binder
import android.os.IBinder
import android.support.v4.content.LocalBroadcastManager
import net.wuqs.ontime.db.Alarm
import net.wuqs.ontime.feature.currentalarm.AlarmRinger
import net.wuqs.ontime.util.AlarmWakeLock
import net.wuqs.ontime.util.Logger
import java.util.*

const val ACTION_ALARM_START = "net.wuqs.ontime.action.ALARM_START"
const val ACTION_ALARM_DISMISS = "net.wuqs.ontime.action.ALARM_DISMISS"
const val ACTION_ALARM_SNOOZE = "net.wuqs.ontime.action.ALARM_SNOOZE"
const val ACTION_ALARM_DONE = "net.wuqs.ontime.action.ALARM_DONE"

const val EXTRA_SNOOZE_INTERVAL = "net.wuqs.ontime.extra.SNOOZE_INTERVAL"

class AlarmService : Service() {

    private val logger = Logger("AlarmService")

    private val binder = Binder()

    private var isBound = false

    private var currentAlarm: Alarm? = null

    private val pendingAlarms = arrayListOf<Alarm>()

    private lateinit var alarmUpdateHandler: AlarmUpdateHandler

    private fun startAlarm(alarm: Alarm) {
        logger.i("Alarm started: $alarm")

        // Update disabled alarm
        if (!alarm.isEnabled) {
            logger.i("Disabled alarm triggered: $alarm")
            alarm.snoozed = 0
            alarm.nextTime = alarm.getNextOccurrence().also {
                if (it == null) alarm.isEnabled = false
            }
            alarmUpdateHandler.asyncUpdateAlarm(alarm)
            return
        }

        AlarmWakeLock.acquireCpuWakeLock(this)
        if (currentAlarm == null) {
            alarm.nextTime = alarm.getNextOccurrence().also {
                if (it == null) alarm.isEnabled = false
            }
            currentAlarm = alarm

            // Show notification.
            showAlarmStartNotification(this, alarm)

            AlarmRinger.start(this, alarm)
        } else {
            pendingAlarms += alarm
        }

        logger.i("Current: $currentAlarm")
        logger.i("Pending: ${pendingAlarms.joinToString()}")
    }

    private fun dismissCurrentAlarm() {
        currentAlarm!!.snoozed = 0
        stopCurrentAlarm()
    }

    private fun snoozeCurrentAlarm(quantity: Int, unit: Int) {
        currentAlarm!!.let {
            it.nextTime = Calendar.getInstance().apply { add(unit, quantity) }
            it.snoozed += 1
            it.isEnabled = true
        }
        stopCurrentAlarm()
    }

    private fun stopCurrentAlarm() {
        logger.i("Alarm stopped: $currentAlarm")

        alarmUpdateHandler.asyncUpdateAlarm(currentAlarm!!)
        AlarmRinger.stop(this)

        LocalBroadcastManager.getInstance(this).sendBroadcast(Intent(ACTION_ALARM_DONE))
        stopForeground(true)
        currentAlarm = null

        if (pendingAlarms.isEmpty()) {
            AlarmWakeLock.releaseCpuLock()
        } else {
            startAlarm(pendingAlarms.removeAt(0))
        }
    }

    private val receiver = object : BroadcastReceiver() {
        override fun onReceive(context: Context?, intent: Intent?) {
            if (intent == null) return
            logger.v("onReceive() ${intent.action}")
            when (intent.action) {
                ACTION_ALARM_START -> {
                    val alarm = intent.getBundleExtra(EXTRA_ALARM_INSTANCE)
                            .getParcelable<Alarm>(EXTRA_ALARM_INSTANCE)
                    startAlarm(alarm)
                }
                ACTION_ALARM_DISMISS -> {
                    dismissCurrentAlarm()
                }
                ACTION_ALARM_SNOOZE -> {
                    val (quantity, unit) = intent.getIntArrayExtra(EXTRA_SNOOZE_INTERVAL)
                    snoozeCurrentAlarm(quantity, unit)
                }
            }
        }
    }

    override fun onCreate() {
        super.onCreate()

        val filter = IntentFilter().apply {
            addAction(ACTION_ALARM_DISMISS)
            addAction(ACTION_ALARM_SNOOZE)
        }

        alarmUpdateHandler = AlarmUpdateHandler(this)

        LocalBroadcastManager.getInstance(this).registerReceiver(receiver, filter)
    }

    override fun onStartCommand(intent: Intent?, flags: Int, startId: Int): Int {
        if (intent == null) return Service.START_NOT_STICKY

        when (intent.action) {
            ACTION_ALARM_START -> {
                val alarm = intent.getBundleExtra(EXTRA_ALARM_INSTANCE)
                        .getParcelable<Alarm>(EXTRA_ALARM_INSTANCE)
                startAlarm(alarm)
            }
            ACTION_ALARM_DISMISS -> dismissCurrentAlarm()
        }

        return Service.START_NOT_STICKY
    }

    override fun onBind(intent: Intent): IBinder {
        isBound = true
        return binder
    }

    override fun onUnbind(intent: Intent?): Boolean {
        isBound = false
        return super.onUnbind(intent)
    }

    override fun onDestroy() {
        super.onDestroy()
        logger.v("onDestroy()")
        AlarmRinger.release()
        LocalBroadcastManager.getInstance(this).unregisterReceiver(receiver)
    }
}
