package net.wuqs.ontime.db

import android.app.Application
import android.arch.lifecycle.AndroidViewModel
import android.arch.lifecycle.MediatorLiveData
import net.wuqs.ontime.alarm.isDaily
import net.wuqs.ontime.alarm.isMonthly
import net.wuqs.ontime.alarm.isWeekly
import net.wuqs.ontime.alarm.sameDayAs
import java.util.*

class AlarmDataModel(application: Application) : AndroidViewModel(application) {
    private val dao = AppDatabase.getInstance(application)!!.alarmDao
    val alarms = MediatorLiveData<List<Alarm>>()

    var dataState: Int = ALARMS_ALL
        set(value) {
            if (value != field) {
                field = value
                updateData()
            }
        }

    private val all = dao.alarmsHasNextTimeLive
    private val history = dao.historicalAlarmsLive

    init {
        alarms.addSource(all) { updateData() }
        alarms.addSource(history) { updateData() }
    }

    private fun updateData() {
        when (dataState) {
            ALARMS_ALL -> {
                all.value?.let { list -> alarms.value = list }
            }
            ALARMS_TODAY -> {
                val now = Calendar.getInstance()
                all.value?.let { list -> alarms.value = list.filter { now.sameDayAs(it.nextTime) } }
            }
            ALARMS_DAILY -> {
                all.value?.let { list ->
                    alarms.value = list.filter { it.isDaily() }
                }
            }
            ALARMS_WEEKLY -> {
                all.value?.let { list ->
                    alarms.value = list.filter { it.isWeekly() }
                }
            }
            ALARMS_MONTHLY -> {
                all.value?.let { list ->
                    alarms.value = list.filter { it.isMonthly() }
                }
            }
            ALARMS_HISTORY -> {
                history.value?.let { list -> alarms.value = list }
            }
        }
    }

    companion object {
        const val ALARMS_ALL = 0x00
        const val ALARMS_TODAY = 0x11
        const val ALARMS_DAILY = 0x21
        const val ALARMS_WEEKLY = 0x22
        const val ALARMS_MONTHLY = 0x23
        const val ALARMS_HISTORY = 0x01
    }
}