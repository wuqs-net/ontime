package net.wuqs.ontime.feature.currentalarm

import androidx.recyclerview.widget.RecyclerView
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.Button
import net.wuqs.ontime.R
import net.wuqs.ontime.data.SnoozeLength
import java.util.*

class DelayOptionAdapter(
        private val listener: DelayOptionFragment.DelayOptionListener,
        private val options: List<SnoozeLength>
) : RecyclerView.Adapter<DelayOptionAdapter.ViewHolder>() {

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): ViewHolder {
        val button = LayoutInflater.from(parent.context)
                .inflate(R.layout.item_delay_option, parent, false)
        return ViewHolder(button)
    }

    override fun onBindViewHolder(holder: ViewHolder, position: Int) {
        val (amount, unit) = options[position]
        val intervalStr = when (unit) {
            Calendar.SECOND -> R.plurals.seconds_with_quan
            Calendar.MINUTE -> R.plurals.minutes_with_quan
            Calendar.HOUR_OF_DAY -> R.plurals.hours_with_quan
            Calendar.DATE -> R.plurals.days_with_quan
            Calendar.WEEK_OF_YEAR -> R.plurals.weeks_with_quan
            else -> 0
        }
        (holder.view as Button).apply {
            text = resources.getQuantityString(intervalStr, amount, amount)
            setOnClickListener { listener.onDelayOptionClick(amount, unit) }
        }
    }

    override fun getItemCount(): Int {
        return options.size
    }

    class ViewHolder(val view: View) : RecyclerView.ViewHolder(view)

    companion object {
        val ALL_INTERVALS = listOf(
                SnoozeLength("3m"),
                SnoozeLength("10m"),
                SnoozeLength("30m"),
                SnoozeLength("1h"),
                SnoozeLength("3h"),
                SnoozeLength("6h"),
                SnoozeLength("1d"),
                SnoozeLength("2d"),
                SnoozeLength("1w")
        )
    }
}