package net.wuqs.ontime.ui.alarmeditscreen

import android.support.v7.widget.RecyclerView
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.CheckBox
import net.wuqs.ontime.R

class MonthDayAdapter(private val mListener: OnDayClickListener, var dates: Int)
    : RecyclerView.Adapter<MonthDayAdapter.ViewHolder> () {

    interface OnDayClickListener {
        fun onDayClick(which: Int, isChecked: Boolean)
    }

    private val mDays = List(31) { it + 1 }

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): ViewHolder {
        val view = LayoutInflater.from(parent.context)
                .inflate(R.layout.item_day_of_month, parent, false)
        return ViewHolder(view)
    }

    override fun onBindViewHolder(holder: ViewHolder, position: Int) {
        (holder.view as CheckBox).apply {
            text = mDays[position].toString()
            isChecked = dates shr position and 1 == 1
            setOnClickListener { mListener.onDayClick(position, isChecked) }
        }
    }

    override fun getItemCount() = mDays.size

    inner class ViewHolder(val view: View) : RecyclerView.ViewHolder(view)

}