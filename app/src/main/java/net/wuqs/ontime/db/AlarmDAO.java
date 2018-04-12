package net.wuqs.ontime.db;

import android.arch.lifecycle.LiveData;
import android.arch.persistence.room.Dao;
import android.arch.persistence.room.Delete;
import android.arch.persistence.room.Insert;
import android.arch.persistence.room.OnConflictStrategy;
import android.arch.persistence.room.Query;

import java.util.List;

@Dao
public interface AlarmDAO {
    @Query("SELECT * FROM alarms")
    LiveData<List<AlarmModel>> getAll();

    @Query("SELECT COUNT(*) FROM alarms")
    int count();

    @Query("SELECT * FROM alarms WHERE id IN (:alarmIds)")
    LiveData<List<AlarmModel>> loadAllByIds(int[] alarmIds);

    @Query("SELECT * FROM alarms WHERE id = :alarmId")
    AlarmModel loadAlarm(int alarmId);

    @Query("SELECT * FROM alarms WHERE id = :alarmId")
    LiveData<AlarmModel> getAlarm(int alarmId);

    @Insert
    long[] insertAll(AlarmModel... alarms);

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    long insert(AlarmModel alarm);

    @Delete
    void delete(AlarmModel alarm);
}
