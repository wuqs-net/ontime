package net.wuqs.ontime.alarm

import android.content.Context
import android.content.res.Resources
import android.support.v4.util.ArrayMap
import android.text.format.DateFormat
import android.text.format.DateUtils
import net.wuqs.ontime.R
import net.wuqs.ontime.db.Alarm
import java.text.SimpleDateFormat
import java.util.*

const val ALARM_INSTANCE = "net.wuqs.ontime.extra.ALARM_INSTANCE"

const val CREATE_ALARM_REQUEST = 2
const val EDIT_ALARM_REQUEST = 3

private const val MINUTE_IN_MILLIS = 60 * 1000

fun getTimeString(
    context: Context,
    alarm: Alarm,
    showAmPm: Boolean = true,
    hairSpace: Boolean = false
): String {
    val cal = Calendar.getInstance().apply {
        this[Calendar.HOUR_OF_DAY] = alarm.hour
        this[Calendar.MINUTE] = alarm.minute
        this[Calendar.SECOND] = 0
        this[Calendar.MILLISECOND] = 0
    }
    return getTimeString(context, cal, showAmPm, hairSpace)
}

fun getTimeString(
    context: Context,
    calendar: Calendar?,
    showAmPm: Boolean = true,
    hairSpace: Boolean = false
): String {
    if (calendar == null) return ""
    val skeleton = if (DateFormat.is24HourFormat(context)) "Hm" else "hm"
    var pattern = DateFormat.getBestDateTimePattern(Locale.getDefault(), skeleton)
    if (!showAmPm) pattern = pattern.replace("a", "").trim()
    val str = DateFormat.format(pattern, calendar).toString()
    if (hairSpace) return str.replace(' ', '\u200A')
    return str
}

fun getDateString(cal: Calendar?, showWeek: Boolean = true, showYear: Boolean = true): String {
    if (cal == null) return ""
    val now = Calendar.getInstance()
    var skeleton = if (cal[Calendar.YEAR] == now[Calendar.YEAR] || !showYear) {
        "MMMd"
    } else {
        "yyyyMMMd"
    }
    if (showWeek) skeleton += "E"
    val pattern = DateFormat.getBestDateTimePattern(Locale.getDefault(), skeleton)
    return DateFormat.format(pattern, cal).toString()
}

fun getDateTimeString(ctx: Context, c: Calendar?, showWeek: Boolean = true): String {
    if (c == null) return ""
    val now = Calendar.getInstance()
    val skeleton = StringBuilder().apply {
        if (!c.sameYearAs(now)) append("yyyy")
        append("MMMd")
        if (showWeek) append("E")
        append(if (DateFormat.is24HourFormat(ctx)) "Hm" else "hma")
    }.toString()
    val inFormat = DateFormat.getBestDateTimePattern(Locale.getDefault(), skeleton)
    return DateFormat.format(inFormat, c).toString()
}

fun getRelativeDateTimeString(ctx: Context, c: Calendar?): String {
    if (c == null) return ""

    val tomorrowResolution = Calendar.getInstance().apply {
        setHms(0)
        add(Calendar.DAY_OF_YEAR, 2)
    }
    val yesterdayResolution = Calendar.getInstance().apply {
        setHms(0)
        add(Calendar.DAY_OF_YEAR, -1)
    }
    if (c.before(tomorrowResolution) && !c.before(yesterdayResolution)) {
        return DateUtils.getRelativeDateTimeString(ctx, c.timeInMillis,
                DateUtils.DAY_IN_MILLIS, DateUtils.WEEK_IN_MILLIS,
                DateUtils.FORMAT_SHOW_DATE
                        or DateUtils.FORMAT_SHOW_TIME
                        or DateUtils.FORMAT_ABBREV_MONTH).toString()
    } else {
        return DateUtils.formatDateTime(ctx, c.timeInMillis,
                DateUtils.FORMAT_SHOW_DATE
                        or DateUtils.FORMAT_SHOW_TIME
                        or DateUtils.FORMAT_ABBREV_MONTH)
    }
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

fun Alarm.getTitleOrDefault(context: Context): String {
    return if (title.isNullOrBlank()) {
        context.getString(R.string.msg_default_alarm_title)
    } else {
        title!!
    }
}

fun Alarm.getRepeatTypeText(resources: Resources): CharSequence {
    val index = when (repeatType) {
        Alarm.NON_REPEAT -> 0
        Alarm.REPEAT_DAILY -> 1
        Alarm.REPEAT_WEEKLY -> 2
        Alarm.REPEAT_MONTHLY_BY_DATE -> 3
        Alarm.REPEAT_YEARLY_BY_DATE -> 4
        else -> throw IllegalArgumentException("Illegal repeat type")
    }
    return resources.getStringArray(R.array.repeat_types)[index]
}

fun Alarm.getRepeatCycleText(resources: Resources): CharSequence {
    val cycles = arrayOf(0, R.plurals.days, R.plurals.weeks, R.plurals.months, R.plurals.years)
    return resources.getQuantityText(cycles[repeatType and 0xF], repeatCycle)
}

fun getRepeatString(ctx: Context, alarm: Alarm): String {
    when (alarm.repeatType) {
        Alarm.NON_REPEAT -> return ""
        Alarm.REPEAT_DAILY -> {
            val str = ctx.resources.getQuantityString(
                    R.plurals.days_for_repeat,
                    alarm.repeatCycle,
                    alarm.repeatCycle
            )
            if (alarm.repeatCycle == 1) {
                return ctx.getString(R.string.msg_days_for_repeat_single)
            }
            return str
        }
        Alarm.REPEAT_WEEKLY -> {
            val indexStr = run {
                val daysOfWeek = when (Locale.getDefault().language) {
                    Locale.CHINESE.language -> getShortWeekDays()
                    else -> getShortWeekDays("E")
                }
                val repeatDays = daysOfWeek.filterKeys {
                    alarm.repeatIndex.isWeekdaySet(it)
                }.values
                repeatDays.joinToString()
            }
            val str = ctx.resources.getQuantityString(R.plurals.weeks_for_repeat,
                    alarm.repeatCycle, alarm.repeatCycle, indexStr)
            if (alarm.repeatCycle == 1) {
                return ctx.getString(R.string.msg_weeks_for_repeat_single, null, indexStr)
            }
            return str
        }
        Alarm.REPEAT_MONTHLY_BY_DATE -> {
            val indexStr = run {
                val daysOfMonth = MutableList(31) { index -> index + 1 }
                val repeatDays = daysOfMonth.filterIndexed { index, _ ->
                    alarm.repeatIndex shr index and 1 == 1
                }
                repeatDays.joinToString()
            }
            val str = ctx.resources.getQuantityString(R.plurals.months_for_repeat,
                    alarm.repeatCycle, alarm.repeatCycle, indexStr)
            if (alarm.repeatCycle == 1) {
                return ctx.getString(R.string.msg_months_for_repeat_single, null, indexStr)
            }
            return str
        }
        Alarm.REPEAT_YEARLY_BY_DATE -> {
            val date = getDateString(alarm.activateDate, false, false)
            val str = ctx.resources.getQuantityString(R.plurals.years_for_repeat,
                    alarm.repeatCycle, alarm.repeatCycle, date)
            if (alarm.repeatCycle == 1) {
                return ctx.getString(R.string.msg_years_for_repeat_single, null, date)
            }
            return str
        }
        else -> {
            return ""
        }

    }

}

fun Calendar.setMidnight(year: Int, month: Int, date: Int) = apply {
    set(year, month, date, 0, 0, 0)
    set(Calendar.MILLISECOND, 0)
}

fun Calendar.setHms(hour: Int, minute: Int = 0, second: Int = 0) = apply {
    set(Calendar.HOUR_OF_DAY, hour)
    set(Calendar.MINUTE, minute)
    set(Calendar.SECOND, second)
    set(Calendar.MILLISECOND, 0)
}

fun Calendar.sameDayAs(another: Calendar?): Boolean {
    if (another == null) return false

    if (this[Calendar.YEAR] != another[Calendar.YEAR]) return false
    if (this[Calendar.DAY_OF_YEAR] != another[Calendar.DAY_OF_YEAR]) return false
    return true
}

fun Calendar.sameYearAs(other: Calendar?): Boolean {
    if (other == null) return false
    return this[Calendar.YEAR] == other[Calendar.YEAR]
}

fun getOrderedWeekDays(firstDay: Int) = List(7) { (firstDay + it - 1) % 7 + 1 }

fun getShortWeekDays(pattern: String = "ccccc"): ArrayMap<Int, String> {
    if (shortWeekDays[pattern] == null) {
        val format = SimpleDateFormat(pattern, Locale.getDefault())
        val calendar = Calendar.getInstance()
        shortWeekDays[pattern] = ArrayMap<Int, String>(7).apply {
            for (i in Calendar.SUNDAY..Calendar.SATURDAY) {
                calendar[Calendar.DAY_OF_WEEK] = i
                put(i, format.format(calendar.time))
            }
        }
    }
    return shortWeekDays[pattern]!!
}

private val shortWeekDays = ArrayMap<String, ArrayMap<Int, String>>()

fun Alarm.isNonRepeat() = repeatType == 0
fun Alarm.isDaily() = repeatType and 0xF == 1
fun Alarm.isWeekly() = repeatType and 0xF == 2
fun Alarm.isMonthly() = repeatType and 0xF == 3
fun Alarm.isYearly() = repeatType and 0xF == 4