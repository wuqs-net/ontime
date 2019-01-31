package net.wuqs.ontime.feature.editalarm

import android.os.Bundle
import androidx.fragment.app.Fragment
import androidx.recyclerview.widget.GridLayoutManager
import android.view.View
import kotlinx.android.synthetic.main.fragment_repeat_monthly.*
import net.wuqs.ontime.R
import java.util.*

/**
 * A simple [Fragment] subclass.
 * Activities that contain this fragment must implement the
 * [RepeatOptionFragment.OnRepeatIndexPickListener] interface
 * to handle interaction events.
 * Use the [RepeatOptionFragment.newInstance] factory method to
 * create an instance of this fragment.
 *
 */
class MonthlyRepeatFragment : RepeatOptionFragment(),
        MonthDayAdapter.OnDayClickListener {

    override val mLayout = R.layout.fragment_repeat_monthly

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        rv_day_picker.layoutManager = GridLayoutManager(context, 7)
        rv_day_picker.adapter = MonthDayAdapter(this, alarm.repeatIndex)
        if (alarm.repeatIndex == 0) checkDefaultDate()
        mLogger.v("onViewCreated")
    }

    override fun onDayClick(which: Int, isChecked: Boolean) {
        val setBit = 1 shl which
        alarm.repeatIndex = if (isChecked) {
            alarm.repeatIndex or setBit
        } else {
            alarm.repeatIndex and setBit.inv()
        }
        if (alarm.repeatIndex == 0) {
            checkDefaultDate()
        }
        mListener?.updateRepeatOption(repeatIndex = alarm.repeatIndex)
    }

    private fun checkDefaultDate() {
        val defaultDate = alarm.activateDate!![Calendar.DATE] - 1
        onDayClick(defaultDate, true)
        (rv_day_picker.adapter as MonthDayAdapter).run {
            dates = alarm.repeatIndex
            notifyItemChanged(defaultDate)
        }
    }
}

val Int.binString: String get() = Integer.toBinaryString(this)
val Int.hexString: String get() = Integer.toHexString(this)
fun Int.setBit(bitIndex: Int, on: Boolean) = if (on) {
    this or (1 shl bitIndex)
} else {
    this and (1 shl bitIndex).inv()
}