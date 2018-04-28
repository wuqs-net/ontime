package net.wuqs.ontime

import android.app.Activity
import android.content.DialogInterface
import android.content.Intent
import android.media.AudioManager
import android.media.RingtoneManager
import android.os.Bundle
import android.support.v7.app.AppCompatActivity
import android.text.Editable
import android.text.TextWatcher
import android.view.Menu
import android.view.MenuItem
import android.view.View
import android.widget.AdapterView
import android.widget.ArrayAdapter
import kotlinx.android.synthetic.main.activity_set_alarm.*
import kotlinx.android.synthetic.main.include_alarm_repeat_config.*
import net.wuqs.ontime.alarm.*
import net.wuqs.ontime.db.Alarm
import net.wuqs.ontime.dialog.DatePickerFragment
import net.wuqs.ontime.dialog.DeleteDialogFragment
import net.wuqs.ontime.dialog.TimePickerFragment
import net.wuqs.ontime.utils.LogUtils

class SetAlarmActivity
    : AppCompatActivity(),
        TextWatcher,
        View.OnClickListener,
        AdapterView.OnItemSelectedListener,
        DialogInterface.OnClickListener,
        DatePickerFragment.OnDateSetListener,
        TimePickerFragment.OnTimeSetListener {

    val alarm: Alarm = Alarm()
    lateinit var miSaveAlarm: MenuItem

    var alarmId = 0L
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_set_alarm)
        volumeControlStream = AudioManager.STREAM_ALARM

        val adapter = ArrayAdapter.createFromResource(this,
                R.array.repeat_types, android.R.layout.simple_spinner_dropdown_item)
        adapter.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item)
        spinnerRepeat.adapter = adapter
        spinnerRepeat.onItemSelectedListener = this

        tvTime.setOnClickListener(this)
        etActivateDate.setOnClickListener(this)
        etCycle.addTextChangedListener(this)

        alarmId = intent.getLongExtra(ALARM_ID, 0)
        LOGGER.v(alarmId.toString())
        if (alarmId != 0L) {
            title = getString(R.string.title_edit_alarm)
            alarm.copyFrom(intent.getParcelableExtra(ALARM_INSTANCE))
            tilTitle.editText?.setText(alarm.title)
        } else {
            alarm.apply {
                hour = intent.getIntExtra(NEW_ALARM_HOUR, 0)
                minute = intent.getIntExtra(NEW_ALARM_MINUTE, 0)
                ringtoneUri = RingtoneManager.getDefaultUri(RingtoneManager.TYPE_ALARM)
            }
            title = getString(R.string.title_new_alarm)
        }
        tvTime.text = getTimeString(this, alarm)

        spinnerRepeat.setSelection(alarm.repeatType and 0xF)
        etCycle.setText(alarm.repeatCycle.toString())
        etActivateDate.setText(getDateString(alarm.activateDate))
    }

    override fun onItemSelected(parent: AdapterView<*>?, view: View?, position: Int, id: Long) {
        val repeatTypes = arrayOf(ALARM_NON_REPEAT, ALARM_REPEAT_DAILY, ALARM_REPEAT_WEEKLY,
                ALARM_REPEAT_MONTHLY_BY_DAY_OF_MONTH, ALARM_REPEAT_YEARLY_BY_DAY_OF_MONTH)
        val cycles = arrayOf(0, R.plurals.days, R.plurals.weeks, R.plurals.months, R.plurals.years)
        alarm.repeatType = repeatTypes[position]
        if (position == 0) {
            incRepeat.visibility = View.GONE
            alarm.repeatCycle = 0
            alarm.repeatIndex = 0
//            miSaveAlarm.isEnabled = true
        } else {
            incRepeat.visibility = View.VISIBLE
//            etCycle.setText("1")
            tvCycle.text = resources.getQuantityText(cycles[position], 1)
        }
    }

    override fun onNothingSelected(parent: AdapterView<*>?) {}

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

    override fun onClick(dialog: DialogInterface?, which: Int) {
        when (which) {
            DialogInterface.BUTTON_POSITIVE -> {
                deleteAlarm(this, alarm)
                finish()
            }
        }
    }

    override fun onDateSet(tag: String?, year: Int, month: Int, dayOfMonth: Int) {
        if (tag == DatePickerFragment.SET_ACTIVATE_DATE) {
            alarm.activateDate.set(year, month, dayOfMonth)
            etActivateDate.setText(getDateString(alarm.activateDate))
        }
    }

    override fun onTimeSet(tag: String?, hourOfDay: Int, minute: Int) {
        if (tag == TimePickerFragment.EDIT_ALARM) {
            alarm.hour = hourOfDay
            alarm.minute = minute
            alarm.ringtoneUri = RingtoneManager.getDefaultUri(RingtoneManager.TYPE_ALARM)
            tvTime.text = getTimeString(this, alarm)
        }
    }

    override fun onCreateOptionsMenu(menu: Menu?): Boolean {
        menuInflater.inflate(R.menu.menu_set_alarm, menu)
        menu?.setGroupVisible(R.id.menuGroupEdit, (alarmId != 0L))
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
        val df = DeleteDialogFragment.newInstance(this)
        df.show(supportFragmentManager, DeleteDialogFragment.TAG_DELETE_ALARM)
    }

    private fun showTimePickerDialog() {
        val df = TimePickerFragment.newInstance(this, alarm.hour, alarm.minute)
        df.show(supportFragmentManager, TimePickerFragment.EDIT_ALARM)
    }

    private fun showDatePickerDialog() {
        val df = DatePickerFragment.newInstance(this, alarm.activateDate)
        df.show(supportFragmentManager, DatePickerFragment.SET_ACTIVATE_DATE)
    }

    private fun saveAlarm() {
        alarm.title = tilTitle.editText?.text.toString()
        alarm.isEnabled = true
        if (alarm.repeatType > 0) alarm.repeatCycle = etCycle.text.toString().toInt()
        alarm.nextOccurrence = getNextAlarmOccurrence(alarm)
        updateAlarm(this, alarm)
    }

    private val LOGGER = LogUtils.Logger("SetAlarmActivity")

}
