package net.wuqs.ontime.db

import org.junit.Test

import java.util.*

class AlarmTest {

    @Test
    fun updateNextOccurrence() {
        val alarm = Alarm()
        val next: Calendar

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

        alarm.apply {
            hour = 23
            minute = 41
            repeatType = Alarm.REPEAT_DAILY
            repeatCycle = 1
            repeatIndex = 0
            activateDate = Calendar.getInstance().apply {
                set(1950, Calendar.NOVEMBER, 4, 0, 0, 0)
                set(Calendar.MILLISECOND, 0)
            }
            nextTime = getNextOccurrence()
        }
        next = Calendar.getInstance().apply {
            set(2018, Calendar.MAY, 19, alarm.hour, alarm.minute, 0)
            set(Calendar.MILLISECOND, 0)
        }
        println("activate: ${alarm.activateDate?.time}, next: ${alarm.nextTime?.time}")
        assert(next == alarm.nextTime)
    }
}