package net.wuqs.ontime.feature.home


import androidx.recyclerview.widget.DiffUtil
import androidx.recyclerview.widget.RecyclerView
import android.text.format.DateFormat
import android.view.*
import kotlinx.android.synthetic.main.item_alarm.view.*
import net.wuqs.ontime.R
import net.wuqs.ontime.alarm.*
import net.wuqs.ontime.db.Alarm
import net.wuqs.ontime.feature.home.AlarmListFragment.OnListFragmentActionListener
import net.wuqs.ontime.util.Logger
import java.util.*

/**
 * [RecyclerView.Adapter] that can display a [Alarm] and makes a call to the
 * specified [OnListFragmentActionListener].
 */
class AlarmRecyclerViewAdapter
(private val data: MutableList<Alarm>,
 private val mListener: OnListFragmentActionListener?)
    : RecyclerView.Adapter<AlarmRecyclerViewAdapter.ViewHolder>() {

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): ViewHolder {
        val view = LayoutInflater.from(parent.context)
                .inflate(R.layout.item_alarm, parent, false)
        return ViewHolder(view)
    }

    override fun onBindViewHolder(holder: ViewHolder, position: Int) {
        holder.bindData(data[position])
    }

    override fun getItemCount() = data.size

    fun setAlarms(models: List<Alarm>) {
        val diff = DiffUtil.calculateDiff(AlarmDiffCallback(data, models))
        data.clear()
        data.addAll(models)
        diff.dispatchUpdatesTo(this)
    }

    inner class ViewHolder(private val mView: View) : RecyclerView.ViewHolder(mView) {

        fun bindData(item: Alarm) = with(mView) {
            setOnClickListener { mListener?.onListItemClick(data[layoutPosition]) }

            swc_enable_alarm.let {
                it.setOnCheckedChangeListener(null)
                it.isEnabled = item.nextTime != null
                it.isChecked = item.isEnabled
                it.visibility = if (item.isHistorical) View.INVISIBLE else View.VISIBLE
                it.setOnCheckedChangeListener { _, isChecked ->
                    mListener?.onAlarmSwitchClick(data[layoutPosition], isChecked)
                    updateEnabledDisplay(isChecked)
                }
            }

            updateEnabledDisplay(item.isEnabled)

            tv_alarm_time.text = item.createTimeString(context, false, true)
            if (DateFormat.is24HourFormat(context)) {
                iv_dot_am.visibility = View.INVISIBLE
                iv_dot_pm.visibility = View.INVISIBLE
            } else {
                iv_dot_am.visibility = if (item.hour < 12) View.VISIBLE else View.INVISIBLE
                iv_dot_pm.visibility = if (item.hour >= 12) View.VISIBLE else View.INVISIBLE
            }

            item.title.let {
                tv_alarm_title.visibility = if (it.isNullOrEmpty()) View.GONE else View.VISIBLE
                tv_alarm_title.text = it
            }

            // Display information about snooze and next alarm
            item.nextTime.let {
                tv_next_date.text = when {
                    it == null -> item.activateDate.createDateString()
                    item.snoozed <= 0 -> it.createDateString()
                    else -> context.getString(
                            R.string.msg_snoozed_time,
                            if (it.sameDayAs(Calendar.getInstance())) {
                                it.createTimeString(context)
                            } else {
                                it.createDateString()
                            },
                            context.resources.getQuantityString(R.plurals.msg_times,
                                    item.snoozed, item.snoozed)
                    )
                }
            }
            iv_snoozed.run {
                if (item.snoozed > 0) {
                    visibility = View.VISIBLE
                    setImageResource(R.drawable.ic_postpone_black)
                    // Change back to right arrow if not preferred
                } else if (item.nextTime?.after(item.getNextOccurrence()) == true) {
                    visibility = View.VISIBLE
                    setImageResource(R.drawable.ic_skip_next_black)
                } else {
                    visibility = View.GONE
                }
            }

            // Display information about repeat
            if (item.repeatType == Alarm.NON_REPEAT) {
                group_repeat.visibility = View.GONE
            } else {
                group_repeat.visibility = View.VISIBLE
                tv_repeat_pattern.text = item.getRepeatString(context)
            }

            // Create context menu.
            setOnCreateContextMenuListener { menu, v, menuInfo ->
                MenuInflater(v.context).inflate(R.menu.menu_alarm_list_context, menu)

                // Set title for the menu.
                // If the alarm title is empty, use its next time or activate time.
                menu.setHeaderTitle(item.getTitleOrTime(context))

                val menuItemListener = MenuItem.OnMenuItemClickListener {
                    mListener?.onContextMenuItemSelected(data[layoutPosition], it)
                    true
                }
                menu.findItem(R.id.item_skip_once).run {
                    setOnMenuItemClickListener(menuItemListener)
                    isVisible = item.nextTime?.let { item.getNextOccurrence(it) } != null
                }
                menu.findItem(R.id.item_delete).run {
                    setOnMenuItemClickListener(menuItemListener)
                }
            }

            // Countdown to next alarm
//            item.nextTime?.let {
//                val now = Calendar.getInstance()
//                if (layoutPosition == 0 && now.sameDayAs(it)) {
//                    object : CountDownTimer(it.timeInMillis - now.timeInMillis, 1000) {
//                        override fun onTick(millisUntilFinished: Long) {
//                            var millis = millisUntilFinished
//                            val hour = millis / (1000 * 60 * 60)
//                            millis %= 1000 * 60 * 60
//                            val minute = millis / (1000 * 60)
//                            millis %= 1000 * 60
//                            val second = millis / 1000
//                            tv_countdown.text = "%02d:%02d:%02d".format(hour, minute, second)
//                        }
//                        override fun onFinish() {
//                            tv_countdown.visibility = View.GONE
//                        }
//                    }.start()
//                    tv_countdown.visibility = View.VISIBLE
//                } else {
//                    tv_countdown.visibility = View.GONE
//                }
//            }
            tv_countdown.visibility = View.GONE
        }

        private fun updateEnabledDisplay(enabled: Boolean) = mView.run {
            tv_alarm_time.isEnabled = enabled
            iv_dot_am.isEnabled = enabled
            iv_dot_pm.isEnabled = enabled
            tv_alarm_title.isEnabled = enabled
            tv_repeat_pattern.isEnabled = enabled
            tv_next_date.isEnabled = enabled
            iv_repeat.isEnabled = enabled
            iv_snoozed.isEnabled = enabled
        }
    }

    private val mLogger = Logger("AlarmAdapter")

}
