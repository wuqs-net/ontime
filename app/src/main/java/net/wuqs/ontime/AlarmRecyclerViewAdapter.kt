package net.wuqs.ontime

import android.support.v7.widget.RecyclerView
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.TextView


import net.wuqs.ontime.AlarmFragment.OnListFragmentInteractionListener
import net.wuqs.ontime.dummy.DummyContent.DummyItem

import kotlinx.android.synthetic.main.fragment_alarm.view.*
import net.wuqs.ontime.alarm.Alarm
import net.wuqs.ontime.alarm.getTimeString
import net.wuqs.ontime.db.AlarmModel

/**
 * [RecyclerView.Adapter] that can display a [DummyItem] and makes a call to the
 * specified [OnListFragmentInteractionListener].
 * TODO: Replace the implementation with code for your data type.
 */
class AlarmRecyclerViewAdapter// Notify the active callbacks interface (the activity, if the fragment is attached to
// one) that an item has been selected.
(private val data: MutableList<Alarm>, private val mListener: OnListFragmentInteractionListener?)
    : RecyclerView.Adapter<AlarmRecyclerViewAdapter.ViewHolder>() {

    private val mOnClickListener: View.OnClickListener

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): ViewHolder {
        val view = LayoutInflater.from(parent.context)
                .inflate(R.layout.fragment_alarm, parent, false)
        return ViewHolder(view)
    }

    override fun onBindViewHolder(holder: ViewHolder, position: Int) {
        val item = data[position]

        with(holder) {

        }
        with(holder.mView) {
            tag = item
            setOnClickListener(mOnClickListener)

            content.text = "不重复"
            tvAlarmTime.text = getTimeString(holder.context, item)
            tvAlarmTitle.visibility = if (!item.title.isEmpty()) View.VISIBLE else View.GONE
            tvAlarmTitle.text = item.title
            tvCountdown.text = "10 小时 58 分钟 后"
//            tvCountdown.visibility = if (position == 0) View.VISIBLE else View.GONE
            tvCountdown.visibility = View.GONE
            switch1.isChecked = true
        }
    }

    override fun getItemCount(): Int = data.size

    fun setAlarms(models: List<AlarmModel>) {
        data.clear()
        for (model in models) {
            data.add(Alarm(model))
        }
        notifyDataSetChanged()
    }

    inner class ViewHolder(val mView: View) : RecyclerView.ViewHolder(mView) {
        val context = mView.context
        val tvAlarmTime = mView.tvAlarmTime
        val tvAlarmTitle = mView.tvAlarmTitle
        val mContentView = mView.content
        val tvCountdown = mView.tvCountdown

        override fun toString(): String {
            return super.toString() + " '" + mContentView.text + "'"
        }
    }

    init {
        mOnClickListener = View.OnClickListener { v ->
            val item = v.tag as Alarm
            // Notify the active callbacks interface (the activity, if the fragment is attached to
            // one) that an item has been selected.
            mListener?.onListFragmentInteraction(item)
        }
    }
}
