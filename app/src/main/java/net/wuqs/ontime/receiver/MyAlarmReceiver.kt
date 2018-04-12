package net.wuqs.ontime.receiver

import android.content.BroadcastReceiver
import android.content.Context
import android.content.Intent
import android.util.Log
import net.wuqs.ontime.AlarmActivity
import net.wuqs.ontime.alarm.Alarm

class MyAlarmReceiver : BroadcastReceiver() {

    override fun onReceive(context: Context, intent: Intent) {
        // This method is called when the BroadcastReceiver is receiving an Intent broadcast.
        Log.i("MyAlarmReceiver", "Alarm started")
        val alarm: Alarm = intent.getBundleExtra(Alarm.ALARM_INSTANCE).getParcelable(Alarm.ALARM_INSTANCE)
        Log.v("MyAlarmReceiver", intent.extras.toString())
        val myIntent = Intent(context, AlarmActivity::class.java).apply {
            addFlags(Intent.FLAG_ACTIVITY_NEW_TASK)
//            putExtra(Alarm.ALARM_ID, intent.getIntExtra(Alarm.ALARM_ID, -1)
            putExtra(Alarm.ALARM_INSTANCE, alarm)
        }
        context.startActivity(myIntent)
    }
}
