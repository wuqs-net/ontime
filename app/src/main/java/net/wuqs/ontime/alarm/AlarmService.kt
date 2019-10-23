package net.wuqs.ontime.alarm

import android.app.Service
import android.content.BroadcastReceiver
import android.content.Context
import android.content.Intent
import android.content.IntentFilter
import android.os.Binder
import android.os.IBinder
import androidx.localbroadcastmanager.content.LocalBroadcastManager
import net.wuqs.ontime.db.Alarm
import net.wuqs.ontime.feature.currentalarm.AlarmActivity
import net.wuqs.ontime.feature.currentalarm.AlarmRinger
import net.wuqs.ontime.util.AlarmWakeLock
import net.wuqs.ontime.util.Logger
import java.util.*

const val ACTION_ALARM_START = "net.wuqs.ontime.action.ALARM_START"
const val ACTION_ALARM_DISMISS = "net.wuqs.ontime.action.ALARM_DISMISS"
const val ACTION_ALARM_SHOW_SNOOZE_OPTIONS = "net.wuqs.ontime.action.ALARM_SHOW_SNOOZE_OPTIONS"
const val ACTION_ALARM_SNOOZE = "net.wuqs.ontime.action.ALARM_SNOOZE"
const val ACTION_ALARM_DONE = "net.wuqs.ontime.action.ALARM_DONE"

const val EXTRA_SNOOZE_INTERVAL = "net.wuqs.ontime.extra.SNOOZE_INTERVAL"

class AlarmService : Service() {

    private val logger = Logger("AlarmService")

    private val binder = Binder()

    private var isBound = false

    /** Current [Alarm] that the user interacts with */
    private var currentAlarm: Alarm? = null

    /** The time when the current [Alarm] starts */
    private var currentAlarmStart: Calendar? = null

    /**
     * List of [Alarm]s that haven't been processed
     */
    private val pendingAlarms = arrayListOf<Alarm>()

    private lateinit var alarmUpdateHandler: AlarmUpdateHandler

    /**
     * Starts an [Alarm]. If there is a current Alarm, add the alarm to the queue.
     */
    private fun startAlarm(alarm: Alarm) {
        logger.i("Alarm started: $alarm")

        val lbm = LocalBroadcastManager.getInstance(this)
        lbm.sendBroadcast(Intent(ACTION_ALARM_START))

        // Update disabled alarm
        if (!alarm.isEnabled) {
            logger.i("Disabled alarm triggered: $alarm")
            alarm.snoozed = 0
            alarm.nextTime = alarm.getNextOccurrence(alarm.nextTime!!)
            alarmUpdateHandler.asyncUpdateAlarm(alarm)
            return
        }

        AlarmWakeLock.acquireCpuWakeLock(this)

        if (currentAlarm == null) {
            currentAlarmStart = alarm.nextTime
            alarm.nextTime = alarm.getNextOccurrence(alarm.nextTime!!)
            currentAlarm = alarm

            // Show notification.
            val notification = buildAlarmNotification(this, CHANNEL_ALARM, alarm, true)
            startForeground(NOTIFICATION_ID_ALARM, notification)

            AlarmRinger.start(this, alarm)
        } else {
            pendingAlarms += alarm
        }

        logger.i("Current: $currentAlarm")
        logger.i("Pending: ${pendingAlarms.joinToString()}")
    }

    private fun dismissCurrentAlarm(intent: Intent?) {
        currentAlarm!!.snoozed = 0

        val updateAlarmTime = { alarm: Alarm -> alarm.nextTime = Calendar.getInstance() }

        if (currentAlarm!!.isNonRepeat()) {
            currentAlarm!!.apply {
                isHistorical = true
                activateDate = currentAlarmStart
                updateAlarmTime(this)
                notes = intent?.getStringExtra("records") ?: ""
            }
        } else {
            val historicalAlarm = Alarm(currentAlarm!!).apply {
                id = Alarm.INVALID_ID
                updateAlarmTime(this)
                repeatType = Alarm.NON_REPEAT
                activateDate = currentAlarmStart
                isHistorical = true
                parentAlarmId = currentAlarm!!.id
                notes = intent?.getStringExtra("records") ?: ""
            }
            alarmUpdateHandler.asyncAddAlarm(historicalAlarm)
        }

        stopCurrentAlarm()
    }

    private fun showSnoozeOptions() {
        sendBroadcast(Intent(Intent.ACTION_CLOSE_SYSTEM_DIALOGS))
        val snoozeIntent = Intent(this, AlarmActivity::class.java).apply {
            action = ACTION_ALARM_SHOW_SNOOZE_OPTIONS
            addFlags(Intent.FLAG_ACTIVITY_NEW_TASK)
            putExtra(EXTRA_ALARM_INSTANCE, currentAlarm)
        }
        startActivity(snoozeIntent)
    }

    private fun snoozeCurrentAlarm(quantity: Int, unit: Int) {
        currentAlarm!!.let {
            it.nextTime = Calendar.getInstance().apply { add(unit, quantity) }
            it.snoozed += 1
            it.isEnabled = true
        }
        stopCurrentAlarm()
    }

    /**
     * Stops the current [Alarm]. The Alarm can be dismissed, snoozed, or completed.
     */
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
                            .getParcelable<Alarm>(EXTRA_ALARM_INSTANCE)!!
                    startAlarm(alarm)
                }
                ACTION_ALARM_DISMISS -> {
                    dismissCurrentAlarm(intent)
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
        if (intent == null) return START_NOT_STICKY

        logger.v("onStartCommand(): action=${intent.action}")
        when (intent.action) {
            ACTION_ALARM_START -> {
                val alarm = intent.getBundleExtra(EXTRA_ALARM_INSTANCE)
                        .getParcelable<Alarm>(EXTRA_ALARM_INSTANCE)!!
                startAlarm(alarm)
            }
            ACTION_ALARM_DISMISS -> dismissCurrentAlarm(intent)
            ACTION_ALARM_SHOW_SNOOZE_OPTIONS -> showSnoozeOptions()
        }

        return START_NOT_STICKY
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
        LocalBroadcastManager.getInstance(this).unregisterReceiver(receiver)
    }
}
