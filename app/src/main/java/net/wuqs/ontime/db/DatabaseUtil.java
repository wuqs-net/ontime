package net.wuqs.ontime.db;

import android.os.AsyncTask;
import android.util.Log;

import net.wuqs.ontime.alarm.Alarm;

import java.util.ArrayList;
import java.util.List;

public class DatabaseUtil {

    private static String TAG = "DatabaseUtil";

    public static void addAlarm(AppDatabase db, Alarm alarm) {
        AlarmModel model = new AlarmModel();
        model.set(alarm);
        int id = (int) db.alarmDAO().insert(model);
        alarm.setId(id);
        Log.d(TAG, "Alarm saved: " + alarm);
    }

    public static void updateAlarm(AppDatabase db, Alarm alarm) {
        AlarmModel model = new AlarmModel();
        model.set(alarm);
        model.setId(alarm.getId());
        db.alarmDAO().insert(model);
    }

    public static void deleteAlarm(AppDatabase db, Alarm alarm) {
        AlarmModel model = new AlarmModel();
        model.set(alarm);
        model.setId(alarm.getId());
        db.alarmDAO().delete(model);
    }
}
