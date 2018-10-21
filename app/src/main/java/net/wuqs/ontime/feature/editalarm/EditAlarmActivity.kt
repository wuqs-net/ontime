package net.wuqs.ontime.feature.editalarm

import android.app.Activity
import android.content.Context
import android.content.Intent
import android.media.AudioManager
import android.media.RingtoneManager
import android.os.Build
import android.os.Bundle
import android.support.v4.app.DialogFragment
import android.support.v4.app.NavUtils
import android.support.v7.app.AppCompatActivity
import android.view.Menu
import android.view.MenuItem
import android.view.inputmethod.EditorInfo
import kotlinx.android.synthetic.main.activity_edit_alarm.*
import net.wuqs.ontime.R
import net.wuqs.ontime.alarm.*
import net.wuqs.ontime.db.Alarm
import net.wuqs.ontime.feature.home.MainActivity
import net.wuqs.ontime.feature.home.RESULT_DELETE_ALARM
import net.wuqs.ontime.feature.home.RESULT_SAVE_ALARM
import net.wuqs.ontime.feature.shared.dialog.PromptDialogFragment
import net.wuqs.ontime.feature.shared.dialog.SpinnerDialogFragment
import net.wuqs.ontime.feature.shared.dialog.TimePickerDialogFragment
import net.wuqs.ontime.util.LogUtils
import net.wuqs.ontime.util.getCustomTaskDescription
import net.wuqs.ontime.util.hideSoftInput
import net.wuqs.ontime.util.shortToast
import java.util.*

class EditAlarmActivity : AppCompatActivity(),
        PromptDialogFragment.OnClickListener,
        TimePickerDialogFragment.OnTimeSetListener,
        SpinnerDialogFragment.OptionListener,
        RepeatOptionFragment.OnRepeatIndexPickListener {

    private lateinit var mAlarmUpdateHandler: AlarmUpdateHandler

    private lateinit var alarm: Alarm

    private var alarmEdited = false

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_edit_alarm)

        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.LOLLIPOP) {
            setTaskDescription(getCustomTaskDescription())
        }

        volumeControlStream = AudioManager.STREAM_ALARM

        mAlarmUpdateHandler = AlarmUpdateHandler(this)

        supportActionBar?.setDisplayHomeAsUpEnabled(true)

        alarm = intent.getParcelableExtra(ALARM_INSTANCE)
        mLogger.v("Edit alarm: $alarm")
        if (alarm.id == Alarm.INVALID_ID) {
            title = getString(R.string.title_new_alarm)
            alarm.ringtoneUri = RingtoneManager.getDefaultUri(RingtoneManager.TYPE_ALARM)
            alarmEdited = true
            mLogger.v("Alarm changes made: new alarm")
        } else {
            title = getString(R.string.title_edit_alarm)
            et_alarm_title.setText(alarm.title)
        }

        et_alarm_title.setOnEditorActionListener { v, actionId, _ ->
            when (actionId) {
                EditorInfo.IME_ACTION_DONE -> {
                    v.clearFocus()
                    hideSoftInput(v)
                    true
                }
                else -> false
            }
        }

        tv_alarm_time.text = getTimeString(this, alarm)
        tv_alarm_time.setOnClickListener { showTimePickerDialog() }
        oiv_repeat_type.setOnClickListener { showRepeatPickerDialog() }

        val ringtone = RingtoneManager.getRingtone(this, alarm.ringtoneUri)
        oiv_ringtone.valueText = ringtone.getTitle(this)
        oiv_ringtone.setOnClickListener { showRingtonePicker() }

        cb_vibrate.isChecked = alarm.vibrate

        et_notes.setText(alarm.notes)

        updateNextAlarmDate(alarm.snoozed != 0)
        updateRepeatDisplay()
    }

    override fun onDialogPositiveClick(dialogFragment: DialogFragment) {
        when (dialogFragment.tag) {
            TAG_DELETE_ALARM -> {
                val data = Intent(this, MainActivity::class.java)
                        .putExtra(ALARM_INSTANCE, alarm)
                setResult(RESULT_DELETE_ALARM, data)
                finish()
            }
        }
    }

    override fun onDialogNegativeClick(dialogFragment: DialogFragment) {
        when (dialogFragment.tag) {
            TAG_DISCARD_CHANGES -> {
                NavUtils.navigateUpFromSameTask(this@EditAlarmActivity)
            }
        }
    }

    override fun onTimeSet(fragment: TimePickerDialogFragment, hourOfDay: Int, minute: Int) {
        if (fragment.tag == TAG_EDIT_ALARM) {
            alarmEdited = true
            mLogger.v("Alarm changes made: time")
            alarm.hour = hourOfDay
            alarm.minute = minute
            tv_alarm_time.text = getTimeString(this, alarm)
            updateNextAlarmDate()
        }
    }

    override fun updateRepeatOption(repeatType: Int?, repeatCycle: Int?, repeatIndex: Int?) {
        alarmEdited = true
        mLogger.v("Alarm changes made: repeat option")
        updateNextAlarmDate()
    }

    override fun updateActivateDate(year: Int, month: Int, dayOfMonth: Int) {
        alarmEdited = true
        mLogger.v("Alarm changes made: date")
        alarm.activateDate!!.setMidnight(year, month, dayOfMonth)
        updateNextAlarmDate()
    }

    override fun onOptionClick(dialog: DialogFragment, which: Int) {
        val types = arrayOf(
                Alarm.NON_REPEAT,
                Alarm.REPEAT_DAILY,
                Alarm.REPEAT_WEEKLY,
                Alarm.REPEAT_MONTHLY_BY_DATE,
                Alarm.REPEAT_YEARLY_BY_DATE
        )
        types[which].let {
            when (it) {
                alarm.repeatType -> return
                Alarm.NON_REPEAT -> {
                    alarm.repeatCycle = 0
                    alarm.repeatIndex = 0
                }
                Alarm.REPEAT_DAILY -> {
                    alarm.repeatCycle = 1
                    alarm.repeatIndex = 0
                }
                Alarm.REPEAT_WEEKLY -> {
                    alarm.repeatCycle = 1
                    alarm.repeatIndex = (Calendar.getInstance().firstDayOfWeek shl 8) + 0b0111110
                }
                Alarm.REPEAT_MONTHLY_BY_DATE -> {
                    alarm.repeatCycle = 1
                    alarm.repeatIndex = 0
                }
                Alarm.REPEAT_YEARLY_BY_DATE -> {
                    alarm.repeatCycle = 1
                    alarm.repeatIndex = 0
                }
            }
            alarm.repeatType = it
            alarmEdited = true
            mLogger.v("Alarm changes made: repeat type")
        }
        updateRepeatDisplay()
        updateNextAlarmDate()
    }

    override fun onCreateOptionsMenu(menu: Menu?): Boolean {
        menuInflater.inflate(R.menu.menu_set_alarm, menu)
        menu?.run {
            setGroupVisible(R.id.menuGroupEdit, alarm.id != Alarm.INVALID_ID)
        }
        return true
    }

    override fun onOptionsItemSelected(item: MenuItem?): Boolean {
        when (item?.itemId) {
            R.id.miSaveAlarm -> {
                // Save alarm
                if (alarm.nextTime == null) {
                    if (alarm.repeatType == Alarm.NON_REPEAT) {
                        // Prevent the user from setting a non-repeat alarm in the past
                        shortToast(R.string.msg_cannot_set_past_time)
                    } else {
                        shortToast(R.string.msg_select_at_least_one_day)
                    }
                    return true
                }
                alarm.title = et_alarm_title.text.toString()
                alarm.notes = et_notes.text.toString()
                alarm.vibrate = cb_vibrate.isChecked
                alarm.isEnabled = true
                if (alarm.repeatType == Alarm.NON_REPEAT) alarm.repeatCycle = 0
                val data = Intent(this, MainActivity::class.java)
                        .putExtra(ALARM_INSTANCE, alarm)
                setResult(RESULT_SAVE_ALARM, data)
                finish()
                return true
            }
            R.id.miDelete -> {
                promptDelete()
                return true
            }
            android.R.id.home -> {
                promptDiscard()
                return true
            }
            else -> return super.onOptionsItemSelected(item)
        }
    }

    override fun onActivityResult(requestCode: Int, resultCode: Int, data: Intent?) {
        if (requestCode == 1 && resultCode == Activity.RESULT_OK) {
            data?.let {
                alarm.ringtoneUri = it.getParcelableExtra(RingtoneManager.EXTRA_RINGTONE_PICKED_URI)
                val ringtone = RingtoneManager.getRingtone(this, alarm.ringtoneUri)
                oiv_ringtone.valueText = ringtone.getTitle(this)
                alarmEdited = true
            }
        }
    }

    override fun onBackPressed() {
        promptDiscard()
    }

    private fun promptDelete() {
        PromptDialogFragment.show(
                this,
                R.string.prompt_delete_alarm,
                R.string.action_delete,
                R.string.action_cancel,
                TAG_DELETE_ALARM
        )
    }

    private fun promptDiscard() {
        if (et_alarm_title.text.toString() != alarm.title) alarmEdited = true
        if (et_notes.text.toString() != alarm.notes) alarmEdited = true
        if (cb_vibrate.isChecked != alarm.vibrate) alarmEdited = true
        if (!alarmEdited) {
            NavUtils.navigateUpFromSameTask(this)
            return
        }
        PromptDialogFragment.show(
                this,
                if (alarm.id == Alarm.INVALID_ID) {
                    R.string.prompt_discard_new_alarm
                } else {
                    R.string.prompt_discard_changes
                },
                R.string.action_keep_editing,
                R.string.action_discard,
                TAG_DISCARD_CHANGES
        )
    }

    private fun showTimePickerDialog() {
        TimePickerDialogFragment.show(this, alarm.hour, alarm.minute, TAG_EDIT_ALARM)
    }

    private fun showRepeatPickerDialog() {
        SpinnerDialogFragment.show(this, R.array.repeat_types, TAG_REPEAT_TYPE)
    }

    private fun showRingtonePicker() {
        val intent = Intent(RingtoneManager.ACTION_RINGTONE_PICKER).apply {
            putExtra(RingtoneManager.EXTRA_RINGTONE_TYPE, RingtoneManager.TYPE_ALL)
            putExtra(RingtoneManager.EXTRA_RINGTONE_DEFAULT_URI,
                    RingtoneManager.getDefaultUri(RingtoneManager.TYPE_ALARM))
            putExtra(RingtoneManager.EXTRA_RINGTONE_EXISTING_URI, alarm.ringtoneUri)
        }
        startActivityForResult(intent, 1)
    }

    private fun updateRepeatDisplay() {
        oiv_repeat_type.valueText = alarm.getRepeatTypeText(resources)
        supportFragmentManager.beginTransaction().run {
            replace(R.id.fragment_repeat, RepeatOptionFragment.newInstance(alarm))
            commit()
        }
    }

    private fun updateNextAlarmDate(snoozed: Boolean = false) {
        if (snoozed) {
            val dateTime = getDateTimeString(this, alarm.nextTime)
            tv_next_date.text = getString(R.string.msg_snoozed_until, dateTime)
            return
        }
        alarm.getNextOccurrence().let {
            tv_next_date.text = if (it != null) {
                getString(R.string.msg_next_date, getDateString(it))
            } else {
                if (alarm.repeatType == Alarm.NON_REPEAT) {
                    getString(R.string.msg_cannot_set_past_time)
                } else {
                    getString(R.string.msg_select_at_least_one_day)
                }
            }
            mLogger.i(it?.time.toString())
            alarm.nextTime = it
            alarm.snoozed = 0
        }

    }

    companion object {
        fun createIntent(context: Context) = Intent(context, EditAlarmActivity::class.java)
    }

    private val mLogger = LogUtils.Logger("EditAlarmActivity")

}

private const val TAG_EDIT_ALARM = "EDIT_ALARM"
private const val TAG_DELETE_ALARM = "DELETE_ALARM"
private const val TAG_DISCARD_CHANGES = "DISCARD_CHANGES"
private const val TAG_REPEAT_TYPE = "REPEAT_TYPE"
