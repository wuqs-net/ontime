package net.wuqs.ontime.ui.feature.alarm

import android.support.v7.widget.RecyclerView
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.Button
import net.wuqs.ontime.R
import java.util.*

class DelayOptionAdapter(
    private val mListener: OnListItemClickListener,
    private val mOptions: List<Pair<Int, Int>>
) : RecyclerView.Adapter<DelayOptionAdapter.ViewHolder>() {

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): ViewHolder {
        val button = LayoutInflater.from(parent.context)
                .inflate(R.layout.item_delay_option, parent, false)
        return ViewHolder(button)
    }

    override fun onBindViewHolder(holder: ViewHolder, position: Int) {
        val (quantity, unit) = mOptions[position]
        val intervalStr = when (unit) {
            Calendar.MINUTE -> R.plurals.minutes_with_quan
            Calendar.HOUR_OF_DAY -> R.plurals.hours_with_quan
            Calendar.DATE -> R.plurals.days_with_quan
            Calendar.WEEK_OF_YEAR -> R.plurals.weeks_with_quan
            else -> 0
        }
        (holder.view as Button).apply {
            text = resources.getQuantityString(intervalStr, quantity, quantity)
            setOnClickListener { mListener.onDelayOptionClick(quantity, unit) }
        }
    }

    override fun getItemCount(): Int {
        return mOptions.size
    }

    class ViewHolder(val view: View) : RecyclerView.ViewHolder(view)

    interface OnListItemClickListener {
        fun onDelayOptionClick(quantity: Int, unit: Int)
    }

    companion object {
        val ALL_INTERVALS = listOf(
                3 to Calendar.MINUTE,
                10 to Calendar.MINUTE,
                1 to Calendar.HOUR_OF_DAY,
                30 to Calendar.MINUTE,
                3 to Calendar.HOUR_OF_DAY,
                6 to Calendar.HOUR_OF_DAY,
                1 to Calendar.DATE,
                2 to Calendar.DATE,
                1 to Calendar.WEEK_OF_YEAR
        )
    }
}