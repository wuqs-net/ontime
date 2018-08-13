package net.wuqs.ontime.ui.alarmeditscreen

import android.content.Context
import android.content.DialogInterface
import android.content.Intent
import android.media.AudioManager
import android.media.RingtoneManager
import android.os.Bundle
import android.support.v4.app.DialogFragment
import android.support.v4.app.NavUtils
import android.support.v7.app.AppCompatActivity
import android.view.*
import kotlinx.android.synthetic.main.activity_edit_alarm.*
import net.wuqs.ontime.R
import net.wuqs.ontime.alarm.*
import net.wuqs.ontime.db.Alarm
import net.wuqs.ontime.ui.dialog.DeleteDialogFragment
import net.wuqs.ontime.ui.dialog.SpinnerDialogFragment
import net.wuqs.ontime.ui.dialog.TimePickerFragment
import net.wuqs.ontime.ui.mainscreen.MainActivity
import net.wuqs.ontime.util.LogUtils
import net.wuqs.ontime.util.shortToast

class EditAlarmActivity : AppCompatActivity(),
        View.OnClickListener,
        DialogInterface.OnClickListener,
        TimePickerFragment.TimeSetListener,
        SpinnerDialogFragment.SpinnerDialogListener,
        RepeatOptionFragment.OnRepeatIndexPickListener {

    private lateinit var alarm: Alarm

    private lateinit var mAlarmUpdateHandler: AlarmUpdateHandler

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_edit_alarm)
        volumeControlStream = AudioManager.STREAM_ALARM

        mAlarmUpdateHandler = AlarmUpdateHandler(this)

        supportActionBar?.setDisplayHomeAsUpEnabled(true)

        alarm = intent.getParcelableExtra(ALARM_INSTANCE)
        mLogger.v("Edit alarm: $alarm")
        if (alarm.id == Alarm.INVALID_ID) {
            title = getString(R.string.title_new_alarm)
            alarm.ringtoneUri = RingtoneManager.getDefaultUri(RingtoneManager.TYPE_ALARM)
        } else {
            title = getString(R.string.title_edit_alarm)
            til_title.editText?.setText(alarm.title)
        }

        tvTime.text = getTimeString(this, alarm)
        tvTime.setOnClickListener(this)
        oiv_repeat_type.setOnClickListener(this)

        updateNextAlarmDate()
        updateRepeatDisplay()
    }

    override fun onClick(v: View?) {
        when (v) {
            tvTime -> showTimePickerDialog()
            oiv_repeat_type -> showRepeatPickerDialog()
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

    override fun onTimeSet(tag: String?, hourOfDay: Int, minute: Int) {
        if (tag == TimePickerFragment.EDIT_ALARM) {
            alarm.hour = hourOfDay
            alarm.minute = minute
            alarm.ringtoneUri = RingtoneManager.getDefaultUri(RingtoneManager.TYPE_ALARM)
            tvTime.text = getTimeString(this, alarm)
            updateNextAlarmDate()
        }
    }

    override fun updateRepeatOption(repeatType: Int?, repeatCycle: Int?, repeatIndex: Int?) {
        repeatType?.let { alarm.repeatType = it }
        repeatCycle?.let { alarm.repeatCycle = it }
        repeatIndex?.let { alarm.repeatIndex = it }
        updateNextAlarmDate()
    }

    override fun updateActivateDate(year: Int, month: Int, dayOfMonth: Int) {
        alarm.activateDate!!.setMidnight(year, month, dayOfMonth)
        updateNextAlarmDate()
    }

    override fun onOptionClick(dialog: DialogFragment, which: Int) {
        val types = arrayOf(
                Alarm.NON_REPEAT,
                Alarm.REPEAT_DAILY,
//                Alarm.REPEAT_WEEKLY,
                Alarm.REPEAT_MONTHLY_BY_DATE,
                Alarm.REPEAT_YEARLY_BY_DATE
        )
        types[which].let {
            when (it) {
                alarm.repeatType -> return@let
                Alarm.NON_REPEAT -> {
                    alarm.repeatCycle = 0
                    alarm.repeatIndex = 0
                }
                Alarm.REPEAT_DAILY -> {
                    alarm.repeatCycle = 1
                    alarm.repeatIndex = 0
                }
                Alarm.REPEAT_MONTHLY_BY_DATE -> {
                    alarm.repeatCycle = 1
                    alarm.repeatIndex = 0
                }
            }
            alarm.repeatType = it
        }
        updateRepeatDisplay()
        updateNextAlarmDate()
    }

    override fun onCreateOptionsMenu(menu: Menu?): Boolean {
        menuInflater.inflate(R.menu.menu_set_alarm, menu)
        menu?.run {
            setGroupVisible(R.id.menuGroupEdit, alarm.id != Alarm.INVALID_ID)
//            findItem(R.id.item_enable)?.isChecked = alarm.isEnabled
        }

        return true
    }

    override fun onOptionsItemSelected(item: MenuItem?): Boolean {
        when (item?.itemId) {
            R.id.miSaveAlarm -> {
                // Save alarm
                if (alarm.repeatType == Alarm.NON_REPEAT && alarm.nextTime == null) {
                    // Prevent the user from setting a non-repeat alarm in the past
                    shortToast(R.string.msg_cannot_set_past_time)
                    return true
                }
                alarm.title = til_title.editText?.text.toString()
                alarm.isEnabled = true
                alarm.snoozed = 0
                if (alarm.repeatType == Alarm.NON_REPEAT) alarm.repeatCycle = 0
                val data = Intent(this, MainActivity::class.java)
                        .putExtra(ALARM_INSTANCE, alarm)
                setResult(MainActivity.RESULT_SAVE, data)
                finish()
                return true
            }
//            R.id.item_enable -> item.let {
//                it.isChecked = !it.isChecked
//                alarm.isEnabled = it.isChecked
//                return true
//            }
            R.id.miDelete -> {
                promptDelete()
                return true
            }
            android.R.id.home -> {
                NavUtils.navigateUpFromSameTask(this)
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

    private fun showRepeatPickerDialog() {
        SpinnerDialogFragment.newInstance(R.array.repeat_types)
                .show(supportFragmentManager, SpinnerDialogFragment.CHOOSE_REPEAT_TYPE)
    }

    private fun updateRepeatDisplay() {
        oiv_repeat_type.valueText = alarm.getRepeatTypeText(resources)
        supportFragmentManager.beginTransaction().run {
            replace(R.id.fragment_repeat, RepeatOptionFragment.newInstance(alarm))
            commit()
        }
    }

    private fun updateNextAlarmDate(onlyDisplay: Boolean = false) {
        alarm.getNextOccurrence().let {
            tv_next_date.text = if (it != null) {
                getString(R.string.msg_next_date, getDateString(it, false))
            } else {
                getString(R.string.msg_cannot_set_past_time)
            }
            mLogger.i(it?.time.toString())
            alarm.nextTime = it
        }

    }

    companion object {
        fun createIntent(context: Context) = Intent(context, EditAlarmActivity::class.java)
    }

    private val mLogger = LogUtils.Logger("EditAlarmActivity")

}
