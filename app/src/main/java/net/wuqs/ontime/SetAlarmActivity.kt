package net.wuqs.ontime

import android.app.Activity
import android.app.TimePickerDialog
import android.content.Intent
import android.media.RingtoneManager
import android.os.Bundle
import android.support.v7.app.AlertDialog
import android.support.v7.app.AppCompatActivity
import android.view.Menu
import android.view.MenuItem
import android.view.View
import android.widget.Toast
import kotlinx.android.synthetic.main.activity_set_alarm.*
import net.wuqs.ontime.alarm.*
import net.wuqs.ontime.db.Alarm

class SetAlarmActivity : AppCompatActivity() {

    val TAG = "SetAlarmActivity"
    lateinit var alarm: Alarm
    var alarmId = -1

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_set_alarm)
        tvTime.setOnClickListener { v -> onTimeClick(v) }

        alarmId = intent.getIntExtra(ALARM_ID, -1)

        if (alarmId != -1) {
            title = getString(R.string.title_edit_alarm)
            alarm = intent.getParcelableExtra(ALARM_INSTANCE)
            tvTime.text = getTimeString(this, alarm)
            tilTitle.editText?.setText(alarm.title)
        } else {
            alarm = Alarm(intent.getIntExtra(NEW_ALARM_HOUR, 0),
                    intent.getIntExtra(NEW_ALARM_MINUTE, 0),
                    RingtoneManager.getDefaultUri(RingtoneManager.TYPE_ALARM))
            title = getString(R.string.title_new_alarm)
            tvTime.text = getTimeString(this, alarm)
        }
    }

    fun onTimeClick(view: View) {
        showTimePickerDialog()
    }

    override fun onCreateOptionsMenu(menu: Menu?): Boolean {
        menuInflater.inflate(R.menu.menu_set_alarm, menu)
        menu?.setGroupVisible(R.id.menuGroupEdit, (alarmId != -1))
        return true
    }

    override fun onOptionsItemSelected(item: MenuItem?): Boolean {
        when (item?.itemId) {
            R.id.miSaveAlarm -> {
                saveAlarm()
                val data = Intent(this, MainActivity::class.java)
                        .putExtra(NEW_CREATED_ALARM, getTimeDeltaFromNow(alarm))
                setResult(Activity.RESULT_OK, data)
                finish()
                return true
            }
            R.id.miDelete -> {
                promptDelete()
//                setResult(Activity.RESULT_CANCELED)
//                finish()
                return true
            }
            else -> return super.onOptionsItemSelected(item)
        }
    }

    private fun promptDelete() {
        val builder = AlertDialog.Builder(this)
        builder.apply {
            setMessage("Delete alarm?")
            setPositiveButton("Delete", { dialog, which ->
                deleteAlarm(this@SetAlarmActivity, alarm)
                Toast.makeText(this@SetAlarmActivity, "Delete", Toast.LENGTH_SHORT).show()
                finish()
            })
            setNegativeButton("Cancel", { dialog, which ->
                Toast.makeText(this@SetAlarmActivity, "Cancel", Toast.LENGTH_SHORT).show()
            })
        }.create().show()
    }

    private fun showTimePickerDialog() {
        val timePickerDialog = createTimePickerDialog(this,
                TimePickerDialog.OnTimeSetListener { view, hourOfDay, minute ->
                    alarm.hour = hourOfDay
                    alarm.minute = minute
                    alarm.ringtoneUri = RingtoneManager.getDefaultUri(RingtoneManager.TYPE_ALARM)
                    tvTime.text = getTimeString(this, alarm)
                },
                alarm.hour, alarm.minute)
        timePickerDialog.show()
    }

    private fun saveAlarm() {
        alarm.title = tilTitle.editText?.text.toString()
        updateAlarmToDatabase(this, alarm)
    }

}
