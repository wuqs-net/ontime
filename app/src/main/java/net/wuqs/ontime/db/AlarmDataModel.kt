package net.wuqs.ontime.db

import android.app.Application
import android.arch.lifecycle.AndroidViewModel

class AlarmDataModel(application: Application) : AndroidViewModel(application) {
    private val db = AppDatabase[application]!!
    val alarms = db.alarmDAO.getAllLive()
}