package net.wuqs.ontime.db

import android.arch.persistence.db.framework.FrameworkSQLiteOpenHelperFactory
import android.arch.persistence.room.testing.MigrationTestHelper
import android.database.Cursor
import android.support.test.InstrumentationRegistry
import android.support.test.runner.AndroidJUnit4
import org.junit.Rule
import org.junit.Test
import org.junit.runner.RunWith
import java.io.IOException

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
        var db = helper.createDatabase(TEST_DB, 4)

        // Insert some data
        db.execSQL("INSERT INTO '$TEST_DB' VALUES (1, 12, 30, 'Test', 'uri', 1, 0, 0, 0, 12345612, NULL, 0)")
        db.close()

        db = helper.runMigrationsAndValidate(TEST_DB, 5, true, AppDatabase.MIGRATION_4_5)

    }

    @Test
    @Throws(IOException::class)
    fun migrate5To6() {
        var db = helper.createDatabase(TEST_DB, 5)

        // Insert some data
        db.execSQL("INSERT INTO '$TEST_DB' VALUES (1, 12, 30, 'Test', 'uri', 1, 1, 0x11, 2, 0, 123456121111, NULL, 0)")
        db.close()

        db = helper.runMigrationsAndValidate(TEST_DB, 6, true, AppDatabase.MIGRATION_5_6)
        val cursor = db.query("SELECT * FROM '$TEST_DB' LIMIT 1")
        cursor.moveToFirst()

        for (i in 0 until cursor.columnCount) {
            print("${cursor.getColumnName(i)}: ")
            when (cursor.getType(i)) {
                Cursor.FIELD_TYPE_INTEGER -> println(cursor.getLong(i))
                Cursor.FIELD_TYPE_STRING -> println(cursor.getString(i))
                Cursor.FIELD_TYPE_NULL -> println("NULL")
            }
        }
    }

    companion object {
        private const val TEST_DB = "alarms"
    }
}