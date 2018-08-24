package net.wuqs.ontime.db

import android.arch.persistence.room.Room
import android.support.test.InstrumentationRegistry
import android.support.test.runner.AndroidJUnit4
import org.junit.After
import org.junit.Before
import org.junit.runner.RunWith

@RunWith(AndroidJUnit4::class)
class DbReadWriteTest {
    private lateinit var mAlarmDao: AlarmDao
    private lateinit var mDb: AppDatabase

    @Before
    fun createDb() {
        val context = InstrumentationRegistry.getTargetContext()
        mDb = Room.inMemoryDatabaseBuilder(context, AppDatabase::class.java).build()
        mAlarmDao = mDb.alarmDao
    }

    @After
    fun closeDb() {
        mDb.close()
    }

}