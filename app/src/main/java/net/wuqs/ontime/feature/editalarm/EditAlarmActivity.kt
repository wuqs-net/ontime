package net.wuqs.ontime.feature.editalarm

import android.content.Context
import android.content.Intent
import android.media.AudioManager
import android.media.RingtoneManager
import android.net.Uri
import android.os.Bundle
import android.view.Menu
import android.view.MenuItem
import android.view.inputmethod.EditorInfo
import androidx.appcompat.app.AppCompatActivity
import androidx.core.app.NavUtils
import androidx.fragment.app.DialogFragment
import androidx.fragment.app.transaction
import kotlinx.android.synthetic.main.activity_edit_alarm.*
import kotlinx.android.synthetic.main.fragment_ringtone_option.*
import net.wuqs.ontime.R
import net.wuqs.ontime.alarm.*
import net.wuqs.ontime.db.Alarm
import net.wuqs.ontime.feature.home.MainActivity
import net.wuqs.ontime.feature.home.RESULT_DELETE_ALARM
import net.wuqs.ontime.feature.home.RESULT_SAVE_ALARM
import net.wuqs.ontime.feature.shared.dialog.PromptDialogFragment
import net.wuqs.ontime.feature.shared.dialog.SpinnerDialogFragment
import net.wuqs.ontime.feature.shared.dialog.TimePickerDialogFragment
import net.wuqs.ontime.util.Logger
import net.wuqs.ontime.util.changeTaskDescription
import net.wuqs.ontime.util.hideSoftInput
import net.wuqs.ontime.util.toast
import java.util.*

const val TAG_DELETE_ALARM = "DELETE_ALARM"

class EditAlarmActivity : AppCompatActivity(),
        PromptDialogFragment.OnClickListener,
        TimePickerDialogFragment.OnTimeSetListener,
        SpinnerDialogFragment.OptionListener,
        RepeatOptionFragment.OnRepeatIndexPickListener,
        RingtoneOptionFragment.OnOptionChangeListener {

    private lateinit var mAlarmUpdateHandler: AlarmUpdateHandler

    private lateinit var alarm: Alarm

    private var alarmEdited = false

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_edit_alarm)

        changeTaskDescription()

        volumeControlStream = AudioManager.STREAM_ALARM

        mAlarmUpdateHandler = AlarmUpdateHandler(this)

        supportActionBar?.setDisplayHomeAsUpEnabled(true)

        alarm = intent.getParcelableExtra(EXTRA_ALARM_INSTANCE)
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

        tv_alarm_time.text = alarm.createTimeString(this)
        tv_alarm_time.setOnClickListener { showTimePickerDialog() }
        oiv_repeat_type.setOnClickListener { showRepeatPickerDialog() }

        supportFragmentManager.transaction {
            replace(R.id.fragment_ringtone, RingtoneOptionFragment.newInstance(alarm))
        }

        et_notes.setText(alarm.notes)

        updateNextAlarmDate(displayOnly = true)
        updateRepeatDisplay()
    }

    override fun onDialogPositiveClick(dialogFragment: DialogFragment) {
        when (dialogFragment.tag) {
            TAG_DELETE_ALARM -> {
                val data = Intent(this, MainActivity::class.java)
                        .putExtra(EXTRA_ALARM_INSTANCE, alarm)
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
            tv_alarm_time.text = alarm.createTimeString(this)
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
                        toast(R.string.msg_cannot_set_past_time)

                    } else {
                        toast(R.string.msg_select_at_least_one_day)
                    }
                    return true
                }
                alarm.title = et_alarm_title.text.toString()
                alarm.notes = et_notes.text.toString()
                alarm.vibrate = cb_vibrate.isChecked
                alarm.isEnabled = true
                if (alarm.repeatType == Alarm.NON_REPEAT) alarm.repeatCycle = 0
                val data = Intent(this, MainActivity::class.java)
                        .putExtra(EXTRA_ALARM_INSTANCE, alarm)
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

    override fun onRingtoneUriSet(uri: Uri?) {
        alarm.ringtoneUri = uri
        alarmEdited = true
    }

    override fun onSilenceAfterSet(silenceAfter: Int) {
        alarm.silenceAfter = silenceAfter
        alarmEdited = true
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

    private fun updateRepeatDisplay() {
        oiv_repeat_type.valueText = alarm.getRepeatTypeText(resources)
        supportFragmentManager.beginTransaction().run {
            replace(R.id.fragment_repeat, RepeatOptionFragment.newInstance(alarm))
            commit()
        }
    }

    /**
     * Updates the next occurrence of the [Alarm] and its display.
     *
     * @param displayOnly if `true`, updates the display without changing any data. This parameter
     * is `false` by default.
     */
    private fun updateNextAlarmDate(displayOnly: Boolean = false) {
        if (!displayOnly) {
            alarm.nextTime = alarm.getNextOccurrence()
            alarm.snoozed = 0
            mLogger.i("nextTime changed to ${alarm.nextTime?.time}")
        }

        if (alarm.nextTime == null) {
            tv_next_date.text = if (alarm.repeatType == Alarm.NON_REPEAT) {
                getString(R.string.msg_cannot_set_past_time)
            } else {
                getString(R.string.msg_select_at_least_one_day)
            }
            return
        }

        alarm.nextTime?.let {
            if (alarm.snoozed > 0) {
                val time = it.createDateTimeString(this)
                tv_next_date.text = getString(R.string.msg_snoozed_until, time)
                return
            }
            val date = it.createDateString()
            tv_next_date.text = when {
                it.after(alarm.getNextOccurrence()) -> getString(R.string.msg_skipped_to, date)
                else -> getString(R.string.msg_next_date, date)
            }
        }

    }

    companion object {
        fun createIntent(context: Context) = Intent(context, EditAlarmActivity::class.java)
    }

    private val mLogger = Logger("EditAlarmActivity")

}

private const val TAG_EDIT_ALARM = "EDIT_ALARM"
private const val TAG_DISCARD_CHANGES = "DISCARD_CHANGES"
private const val TAG_REPEAT_TYPE = "REPEAT_TYPE"