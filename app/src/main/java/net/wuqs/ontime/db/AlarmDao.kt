package net.wuqs.ontime.db

import android.arch.lifecycle.LiveData
import android.arch.persistence.room.*
import android.arch.persistence.room.OnConflictStrategy.REPLACE

@Dao
interface AlarmDao {

    /** All alarms in the database. */
    @Query("SELECT * FROM alarms ORDER BY next_occurrence IS NULL, next_occurrence")
    fun getAllLive(): LiveData<List<Alarm>>

    /** All alarms in the database. */
    @get:Query("SELECT * FROM alarms")
    val allSync: List<Alarm>

    @get:Query("SELECT * FROM alarms WHERE next_occurrence IS NOT NULL ORDER BY next_occurrence")
    val alarmsHasNextTime: List<Alarm>

    @get:Query("SELECT * FROM alarms WHERE next_occurrence IS NOT NULL ORDER BY next_occurrence")
    val alarmsHasNextTimeLive: LiveData<List<Alarm>>

    @get:Query("SELECT * FROM alarms WHERE next_occurrence IS NULL ORDER BY activate_date DESC")
    val historicalAlarmsLive: LiveData<List<Alarm>>

    /** The number of alarms stored in database. */
    @get:Query("SELECT COUNT(*) FROM alarms")
    val count: Int

    /** Returns the [Alarm] of the given id. */
    @Query("SELECT * FROM alarms WHERE id = :id LIMIT 1")
    operator fun get(id: Int): Alarm

    @Query("SELECT * FROM alarms WHERE id = :id LIMIT 1")
    fun getAlarm(id: Int): LiveData<Alarm>

    @Insert(onConflict = REPLACE)
    fun insertAll(vararg alarms: Alarm): LongArray

    @Insert(onConflict = REPLACE)
    fun insert(alarm: Alarm): Long

    @Update
    fun update(alarm: Alarm): Int

    @Update
    fun updateAll(alarms: Collection<Alarm>)

    @Delete
    fun delete(vararg alarms: Alarm): Int
}
