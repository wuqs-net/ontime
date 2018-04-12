package net.wuqs.ontime.db;

import android.app.Application;
import android.arch.lifecycle.AndroidViewModel;
import android.arch.lifecycle.LiveData;

import java.util.List;

public class AlarmDataModel extends AndroidViewModel {

    private final LiveData<List<Alarm>> alarms;

    public AlarmDataModel(Application application) {
        super(application);
        alarms = AppDatabase.getInstance(getApplication()).alarmDAO().getAll();
    }

    public LiveData<List<Alarm>> getAlarms() {
        return alarms;
    }

    public LiveData<Alarm> getAlarm(int id) {
        return AppDatabase.getInstance(getApplication()).alarmDAO().getAlarm(id);
    }
}
