package net.wuqs.ontime.ui.alarmeditscreen

import android.support.v7.widget.RecyclerView
import android.view.LayoutInflater
import android.view.ViewGroup
import net.wuqs.ontime.R

class MonthDayAdapter(private val mListener: OnDayClickListener, var dates: Int)
    : RecyclerView.Adapter<DayItemHolder> () {

    interface OnDayClickListener {
        fun onDayClick(which: Int, isChecked: Boolean)
    }

    private val mDays = List(31) { it + 1 }

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): DayItemHolder {
        val view = LayoutInflater.from(parent.context)
                .inflate(R.layout.item_day_of_month, parent, false)
        return DayItemHolder(view)
    }

    override fun onBindViewHolder(holder: DayItemHolder, position: Int) {
        holder.toggleBtn.apply {
            val dayText = mDays[position].toString()
            textOn = dayText
            textOff = dayText
            isChecked = dates shr position and 1 == 1
            setOnClickListener { mListener.onDayClick(position, isChecked) }
        }
    }

    override fun getItemCount() = mDays.size
}