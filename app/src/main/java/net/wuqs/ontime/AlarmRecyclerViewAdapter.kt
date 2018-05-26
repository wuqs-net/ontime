package net.wuqs.ontime


import android.support.v7.util.DiffUtil
import android.support.v7.widget.RecyclerView
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import kotlinx.android.synthetic.main.fragment_alarm.view.*
import net.wuqs.ontime.AlarmListFragment.OnListFragmentActionListener
import net.wuqs.ontime.alarm.getDateString
import net.wuqs.ontime.alarm.getTimeString
import net.wuqs.ontime.db.Alarm
import net.wuqs.ontime.utils.LogUtils

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
                .inflate(R.layout.fragment_alarm, parent, false)
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
            alarm_enable.setOnClickListener {
                mListener?.onAlarmSwitchClick(data[adapterPosition], alarm_enable.isChecked)
            }

            alarm_time.text = getTimeString(this.context, item)
            alarm_title.visibility = if (!item.title!!.isEmpty()) View.VISIBLE else View.GONE
            alarm_title.text = item.title

            next_date.text = getDateString(item.nextTime)
            repeat_icon.visibility =
                    if (item.repeatType == Alarm.NON_REPEAT) View.GONE
                    else View.VISIBLE

            next_countdown.visibility = View.GONE
//            tvCountdown.text = "10小时58分钟后"
//            tvCountdown.visibility = if (position == 0) View.VISIBLE else View.GONE
            alarm_enable.isChecked = item.isEnabled
            alarm_enable.isEnabled = item.getNextOccurrence() != null
        }
    }

    private val LOGGER = LogUtils.Logger("AlarmAdapter")

}
