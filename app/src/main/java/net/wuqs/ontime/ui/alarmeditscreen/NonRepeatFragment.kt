package net.wuqs.ontime.ui.alarmeditscreen

import net.wuqs.ontime.R
import net.wuqs.ontime.alarm.setHms
import net.wuqs.ontime.ui.dialog.DatePickerDialogFragment
import java.util.*

class NonRepeatFragment : RepeatOptionFragment() {
    override val mLayout = R.layout.fragment_non_repeat

    override fun editActivateDate() {
        val minDate = Calendar.getInstance().apply {
            setHms(alarm.hour, alarm.minute)
            if (timeInMillis < System.currentTimeMillis()) add(Calendar.DATE, 1)
            setHms(0)
        }
        DatePickerDialogFragment.show(this, alarm.activateDate!!, TAG_ACTIVATE_DATE, minDate)
    }
}