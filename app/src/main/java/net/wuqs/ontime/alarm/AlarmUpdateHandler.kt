package net.wuqs.ontime.alarm

import android.content.Context
import android.os.AsyncTask
import android.support.design.widget.Snackbar
import android.view.View
import net.wuqs.ontime.R
import net.wuqs.ontime.db.*
import net.wuqs.ontime.util.LogUtils

/**
 * API for asynchronously updating a single [Alarm].
 */
class AlarmUpdateHandler(context: Context,
                         private val snackbarAnchor: View? = null) {
    private val context = context.applicationContext
    private val db = AppDatabase.getInstance(this.context)!!

    fun asyncAddAlarm(alarm: Alarm, showSnackbar: Boolean = false) {
        AddAlarmTask(this, alarm, showSnackbar).execute()
    }

    fun asyncUpdateAlarm(alarm: Alarm, showSnackbar: Boolean = false) {
        UpdateAlarmTask(this, alarm, showSnackbar).execute()
    }

    fun asyncDeleteAlarm(alarm: Alarm) {
        DeleteAlarmTask(this, alarm).execute()
    }

    private class AddAlarmTask(private val handler: AlarmUpdateHandler,
                               private val alarm: Alarm,
                               private val showSnackbar: Boolean)
        : AsyncTask<Unit, Unit, Alarm>() {

        override fun doInBackground(vararg params: Unit?): Alarm = with(handler) {
            addAlarmToDb(db, alarm)
            AlarmStateManager.scheduleAlarm(context, alarm)
            return alarm
        }

        override fun onPostExecute(result: Alarm?) = with(handler) {
            if (showSnackbar && result?.isEnabled == true && snackbarAnchor != null) {
                showAlarmSetSnackbar(snackbarAnchor, result.nextTime?.timeInMillis)
            }
        }
    }

    private class UpdateAlarmTask(private val handler: AlarmUpdateHandler,
                                  private val alarm: Alarm,
                                  private val showSnackbar: Boolean)
        : AsyncTask<Unit, Unit, Alarm>() {

        override fun doInBackground(vararg params: Unit?): Alarm = with(handler) {
            //            AlarmStateManager.cancelAlarm(context, alarm)
            if (alarm.nextTime != null) AlarmStateManager.scheduleAlarm(context, alarm)
            updateAlarmToDb(db, alarm)
            return alarm
        }

        override fun onPostExecute(result: Alarm) = with(handler) {
            if (showSnackbar && result.isEnabled && snackbarAnchor != null) {
                showAlarmSetSnackbar(snackbarAnchor, result.nextTime?.timeInMillis)
            }
        }
    }

    private class DeleteAlarmTask(private val handler: AlarmUpdateHandler,
                                  private val alarm: Alarm)
        : AsyncTask<Unit, Unit, Unit>() {

        override fun doInBackground(vararg params: Unit?) = with(handler) {
            AlarmStateManager.cancelAlarm(context, alarm)
            deleteAlarmFromDb(db, alarm)
        }
    }

    private fun showAlarmSetSnackbar(anchor: View, alarmTime: Long?) {
        if (alarmTime == null) {
            mLogger.w("Attempted to show Snackbar with null time")
            return
        }

        val msg = anchor.context.getString(R.string.msg_alarm_will_go_off,
                getTimeDistanceString(anchor.context, alarmTime))
        Snackbar.make(anchor, msg, Snackbar.LENGTH_SHORT).show()
    }

    private val mLogger = LogUtils.Logger("AlarmUpdateHandler")
}