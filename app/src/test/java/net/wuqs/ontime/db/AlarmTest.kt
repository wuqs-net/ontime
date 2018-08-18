package net.wuqs.ontime.db

import org.junit.Test

import java.util.*

class AlarmTest {

    val alarm = Alarm()
    val next = Calendar.getInstance().apply { set(Calendar.MILLISECOND, 0) }!!

    @Test
    fun testRepeatDaily() {
        //        alarm.apply {
//            hour = 9
//            minute = 0
//            repeatType = Alarm.REPEAT_DAILY
//            repeatCycle = 2
//            repeatIndex = 0
//            activateDate = Calendar.getInstance().apply {
//                set(2018, 4, 8, hour, minute, 0)
//                set(Calendar.MILLISECOND, 0)
//            }
//            getNextOccurrence()
//        }
//        next.apply {
//            set(2018, 4, 12, 9, 0, 0)
//            set(Calendar.MILLISECOND, 0)
//        }
//        println(alarm.nextTime!!.time)
//        assert(next.compareTo(alarm.nextTime!!) == 0)
//
//        alarm.apply {
//            hour = 4
//            minute = 0
//            repeatType = Alarm.REPEAT_DAILY
//            repeatCycle = 2
//            repeatIndex = 0
//            activateDate = Calendar.getInstance().apply {
//                set(2018, 2, 1, hour, minute, 0)
//                set(Calendar.MILLISECOND, 0)
//            }
//            getNextOccurrence()
//        }
//        next.apply {
//            set(2018, 4, 12, 4, 0, 0)
//            set(Calendar.MILLISECOND, 0)
//        }
//        println(alarm.nextTime!!.time)
//        assert(next.compareTo(alarm.nextTime!!) == 0)
//
//        alarm.apply {
//            hour = 1
//            minute = 0
//            repeatType = Alarm.REPEAT_DAILY
//            repeatCycle = 1
//            repeatIndex = 0
//            activateDate = Calendar.getInstance().apply {
//                set(1970, 1, 1, hour, minute, 0)
//                set(Calendar.MILLISECOND, 0)
//            }
//            getNextOccurrence()
//        }
//        next.apply {
//            set(2018, 4, 11, 1, 0, 0)
//            set(Calendar.MILLISECOND, 0)
//        }
//        println(alarm.nextTime!!.time)
//        assert(next.compareTo(alarm.nextTime!!) == 0)

//        alarm.apply {
//            hour = 20
//            minute = 8
//            repeatType = Alarm.REPEAT_DAILY
//            repeatCycle = 1
//            repeatIndex = 0
//            activateDate = Calendar.getInstance().apply {
//                set(2018, 4, 11, 0, 0, 0)
//                set(Calendar.MILLISECOND, 0)
//            }
//            getNextOccurrence()
//        }
//        next.apply {
//            set(2018, Calendar.MAY, 13, 20, 8, 0)
//            set(Calendar.MILLISECOND, 0)
//        }
//        println(alarm.nextTime!!.time)
//        assert(next.compareTo(alarm.nextTime!!) == 0)
    }

    @Test
    fun testRepeatWeekly() {
        alarm.apply {
            hour = 23
            minute = 41
            repeatType = Alarm.REPEAT_WEEKLY
            repeatCycle = 2
            repeatIndex = 0b1000010 + (2 shl 8)
            activateDate = GregorianCalendar(2018, Calendar.AUGUST, 12)
            nextTime = getNextOccurrence()
        }
        next.apply {
            set(2018, Calendar.AUGUST, 20, alarm.hour, alarm.minute, 0)
        }
        println("activate: ${alarm.activateDate?.time}, next: ${alarm.nextTime?.time}")
        assert(next == alarm.nextTime)

        alarm.apply {
            hour = 23
            minute = 41
            repeatType = Alarm.REPEAT_WEEKLY
            repeatCycle = 1
            repeatIndex = 0b1000010 + (1 shl 8)
            activateDate = GregorianCalendar(2018, Calendar.AUGUST, 12)
            nextTime = getNextOccurrence()
        }
        next.apply {
            set(2018, Calendar.AUGUST, 18, alarm.hour, alarm.minute, 0)
        }
        println("activate: ${alarm.activateDate?.time}, next: ${alarm.nextTime?.time}")
        assert(next == alarm.nextTime)
    }

    @Test
    fun testRepeatMonthly() {
        alarm.apply {
            hour = 23
            minute = 41
            repeatType = Alarm.REPEAT_MONTHLY_BY_DATE
            repeatCycle = 1
            repeatIndex = 0b111111111111000010
            activateDate = GregorianCalendar(2018, Calendar.AUGUST, 12)
            nextTime = getNextOccurrence()
        }
        next.apply {
            set(2018, Calendar.AUGUST, 18, alarm.hour, alarm.minute, 0)
        }
        println("activate: ${alarm.activateDate?.time}, next: ${alarm.nextTime?.time}")
        assert(next == alarm.nextTime)
    }

    @Test
    fun updateNextOccurrence() {
//        alarm.apply {
//            hour = 23
//            minute = 41
//            repeatType = Alarm.REPEAT_MONTHLY_BY_DATE
//            repeatCycle = 1
//            repeatIndex = 0b101
//            activateDate = Calendar.getInstance().apply {
//                set(1950, Calendar.NOVEMBER, 4, 0, 0, 0)
//                set(Calendar.MILLISECOND, 0)
//            }
//            nextTime = getNextOccurrence()
//        }
//        next.apply {
//            set(2018, Calendar.AUGUST, 3, alarm.hour, alarm.minute, 0)
//        }
//        println("activate: ${alarm.activateDate?.time}, next: ${alarm.nextTime?.time}")
//        assert(next == alarm.nextTime)
//
//        alarm.apply {
//            hour = 23
//            minute = 41
//            repeatType = Alarm.REPEAT_MONTHLY_BY_DATE
//            repeatCycle = 2
//            repeatIndex = 0b111
//            activateDate = Calendar.getInstance().apply {
//                set(2018, Calendar.AUGUST, 4, 0, 0, 0)
//                set(Calendar.MILLISECOND, 0)
//            }
//            nextTime = getNextOccurrence()
//        }
//        next.apply {
//            set(2018, Calendar.OCTOBER, 1, alarm.hour, alarm.minute, 0)
//        }
//        println("activate: ${alarm.activateDate?.time}, next: ${alarm.nextTime?.time}")
//        assert(next == alarm.nextTime)

        alarm.apply {
            hour = 23
            minute = 41
            repeatType = Alarm.REPEAT_MONTHLY_BY_DATE
            repeatCycle = 2
            repeatIndex = 0b100000000001
            activateDate = Calendar.getInstance().apply {
                set(2017, Calendar.SEPTEMBER, 4, 0, 0, 0)
                set(Calendar.MILLISECOND, 0)
            }
            nextTime = getNextOccurrence(GregorianCalendar(2018, Calendar.AUGUST, 5))
        }
        next.apply {
            set(2018, Calendar.SEPTEMBER, 1, alarm.hour, alarm.minute, 0)
        }
        println("activate: ${alarm.activateDate?.time}, next: ${alarm.nextTime?.time}")
        assert(next == alarm.nextTime)
    }
}