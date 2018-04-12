package net.wuqs.ontime.receiver

import android.content.BroadcastReceiver
import android.content.Context
import android.content.Intent
import android.util.Log
import net.wuqs.ontime.AlarmActivity
import net.wuqs.ontime.alarm.ALARM_INSTANCE
import net.wuqs.ontime.db.Alarm

class MyAlarmReceiver : BroadcastReceiver() {

    override fun onReceive(context: Context, intent: Intent) {
        // This method is called when the BroadcastReceiver is receiving an Intent broadcast.
        val alarm: Alarm = intent.getBundleExtra(ALARM_INSTANCE).getParcelable(ALARM_INSTANCE)
        Log.i("MyAlarmReceiver", "Alarm started: $alarm")
        val myIntent = Intent(context, AlarmActivity::class.java).apply {
            addFlags(Intent.FLAG_ACTIVITY_NEW_TASK)
//            putExtra(ALARM_ID, intent.getIntExtra(ALARM_ID, -1)
            putExtra(ALARM_INSTANCE, alarm)
        }
        context.startActivity(myIntent)
    }
}
