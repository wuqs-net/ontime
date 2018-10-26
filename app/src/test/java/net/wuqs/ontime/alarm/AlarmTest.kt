package net.wuqs.ontime.alarm

import net.wuqs.ontime.db.Alarm
import org.junit.Test
import java.util.*
import java.util.Calendar.*

class AlarmTest {

    /**
     * Asserts that the `expected` time and the `actual` time are the same.
     */
    private fun assertSameTime(expected: Calendar?, actual: Calendar?) {
        assert(expected == actual) { "expected: ${expected?.time}, actual: ${actual?.time}" }
    }

    private fun createMonthlyRepeatIndex(vararg days: Int) = days.sumBy { 1 shl (it - 1) }

    private fun createWeeklyRepeatIndex(firstDay: Int, vararg days: Int): Int {
        return days.sumBy { 1 shl (it - 1) } + (firstDay shl 8)
    }

    // Test if the calculation is correct when current time is exactly when the alarm fires.
    @Test
    fun testRepeatDaily1() {
        val now = GregorianCalendar(2018, OCTOBER, 25, 9, 0)
        val alarm = Alarm(
                hour = 9,
                minute = 0,
                repeatType = Alarm.REPEAT_DAILY,
                repeatCycle = 1,
                activateDate = GregorianCalendar(2018, OCTOBER, 25)
        )
        val next = GregorianCalendar(2018, OCTOBER, 26, 9, 0)
        assertSameTime(next, alarm.getNextOccurrence(now))
    }

    // Test if the calculation is correct when the first day of week is not Sunday.
    @Test
    fun testRepeatWeekly1() {
        val now = GregorianCalendar(2018, OCTOBER, 26, 9, 0)
        val alarm = Alarm(
                hour = 8,
                minute = 0,
                repeatType = Alarm.REPEAT_WEEKLY,
                repeatCycle = 2,
                repeatIndex = createWeeklyRepeatIndex(MONDAY, MONDAY, SUNDAY),
                activateDate = GregorianCalendar(2018, OCTOBER, 25)
        )
        val next = GregorianCalendar(2018, OCTOBER, 28, 8, 0)
        assertSameTime(next, alarm.getNextOccurrence(now))
    }

    // Test if the calculation is correct when current time is exactly when the alarm fires.
    @Test
    fun testRepeatWeekly2() {
        val now = GregorianCalendar(2018, OCTOBER, 25, 9, 0)
        val alarm = Alarm(
                hour = 9,
                minute = 0,
                repeatType = Alarm.REPEAT_WEEKLY,
                repeatCycle = 1,
                repeatIndex = 0b0111110 + (1 shl 8),
                activateDate = GregorianCalendar(2018, OCTOBER, 25)
        )
        val next = GregorianCalendar(2018, OCTOBER, 26, 9, 0)
        assertSameTime(next, alarm.getNextOccurrence(now))
    }

    // Test if the calculation is correct when there are multiple dates in a month on which the
    // alarm needs to fire on.
    @Test
    fun testRepeatMonthly1() {
        val now = GregorianCalendar(2018, OCTOBER, 26, 9, 0)
        val alarm = Alarm(
                hour = 23,
                minute = 45,
                repeatType = Alarm.REPEAT_MONTHLY_BY_DATE,
                repeatCycle = 2,
                repeatIndex = createMonthlyRepeatIndex(25, 27, 29),
                activateDate = GregorianCalendar(2018, OCTOBER, 25)
        )
        val next = GregorianCalendar(2018, OCTOBER, 27, 23, 45)
        assertSameTime(next, alarm.getNextOccurrence(now))
    }

    // Test if the calculation is correct when current time is exactly when the alarm fires.
    @Test
    fun testRepeatMonthly2() {
        val now = GregorianCalendar(2018, OCTOBER, 25, 9, 0)
        val alarm = Alarm(
                hour = 9,
                minute = 0,
                repeatType = Alarm.REPEAT_MONTHLY_BY_DATE,
                repeatCycle = 1,
                repeatIndex = createMonthlyRepeatIndex(25, 26, 27),
                activateDate = GregorianCalendar(2018, OCTOBER, 25)
        )
        val next = GregorianCalendar(2018, OCTOBER, 26, 9, 0)
        assertSameTime(next, alarm.getNextOccurrence(now))
    }

    // Test if the calculation is correct when current time is exactly when the alarm fires.
    @Test
    fun testRepeatMonthly3() {
        val now = GregorianCalendar(2018, OCTOBER, 27, 9, 0)
        val alarm = Alarm(
                hour = 9,
                minute = 0,
                repeatType = Alarm.REPEAT_MONTHLY_BY_DATE,
                repeatCycle = 1,
                repeatIndex = createMonthlyRepeatIndex(25, 26, 27),
                activateDate = GregorianCalendar(2018, OCTOBER, 25)
        )
        val next = GregorianCalendar(2018, NOVEMBER, 25, 9, 0)
        assertSameTime(next, alarm.getNextOccurrence(now))
    }

    // Test if the calculation is correct when current time is exactly when the alarm fires.
    @Test
    fun testRepeatYearly1() {
        val now = GregorianCalendar(2018, OCTOBER, 25, 9, 0)
        val alarm = Alarm(
                hour = 9,
                minute = 0,
                repeatType = Alarm.REPEAT_YEARLY_BY_DATE,
                repeatCycle = 1,
                activateDate = GregorianCalendar(2018, OCTOBER, 25)
        )
        val next = GregorianCalendar(2019, OCTOBER, 25, 9, 0)
        assertSameTime(next, alarm.getNextOccurrence(now))
    }
}