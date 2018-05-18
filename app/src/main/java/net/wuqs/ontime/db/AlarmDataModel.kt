package net.wuqs.ontime.db

import android.app.Application
import android.arch.lifecycle.AndroidViewModel
import android.arch.lifecycle.LiveData

class AlarmDataModel(application: Application) : AndroidViewModel(application) {
    private val db = AppDatabase[application]!!
    val alarms: LiveData<List<Alarm>> = db.alarmDAO.alarmsLive
}