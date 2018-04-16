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
import net.wuqs.ontime.SetAlarmActivity
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
const val DELTA_NEXT_OCCURRENCE = "net.wuqs.ontime.extra.DELTA_NEXT_OCCURRENCE"

const val ALARM_NON_REPEAT = 0x0
const val ALARM_REPEAT_DAILY = 0x11
const val ALARM_REPEAT_WEEKLY = 0x22
const val ALARM_REPEAT_MONTHLY_BY_DAY_OF_MONTH = 0x43
const val ALARM_REPEAT_MONTHLY_BY_DAY_OF_WEEK = 0x83
const val ALARM_REPEAT_YEARLY_BY_DAY_OF_MONTH = 0x144
const val ALARM_REPEAT_YEARLY_BY_DAY_OF_WEEK = 0x184

const val CREATE_ALARM_REQUEST = 2
const val EDIT_ALARM_REQUEST = 3

const val MINUTE_IN_MILLIS = 60 * 1000

fun getTimeDeltaFromNow(alarm: Alarm): Long {
    return alarm.nextOccurrence.timeInMillis - Calendar.getInstance().timeInMillis
}

fun getNextAlarmOccurrence(alarm: Alarm): Calendar {
    val next = Calendar.getInstance()
    val nowMillis = next.timeInMillis

    // Set time
    next[Calendar.HOUR_OF_DAY] = alarm.hour
    next[Calendar.MINUTE] = alarm.minute
    next[Calendar.SECOND] = 0
    next[Calendar.MILLISECOND] = 0

    when (alarm.repeatType) {
        ALARM_NON_REPEAT -> {
            if (next.timeInMillis < nowMillis) next.add(Calendar.DAY_OF_YEAR, 1)
        }
        ALARM_REPEAT_DAILY -> {
//            if (nowMillis < alarm.activateDate.timeInMillis) {
                next[Calendar.DAY_OF_MONTH] = alarm.activateDate[Calendar.DAY_OF_MONTH]
                next[Calendar.MONTH] = alarm.activateDate[Calendar.MONTH]
                next[Calendar.YEAR] = alarm.activateDate[Calendar.YEAR]
//            }
            if (next.timeInMillis < nowMillis) {
                val diff = (nowMillis - next.timeInMillis) / (1000 * 60 * 60 * 24 * alarm.repeatCycle)
                next.add(Calendar.DAY_OF_YEAR, (diff.toInt() + 1) * alarm.repeatCycle)
            }
//            while (next.timeInMillis < nowMillis) next.add(Calendar.DAY_OF_YEAR, alarm.repeatCycle)
        }
    }

    // Daylight Saving Time change
    next[Calendar.HOUR_OF_DAY] = alarm.hour
    next[Calendar.MINUTE] = alarm.minute
    return next
}

fun getTimeString(context: Context, alarm: Alarm): String {
    val cal = Calendar.getInstance()
    cal[Calendar.HOUR_OF_DAY] = alarm.hour
    cal[Calendar.MINUTE] = alarm.minute
    cal[Calendar.SECOND] = 0
    cal[Calendar.MILLISECOND] = 0
//    DateFormat.getBestDateTimePattern(Locale.getDefault(), "hm")
    return DateFormat.getTimeFormat(context).format(cal.time)
}

fun getDateString(context: Context, c: Calendar, showWeek: Boolean = true): String {
    var skeleton =
            if (c[Calendar.YEAR] == Calendar.getInstance()[Calendar.YEAR]) "MMMd"
            else "yyyyMMMd"
    if (showWeek) skeleton += "E"
    val pattern = DateFormat.getBestDateTimePattern(Locale.getDefault(), skeleton)
    return DateFormat.format(pattern, c).toString()
//    return DateFormat.getDateFormat(context).format(c.time)
}

fun getTimeDistanceString(context: Context, deltaMillis: Long): String {
    val formats = context.resources.getStringArray(R.array.time_distance)
    if (deltaMillis < MINUTE_IN_MILLIS) return formats[0]

    var delta = deltaMillis / MINUTE_IN_MILLIS
    val minute = (delta % 60).toInt()
    delta /= 60
    val hour = (delta % 24).toInt()
    val day = (delta / 24).toInt()

    val minuteStr = context.resources.getQuantityString(R.plurals.minutes_with_quan, minute, minute)
    val hourStr = context.resources.getQuantityString(R.plurals.hours_with_quan, hour, hour)
    val dayStr = context.resources.getQuantityString(R.plurals.days_with_quan, day, day)

    val showMinute = if (minute > 0) 1 else 0
    val showHour = if (hour > 0) 2 else 0
    val showDay = if (day > 0) 4 else 0

    val index = showMinute + showHour + showDay

    return formats[index].format(dayStr, hourStr, minuteStr)
}

fun getRepeatString(context: Context, alarm: Alarm): String {
    val patterns = context.resources.getStringArray(R.array.repeat_patterns)
    if (alarm.repeatType == 0) {
        return patterns[0]
    }
    val cycles = listOf(0, R.plurals.days_for_repeat, R.plurals.weeks_for_repeat,
            R.plurals.months_for_repeat, R.plurals.years_for_repeat)

    val startFromStr = getDateString(context, alarm.activateDate, false)

    val cycleStr = context.resources.getQuantityString(cycles[alarm.repeatType and 0xF],
            alarm.repeatCycle, alarm.repeatCycle)

    var indexStr = ""
    if (alarm.repeatType == 2) {
        val indexList = mutableListOf<String>()
        for (i in 0..6) {
            if ((alarm.repeatIndex shr i) and 1 == 1) {
                val c = Calendar.getInstance()
                c[Calendar.DAY_OF_WEEK] = i + 1
                indexList.add(c.getDisplayName(Calendar.DAY_OF_WEEK, Calendar.SHORT, Locale.getDefault()))
            }
        }
        indexStr = indexList.joinToString()
    }

    return patterns[alarm.repeatType and 0xF].format(cycleStr, indexStr, startFromStr)
}

fun updateAlarm(context: Context, alarm: Alarm) {
    object : AsyncTask<Unit, Unit, Unit>() {
        override fun doInBackground(vararg params: Unit?) {
            if (alarm.id == 0) DatabaseUtil.addAlarm(AppDatabase.getInstance(context), alarm)
            else DatabaseUtil.updateAlarm(AppDatabase.getInstance(context), alarm)
        }

        override fun onPostExecute(result: Unit?) {
            if (alarm.isEnabled) registerAlarm(context, alarm)
            else cancelAlarm(context, alarm)
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
    val pIntent = PendingIntent.getBroadcast(context, alarm.id, intent,
            PendingIntent.FLAG_UPDATE_CURRENT)
    val am = context.getSystemService(Context.ALARM_SERVICE) as AlarmManager
    am.setExact(AlarmManager.RTC_WAKEUP, alarm.nextOccurrence.timeInMillis, pIntent)
    Log.d("AlarmUtil", "Alarm registered: $alarm")
}

fun cancelAlarm(context: Context, alarm: Alarm) {
    val intent = Intent(context, MyAlarmReceiver::class.java)
            .setAction(ALARM_START_ACTION)
    PendingIntent.getBroadcast(context, alarm.id, intent, PendingIntent.FLAG_UPDATE_CURRENT)
            .cancel()
    Log.d("AlarmUtil", "Alarm cancelled: $alarm")
}

fun deleteAlarm(context: Context, alarm: Alarm) {
    cancelAlarm(context, alarm)
    object : AsyncTask<Unit, Unit, Unit>() {
        override fun doInBackground(vararg params: Unit?) {
            DatabaseUtil.deleteAlarm(AppDatabase.getInstance(context), alarm)
        }
    }.execute()
}