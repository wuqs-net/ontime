package net.wuqs.ontime.ui.mainscreen

import android.content.BroadcastReceiver
import android.content.Context
import android.content.Intent
import android.content.IntentFilter
import android.media.AudioManager
import android.os.Build
import android.os.Bundle
import android.support.v4.content.LocalBroadcastManager
import android.support.v7.app.AppCompatActivity
import android.view.View
import android.widget.Toast
import kotlinx.android.synthetic.main.activity_main.*
import net.wuqs.ontime.R
import net.wuqs.ontime.alarm.*
import net.wuqs.ontime.db.Alarm
import net.wuqs.ontime.ui.alarmeditscreen.EditAlarmActivity
import net.wuqs.ontime.ui.dialog.TimePickerDialogFragment
import net.wuqs.ontime.ui.missedalarms.MissedAlarmsActivity
import net.wuqs.ontime.util.LogUtils
import net.wuqs.ontime.util.getCustomTaskDescription
import java.util.*


class MainActivity : AppCompatActivity(),
        AlarmListFragment.OnListFragmentActionListener,
        TimePickerDialogFragment.OnTimeSetListener {

    private lateinit var mAlarmUpdateHandler: AlarmUpdateHandler

    private val receiver = object : BroadcastReceiver() {
        override fun onReceive(context: Context?, intent: Intent?) {
            logger.d("onReceive(): ${intent?.action}")
            when (intent!!.action) {
                ACTION_SHOW_MISSED_ALARMS -> {
                    if (!intent.hasExtra(EXTRA_MISSED_ALARMS)) return
                    logger.i("Show missed alarms")
                    val missedAlarmsIntent = Intent(
                            this@MainActivity,
                            MissedAlarmsActivity::class.java
                    ).apply {
                        putExtras(intent)
                    }
                    startActivity(missedAlarmsIntent)
                }
            }
        }

    }
    // TODO: display missed alarm

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_main)
        logger.v("onCreate")

        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.LOLLIPOP) {
            setTaskDescription(getCustomTaskDescription())
        }

        mAlarmUpdateHandler = AlarmUpdateHandler(this, fabCreateAlarm)

        val filter = IntentFilter(ACTION_SHOW_MISSED_ALARMS)
        val lbm = LocalBroadcastManager.getInstance(this)
        lbm.registerReceiver(receiver, filter)

        volumeControlStream = AudioManager.STREAM_ALARM
        fabCreateAlarm.setOnClickListener { onFabCreateAlarmClick() }
        sendBroadcast(AlarmStateManager.createScheduleAllAlarmsIntent(this))
    }

    override fun onActivityResult(requestCode: Int, resultCode: Int, data: Intent?) {
        when (requestCode) {
            CREATE_ALARM_REQUEST, EDIT_ALARM_REQUEST -> {
                if (resultCode == RESULT_SAVE) {
                    val alarm = data!!.getParcelableExtra<Alarm>(ALARM_INSTANCE)
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

    override fun onTimeSet(fragment: TimePickerDialogFragment, hourOfDay: Int, minute: Int) {
        if (fragment.tag == TAG_NEW_ALARM) {
            val alarm = Alarm(hour = hourOfDay, minute = minute)
            // TODO: automatically change start date if alarm time is before now
            val editAlarmIntent = EditAlarmActivity.createIntent(this)
                    .putExtra(ALARM_INSTANCE, alarm)
            startActivityForResult(editAlarmIntent, CREATE_ALARM_REQUEST)
        }
    }

    override fun onListItemClick(item: Alarm) {
        Toast.makeText(this, item.toString(), Toast.LENGTH_SHORT).show()
        logger.v("onListItemClick: $item")
        val editAlarmIntent = EditAlarmActivity.createIntent(this)
                .putExtra(ALARM_INSTANCE, item)
        startActivityForResult(editAlarmIntent, EDIT_ALARM_REQUEST)
    }

    override fun onAlarmSwitchClick(item: Alarm, isChecked: Boolean) {
        item.isEnabled = isChecked
        mAlarmUpdateHandler.asyncUpdateAlarm(item, true)
    }

    override fun onRecyclerViewUpdate(itemCount: Int) {
        tvNoAlarm.visibility = if (itemCount == 0) View.VISIBLE else View.GONE
        logger.d("RecyclerView updated, item count: $itemCount")
    }

    private fun onFabCreateAlarmClick() {
        val calendar = Calendar.getInstance().apply {
            add(Calendar.HOUR_OF_DAY, if (this[Calendar.MINUTE] < 50) 1 else 2)
            this[Calendar.MINUTE] = 0
        }
        val hour = calendar[Calendar.HOUR_OF_DAY]
        val minute = calendar[Calendar.MINUTE]
        TimePickerDialogFragment.show(this, hour, minute, TAG_NEW_ALARM)
    }

    override fun onDestroy() {
        val lbm = LocalBroadcastManager.getInstance(this)
        lbm.unregisterReceiver(receiver)
        super.onDestroy()
    }

    companion object {
        const val RESULT_SAVE = 1
        const val RESULT_DELETE = 2
    }

    private val logger = LogUtils.Logger("MainActivity")
}

private const val TAG_NEW_ALARM = "NEW_ALARM"
