package net.wuqs.ontime.db;

import android.util.Log;

public class DatabaseUtil {

    private static String TAG = "DatabaseUtil";

    public static void addAlarm(AppDatabase db, Alarm alarm) {
        int id = (int) db.alarmDAO().insert(alarm);
        alarm.setId(id);
        Log.d(TAG, "Alarm saved: " + alarm);
    }

    public static void updateAlarm(AppDatabase db, Alarm alarm) {
        db.alarmDAO().insert(alarm);
    }

    public static void deleteAlarm(AppDatabase db, Alarm alarm) {
        db.alarmDAO().delete(alarm);
    }
}
