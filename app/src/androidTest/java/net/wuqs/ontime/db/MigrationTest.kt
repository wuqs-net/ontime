package net.wuqs.ontime.db

import android.arch.persistence.db.SupportSQLiteDatabase
import android.arch.persistence.db.framework.FrameworkSQLiteOpenHelperFactory
import android.arch.persistence.room.testing.MigrationTestHelper
import android.content.ContentValues
import android.database.Cursor
import android.database.sqlite.SQLiteDatabase
import android.media.RingtoneManager
import android.net.Uri
import android.support.test.InstrumentationRegistry
import android.support.test.runner.AndroidJUnit4
import net.wuqs.ontime.alarm.setHms
import org.junit.Assert.assertTrue
import org.junit.Rule
import org.junit.Test
import org.junit.runner.RunWith
import java.io.IOException
import java.util.*

private const val ALARM_DB = "alarms"

@RunWith(AndroidJUnit4::class)
class MigrationTest {

    @Rule
    @JvmField
    val helper = MigrationTestHelper(
            InstrumentationRegistry.getInstrumentation(),
            AppDatabase::class.java.canonicalName,
            FrameworkSQLiteOpenHelperFactory()
    )

    @Test
    @Throws(IOException::class)
    fun migrate4To5() {
        var db = helper.createDatabase(ALARM_DB, 4)

        // Insert some data.
        db.execSQL("INSERT INTO '$ALARM_DB' VALUES (1, 12, 30, 'Test', 'uri', 1, 0, 0, 0, 12345612, NULL, 0)")
        db.close()

        db = helper.runMigrationsAndValidate(ALARM_DB, 5, true, AppDatabase.MIGRATION_4_5)

    }

    @Test
    @Throws(IOException::class)
    fun migrate6To7() {
        var db = helper.createDatabase(ALARM_DB, 6)

        // Insert some data.
        val alarms = Array(1000) { randomAlarm8((it + 1).toLong()) }
        for (alarm in alarms) db.insert7(alarm)
        db.close()

        db = helper.runMigrationsAndValidate(ALARM_DB, 7, true, AppDatabase.MIGRATION_6_7)
        val cursor = db.query("SELECT * FROM '$ALARM_DB'")

        while (cursor.moveToNext()) {
            val alarm = cursor.getAlarm7()
            println("expected: ${alarms[cursor.position]}")
            println("actual: $alarm")
            assertTrue(alarms[cursor.position] == alarm)
        }
    }

    @Test
    @Throws(IOException::class)
    fun testMigrate7To8() {
        var db = helper.createDatabase(ALARM_DB, 7)

        // Insert some data.
        val r = Random()
        val alarms = Array(1000) {
            if (r.nextBoolean()) {
                randomAlarm8(id = (it + 1).toLong(), ringtoneUri = null)
            } else {
                randomAlarm8(id = (it + 1).toLong())
            }
        }
        for (alarm in alarms) db.insert7(alarm)
        db.close()

        db = helper.runMigrationsAndValidate(ALARM_DB, 8, true, AppDatabase.MIGRATION_7_8)
        val cursor = db.query("SELECT * FROM '$ALARM_DB'")

        while (cursor.moveToNext()) {
            val alarm = cursor.getAlarm8()
            println("expected: ${alarms[cursor.position]}")
            println("actual: $alarm")
            assertTrue("expected: ${alarms[cursor.position]}\nactual: $alarm",
                    alarms[cursor.position] == alarm)
        }
    }

    /**
     * Inserts an [Alarm] into the database, for database version 7.
     */
    private fun SupportSQLiteDatabase.insert7(alarm: Alarm) {
        val values = ContentValues().apply {
            put("id", alarm.id)
            put("hour", alarm.hour)
            put("minute", alarm.minute)
            put("title", alarm.title)
            put("ringtone_uri", alarm.ringtoneUri.toString())
            put("vibrate", alarm.vibrate)
            put("enabled", alarm.isEnabled)
            put("repeat_type", alarm.repeatType)
            put("repeat_cycle", alarm.repeatCycle)
            put("repeat_index", alarm.repeatIndex)
            put("activate_date", alarm.activateDate!!.timeInMillis)
            put("next_occurrence", alarm.nextTime?.timeInMillis)
            put("snoozed", alarm.snoozed)
            put("notes", alarm.notes)
        }
        insert("alarms", SQLiteDatabase.CONFLICT_NONE, values)
    }

    /**
     * Inserts an [Alarm] into the database, for database version 8.
     */
    private fun SupportSQLiteDatabase.insert8(alarm: Alarm) {
        val values = ContentValues().apply {
            put("id", alarm.id)
            put("hour", alarm.hour)
            put("minute", alarm.minute)
            put("title", alarm.title)
            put("ringtone_uri", alarm.ringtoneUri.toStringOrNull())
            put("vibrate", alarm.vibrate)
            put("enabled", alarm.isEnabled)
            put("repeat_type", alarm.repeatType)
            put("repeat_cycle", alarm.repeatCycle)
            put("repeat_index", alarm.repeatIndex)
            put("activate_date", alarm.activateDate.toLong())
            put("next_occurrence", alarm.nextTime.toLong())
            put("snoozed", alarm.snoozed)
            put("notes", alarm.notes)
        }
        insert("alarms", SQLiteDatabase.CONFLICT_NONE, values)
    }

    /**
     * Gets the [Alarm] currently in the [Cursor], for database version 7.
     */
    private fun Cursor.getAlarm7(): Alarm {
        return Alarm(
                getLong(getColumnIndexOrThrow("id")),
                getInt(getColumnIndexOrThrow("hour")),
                getInt(getColumnIndexOrThrow("minute")),
                getString(getColumnIndexOrThrow("title")),
                Uri.parse(getString(getColumnIndexOrThrow("ringtone_uri"))),
                getInt(getColumnIndexOrThrow("vibrate")) != 0,
                getInt(getColumnIndexOrThrow("enabled")) != 0,
                getInt(getColumnIndexOrThrow("repeat_type")),
                getInt(getColumnIndexOrThrow("repeat_cycle")),
                getInt(getColumnIndexOrThrow("repeat_index")),
                Calendar.getInstance().apply {
                    timeInMillis = getLong(getColumnIndexOrThrow("activate_date"))
                },
                if (getType(getColumnIndexOrThrow("next_occurrence")) == Cursor.FIELD_TYPE_NULL) {
                    null
                } else {
                    Calendar.getInstance().apply {
                        timeInMillis = getLong(getColumnIndexOrThrow("next_occurrence"))
                    }
                },
                getInt(getColumnIndexOrThrow("snoozed")),
                getString(getColumnIndexOrThrow("notes"))
        )
    }

    /**
     * Gets the [Alarm] currently in the [Cursor], for database version 8.
     */
    private fun Cursor.getAlarm8(): Alarm {
        return Alarm(
                getLong(getColumnIndexOrThrow("id")),
                getInt(getColumnIndexOrThrow("hour")),
                getInt(getColumnIndexOrThrow("minute")),
                getString(getColumnIndexOrThrow("title")),
                getString(getColumnIndexOrThrow("ringtone_uri")).toUri(),
                getInt(getColumnIndexOrThrow("vibrate")).toBoolean(),
                getInt(getColumnIndexOrThrow("enabled")).toBoolean(),
                getInt(getColumnIndexOrThrow("repeat_type")),
                getInt(getColumnIndexOrThrow("repeat_cycle")),
                getInt(getColumnIndexOrThrow("repeat_index")),
                getLong(getColumnIndexOrThrow("activate_date")).toCalendar(),
                if (getType(getColumnIndexOrThrow("next_occurrence")) == Cursor.FIELD_TYPE_NULL) {
                    null
                } else {
                    getLong(getColumnIndexOrThrow("next_occurrence")).toCalendar()
                },
                getInt(getColumnIndexOrThrow("snoozed")),
                getString(getColumnIndexOrThrow("notes"))
        )
    }

    private fun randomAlarm8(
        id: Long? = null,
        title: String? = null,
        ringtoneUri: Uri? = RingtoneManager.getDefaultUri(RingtoneManager.TYPE_ALARM)
    ): Alarm {
        val r = Random()
        val repeatTypes = arrayOf(Alarm.NON_REPEAT, Alarm.REPEAT_DAILY, Alarm.REPEAT_WEEKLY,
                Alarm.REPEAT_MONTHLY_BY_DATE, Alarm.REPEAT_YEARLY_BY_DATE)
        return Alarm(
                id ?: generateId(),
                r.nextInt(24),
                r.nextInt(60),
                title ?: if (r.nextBoolean()) "" else "abc${r.nextInt()}abc",
                ringtoneUri,
                r.nextBoolean(),
                r.nextBoolean(),
                repeatTypes.let { it[r.nextInt(repeatTypes.size)] }
        ).apply {
            repeatCycle = if (repeatType == Alarm.NON_REPEAT) {
                0
            } else {
                r.nextInt(10) + 1
            }
            repeatIndex = when (repeatType) {
                Alarm.REPEAT_WEEKLY -> r.nextInt(0b1111111) + 1 + 0b100000000
                Alarm.REPEAT_MONTHLY_BY_DATE -> r.nextInt(0x7fffffff) + 1
                else -> 0
            }
            activateDate = Calendar.getInstance().apply {
                timeInMillis = timeInMillis - 1000000000 + r.nextInt(2000000000)
                setHms(0)
            }
            nextTime = getNextOccurrence()
            if (nextTime != null) {
                r.nextInt(5).let {
                    snoozed = it
                    nextTime?.add(Calendar.MINUTE, 3 * it)
                }
            } else {
                snoozed = 0
                isEnabled = false
            }
            notes = "notes for ${this.id}"
        }
    }

}