package net.wuqs.ontime.ui.alarmeditscreen

import android.content.Context
import android.os.Bundle
import android.support.v4.app.Fragment
import android.text.Editable
import android.text.TextWatcher
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import kotlinx.android.synthetic.main.fragment_non_repeat.*
import kotlinx.android.synthetic.main.partial_edit_cycle_number.*
import net.wuqs.ontime.R
import net.wuqs.ontime.alarm.getDateString
import net.wuqs.ontime.alarm.getRepeatCycleText
import net.wuqs.ontime.alarm.setMidnight
import net.wuqs.ontime.db.Alarm
import net.wuqs.ontime.ui.dialog.DatePickerDialogFragment
import net.wuqs.ontime.util.LogUtils

abstract class RepeatOptionFragment : Fragment(), TextWatcher, DatePickerDialogFragment.OnDateSetListener {

    protected var mListener: OnRepeatIndexPickListener? = null

    protected abstract val mLayout: Int

    protected lateinit var alarm: Alarm

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        arguments?.let {
            alarm = it.getParcelable(ARG_ALARM)
        }
        mLogger.v("onCreate")
    }

    override fun onCreateView(inflater: LayoutInflater, container: ViewGroup?,
                              savedInstanceState: Bundle?): View? {
        return inflater.inflate(mLayout, container, false)
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        et_repeatCycle?.apply {
            setText(alarm.repeatCycle.toString())
            addTextChangedListener(this@RepeatOptionFragment)
        }
        oiv_date?.apply {
            valueText = getDateString(alarm.activateDate)
            setOnClickListener { editActivateDate() }
        }
        showRepeatCycle()
    }

    override fun onAttach(context: Context?) {
        super.onAttach(context)
        mListener = if (context is OnRepeatIndexPickListener) {
            context
        } else {
            throw RuntimeException("$context must implement OnRepeatIndexPickListener")
        }
    }

    override fun onDetach() {
        super.onDetach()
        mListener = null
    }

    // Update repeat cycle after text changed
    override fun afterTextChanged(s: Editable?) {
        s.toString().toIntOrNull()?.let {
            if (it < 1) {
                s?.replace(0, s.length, "1")
                alarm.repeatCycle = 1
            } else {
                alarm.repeatCycle = it
            }
        }
        et_repeatCycle.hint = alarm.repeatCycle.toString()
        showRepeatCycle()
        mListener?.updateRepeatOption(repeatCycle = alarm.repeatCycle)
    }

    override fun beforeTextChanged(s: CharSequence?, start: Int, count: Int, after: Int) {
    }

    override fun onTextChanged(s: CharSequence?, start: Int, before: Int, count: Int) {
    }

    override fun onDateSet(fragment: DatePickerDialogFragment, year: Int, month: Int, dayOfMonth: Int) {
        alarm.activateDate!!.let {
            it.setMidnight(year, month, dayOfMonth)
            mLogger.v(it.time.toString())
            oiv_date.valueText = getDateString(it)
        }
        mListener?.updateActivateDate(year, month, dayOfMonth)
    }

    protected open fun editActivateDate() {
        DatePickerDialogFragment.show(this, alarm.activateDate!!, TAG_ACTIVATE_DATE)
    }

    private fun showRepeatCycle() {
        tv_repeatCycle?.text = getString(R.string.msg_every_x_cycles_2, alarm.getRepeatCycleText(resources))
    }

    interface OnRepeatIndexPickListener {
        fun updateRepeatOption(repeatType: Int? = null, repeatCycle: Int? = null,
                               repeatIndex: Int? = null)

        fun updateActivateDate(year: Int, month: Int, dayOfMonth: Int)
    }

    companion object {
        @JvmStatic
        fun newInstance(alarm: Alarm): RepeatOptionFragment {
            val fragment = when (alarm.repeatType and 0xF) {
                0 -> NonRepeatFragment()
                1 -> DailyRepeatFragment()
                2 -> WeeklyRepeatFragment()
                3 -> MonthlyRepeatFragment()
                4 -> YearlyRepeatFragment()
                else -> throw IllegalArgumentException("Illegal repeat type")
            }
            return fragment.apply {
                arguments = Bundle().apply { putParcelable(ARG_ALARM, alarm) }
            }
        }

        const val ARG_ALARM = "alarm"

    }

    protected val mLogger = LogUtils.Logger("RepeatOptionFragment")
}

const val TAG_ACTIVATE_DATE = "activateDate"