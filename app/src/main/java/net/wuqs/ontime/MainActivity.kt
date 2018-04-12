package net.wuqs.ontime

import android.app.Activity
import android.app.TimePickerDialog
import android.content.Intent
import android.media.AudioManager
import android.os.Bundle
import android.support.design.widget.Snackbar
import android.support.v7.app.AppCompatActivity
import android.support.v7.widget.RecyclerView
import android.util.Log
import android.view.View
import android.widget.Toast
import kotlinx.android.synthetic.main.activity_main.*
import net.wuqs.ontime.alarm.*
import net.wuqs.ontime.db.Alarm
import java.util.*


class MainActivity : AppCompatActivity(), AlarmFragment.OnListFragmentInteractionListener {
    private val CREATE_ALARM_REQUEST = 2

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_main)
        volumeControlStream = AudioManager.STREAM_ALARM
        fabCreateAlarm.setOnClickListener { v -> onFabCreateAlarmClick(v) }
    }

    override fun onListFragmentInteraction(item: Alarm?) {
        Toast.makeText(this, item?.toString(), Toast.LENGTH_SHORT).show()
        val intent = Intent(this, SetAlarmActivity::class.java).apply {
            putExtra(ALARM_ID, item?.id)
            putExtra(ALARM_INSTANCE, item)
        }
        startActivityForResult(intent, CREATE_ALARM_REQUEST)
    }

    override fun onActivityResult(requestCode: Int, resultCode: Int, data: Intent?) {
        when (requestCode) {
            CREATE_ALARM_REQUEST ->
                if (resultCode == Activity.RESULT_OK) {
                    val delta = data!!.getLongExtra(NEW_CREATED_ALARM, 0)
                    val msg = getString(R.string.msg_alarm_will_go_off, getTimeDistanceString(this, delta))
//                    (fragment as AlarmFragment).updateRecyclerView()
                    Snackbar.make(fabCreateAlarm, msg, Snackbar.LENGTH_SHORT).show()
                }
        }
    }

    override fun onRecyclerViewUpdate(recyclerView: RecyclerView) {
        tvNoAlarm.visibility = if (recyclerView.adapter.itemCount == 0) View.VISIBLE else View.GONE
        Log.d("MainActivity", "Recyclerview updated")
//        Toast.makeText(this, recyclerView.adapter.itemCount.toString(), Toast.LENGTH_SHORT).show()
    }

    fun onFabCreateAlarmClick(v: View) {
        val calendar = Calendar.getInstance()
        val myHour = calendar.get(Calendar.HOUR_OF_DAY)
        val myMinute = calendar.get(Calendar.MINUTE)

        val onTimeSetListener = TimePickerDialog.OnTimeSetListener { view, hourOfDay, minute ->
            val intentSetAlarmActivity = Intent(this, SetAlarmActivity::class.java)
                    .apply {
                        putExtra(IS_NEW_ALARM, true)
                        putExtra(ALARM_ID, -1)
                        putExtra(NEW_ALARM_HOUR, hourOfDay)
                        putExtra(NEW_ALARM_MINUTE, minute)
                    }
            startActivityForResult(intentSetAlarmActivity, CREATE_ALARM_REQUEST)
        }

        val dialog = createTimePickerDialog(this, onTimeSetListener, myHour, myMinute)
        dialog.show()
    }
}
