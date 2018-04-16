package net.wuqs.ontime

import android.app.Activity
import android.content.Intent
import android.media.AudioManager
import android.os.Bundle
import android.support.design.widget.Snackbar
import android.support.v7.app.AppCompatActivity
import android.support.v7.widget.RecyclerView
import android.support.v7.widget.SwitchCompat
import android.util.Log
import android.view.View
import android.widget.Toast
import kotlinx.android.synthetic.main.activity_main.*
import net.wuqs.ontime.alarm.*
import net.wuqs.ontime.db.Alarm
import net.wuqs.ontime.pickers.TimePickerFragment
import java.util.*


class MainActivity : AppCompatActivity(), AlarmFragment.OnListFragmentInteractionListener {

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_main)
        volumeControlStream = AudioManager.STREAM_ALARM
        fabCreateAlarm.setOnClickListener { v -> onFabCreateAlarmClick(v) }
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

    override fun onListItemClick(item: Alarm?) {
        Toast.makeText(this, item?.toString(), Toast.LENGTH_SHORT).show()
        Log.v("MainActivity", item?.toString())
        val intent = Intent(this, SetAlarmActivity::class.java).apply {
            putExtra(ALARM_ID, item?.id)
            putExtra(ALARM_INSTANCE, item)
        }
        startActivityForResult(intent, EDIT_ALARM_REQUEST)
    }

    override fun onAlarmSwitchClick(switch: SwitchCompat, alarm: Alarm) {
        alarm.isEnabled = switch.isChecked
        if (alarm.isEnabled) {
            // If next occurrence is before now, update the next occurrence
            if (alarm.nextOccurrence.before(Calendar.getInstance()))
                alarm.nextOccurrence = getNextAlarmOccurrence(alarm)
            val delta = getTimeDeltaFromNow(alarm)
            val msg = getString(R.string.msg_alarm_will_go_off, getTimeDistanceString(this, delta))
            Snackbar.make(fabCreateAlarm, msg, Snackbar.LENGTH_SHORT).show()
        }
        updateAlarm(this, alarm)
    }

    override fun onRecyclerViewUpdate(recyclerView: RecyclerView) {
        tvNoAlarm.visibility = if (recyclerView.adapter.itemCount == 0) View.VISIBLE else View.GONE
        Log.d("MainActivity", "RecyclerView updated")
    }

    private fun onFabCreateAlarmClick(v: View) {
        val dialogFragment = TimePickerFragment()
        dialogFragment.show(fragmentManager, TimePickerFragment.NEW_ALARM)
    }
}
