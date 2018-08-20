package net.wuqs.ontime.ui.mainscreen

import android.app.ActivityManager.TaskDescription
import android.content.Intent
import android.graphics.BitmapFactory
import android.media.AudioManager
import android.os.Build
import android.os.Bundle
import android.support.v7.app.AppCompatActivity
import android.view.View
import android.widget.Toast
import kotlinx.android.synthetic.main.activity_main.*
import net.wuqs.ontime.R
import net.wuqs.ontime.alarm.*
import net.wuqs.ontime.db.Alarm
import net.wuqs.ontime.ui.alarmeditscreen.EditAlarmActivity
import net.wuqs.ontime.ui.dialog.TimePickerFragment
import net.wuqs.ontime.util.LogUtils
import java.util.*


class MainActivity : AppCompatActivity(),
        AlarmListFragment.OnListFragmentActionListener,
        TimePickerFragment.TimeSetListener {

    private lateinit var mAlarmUpdateHandler: AlarmUpdateHandler

//    val mReceiver = object : BroadcastReceiver() {
//        override fun onReceive(context: Context?, intent: Intent?) {
//            when (intent!!.action) {
//                OnTimeApplication.SHOW_MISSED_ALARMS_ACTION -> {
//                    val missedAlarms = intent.getParcelableArrayExtra(OnTimeApplication.EXTRA_MISSED_ALARMS)
//
//                    Toast.makeText(this@MainActivity, missedAlarms.toString(), Toast.LENGTH_SHORT).show()
//                }
//            }
//        }
//
//    }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_main)
        mLogger.v("onCreate")

        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.LOLLIPOP) {
            val bitmap = BitmapFactory.decodeResource(resources, R.mipmap.ic_launcher)
            val color = if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.M) {
                getColor(R.color.colorPrimaryDark)
            } else {
                @Suppress("DEPRECATION")
                resources.getColor(R.color.colorPrimaryDark)
            }
            val task = TaskDescription(getString(R.string.app_name), bitmap, color)
            setTaskDescription(task)
        }

        mAlarmUpdateHandler = AlarmUpdateHandler(this, fabCreateAlarm)

//        val filter = IntentFilter().apply {
//            addAction(OnTimeApplication.SHOW_MISSED_ALARMS_ACTION)
//        }
//        registerReceiver(mReceiver, filter)

        volumeControlStream = AudioManager.STREAM_ALARM
        fabCreateAlarm.setOnClickListener { onFabCreateAlarmClick() }
        sendBroadcast(AlarmStateManager.createScheduleAllAlarmsIntent(this))
    }

    override fun onActivityResult(requestCode: Int, resultCode: Int, data: Intent?) {
        when (requestCode) {
            CREATE_ALARM_REQUEST, EDIT_ALARM_REQUEST -> {
                if (resultCode == RESULT_SAVE) {
                    val alarm = data!!.getParcelableExtra<Alarm>(ALARM_INSTANCE)
                    alarm.nextTime = alarm.getNextOccurrence()
                    if (alarm.id == Alarm.INVALID_ID) {
                        mAlarmUpdateHandler.asyncAddAlarm(alarm, true)
                    } else {
                        mAlarmUpdateHandler.asyncUpdateAlarm(alarm, true)
                    }
                } else if (resultCode == RESULT_DELETE) {
                    val alarm = data!!.getParcelableExtra<Alarm>(ALARM_INSTANCE)
                    mAlarmUpdateHandler.asyncDeleteAlarm(alarm)
                }
            }
        }
    }

    override fun onTimeSet(tag: String?, hourOfDay: Int, minute: Int) {
        if (tag == TimePickerFragment.NEW_ALARM) {
            val editAlarmIntent = EditAlarmActivity.createIntent(this)
                    .putExtra(ALARM_INSTANCE, Alarm(hour = hourOfDay, minute = minute))
            startActivityForResult(editAlarmIntent, CREATE_ALARM_REQUEST)
        }
    }

    override fun onListItemClick(item: Alarm) {
        Toast.makeText(this, item.toString(), Toast.LENGTH_SHORT).show()
        mLogger.v("onListItemClick: $item")
        val editAlarmIntent = EditAlarmActivity.createIntent(this)
                .putExtra(ALARM_INSTANCE, item)
        startActivityForResult(editAlarmIntent, EDIT_ALARM_REQUEST)
    }

    override fun onAlarmSwitchClick(item: Alarm, isChecked: Boolean) {
        item.isEnabled = isChecked
//        if (item.getNextOccurrence() != null) {
//            item.isEnabled = isChecked
//            item.nextTime = item.getNextOccurrence()
//        } else {
//            shortToast(R.string.msg_cannot_set_past_time)
//        }
        mAlarmUpdateHandler.asyncUpdateAlarm(item, true)
    }

    override fun onRecyclerViewUpdate(itemCount: Int) {
        tvNoAlarm.visibility = if (itemCount == 0) View.VISIBLE else View.GONE
        mLogger.d("RecyclerView updated, item count: $itemCount")
    }

    private fun onFabCreateAlarmClick() {
        val hour = Calendar.getInstance().let {
            it[Calendar.HOUR_OF_DAY] + if (it[Calendar.MINUTE] < 50) 1 else 2
        }
        val minute = 0
        TimePickerFragment.newInstance(hour, minute)
                .show(supportFragmentManager, TimePickerFragment.NEW_ALARM)
    }

//    override fun onDestroy() {
//        unregisterReceiver(mReceiver)
//        super.onDestroy()
//    }

    companion object {
        const val RESULT_SAVE = 1
        const val RESULT_DELETE = 2
    }

    private val mLogger = LogUtils.Logger("MainActivity")
}
