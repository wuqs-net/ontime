package net.wuqs.ontime.alarm

import android.content.Context
import android.text.format.DateFormat
import net.wuqs.ontime.R
import net.wuqs.ontime.db.Alarm
import java.util.*

const val ALARM_INSTANCE = "net.wuqs.ontime.extra.ALARM_INSTANCE"

const val CREATE_ALARM_REQUEST = 2
const val EDIT_ALARM_REQUEST = 3

private const val MINUTE_IN_MILLIS = 60 * 1000

fun getTimeString(context: Context, alarm: Alarm): String {
    val cal = Calendar.getInstance()
    cal[Calendar.HOUR_OF_DAY] = alarm.hour
    cal[Calendar.MINUTE] = alarm.minute
    cal[Calendar.SECOND] = 0
    cal[Calendar.MILLISECOND] = 0
//    DateFormat.getBestDateTimePattern(Locale.getDefault(), "hm")
    return DateFormat.getTimeFormat(context).format(cal.time)
}

fun getDateString(c: Calendar?, showWeek: Boolean = true): String {
    if (c == null) return ""
    var skeleton =
            if (c[Calendar.YEAR] == Calendar.getInstance()[Calendar.YEAR]) "MMMd"
            else "yyyyMMMd"
    if (showWeek) skeleton += "E"
    val pattern = DateFormat.getBestDateTimePattern(Locale.getDefault(), skeleton)
    return DateFormat.format(pattern, c).toString()
//    return DateFormat.getDateFormat(context).format(c.time)
}

fun getTimeDistanceString(context: Context, alarmTime: Long): String {
    var delta = alarmTime - Calendar.getInstance().timeInMillis

    val formats = context.resources.getStringArray(R.array.time_distance)
    if (delta < MINUTE_IN_MILLIS) return formats[0]

    delta /= MINUTE_IN_MILLIS
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
    if (alarm.repeatType == 0) return getDateString(alarm.activateDate!!, true)
    val cycles = listOf(0, R.plurals.days_for_repeat, R.plurals.weeks_for_repeat,
            R.plurals.months_for_repeat, R.plurals.years_for_repeat)

    val startFromStr = getDateString(alarm.activateDate!!, false)

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