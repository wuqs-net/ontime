package net.wuqs.ontime.ui.mainscreen


import android.support.v7.util.DiffUtil
import android.support.v7.widget.RecyclerView
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import kotlinx.android.synthetic.main.item_alarm.view.*
import net.wuqs.ontime.R
import net.wuqs.ontime.alarm.getDateString
import net.wuqs.ontime.alarm.getRepeatString
import net.wuqs.ontime.alarm.getTimeString
import net.wuqs.ontime.alarm.sameDayAs
import net.wuqs.ontime.db.Alarm
import net.wuqs.ontime.ui.mainscreen.AlarmListFragment.OnListFragmentActionListener
import net.wuqs.ontime.util.LogUtils
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
        mListener?.onRecyclerViewUpdate(itemCount)
        diff.dispatchUpdatesTo(this)
    }

    inner class ViewHolder(private val mView: View) : RecyclerView.ViewHolder(mView) {
        fun bindData(item: Alarm) = with(mView) {
            setOnClickListener { mListener?.onListItemClick(data[adapterPosition]) }

            swc_enable_alarm.let {
                it.setOnCheckedChangeListener(null)
                it.isEnabled = item.nextTime != null
                it.isChecked = item.isEnabled
                it.setOnCheckedChangeListener { _, isChecked ->
                    mListener?.onAlarmSwitchClick(data[adapterPosition], isChecked)
                    updateEnabledDisplay(isChecked)
                }
            }

            updateEnabledDisplay(item.isEnabled)

            tv_alarm_time.text = getTimeString(context, item)
            tv_alarm_title.text = item.title

            // Display information about snooze and next alarm
            item.nextTime.let {
                tv_next_date.visibility = if (it == null) View.GONE else View.VISIBLE
                tv_next_date.text = when {
                    it == null -> ""
                    item.snoozed <= 0 -> getDateString(it)
                    else -> context.getString(
                            R.string.msg_snoozed_until,
                            if (it.sameDayAs(Calendar.getInstance())) {
                                getTimeString(context, it)
                            } else {
                                getDateString(it)
                            },
                            context.resources.getQuantityString(R.plurals.msg_times,
                                    item.snoozed, item.snoozed)
                    )
                }
            }
            iv_snoozed.visibility = if (item.snoozed <= 0) View.GONE else View.VISIBLE

            // Display information about repeat
            if (item.repeatType == Alarm.NON_REPEAT) {
                group_repeat.visibility = View.GONE
            } else {
                group_repeat.visibility = View.VISIBLE
                tv_repeat_pattern.text = getRepeatString(context, item)
            }

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
            tv_alarm_title.isEnabled = enabled
            tv_repeat_pattern.isEnabled = enabled
            tv_next_date.isEnabled = enabled
            iv_repeat.isEnabled = enabled
            iv_snoozed.isEnabled = enabled
        }
    }

    private val mLogger = LogUtils.Logger("AlarmAdapter")

}