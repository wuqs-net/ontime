package net.wuqs.ontime.db

import androidx.room.Room
import androidx.test.ext.junit.runners.AndroidJUnit4
import androidx.test.platform.app.InstrumentationRegistry
import org.junit.After
import org.junit.Before
import org.junit.runner.RunWith

@RunWith(AndroidJUnit4::class)
class DbReadWriteTest {
    private lateinit var mAlarmDao: AlarmDao
    private lateinit var mDb: AppDatabase

    @Before
    fun createDb() {
        val context = InstrumentationRegistry.getInstrumentation().targetContext
        mDb = Room.inMemoryDatabaseBuilder(context, AppDatabase::class.java).build()
        mAlarmDao = mDb.alarmDao
    }

    @After
    fun closeDb() {
        mDb.close()
    }

}