package net.wuqs.ontime.alarm

import net.wuqs.ontime.db.Alarm
import java.util.*
import java.util.concurrent.TimeUnit

/**
 * Finds the next time this non-repeat alarm will go off.
 */
fun Alarm.nextTimeNonRepeat(now: Calendar = Calendar.getInstance()): Calendar? {
    val next = activateDate!!.let {
        GregorianCalendar(it[Calendar.YEAR], it[Calendar.MONTH], it[Calendar.DAY_OF_MONTH],
                hour, minute)
    }
    return next.takeIf { it.after(now) }
}

/**
 * Finds the next time this daily alarm will go off.
 */
fun Alarm.nextTimeDaily(now: Calendar = Calendar.getInstance()): Calendar? {
    val next = activateDate!!.let {
        GregorianCalendar(it[Calendar.YEAR], it[Calendar.MONTH], it[Calendar.DAY_OF_MONTH],
                hour, minute)
    }
    if (!next.after(now)) {
        val c = (now dayDiff next) / repeatCycle + 1
        next.add(Calendar.DATE, c.toInt() * repeatCycle)
    }
    return next
}

/**
 * Finds the next time this weekly alarm will go off.
 */
fun Alarm.nextTimeWeekly(now: Calendar = Calendar.getInstance()): Calendar? {
    if (repeatIndex and 0x7F == 0) return null
    val next = activateDate!!.let {
        GregorianCalendar(it[Calendar.YEAR], it[Calendar.MONTH], it[Calendar.DAY_OF_MONTH],
                hour, minute)
    }
    val firstDayOfWeek = repeatIndex ushr 8
    val weekDays = getOrderedWeekDays(firstDayOfWeek)

    next.add(Calendar.DAY_OF_WEEK, -weekDays.indexOf(next[Calendar.DAY_OF_WEEK]))
    val week = (now dayDiff next) / (7 * repeatCycle)
    next.add(Calendar.DAY_OF_YEAR, week.toInt() * 7 * repeatCycle)

    for (i in weekDays) {
        if (repeatIndex.isWeekdaySet(i) && next.after(now) && next.after(activateDate)) return next
        next.add(Calendar.DAY_OF_WEEK, 1)
    }

    next.add(Calendar.DAY_OF_WEEK, 7 * (repeatCycle - 1))
    weekDays.forEachIndexed { index, i ->
        if (repeatIndex.isWeekdaySet(i)) {
            next.add(Calendar.DAY_OF_WEEK, index)
            return next
        }
    }
    return null
}

/**
 * Finds the next time this monthly alarm will go off.
 */
fun Alarm.nextTimeMonthlyByDate(now: Calendar = Calendar.getInstance()): Calendar? {
    if (repeatIndex == 0) return null

    val next = activateDate!!.let {
        GregorianCalendar(it[Calendar.YEAR], it[Calendar.MONTH], it[Calendar.DAY_OF_MONTH],
                hour, minute)
    }
    if (next.before(now)) {
        val c = (now monthDiff next) / repeatCycle
        next.add(Calendar.MONTH, c * repeatCycle)
        next[Calendar.DATE] = now[Calendar.DATE]
    }
    for (i in next[Calendar.DATE]..31) {
        if (repeatIndex.isMonthDaySet(i)) {
            next[Calendar.DATE] = i
            if (next.after(now)) {
                if (next[Calendar.DATE] == i) {
                    return next
                } else {
                    next.add(Calendar.MONTH, -1)
                    break
                }
            }
        }
    }
    return next.apply {
        for (i in 1..31) {
            if (repeatIndex.isMonthDaySet(i)) {
                do {
                    add(Calendar.MONTH, repeatCycle)
                } while (i > getActualMaximum(Calendar.DATE))
                set(Calendar.DATE, i)
                if (get(Calendar.DATE) != i) {
                    add(Calendar.MONTH, -1)
                    set(Calendar.DATE, getActualMaximum(Calendar.DATE))
                }
                break
            }
        }
    }
}

/**
 * Finds the next time this yearly alarm will go off.
 */
fun Alarm.nextTimeYearlyByDate(now: Calendar = Calendar.getInstance()): Calendar? {
    val next = activateDate!!.let {
        GregorianCalendar(it[Calendar.YEAR], it[Calendar.MONTH], it[Calendar.DAY_OF_MONTH],
                hour, minute)
    }
    if (next.after(now)) return next
    var (count, rem) = (now[Calendar.YEAR] - next[Calendar.YEAR]) divby repeatCycle
    if (rem != 0) count += 1
    next.add(Calendar.YEAR, count * repeatCycle)
    if (next.after(now)) return next
    return next.apply { add(Calendar.YEAR, repeatCycle) }
}

/**
 * Determines if the time set for this alarm is earlier than now. For example, it is 11:00 now, an
 * alarm set for 10:00 calling this function will return `true`.
 */
fun Alarm.isSetTimeEarlierThanNow(): Boolean {
    val now = Calendar.getInstance()
    return hour * 60 + minute <= now[Calendar.HOUR_OF_DAY] * 60 + now[Calendar.MINUTE]
}

/**
 * Checks if the day of week specified by [calendarDay] is set to `on`.
 *
 * @receiver an Integer representing the weekly repeat pattern.
 * @param calendarDay day of week indicated by [Calendar.SUNDAY], [Calendar.MONDAY], etc.
 */
internal fun Int.isWeekdaySet(calendarDay: Int) = this shr (calendarDay - 1) and 1 == 1

/**
 * Checks if the day of week specified by [calendarDay] is set to `on`.
 *
 * @receiver an Integer representing the monthly repeat pattern.
 * @param calendarDay day of month indicated by 1, 2, 3, etc.
 */
private fun Int.isMonthDaySet(calendarDay: Int) = this shr (calendarDay - 1) and 1 == 1

private infix fun Calendar.monthDiff(other: Calendar) =
        ((this[Calendar.YEAR] * 12 + this[Calendar.MONTH])
                - (other[Calendar.YEAR] * 12 + other[Calendar.MONTH]))

private infix fun Calendar.dayDiff(other: Calendar) =
        TimeUnit.MILLISECONDS.toDays((this.timeInMillis + this[Calendar.DST_OFFSET])
                - (other.timeInMillis + other[Calendar.DST_OFFSET]))

private infix fun Int.divby(other: Int) = Pair(this / other, this % other)