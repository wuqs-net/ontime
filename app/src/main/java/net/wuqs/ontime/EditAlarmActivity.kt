package net.wuqs.ontime

import android.content.Context
import android.content.DialogInterface
import android.content.Intent
import android.media.AudioManager
import android.media.RingtoneManager
import android.os.Bundle
import android.support.v4.app.DialogFragment
import android.support.v7.app.AppCompatActivity
import android.text.Editable
import android.text.TextWatcher
import android.view.Menu
import android.view.MenuItem
import android.view.View
import android.widget.AdapterView
import kotlinx.android.synthetic.main.activity_edit_alarm.*
import kotlinx.android.synthetic.main.layout_repeat_daily_config.*
import net.wuqs.ontime.alarm.ALARM_INSTANCE
import net.wuqs.ontime.alarm.AlarmUpdateHandler
import net.wuqs.ontime.alarm.getDateString
import net.wuqs.ontime.alarm.getTimeString
import net.wuqs.ontime.db.Alarm
import net.wuqs.ontime.dialog.DatePickerFragment
import net.wuqs.ontime.dialog.DeleteDialogFragment
import net.wuqs.ontime.dialog.RepeatTypePickerFragment
import net.wuqs.ontime.dialog.TimePickerFragment
import net.wuqs.ontime.utils.LogUtils
import net.wuqs.ontime.utils.shortToast
import java.util.*

class EditAlarmActivity : AppCompatActivity(),
        View.OnClickListener,
        TextWatcher,
        DialogInterface.OnClickListener,
        DatePickerFragment.DateSetListener,
        TimePickerFragment.TimeSetListener,
        RepeatTypePickerFragment.RepeatDialogListener {

    private lateinit var alarm: Alarm

    private lateinit var mAlarmUpdateHandler: AlarmUpdateHandler

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_edit_alarm)
        volumeControlStream = AudioManager.STREAM_ALARM

        mAlarmUpdateHandler = AlarmUpdateHandler(this)

        alarm = intent.getParcelableExtra(ALARM_INSTANCE)
        LOGGER.v("Edit alarm: $alarm")
        if (alarm.id == Alarm.INVALID_ID) {
            title = getString(R.string.title_new_alarm)
            alarm.ringtoneUri = RingtoneManager.getDefaultUri(RingtoneManager.TYPE_ALARM)
        } else {
            title = getString(R.string.title_edit_alarm)
            tilTitle.editText?.setText(alarm.title)
        }


//        spinnerRepeat.onItemSelectedListener = this
//        spinnerRepeat.setSelection(alarm.repeatType and 0xF)
        updateRepeatDisplay()

        tvTime.setOnClickListener(this)
        start_date.setOnClickListener(this)
        repeat_type.setOnClickListener(this)

        tvTime.text = getTimeString(this, alarm)
        start_date.text = getDateString(alarm.activateDate!!)

        edit_repeat_cycle.addTextChangedListener(this)
        edit_repeat_cycle.setText(alarm.repeatCycle.toString())
    }

    fun onItemSelected(parent: AdapterView<*>?, view: View?, position: Int, id: Long) {
        // TODO: Delete this
        alarm.repeatType = Alarm.REPEAT_TYPES[position]
        flipperRepeat.displayedChild = alarm.repeatType and 0xF
        if (position == 0) {
//            incRepeat.visibility = View.GONE
            alarm.repeatCycle = 0
            alarm.repeatIndex = 0
//            miSaveAlarm.isEnabled = true
        } else {
//            incRepeat.visibility = View.VISIBLE
//            etCycle.setText("1")
            repeat_cycle.text = getRepeatCycleText(1)
        }
    }

    override fun onClick(v: View?) {
        when (v) {
            tvTime -> showTimePickerDialog()
            start_date -> showDatePickerDialog()
            repeat_type -> showRepeatPickerDialog()
        }
    }

    override fun onClick(dialog: DialogInterface?, which: Int) {
        if (which == DialogInterface.BUTTON_POSITIVE) {
            val data = Intent(this, MainActivity::class.java)
                    .putExtra(ALARM_INSTANCE, alarm)
            setResult(MainActivity.RESULT_DELETE, data)
            finish()
        }
    }

    override fun afterTextChanged(s: Editable?) {
        if (s!!.isNotEmpty()) {
            if (s.toString().toInt() < 1) {
                s.replace(0, s.length, "1")
            }
            if (alarm.repeatType != Alarm.NON_REPEAT) {
                repeat_cycle.text = getRepeatCycleText(alarm.repeatCycle)
            }
        }
    }

    override fun beforeTextChanged(s: CharSequence?, start: Int, count: Int, after: Int) {
    }

    override fun onTextChanged(s: CharSequence?, start: Int, before: Int, count: Int) {
    }

    override fun onDateSet(tag: String?, year: Int, month: Int, dayOfMonth: Int) {
        if (tag == DatePickerFragment.SET_ACTIVATE_DATE) {
            alarm.activateDate!!.set(year, month, dayOfMonth, 0, 0, 0)
            alarm.activateDate!![Calendar.MILLISECOND] = 0
            start_date.text = getDateString(alarm.activateDate!!)
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

    override fun onItemChoose(dialog: DialogFragment, which: Int) {
        alarm.repeatType = Alarm.REPEAT_TYPES[which]
        updateRepeatDisplay()
    }

    override fun onCreateOptionsMenu(menu: Menu?): Boolean {
        menuInflater.inflate(R.menu.menu_set_alarm, menu)
        menu?.setGroupVisible(R.id.menuGroupEdit, alarm.id != Alarm.INVALID_ID)
        return true
    }

    override fun onOptionsItemSelected(item: MenuItem?): Boolean {
        when (item?.itemId) {
            R.id.miSaveAlarm -> {
                if (alarm.repeatType == Alarm.NON_REPEAT && alarm.getNextOccurrence() == null) {
                    // If the user set a non-repeat alarm in the past, don't save it
                    shortToast(R.string.msg_cannot_set_past_time)
                    return true
                }
                alarm.title = tilTitle.editText?.text.toString()
                alarm.isEnabled = true
                alarm.repeatCycle = if (alarm.repeatType != Alarm.NON_REPEAT) {
                    edit_repeat_cycle.text.toString().toInt()
                } else {
                    0
                }
                val data = Intent(this, MainActivity::class.java)
                        .putExtra(ALARM_INSTANCE, alarm)
                setResult(MainActivity.RESULT_SAVE, data)
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
        DeleteDialogFragment().show(supportFragmentManager, DeleteDialogFragment.TAG_DELETE_ALARM)
    }

    private fun showTimePickerDialog() {
        TimePickerFragment.newInstance(alarm.hour, alarm.minute)
                .show(supportFragmentManager, TimePickerFragment.EDIT_ALARM)
    }

    private fun showDatePickerDialog() {
        DatePickerFragment.newInstance(alarm.activateDate!!)
                .show(supportFragmentManager, DatePickerFragment.SET_ACTIVATE_DATE)
    }

    private fun showRepeatPickerDialog() {
        RepeatTypePickerFragment()
                .show(supportFragmentManager, RepeatTypePickerFragment.CHOOSE_REPEAT_TYPE)
    }

    private fun updateRepeatDisplay() {
        flipperRepeat.displayedChild = alarm.repeatType and 0xF
        repeat_type.text = resources.getStringArray(R.array.repeat_types)[alarm.repeatType and 0xF]
        if (alarm.repeatType == Alarm.NON_REPEAT) {
            edit_repeat_cycle.removeTextChangedListener(this)
            edit_repeat_cycle.setText("0")
        } else {
            edit_repeat_cycle.addTextChangedListener(this)
            repeat_cycle.text = getRepeatCycleText(alarm.repeatCycle)
        }
    }

    private fun getRepeatCycleText(quantity: Int): CharSequence? {
        val cycles = arrayOf(0, R.plurals.days, R.plurals.weeks, R.plurals.months, R.plurals.years)
        return resources.getQuantityText(cycles[alarm.repeatType and 0xF], quantity)
    }

    companion object {
        fun createIntent(context: Context) = Intent(context, EditAlarmActivity::class.java)
    }

    private val LOGGER = LogUtils.Logger("EditAlarmActivity")

}
