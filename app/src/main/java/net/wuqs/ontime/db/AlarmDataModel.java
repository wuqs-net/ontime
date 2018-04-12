package net.wuqs.ontime.db;

import android.app.Application;
import android.arch.lifecycle.AndroidViewModel;
import android.arch.lifecycle.LiveData;
import android.arch.lifecycle.ViewModel;

import java.util.List;

public class AlarmDataModel extends AndroidViewModel {

    private final LiveData<List<AlarmModel>> alarms;

    public AlarmDataModel(Application application) {
        super(application);
        alarms = AppDatabase.getInstance(getApplication()).alarmDAO().getAll();
    }

    public LiveData<List<AlarmModel>> getAlarms() {
        return alarms;
    }

    public LiveData<AlarmModel> getAlarm(int id) {
        return AppDatabase.getInstance(getApplication()).alarmDAO().getAlarm(id);
    }
}
