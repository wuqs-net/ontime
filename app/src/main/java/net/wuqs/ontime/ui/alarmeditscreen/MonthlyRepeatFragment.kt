package net.wuqs.ontime.ui.alarmeditscreen

import android.os.Bundle
import android.support.v4.app.Fragment
import android.support.v7.widget.GridLayoutManager
import android.view.View
import kotlinx.android.synthetic.main.fragment_repeat_monthly.*
import net.wuqs.ontime.R
import net.wuqs.ontime.util.LogUtils
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
        View.OnClickListener,
        MonthDayAdapter.OnDayClickListener {

    override val mLayout = R.layout.fragment_repeat_monthly

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        rv_day_picker.apply {
            layoutManager = GridLayoutManager(this@MonthlyRepeatFragment.context, 7)
            adapter = MonthDayAdapter(this@MonthlyRepeatFragment, mAlarm.repeatIndex)
        }
        if (mAlarm.repeatIndex == 0) checkDefaultDate()
        mLogger.v("onViewCreated")
    }

    override fun onClick(v: View?) {
        TODO("not implemented") //To change body of created functions use File | Settings | File Templates.
    }

    override fun onDayClick(which: Int, isChecked: Boolean) {
        val setBit = 1 shl which
        mAlarm.repeatIndex = if (isChecked) {
            mAlarm.repeatIndex or setBit
        } else {
            mAlarm.repeatIndex and setBit.inv()
        }
        if (mAlarm.repeatIndex == 0) {
            checkDefaultDate()
        }
        mListener?.updateRepeatOption(repeatIndex = mAlarm.repeatIndex)
    }

    private fun checkDefaultDate() {
        val defaultDate = mAlarm.activateDate!![Calendar.DATE] - 1
        onDayClick(defaultDate, true)
        (rv_day_picker.adapter as MonthDayAdapter).run {
            dates = mAlarm.repeatIndex
            notifyItemChanged(defaultDate)
        }
    }
}

val Int.binString get() = Integer.toBinaryString(this)!!
val Int.hexString get() = Integer.toHexString(this)!!
