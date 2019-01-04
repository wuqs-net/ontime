package net.wuqs.ontime.alarm

import android.content.Context
import android.os.AsyncTask
import net.wuqs.ontime.db.*
import net.wuqs.ontime.util.Logger

/**
 * Utility for asynchronously updating a single [Alarm].
 */
class AlarmUpdateHandler(context: Context) {
    private val context = context.applicationContext
    private val db = AppDatabase.getInstance(this.context)!!

    fun asyncAddAlarm(alarm: Alarm) {
        AddAlarmTask(this, alarm).execute()
    }

    fun asyncUpdateAlarm(alarm: Alarm) {
        UpdateAlarmTask(this, alarm).execute()
    }

    fun asyncDeleteAlarm(alarm: Alarm) {
        DeleteAlarmTask(this, alarm).execute()
    }

    private class AddAlarmTask(
        private val handler: AlarmUpdateHandler,
        private val alarm: Alarm
    ) : AsyncTask<Unit, Unit, Unit>() {

        override fun doInBackground(vararg params: Unit?) {
            with(handler) {
                addAlarmToDb(db, alarm)
                AlarmStateManager.scheduleAlarm(context, alarm)
            }
        }
    }

    private class UpdateAlarmTask(
        private val handler: AlarmUpdateHandler,
        private val alarm: Alarm
    ) : AsyncTask<Unit, Unit, Unit>() {

        override fun doInBackground(vararg params: Unit?) {
            with(handler) {
                //            AlarmStateManager.cancelAlarm(context, alarm)
                if (alarm.nextTime != null) AlarmStateManager.scheduleAlarm(context, alarm)
                updateAlarmToDb(db, alarm)
            }
        }
    }

    private class DeleteAlarmTask(
        private val handler: AlarmUpdateHandler,
        private val alarm: Alarm
    ) : AsyncTask<Unit, Unit, Unit>() {

        override fun doInBackground(vararg params: Unit?) = with(handler) {
            AlarmStateManager.cancelAlarm(context, alarm)
            deleteAlarmFromDb(db, alarm)
        }
    }

    private val mLogger = Logger("AlarmUpdateHandler")
}