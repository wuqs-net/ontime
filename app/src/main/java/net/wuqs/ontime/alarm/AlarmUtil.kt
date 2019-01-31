package net.wuqs.ontime.alarm

import android.content.Context
import android.content.res.Resources
import androidx.collection.ArrayMap
import android.text.format.DateFormat
import android.text.format.DateUtils
import net.wuqs.ontime.R
import net.wuqs.ontime.db.Alarm
import java.text.SimpleDateFormat
import java.util.*

const val EXTRA_ALARM_INSTANCE = "net.wuqs.ontime.extra.ALARM_INSTANCE"

const val CREATE_ALARM_REQUEST = 2
const val EDIT_ALARM_REQUEST = 3

private const val MINUTE_IN_MILLIS = 60 * 1000

/**
 * Creates a String containing the firing time of `this` Alarm.
 *
 * @param context to create the String
 * @param showAmPm whether or not to include AM/PM in 12-hour format
 * @param hairSpace whether or not to replace all regular spaces with hair spaces
 * @return a String containing the firing time of `this` Alarm
 */
fun Alarm.createTimeString(
    context: Context,
    showAmPm: Boolean = true,
    hairSpace: Boolean = false
): String {
    val calendar = Calendar.getInstance().setHms(hour, minute)
    return calendar.createTimeString(context, showAmPm, hairSpace)
}

/**
 * Gets the title of this [Alarm]. If there is no title, return the next occurrence instead.
 * If there is no next occurrence, return the first occurrence instead.
 *
 * @param context the [Context] used to get the String.
 */
fun Alarm.getTitleOrTime(context: Context): String {
    return title.takeUnless { it.isNullOrBlank() }
            ?: nextTime.createDateTimeString(context).ifBlank {
                val time = activateDate!!.clone() as Calendar
                time.setHms(hour, minute)
                time.createDateTimeString(context)
            }
}

/**
 * Creates a String containing the time of `this` Calendar.
 *
 * @param context to create the String
 * @param showAmPm whether or not to include AM/PM in 12-hour format
 * @param hairSpace whether or not to replace all regular spaces with hair spaces
 * @return a String containing the time of `this` Calendar, or an empty String if `this` is `null`
 */
fun Calendar?.createTimeString(
    context: Context,
    showAmPm: Boolean = true,
    hairSpace: Boolean = false
): String {
    if (this == null) return ""
    val skeleton = if (DateFormat.is24HourFormat(context)) "Hm" else "hm"
    var pattern = DateFormat.getBestDateTimePattern(Locale.getDefault(), skeleton)
    if (!showAmPm) pattern = pattern.replace("a", "").trim()
    val str = DateFormat.format(pattern, this).toString()
    if (hairSpace) return str.replace(' ', '\u200A')
    return str
}

/**
 * Creates a String containing the date of `this` Calendar.
 *
 * @param showWeek whether or not to include days of week; default value is `true`
 * @param showYear whether or not to include year; default value is `true`
 * @return a String containing the date of `this` Calendar, or an empty String if `this` is `null`
 */
fun Calendar?.createDateString(showWeek: Boolean = true, showYear: Boolean = true): String {
    if (this == null) return ""
    val now = Calendar.getInstance()
    var skeleton = if (this[Calendar.YEAR] == now[Calendar.YEAR] || !showYear) {
        "MMMd"
    } else {
        "yyyyMMMd"
    }
    if (showWeek) skeleton += "E"
    val pattern = DateFormat.getBestDateTimePattern(Locale.getDefault(), skeleton)
    return DateFormat.format(pattern, this).toString()
}

/**
 * Creates a String containing the date and the time of `this` Calendar.
 *
 * @param showWeek whether or not to include days of week; default value is `true`
 * @return a String containing the date and the time of `this` Calendar, or an empty String if
 * `this` is `null`
 */
fun Calendar?.createDateTimeString(context: Context, showWeek: Boolean = true): String {
    if (this == null) return ""
    val now = Calendar.getInstance()
    val skeleton = StringBuilder().apply {
        if (!sameYearAs(now)) append("yyyy")
        append("MMMd")
        if (showWeek) append("E")
        append(if (DateFormat.is24HourFormat(context)) "Hm" else "hma")
    }.toString()
    val inFormat = DateFormat.getBestDateTimePattern(Locale.getDefault(), skeleton)
    return DateFormat.format(inFormat, this).toString()
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

/**
 * Creates a String describing the difference between a specified time from now.
 *
 * @param context to create the String
 * @param timeInFuture a [Calendar] that represents a time in the future
 * @return a String describing the difference between the specified time from now
 */
fun createTimeDifferenceString(context: Context, timeInFuture: Calendar): String {
    var diff = timeInFuture.timeInMillis - Calendar.getInstance().timeInMillis

    val formats = context.resources.getStringArray(R.array.time_distance)
    if (diff < MINUTE_IN_MILLIS) return formats[0]

    diff /= MINUTE_IN_MILLIS
    val minute = (diff % 60).toInt()
    diff /= 60
    val hour = (diff % 24).toInt()
    val day = (diff / 24).toInt()

    val minuteStr = context.resources.getQuantityString(R.plurals.minutes_with_quan, minute, minute)
    val hourStr = context.resources.getQuantityString(R.plurals.hours_with_quan, hour, hour)
    val dayStr = context.resources.getQuantityString(R.plurals.days_with_quan, day, day)

    val showMinute = if (minute > 0) 1 else 0
    val showHour = if (hour > 0) 2 else 0
    val showDay = if (day > 0) 4 else 0

    val index = showMinute + showHour + showDay

    return formats[index].format(dayStr, hourStr, minuteStr)
}

/**
 * Gets the title of `this` alarm, or the default title ([R.string.msg_default_alarm_title]) if the
 * title is blank.
 *
 * @param context to get the String
 * @return the title of `this` alarm, or the default title if the title is blank
 */
fun Alarm.getTitleOrDefault(context: Context): String {
    return if (title.isNullOrBlank()) {
        context.getString(R.string.msg_default_alarm_title)
    } else {
        title!!
    }
}

/**
 * Gets a CharSequence containing the repeat type of `this` alarm.
 *
 * @param resources to get the text
 * @return a CharSequence containing the repeat type of `this` alarm
 */
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

/**
 * Gets a CharSequence describing the repeat cycle of `this` alarm.
 *
 * @param resources to get the text
 * @return a CharSequence describing the repeat cycle of `this` alarm
 */
fun Alarm.getRepeatCycleText(resources: Resources): CharSequence {
    val cycles = arrayOf(0, R.plurals.days, R.plurals.weeks, R.plurals.months, R.plurals.years)
    return resources.getQuantityText(cycles[repeatType and 0xF], repeatCycle)
}

/**
 * Creates a String describing the repeat pattern of `this` alarm.
 *
 * @param context to create the String
 * @return a String describing the repeat pattern of `this` alarm
 */
fun Alarm.getRepeatString(context: Context): String {
    when (repeatType) {
        Alarm.NON_REPEAT -> return ""
        Alarm.REPEAT_DAILY -> {
            if (repeatCycle == 1) {
                return context.getString(R.string.msg_days_for_repeat_single)
            }
            return context.resources.getQuantityString(R.plurals.days_for_repeat, repeatCycle,
                    repeatCycle)
        }
        Alarm.REPEAT_WEEKLY -> {
            val daysOfWeek = when (Locale.getDefault()) {
                Locale.CHINESE -> getShortWeekDays()
                else -> getShortWeekDays("E")
            }
            val indexStr = daysOfWeek.filterKeys {
                repeatIndex.isWeekdaySet(it)
            }.values.joinToString()

            if (repeatCycle == 1) {
                return context.getString(R.string.msg_weeks_for_repeat_single, null, indexStr)
            }
            return context.resources.getQuantityString(R.plurals.weeks_for_repeat, repeatCycle,
                    repeatCycle, indexStr)
        }
        Alarm.REPEAT_MONTHLY_BY_DATE -> {
            val daysOfMonth = MutableList(31) { index -> index + 1 }
            val indexStr = daysOfMonth.asSequence().filterIndexed { index, _ ->
                repeatIndex shr index and 1 == 1
            }.joinToString()
            if (repeatCycle == 1) {
                return context.getString(R.string.msg_months_for_repeat_single, null, indexStr)
            }
            return context.resources.getQuantityString(R.plurals.months_for_repeat, repeatCycle,
                    repeatCycle, indexStr)
        }
        Alarm.REPEAT_YEARLY_BY_DATE -> {
            val date = activateDate.createDateString(false, false)
            if (repeatCycle == 1) {
                return context.getString(R.string.msg_years_for_repeat_single, null, date)
            }
            return context.resources.getQuantityString(R.plurals.years_for_repeat, repeatCycle,
                    repeatCycle, date)
        }
        else -> return ""
    }
}

// TODO: Documentation
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