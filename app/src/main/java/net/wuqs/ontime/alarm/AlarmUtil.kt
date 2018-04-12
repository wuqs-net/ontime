package net.wuqs.ontime.alarm

import android.app.AlarmManager
import android.app.PendingIntent
import android.content.Context
import android.content.Intent
import android.os.AsyncTask
import android.os.Bundle
import android.text.format.DateFormat
import android.util.Log
import net.wuqs.ontime.R
import net.wuqs.ontime.db.Alarm
import net.wuqs.ontime.db.AppDatabase
import net.wuqs.ontime.db.DatabaseUtil
import net.wuqs.ontime.receiver.MyAlarmReceiver
import java.util.*

const val ALARM_START_ACTION = "net.wuqs.ontime.ALARM_START"

const val ALARM_ID = "net.wuqs.ontime.extra.ALARM_ID"
const val ALARM_INSTANCE = "net.wuqs.ontime.extra.ALARM_INSTANCE"
const val IS_NEW_ALARM = "net.wuqs.ontime.extra.IS_NEW_ALARM"
const val NEW_ALARM_HOUR = "net.wuqs.ontime.extra.NEW_ALARM_HOUR"
const val NEW_ALARM_MINUTE = "net.wuqs.ontime.extra.NEW_ALARM_MINUTE"
const val NEW_CREATED_ALARM = "net.wuqs.ontime.extra.NEW_CREATED_ALARM"

const val MINUTE_IN_MILLIS = 60 * 1000

fun getTimeDeltaFromNow(alarm: Alarm): Long {
    return getNextAlarmOccurence(alarm).timeInMillis - Calendar.getInstance().timeInMillis
}

fun getNextAlarmOccurence(alarm: Alarm): Calendar {
    val next = Calendar.getInstance()
    val nowMillis = next.timeInMillis
    next.set(Calendar.HOUR_OF_DAY, alarm.hour)
    next.set(Calendar.MINUTE, alarm.minute)
    next.set(Calendar.SECOND, 0)
    next.set(Calendar.MILLISECOND, 0)
    if (next.timeInMillis < nowMillis) {
        next.add(Calendar.DAY_OF_YEAR, 1)
    }
    return next
}

fun getTimeString(context: Context, alarm: Alarm): String {
    val cal = Calendar.getInstance()
    cal.set(Calendar.HOUR_OF_DAY, alarm.hour)
    cal.set(Calendar.MINUTE, alarm.minute)
    cal.set(Calendar.SECOND, 0)
    cal.set(Calendar.MILLISECOND, 0)
    return DateFormat.getTimeFormat(context).format(cal.time)
}

fun getTimeDistanceString(context: Context, deltaMillis: Long): String {
    val formats = context.resources.getStringArray(R.array.time_distance)
    if (deltaMillis < MINUTE_IN_MILLIS) return formats[0]

    var delta = deltaMillis / MINUTE_IN_MILLIS
    val minute = (delta % 60).toInt()
    delta /= 60
    val hour = (delta % 24).toInt()
    val day = (delta / 24).toInt()

    val minuteStr = context.resources.getQuantityString(R.plurals.minutes, minute, minute)
    val hourStr = context.resources.getQuantityString(R.plurals.hours, hour, hour)
    val dayStr = context.resources.getQuantityString(R.plurals.days, day, hour)

    val showMinute = if (minute > 0) 1 else 0
    val showHour = if (hour > 0) 2 else 0
    val showDay = if (day > 0) 4 else 0

    val index = showMinute + showHour + showDay

    return formats[index].format(dayStr, hourStr, minuteStr)
}

fun updateAlarmToDatabase(context: Context, alarm: Alarm) {
    object : AsyncTask<Unit, Unit, Unit>() {
        override fun doInBackground(vararg params: Unit?) {
            if (alarm.id in -1..0) DatabaseUtil.addAlarm(AppDatabase.getInstance(context), alarm)
            else DatabaseUtil.updateAlarm(AppDatabase.getInstance(context), alarm)
        }

        override fun onPostExecute(result: Unit?) {
            registerAlarm(context, alarm)
        }
    }.execute()
}

fun registerAlarm(context: Context, alarm: Alarm) {
    val bundle = Bundle()
    bundle.putParcelable(ALARM_INSTANCE, alarm)
    val intent = Intent(context, MyAlarmReceiver::class.java)
            .setAction(ALARM_START_ACTION)
            .putExtra(ALARM_ID, alarm.id)
            .putExtra(ALARM_INSTANCE, bundle)
    val pi = PendingIntent.getBroadcast(context, alarm.id, intent,
            PendingIntent.FLAG_UPDATE_CURRENT)
    val am = context.getSystemService(Context.ALARM_SERVICE) as AlarmManager
    am.set(AlarmManager.RTC_WAKEUP, getNextAlarmOccurence(alarm).getTimeInMillis(), pi)
    Log.d("Alarm", "Alarm registered: " + alarm)
}

fun cancelAlarm(context: Context, alarm: Alarm) {
    val intent = Intent(context, MyAlarmReceiver::class.java)
            .setAction(ALARM_START_ACTION)
    PendingIntent.getBroadcast(context, alarm.id, intent, PendingIntent.FLAG_UPDATE_CURRENT)
            .cancel()
}

fun deleteAlarm(context: Context, alarm: Alarm) {
    cancelAlarm(context, alarm)
    object : AsyncTask<Unit, Unit, Unit>() {
        override fun doInBackground(vararg params: Unit?) {
            DatabaseUtil.deleteAlarm(AppDatabase.getInstance(context), alarm)
        }
    }.execute()
}