package net.wuqs.ontime.feature.editalarm

import android.os.Bundle
import androidx.recyclerview.widget.GridLayoutManager
import android.view.View
import kotlinx.android.synthetic.main.fragment_repeat_weekly.*
import net.wuqs.ontime.R
import java.util.*

class WeeklyRepeatFragment : RepeatOptionFragment(), WeekDayAdapter.OnDayClickListener {
    override val mLayout = R.layout.fragment_repeat_weekly

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        alarm.repeatIndex = (alarm.repeatIndex and 0b1111111) + (Calendar.getInstance().firstDayOfWeek shl 8)

        // TODO: Update next date display / notify user first day of week is changed
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        rv_day_picker.layoutManager = GridLayoutManager(context, 7)
        rv_day_picker.adapter = WeekDayAdapter(this, alarm.repeatIndex)
        tv_advanced.setOnClickListener {
            //            if (group_advanced.visibility == View.VISIBLE) {
//                group_advanced.visibility = View.GONE
//                iv_advanced.setImageResource(R.drawable.ic_arrow_drop_down_black_24dp)
//            } else {
            it.visibility = View.GONE
            group_advanced.visibility = View.VISIBLE
//                iv_advanced.setImageResource(R.drawable.ic_arrow_drop_up_black_24dp)
//            }
        }
        if (alarm.repeatCycle != 1 || alarm.activateDate!!.after(Calendar.getInstance())) {
            tv_advanced.callOnClick()
        }
    }

    override fun onDayClick(calendarDay: Int, isChecked: Boolean) {
        alarm.repeatIndex = alarm.repeatIndex.setBit(calendarDay - 1, isChecked)
        mListener?.updateRepeatOption()
    }
}