package net.wuqs.ontime


import android.support.design.widget.Snackbar
import android.support.v7.util.DiffUtil
import android.support.v7.widget.RecyclerView
import android.support.v7.widget.SwitchCompat
import android.util.Log
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import kotlinx.android.synthetic.main.activity_main.*
import kotlinx.android.synthetic.main.fragment_alarm.view.*
import net.wuqs.ontime.AlarmFragment.OnListFragmentInteractionListener
import net.wuqs.ontime.alarm.*
import net.wuqs.ontime.db.Alarm
import java.util.*

/**
 * [RecyclerView.Adapter] that can display a [Alarm] and makes a call to the
 * specified [OnListFragmentInteractionListener].
 */
class AlarmRecyclerViewAdapter
(private val data: MutableList<Alarm>, private val mListener: OnListFragmentInteractionListener?)
    : RecyclerView.Adapter<AlarmRecyclerViewAdapter.ViewHolder>() {

    private val mOnClickListener: View.OnClickListener

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): ViewHolder {
        val view = LayoutInflater.from(parent.context)
                .inflate(R.layout.fragment_alarm, parent, false)
        return ViewHolder(view)
    }

    override fun onBindViewHolder(holder: ViewHolder, position: Int) {
        Log.v("onBindViewHolder", "onbind")
        val item = data[position]
        with(holder.mView) {
            tag = item
            setOnClickListener(mOnClickListener)

            tvAlarmTime.text = getTimeString(this.context, item)
            tvAlarmTitle.visibility = if (!item.title.isEmpty()) View.VISIBLE else View.GONE
            tvAlarmTitle.text = item.title
            tvRepeatPattern.text = getRepeatString(this.context, item)
            tvCountdown.visibility = View.GONE
            tvCountdown.text = "10 小时 58 分钟 后"
//            tvCountdown.visibility = if (position == 0) View.VISIBLE else View.GONE
            if (switch1.isChecked != item.isEnabled) {
                Log.v("AlarmRecyclerVA", "Switch changed. switch ${switch1.isChecked}, item ${item.isEnabled}")
                switch1.isChecked = item.isEnabled
            }
            switch1.setOnClickListener { mListener?.onAlarmSwitchClick(it as SwitchCompat, item) }
        }
    }

    override fun onBindViewHolder(holder: ViewHolder, position: Int, payloads: MutableList<Any>) {
        onBindViewHolder(holder, position)
        if (payloads.isEmpty()) {
            return
        }
        Log.v("Payload", "${payloads[0]}")
        holder.mView.tag = payloads[0]
    }

    override fun getItemCount(): Int = data.size

    fun setAlarms(models: List<Alarm>) {
        if (data.isEmpty()) {
            DiffUtil.calculateDiff(AlarmDiffCallback(data, models)).dispatchUpdatesTo(this)
            data.addAll(models)
//            notifyDataSetChanged()
        } else {
            DiffUtil.calculateDiff(AlarmDiffCallback(data, models)).dispatchUpdatesTo(this)
            data.clear()
            data.addAll(models)
        }
        Log.v("AlarmRecyclerVA", "Adapter updated\n$models")
    }

    inner class ViewHolder(val mView: View) : RecyclerView.ViewHolder(mView) {
        override fun toString() = "${super.toString()} '${mView.tvAlarmTitle.text}'"
    }

    init {
        mOnClickListener = View.OnClickListener { v ->
            val item = v.tag as Alarm
            // Notify the active callbacks interface (the activity, if the fragment is attached to
            // one) that an item has been selected.
            mListener?.onListItemClick(item)
        }
    }
}
