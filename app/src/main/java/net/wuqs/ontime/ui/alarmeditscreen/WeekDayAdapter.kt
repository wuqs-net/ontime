package net.wuqs.ontime.ui.alarmeditscreen

import android.support.v7.widget.RecyclerView
import android.view.LayoutInflater
import android.view.ViewGroup
import net.wuqs.ontime.R
import net.wuqs.ontime.alarm.getOrderedWeekDays
import net.wuqs.ontime.alarm.getShortWeekDays
import java.util.*

class WeekDayAdapter(private val mListener: OnDayClickListener, var days: Int)
    : RecyclerView.Adapter<DayItemHolder> () {

    interface OnDayClickListener {
        fun onDayClick(calendarDay: Int, isChecked: Boolean)
    }

    private val mFirstDay = Calendar.getInstance().firstDayOfWeek
    private val mDays = getOrderedWeekDays(mFirstDay)

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): DayItemHolder {
        val view = LayoutInflater.from(parent.context)
                .inflate(R.layout.item_day_of_month, parent, false)
        return DayItemHolder(view)
    }

    override fun onBindViewHolder(holder: DayItemHolder, position: Int) {
        val day = mDays[position]
        holder.toggleBtn.apply {
            val dayText = getShortWeekDays()[day]
            text = dayText
            isChecked = days shr (day - 1) and 1 == 1
            setOnClickListener { mListener.onDayClick(day, isChecked) }
        }
    }

    override fun getItemCount() = mDays.size
}