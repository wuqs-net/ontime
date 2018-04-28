package net.wuqs.ontime

import android.app.Activity
import android.content.Intent
import android.media.AudioManager
import android.os.Bundle
import android.support.design.widget.Snackbar
import android.support.v7.app.AppCompatActivity
import android.util.Log
import android.view.View
import android.widget.Toast
import kotlinx.android.synthetic.main.activity_main.*
import net.wuqs.ontime.alarm.*
import net.wuqs.ontime.db.Alarm
import net.wuqs.ontime.dialog.TimePickerFragment
import java.util.*


class MainActivity
    : AppCompatActivity(),
        AlarmListFragment.OnListFragmentInteractionListener,
        TimePickerFragment.OnTimeSetListener {

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_main)
        volumeControlStream = AudioManager.STREAM_ALARM
        fabCreateAlarm.setOnClickListener { onFabCreateAlarmClick() }
    }

    override fun onActivityResult(requestCode: Int, resultCode: Int, data: Intent?) {
        when (requestCode) {
            CREATE_ALARM_REQUEST, EDIT_ALARM_REQUEST -> {
                if (resultCode == Activity.RESULT_OK) {
                    val delta = data!!.getLongExtra(DELTA_NEXT_OCCURRENCE, 0)
                    val msg = getString(R.string.msg_alarm_will_go_off, getTimeDistanceString(this, delta))
                    Snackbar.make(fabCreateAlarm, msg, Snackbar.LENGTH_SHORT).show()
                }
            }
        }
    }

    override fun onTimeSet(tag: String?, hourOfDay: Int, minute: Int) {
        if (tag == TimePickerFragment.NEW_ALARM) {
            val intentSetAlarmActivity = Intent(this, SetAlarmActivity::class.java)
            intentSetAlarmActivity.apply {
                putExtra(IS_NEW_ALARM, true)
                putExtra(ALARM_ID, 0)
                putExtra(NEW_ALARM_HOUR, hourOfDay)
                putExtra(NEW_ALARM_MINUTE, minute)
            }
            startActivityForResult(intentSetAlarmActivity, CREATE_ALARM_REQUEST)
        }
    }

    override fun onListItemClick(item: Alarm) {
        Toast.makeText(this, item.toString(), Toast.LENGTH_SHORT).show()
        Log.v("MainActivity", item.toString())
        val intent = Intent(this, SetAlarmActivity::class.java)
        intent.apply {
            putExtra(ALARM_ID, item.id)
            putExtra(ALARM_INSTANCE, item)
        }
        startActivityForResult(intent, EDIT_ALARM_REQUEST)
    }

    override fun onAlarmSwitchClick(item: Alarm, isChecked: Boolean) {
        item.isEnabled = isChecked
        if (item.isEnabled) {
            // If next occurrence is before now, update the next occurrence
            if (item.nextOccurrence.before(Calendar.getInstance()))
                item.nextOccurrence = getNextAlarmOccurrence(item)
            val delta = getTimeDeltaFromNow(item)
            val msg = getString(R.string.msg_alarm_will_go_off, getTimeDistanceString(this, delta))
            Snackbar.make(fabCreateAlarm, msg, Snackbar.LENGTH_SHORT).show()
        }
        updateAlarm(this, item)
    }

    override fun onRecyclerViewUpdate(itemCount: Int) {
        tvNoAlarm.visibility = if (itemCount == 0) View.VISIBLE else View.GONE
        Log.d("MainActivity", "RecyclerView updated")
    }

    private fun onFabCreateAlarmClick() {
        val dialogFragment = TimePickerFragment.newInstance(this)
        dialogFragment.show(supportFragmentManager, TimePickerFragment.NEW_ALARM)
    }
}
