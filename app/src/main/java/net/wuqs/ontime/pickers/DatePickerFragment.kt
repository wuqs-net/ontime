package net.wuqs.ontime.pickers

import android.app.DatePickerDialog
import android.app.Dialog
import android.app.DialogFragment
import android.os.Bundle
import android.widget.DatePicker
import kotlinx.android.synthetic.main.include_alarm_repeat_config.*
import net.wuqs.ontime.SetAlarmActivity
import net.wuqs.ontime.alarm.getDateString
import java.util.*

class DatePickerFragment : DialogFragment(), DatePickerDialog.OnDateSetListener {

    override fun onCreateDialog(savedInstanceState: Bundle?): Dialog {
        val c = Calendar.getInstance()
        val year = c[Calendar.YEAR]
        val month = c[Calendar.MONTH]
        val day = c[Calendar.DAY_OF_MONTH]

        return DatePickerDialog(activity, this, year, month, day)
    }

    override fun onDateSet(view: DatePicker?, year: Int, month: Int, dayOfMonth: Int) {
        when (tag) {
            SET_ACTIVATE_DATE -> {
                val mActivity = activity as SetAlarmActivity
                mActivity.alarm.activateDate[Calendar.YEAR] = year
                mActivity.alarm.activateDate[Calendar.MONTH] = month
                mActivity.alarm.activateDate[Calendar.DAY_OF_MONTH] = dayOfMonth
                mActivity.etActivateDate.setText(getDateString(mActivity, mActivity.alarm.activateDate))
            }
        }
    }

    fun updateDate(calendar: Calendar) {
        val year = calendar[Calendar.YEAR]
        val month = calendar[Calendar.MONTH]
        val dayOfMonth = calendar[Calendar.DAY_OF_MONTH]
        fragmentManager.executePendingTransactions()
        (dialog as DatePickerDialog).updateDate(year, month, dayOfMonth)
    }

    companion object {
        const val SET_ACTIVATE_DATE = "SET_ACTIVATE_DATE"
    }
}