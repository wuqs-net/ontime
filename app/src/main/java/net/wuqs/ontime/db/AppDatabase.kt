package net.wuqs.ontime.db

import android.arch.persistence.db.SupportSQLiteDatabase
import android.arch.persistence.room.Database
import android.arch.persistence.room.Room
import android.arch.persistence.room.RoomDatabase
import android.arch.persistence.room.TypeConverters
import android.arch.persistence.room.migration.Migration
import android.content.Context

@Database(entities = [Alarm::class], version = 5)
@TypeConverters(DataTypeConverter::class)
abstract class AppDatabase : RoomDatabase() {

    abstract val alarmDao: AlarmDao

    companion object {

        private var INSTANCE: AppDatabase? = null

        private val MIGRATION_1_2 = object : Migration(1, 2) {
            override fun migrate(database: SupportSQLiteDatabase) {
                with(database) {
                    execSQL("ALTER TABLE alarms ADD COLUMN enabled INTEGER NOT NULL DEFAULT 0")
                    execSQL("ALTER TABLE alarms ADD COLUMN repeat_type INTEGER NOT NULL DEFAULT 0")
                    execSQL("ALTER TABLE alarms ADD COLUMN repeat_cycle INTEGER NOT NULL DEFAULT 0")
                    execSQL("ALTER TABLE alarms ADD COLUMN repeat_index INTEGER NOT NULL DEFAULT 0")
                }
            }
        }

        private val MIGRATION_2_3 = object : Migration(2, 3) {
            override fun migrate(database: SupportSQLiteDatabase) {
                with(database) {
                    execSQL("ALTER TABLE alarms ADD COLUMN activate_date INTEGER")
                    execSQL("ALTER TABLE alarms ADD COLUMN next_occurrence INTEGER")
                }
            }
        }

        private val MIGRATION_3_4 = object : Migration(3, 4) {
            override fun migrate(database: SupportSQLiteDatabase) {
                with(database) {
                    execSQL("ALTER TABLE alarms ADD COLUMN snoozed INTEGER NOT NULL DEFAULT 0")
                }
            }
        }

        val MIGRATION_4_5 = newMigration(4, 5) {
            execSQL("ALTER TABLE alarms ADD COLUMN vibrate INTEGER NOT NULL DEFAULT 0")
        }

//        private val MIGRATION_4_5 = newMigration(4, 5) {
//            execSQL("CREATE TABLE alarms_new " +
//                    "(id INTEGER PRIMARY KEY, " +
//                    "hour INTEGER NOT NULL, minute INTEGER NOT NULL, " +
//                    "title TEXT, ringtone_uri TEXT, enabled INTEGER NOT NULL, " +
//                    "repeat_type INTEGER NOT NULL, repeat_cycle INTEGER NOT NULL, " +
//                    "repeat_index INTEGER NOT NULL, activate_date INTEGER, " +
//                    "next_occurrence INTEGER, snoozed INTEGER NOT NULL)")
//        }

        operator fun get(context: Context): AppDatabase? {
            if (INSTANCE == null) {
                synchronized(AppDatabase::class) {
                    INSTANCE = Room.databaseBuilder(context.applicationContext,
                            AppDatabase::class.java, "alarm-database")
                            .addMigrations(
                                    MIGRATION_1_2,
                                    MIGRATION_2_3,
                                    MIGRATION_3_4,
                                    MIGRATION_4_5
                            )
                            .build()
                }
            }
            return INSTANCE
        }

        fun destroyInstance() {
            INSTANCE = null
        }
    }
}

private fun newMigration(
    startVersion: Int,
    endVersion: Int,
    migrate: SupportSQLiteDatabase.() -> Unit
) = object : Migration(startVersion, endVersion) {
    override fun migrate(database: SupportSQLiteDatabase) {
        database.migrate()
    }
}