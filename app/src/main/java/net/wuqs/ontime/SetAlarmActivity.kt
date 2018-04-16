package net.wuqs.ontime

import android.app.Activity
import android.content.Intent
import android.media.AudioManager
import android.media.RingtoneManager
import android.os.Bundle
import android.support.v7.app.AlertDialog
import android.support.v7.app.AppCompatActivity
import android.text.Editable
import android.text.TextWatcher
import android.util.Log
import android.view.Menu
import android.view.MenuItem
import android.view.View
import android.widget.AdapterView
import android.widget.ArrayAdapter
import android.widget.Toast
import kotlinx.android.synthetic.main.activity_set_alarm.*
import kotlinx.android.synthetic.main.include_alarm_repeat_config.*
import net.wuqs.ontime.alarm.*
import net.wuqs.ontime.db.Alarm
import net.wuqs.ontime.pickers.DatePickerFragment
import net.wuqs.ontime.pickers.TimePickerFragment
import java.util.*

class SetAlarmActivity : AppCompatActivity(), TextWatcher, View.OnClickListener {
    val TAG = "SetAlarmActivity"

    lateinit var alarm: Alarm
    lateinit var miSaveAlarm: MenuItem

    var alarmId = 0
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_set_alarm)
        volumeControlStream = AudioManager.STREAM_ALARM

        val adapter = ArrayAdapter.createFromResource(this,
                R.array.repeat_types, android.R.layout.simple_spinner_dropdown_item)
        adapter.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item)
        spinnerRepeat.adapter = adapter
        spinnerRepeat.onItemSelectedListener = object : AdapterView.OnItemSelectedListener {
            override fun onItemSelected(parent: AdapterView<*>?, view: View?, position: Int, id: Long) {
                onRepeatCycleSelect(position)
            }

            override fun onNothingSelected(parent: AdapterView<*>?) {
            }
        }

        tvTime.setOnClickListener(this)
        etActivateDate.setOnClickListener(this)
        etCycle.addTextChangedListener(this)

        alarmId = intent.getIntExtra(ALARM_ID, 0)
        Log.v(TAG, alarmId.toString())
        if (alarmId != 0) {
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

        spinnerRepeat.setSelection(alarm.repeatType and 0xF)
        etCycle.setText(alarm.repeatCycle.toString())
        etActivateDate.setText(getDateString(this, alarm.activateDate))
    }

    fun onRepeatCycleSelect(pos: Int) {
        alarm.repeatType = when (pos) {
            0 -> ALARM_NON_REPEAT
            1 -> ALARM_REPEAT_DAILY
//            2 -> ALARM_REPEAT_WEEKLY
//            3 -> ALARM_REPEAT_MONTHLY_BY_DAY_OF_MONTH
//            4 -> ALARM_REPEAT_YEARLY_BY_DAY_OF_MONTH
            else -> ALARM_NON_REPEAT
        }
        val cycles = arrayOf(0, R.plurals.days, R.plurals.weeks, R.plurals.months, R.plurals.years)
        if (pos == 0) {
            incRepeat.visibility = View.GONE
            alarm.repeatCycle = 0
            alarm.repeatIndex = 0
//            miSaveAlarm.isEnabled = true
        } else {
            incRepeat.visibility = View.VISIBLE
//            etCycle.setText("1")
            tvCycle.text = resources.getQuantityText(cycles[pos], 1)
        }
    }

    override fun onClick(v: View?) {
        when (v) {
            tvTime -> showTimePickerDialog()
            etActivateDate -> showDatePickerDialog()
        }
    }

    override fun afterTextChanged(s: Editable?) {
//        miSaveAlarm.isEnabled = s!!.isNotEmpty()
        if (s!!.isNotEmpty() && s.toString().toInt() < 1) {
            s.replace(0, s.length, "1")
        }
    }

    override fun beforeTextChanged(s: CharSequence?, start: Int, count: Int, after: Int) {
    }

    override fun onTextChanged(s: CharSequence?, start: Int, before: Int, count: Int) {
    }

    override fun onCreateOptionsMenu(menu: Menu?): Boolean {
        menuInflater.inflate(R.menu.menu_set_alarm, menu)
        menu?.setGroupVisible(R.id.menuGroupEdit, (alarmId != 0))
        miSaveAlarm = menu?.findItem(R.id.miSaveAlarm)!!
        return true
    }

    override fun onOptionsItemSelected(item: MenuItem?): Boolean {
        when (item?.itemId) {
            R.id.miSaveAlarm -> {
                saveAlarm()
                val data = Intent(this, MainActivity::class.java)
                        .putExtra(DELTA_NEXT_OCCURRENCE, getTimeDeltaFromNow(alarm))
                setResult(Activity.RESULT_OK, data)
                finish()
                return true
            }
            R.id.miDelete -> {
                promptDelete()
                return true
            }
            android.R.id.home -> {
                onBackPressed()
                return true
            }
            else -> return super.onOptionsItemSelected(item)
        }
    }

    private fun promptDelete() {
        val builder = AlertDialog.Builder(this)
        builder.apply {
            setMessage(R.string.prompt_delete_alarm)
            setPositiveButton(R.string.action_delete, { dialog, which ->
                deleteAlarm(this@SetAlarmActivity, alarm)
//                Toast.makeText(this@SetAlarmActivity, R.string.action_delete, Toast.LENGTH_SHORT).show()
                finish()
            })
            setNegativeButton(R.string.action_cancel, null)
        }.create().show()
    }

    private fun showTimePickerDialog() {
        val dialogFragment = TimePickerFragment()
        dialogFragment.show(fragmentManager, TimePickerFragment.EDIT_ALARM)
        dialogFragment.updateTime(alarm.hour, alarm.minute)
    }

    private fun showDatePickerDialog() {
        val dialogFragment = DatePickerFragment()
        dialogFragment.show(fragmentManager, DatePickerFragment.SET_ACTIVATE_DATE)
        dialogFragment.updateDate(alarm.activateDate)
    }

    private fun saveAlarm() {
        alarm.title = tilTitle.editText?.text.toString()
        alarm.isEnabled = true
        if (alarm.repeatType > 0) alarm.repeatCycle = etCycle.text.toString().toInt()
        alarm.nextOccurrence = getNextAlarmOccurrence(alarm)
        updateAlarm(this, alarm)
    }

}
