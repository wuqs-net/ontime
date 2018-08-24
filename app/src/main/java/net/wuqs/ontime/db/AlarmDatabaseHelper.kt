package net.wuqs.ontime.db

import net.wuqs.ontime.util.LogUtils
import java.security.MessageDigest
import java.util.*

/**
 * Adds the [Alarm] to database.
 *
 * @param db the [AppDatabase] instance.
 * @return the [Alarm] saved to database, with its id updated.
 */
fun addAlarmToDb(db: AppDatabase, alarm: Alarm): Alarm {
//    alarm.id = generateId()
    alarm.id = db.alarmDao.insert(alarm)
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
    val count = db.alarmDao.update(alarm)
    mLogger.i("Alarm updated in database: $alarm")
    return count
}

/**
 * Deletes the [Alarm] from database.
 *
 * @param db the [AppDatabase] instance.
 */
fun deleteAlarmFromDb(db: AppDatabase, alarm: Alarm) {
    db.alarmDao.delete(alarm)
    mLogger.i("Alarm deleted from database: $alarm")
}

fun generateMd5(): String = MessageDigest.getInstance("MD5").run {
    update(System.currentTimeMillis().toString().toByteArray())
    update(ByteArray(8).also { Random().nextBytes(it) })
    digest().joinToString("") { "%02x".format(it) }
}

fun generateId(): Long {
    return (System.currentTimeMillis() shl 17) + Random().nextInt(1 shl 17)
}

private val mLogger = LogUtils.Logger("AlarmDatabaseHelper")