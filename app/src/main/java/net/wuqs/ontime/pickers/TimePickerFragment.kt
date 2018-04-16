package net.wuqs.ontime.pickers

import android.app.Dialog
import android.app.DialogFragment
import android.app.TimePickerDialog
import android.content.Intent
import android.media.RingtoneManager
import android.os.Bundle
import android.text.format.DateFormat
import android.widget.TimePicker
import kotlinx.android.synthetic.main.activity_set_alarm.*
import net.wuqs.ontime.SetAlarmActivity
import net.wuqs.ontime.alarm.*
import java.util.*

class TimePickerFragment : DialogFragment(), TimePickerDialog.OnTimeSetListener {

    override fun onCreateDialog(savedInstanceState: Bundle?): Dialog {
        val c = Calendar.getInstance()
        val hour = c[Calendar.HOUR_OF_DAY]
        val minute = c[Calendar.MINUTE]
        return TimePickerDialog(activity, this, hour, minute,
                DateFormat.is24HourFormat(activity))
    }

    override fun onTimeSet(view: TimePicker?, hourOfDay: Int, minute: Int) {
        when (tag) {
            EDIT_ALARM -> {
                val mActivity = activity as SetAlarmActivity
                mActivity.alarm.hour = hourOfDay
                mActivity.alarm.minute = minute
                mActivity.alarm.ringtoneUri = RingtoneManager.getDefaultUri(RingtoneManager.TYPE_ALARM)
                mActivity.tvTime.text = getTimeString(mActivity, mActivity.alarm)
            }
            NEW_ALARM -> {
                val intentSetAlarmActivity = Intent(activity, SetAlarmActivity::class.java)
                        .apply {
                            putExtra(IS_NEW_ALARM, true)
                            putExtra(ALARM_ID, 0)
                            putExtra(NEW_ALARM_HOUR, hourOfDay)
                            putExtra(NEW_ALARM_MINUTE, minute)
                        }
                activity.startActivityForResult(intentSetAlarmActivity, CREATE_ALARM_REQUEST)
            }
        }
    }

    fun updateTime(hour: Int, minute: Int) {
        fragmentManager.executePendingTransactions()
        (dialog as TimePickerDialog).updateTime(hour, minute)
    }

    companion object {
        const val NEW_ALARM = "NEW_ALARM"
        const val EDIT_ALARM = "EDIT_ALARM"
    }
}