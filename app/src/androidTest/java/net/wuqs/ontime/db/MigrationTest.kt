package net.wuqs.ontime.db

import android.arch.persistence.db.framework.FrameworkSQLiteOpenHelperFactory
import android.arch.persistence.room.testing.MigrationTestHelper
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

//        val values = ContentValues().apply {
//
//        }
//        db.insert(TEST_DB, SQLiteDatabase.CONFLICT_NONE, values)

        // Insert some data
        db.execSQL("INSERT INTO '$TEST_DB' VALUES (1, 12, 30, 'Test', 'uri', 1, 0, 0, 0, 12345612, NULL, 0)")
        db.close()

        db = helper.runMigrationsAndValidate(TEST_DB, 5, true, AppDatabase.MIGRATION_4_5)

//        val cursor = db.query("SELECT * from '$TEST_DB'")
    }

    companion object {
        private const val TEST_DB = "alarms"
    }
}