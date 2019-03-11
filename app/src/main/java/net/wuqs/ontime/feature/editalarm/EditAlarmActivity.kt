package net.wuqs.ontime.feature.editalarm

import android.Manifest
import android.app.Activity
import android.content.Context
import android.content.Intent
import android.content.pm.PackageManager
import android.media.AudioManager
import android.media.RingtoneManager
import android.net.Uri
import android.os.Bundle
import android.provider.Settings
import androidx.core.app.ActivityCompat
import androidx.fragment.app.DialogFragment
import androidx.core.app.NavUtils
import androidx.appcompat.app.AppCompatActivity
import android.view.Menu
import android.view.MenuItem
import android.view.View
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
import net.wuqs.ontime.util.Logger
import net.wuqs.ontime.util.changeTaskDescription
import net.wuqs.ontime.util.hideSoftInput
import net.wuqs.ontime.util.toast
import java.io.File
import java.io.IOException
import java.util.*

private const val PERMISSION_REQUEST_RINGTONE = 2

const val TAG_DELETE_ALARM = "DELETE_ALARM"

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

        oiv_ringtone.setOnClickListener { showRingtonePicker() }
        cl_perm_warn.setOnClickListener { requestRingtonePerm() }

        cb_vibrate.isChecked = alarm.vibrate

        et_notes.setText(alarm.notes)

        updateNextAlarmDate(displayOnly = true)
        updateRepeatDisplay()
    }

    override fun onResume() {
        super.onResume()

        updateRingtoneDisplay()
    }

    override fun onDialogPositiveClick(dialogFragment: DialogFragment) {
        when (dialogFragment.tag) {
            TAG_DELETE_ALARM -> {
                val data = Intent(this, MainActivity::class.java)
                        .putExtra(EXTRA_ALARM_INSTANCE, alarm)
                setResult(RESULT_DELETE_ALARM, data)
                finish()
            }
            TAG_ASK_FOR_PERM -> {
                // Redirect user to app setting screen to allow permission
                val intent = Intent(Settings.ACTION_APPLICATION_DETAILS_SETTINGS).apply {
                    data = Uri.fromParts("package", packageName, null)
                }
                startActivity(intent)
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

    override fun onActivityResult(requestCode: Int, resultCode: Int, data: Intent?) {
        if (requestCode == 1 && resultCode == Activity.RESULT_OK && data != null) {
            alarm.ringtoneUri = data.getParcelableExtra(RingtoneManager.EXTRA_RINGTONE_PICKED_URI)
            mLogger.v("Ringtone picked: ${alarm.ringtoneUri}")
            updateRingtoneDisplay()
            alarmEdited = true
        }
    }

    override fun onRequestPermissionsResult(requestCode: Int, permissions: Array<out String>, grantResults: IntArray) {
        permissions.forEachIndexed { index, perm ->
            if (grantResults[index] == PackageManager.PERMISSION_GRANTED) {
                mLogger.i("$perm granted")
            } else {
                mLogger.i("$perm denied")
            }
        }
        when (requestCode) {
            PERMISSION_REQUEST_RINGTONE -> {
                updateRingtoneDisplay()
                if (grantResults[0] == PackageManager.PERMISSION_DENIED) {
                    if (!ActivityCompat.shouldShowRequestPermissionRationale(this,
                                    Manifest.permission.READ_EXTERNAL_STORAGE)) {
                        PromptDialogFragment.show(
                                this,
                                R.string.msg_ask_for_perm,
                                R.string.action_go_to_settings,
                                android.R.string.cancel,
                                TAG_ASK_FOR_PERM
                        )
                    }
                }
            }
        }
    }

    override fun onBackPressed() {
        promptDiscard()
    }

    private fun hasRingtonePerm(): Boolean {
        alarm.ringtoneUri?.let {
            return when (it.scheme) {
                "file" -> try {
                    mLogger.v("Ringtone path: ${it.path}")
                    File(it.path).canRead()
                } catch (e: IOException) {
                    false
                }
                "content" -> try {
                    contentResolver.query(it, null, null, null, null)?.close()
                    true
                } catch (e: SecurityException) {
                    false
                }
                else -> false
            }
        }
        return true
    }

    private fun requestRingtonePerm() {
        ActivityCompat.requestPermissions(this,
                arrayOf(Manifest.permission.READ_EXTERNAL_STORAGE),
                PERMISSION_REQUEST_RINGTONE)
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
            putExtra(RingtoneManager.EXTRA_RINGTONE_TYPE, RingtoneManager.TYPE_ALARM)
            putExtra(RingtoneManager.EXTRA_RINGTONE_DEFAULT_URI,
                    RingtoneManager.getDefaultUri(RingtoneManager.TYPE_ALARM))
            putExtra(RingtoneManager.EXTRA_RINGTONE_EXISTING_URI, alarm.ringtoneUri)
        }
        startActivityForResult(intent, 1)
//        Intent.ACTION_GET_CONTENT
    }

    private fun updateRingtoneDisplay() {
        val ringtone = RingtoneManager.getRingtone(this, alarm.ringtoneUri)
        oiv_ringtone.valueText = ringtone.getTitle(this)
        cl_perm_warn.visibility = if (hasRingtonePerm()) View.GONE else View.VISIBLE
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
private const val TAG_ASK_FOR_PERM = "ASK_FOR_PERM"