package net.wuqs.ontime.ui.feature.editalarm

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
import net.wuqs.ontime.ui.dialog.PromptDialogFragment
import net.wuqs.ontime.ui.dialog.SpinnerDialogFragment
import net.wuqs.ontime.ui.dialog.TimePickerDialogFragment
import net.wuqs.ontime.ui.feature.main.MainActivity
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

    private lateinit var alarm: Alarm

    private lateinit var mAlarmUpdateHandler: AlarmUpdateHandler

    private var mAlarmEdited = false

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
            mAlarmEdited = true
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

        cb_vibrate.isChecked = alarm.vibrate
        cb_vibrate.setOnCheckedChangeListener { _, isChecked -> alarm.vibrate = isChecked }

        updateNextAlarmDate(alarm.snoozed != 0)
        updateRepeatDisplay()
    }

    override fun onDialogPositiveClick(dialogFragment: DialogFragment) {
        when (dialogFragment.tag) {
            TAG_DELETE_ALARM -> {
                val data = Intent(this, MainActivity::class.java)
                        .putExtra(ALARM_INSTANCE, alarm)
                setResult(MainActivity.RESULT_DELETE, data)
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
            mAlarmEdited = true
            mLogger.v("Alarm changes made: time")
            alarm.hour = hourOfDay
            alarm.minute = minute
            alarm.ringtoneUri = RingtoneManager.getDefaultUri(RingtoneManager.TYPE_ALARM)
            tv_alarm_time.text = getTimeString(this, alarm)
            updateNextAlarmDate()
        }
    }

    override fun updateRepeatOption(repeatType: Int?, repeatCycle: Int?, repeatIndex: Int?) {
        mAlarmEdited = true
        mLogger.v("Alarm changes made: repeat option")
        updateNextAlarmDate()
    }

    override fun updateActivateDate(year: Int, month: Int, dayOfMonth: Int) {
        mAlarmEdited = true
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
            mAlarmEdited = true
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
                alarm.isEnabled = true
                if (alarm.repeatType == Alarm.NON_REPEAT) alarm.repeatCycle = 0
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
                promptDiscard()
                return true
            }
            else -> return super.onOptionsItemSelected(item)
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
        if (et_alarm_title.text.toString() != alarm.title) mAlarmEdited = true
        if (!mAlarmEdited) {
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
