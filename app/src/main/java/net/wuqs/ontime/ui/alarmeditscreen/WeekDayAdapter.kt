package net.wuqs.ontime.ui.alarmeditscreen

import android.support.v7.widget.RecyclerView
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.CheckBox
import net.wuqs.ontime.R
import net.wuqs.ontime.alarm.getOrderedWeekDays
import net.wuqs.ontime.alarm.shortWeekDay
import java.util.*

class WeekDayAdapter(private val mListener: OnDayClickListener, var days: Int)
    : RecyclerView.Adapter<WeekDayAdapter.ViewHolder> () {

    interface OnDayClickListener {
        fun onDayClick(calendarDay: Int, isChecked: Boolean)
    }

    private val mFirstDay = Calendar.getInstance().firstDayOfWeek
    private val mDays = getOrderedWeekDays(mFirstDay)

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): ViewHolder {
        val view = LayoutInflater.from(parent.context)
                .inflate(R.layout.item_day_of_month, parent, false)
        return ViewHolder(view)
    }

    override fun onBindViewHolder(holder: ViewHolder, position: Int) {
        val day = mDays[position]
        (holder.view as CheckBox).apply {
            text = shortWeekDay[day]
            isChecked = days shr (day - 1) and 1 == 1
            setOnClickListener { mListener.onDayClick(day, isChecked) }
        }
    }

    override fun getItemCount() = mDays.size

    inner class ViewHolder(val view: View) : RecyclerView.ViewHolder(view)

}