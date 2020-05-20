package net.wuqs.ontime.feature.editalarm

import android.view.LayoutInflater
import android.view.ViewGroup
import androidx.core.content.ContextCompat
import androidx.recyclerview.widget.RecyclerView
import net.wuqs.ontime.R
import net.wuqs.ontime.db.Alarm
import java.util.*
import kotlin.collections.ArrayList

class MonthDayAdapter(private val mListener: OnDayClickListener, private val alarm: Alarm)
    : RecyclerView.Adapter<DayItemHolder>() {

    interface OnDayClickListener {

        /**
         * Called when a day in the list is clicked.
         * @param dayIndex The index of the day, from 0 to 30.
         * @param isChecked Whether the day is checked or not.
         */
        fun onDayClick(dayIndex: Int, isChecked: Boolean)

    }

    private lateinit var mDays: List<Int>
    private lateinit var calendar: Calendar

    init {
        initList()
    }

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): DayItemHolder {
        val view = LayoutInflater.from(parent.context)
                .inflate(R.layout.item_day_of_month, parent, false)
        return DayItemHolder(view)
    }

    override fun onBindViewHolder(holder: DayItemHolder, position: Int) {
        holder.toggleBtn.apply {
            val day = mDays[position]
            if (day != -1) {
                calendar[Calendar.DAY_OF_MONTH] = day + 1
                var colorId = R.color.selector_date_checkbox_text
                try {
                    calendar[Calendar.DAY_OF_MONTH]
                } catch (e: IllegalArgumentException) {
                    colorId = R.color.selector_date_checkbox_text_nonexistent
                }
                setTextColor(ContextCompat.getColorStateList(context, colorId))
                val dayText = (day + 1).toString()
                text = dayText
                isChecked = alarm.repeatIndex shr day and 1 == 1
                setOnClickListener { mListener.onDayClick(day, isChecked) }
            } else {
                text = null
                isChecked = false
                isEnabled = false
            }
        }
    }

    fun initList() {
        calendar = Calendar.getInstance()
        calendar.isLenient = false
        val list = ArrayList<Int>(42)
        calendar[Calendar.DAY_OF_MONTH] = 1
        for (i in 0 until calendar[Calendar.DAY_OF_WEEK] - calendar.firstDayOfWeek) {
            list += -1
        }
        list += List(31) { it }
        mDays = list
    }

    override fun getItemCount() = mDays.size
}