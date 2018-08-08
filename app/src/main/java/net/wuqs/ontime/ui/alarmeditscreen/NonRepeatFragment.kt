package net.wuqs.ontime.ui.alarmeditscreen

import net.wuqs.ontime.R
import net.wuqs.ontime.alarm.setHms
import net.wuqs.ontime.ui.dialog.DatePickerFragment
import net.wuqs.ontime.ui.dialog.TAG_ACTIVATE_DATE
import java.util.*

class NonRepeatFragment : RepeatOptionFragment() {
    override val mLayout = R.layout.fragment_non_repeat

    override fun editActivateDate() {
        val minDate = Calendar.getInstance().apply {
            setHms(mAlarm.hour, mAlarm.minute)
            if (timeInMillis < System.currentTimeMillis()) add(Calendar.DATE, 1)
        }
        DatePickerFragment.newInstance(mAlarm.activateDate!!, minDate).apply {
            setTargetFragment(this@NonRepeatFragment, 0)
        }.show(fragmentManager, TAG_ACTIVATE_DATE)
    }
}