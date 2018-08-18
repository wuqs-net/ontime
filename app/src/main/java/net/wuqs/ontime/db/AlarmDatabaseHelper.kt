package net.wuqs.ontime.db

import net.wuqs.ontime.util.LogUtils

/**
 * Adds the [Alarm] to database.
 *
 * @param db the [AppDatabase] instance.
 * @return the [Alarm] saved to database, with its id updated.
 */
fun addAlarmToDb(db: AppDatabase, alarm: Alarm): Alarm {
    alarm.id = db.alarmDAO.insert(alarm)
    mLogger.i("Alarm added to database: $alarm")
    return alarm
}

/**
 * Updates the [Alarm] in database.
 *
 * @param db the [AppDatabase] instance.
 * @return the [Alarm] saved to database, with its id updated.
 */
fun updateAlarmToDb(db: AppDatabase, alarm: Alarm): Int {
    val count = db.alarmDAO.update(alarm)
    mLogger.i("Alarm updated in database: $alarm")
    return count
}

/**
 * Deletes the [Alarm] from database.
 *
 * @param db the [AppDatabase] instance.
 */
fun deleteAlarmFromDb(db: AppDatabase, alarm: Alarm) {
    db.alarmDAO.delete(alarm)
    mLogger.i("Alarm deleted from database: $alarm")
}

private val mLogger = LogUtils.Logger("AlarmDatabaseHelper")