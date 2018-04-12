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

    /**
     * Gets all alarms from database
     *
     * @return a {@link LiveData} contains a {@link List} of {@link Alarm}s
     */
    @Query("SELECT * FROM alarms")
    LiveData<List<Alarm>> getAll();

    /**
     * Counts the number of alarms stored in database.
     *
     * @return an integer count of {@link Alarm}s stored in database
     */
    @Query("SELECT COUNT(*) FROM alarms")
    int count();

    @Query("SELECT * FROM alarms WHERE id IN (:ids)")
    LiveData<List<Alarm>> loadAllByIds(int[] ids);

    /**
     * Gets an {@link Alarm} from database by id.
     *
     * @param id id of the {@link Alarm}
     * @return an {@link Alarm} from database for a given id
     */
    @Query("SELECT * FROM alarms WHERE id = :id LIMIT 1")
    Alarm loadAlarm(int id);

    @Query("SELECT * FROM alarms WHERE id = :id LIMIT 1")
    LiveData<Alarm> getAlarm(int id);

    @Insert
    long[] insertAll(Alarm... alarms);

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    long insert(Alarm alarm);

    @Delete
    void delete(Alarm alarm);
}
