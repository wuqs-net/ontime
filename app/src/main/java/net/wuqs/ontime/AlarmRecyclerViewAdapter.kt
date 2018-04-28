package net.wuqs.ontime


import android.support.v7.util.DiffUtil
import android.support.v7.util.SortedList
import android.support.v7.widget.RecyclerView
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import kotlinx.android.synthetic.main.fragment_alarm.view.*
import net.wuqs.ontime.AlarmListFragment.OnListFragmentInteractionListener
import net.wuqs.ontime.alarm.getRepeatString
import net.wuqs.ontime.alarm.getTimeString
import net.wuqs.ontime.db.Alarm
import net.wuqs.ontime.utils.AlarmSortCallback
import net.wuqs.ontime.utils.LogUtils

/**
 * [RecyclerView.Adapter] that can display a [Alarm] and makes a call to the
 * specified [OnListFragmentInteractionListener].
 */
class AlarmRecyclerViewAdapter
(private val data: MutableList<Alarm>,
 private val mListener: OnListFragmentInteractionListener?)
    : RecyclerView.Adapter<AlarmRecyclerViewAdapter.ViewHolder>() {

//    private val sortCallback = AlarmSortCallback(this)
//    private val list = SortedList(Alarm::class.java, sortCallback)

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): ViewHolder {
        val view = LayoutInflater.from(parent.context)
                .inflate(R.layout.fragment_alarm, parent, false)
        return ViewHolder(view)
    }

    override fun onBindViewHolder(holder: ViewHolder, position: Int) {
        val item = data[position]
        with(holder.mView) {
            tag = item
            setOnClickListener { mListener?.onListItemClick(item) }

            tvAlarmTime.text = getTimeString(this.context, item)
            tvAlarmTitle.visibility = if (!item.title.isEmpty()) View.VISIBLE else View.GONE
            tvAlarmTitle.text = item.title
            tvRepeatPattern.text = getRepeatString(this.context, item)
            tvCountdown.visibility = View.GONE
            tvCountdown.text = "10 小时 58 分钟 后"
//            tvCountdown.visibility = if (position == 0) View.VISIBLE else View.GONE
            switch1.isChecked = item.isEnabled
            switch1.setOnClickListener { mListener?.onAlarmSwitchClick(item, switch1.isChecked) }
        }
    }

    override fun getItemCount(): Int = data.size

    fun setAlarms(models: List<Alarm>) {
        val diff = DiffUtil.calculateDiff(AlarmDiffCallback(data, models))
        data.clear()
        data.addAll(models)
        diff.dispatchUpdatesTo(this)
        mListener?.onRecyclerViewUpdate(itemCount)
    }

    inner class ViewHolder(val mView: View) : RecyclerView.ViewHolder(mView) {
        override fun toString() = "${super.toString()} '${mView.tvAlarmTitle.text}'"
    }

    private val LOGGER = LogUtils.Logger("AlarmRcVwAdapter")

}
