package net.wuqs.ontime.ui.missedalarms

import android.support.v7.widget.RecyclerView
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import kotlinx.android.synthetic.main.item_missed_alarm.view.*
import net.wuqs.ontime.R
import net.wuqs.ontime.alarm.getDateTimeString
import net.wuqs.ontime.db.Alarm

class MissedAlarmsAdapter(private val mListener: OnListInteractListener, private val data: List<Alarm>)
    : RecyclerView.Adapter<MissedAlarmsAdapter.MissedAlarmVH>() {

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): MissedAlarmVH {
        val view = LayoutInflater.from(parent.context)
                .inflate(R.layout.item_missed_alarm, parent, false)
        return MissedAlarmVH(view)
    }

    override fun getItemCount(): Int = data.size

    override fun onBindViewHolder(holder: MissedAlarmVH, position: Int) {
        holder.bind(data[position])
    }

    interface OnListInteractListener {
        fun onListItemClick(alarm: Alarm)
    }

    inner class MissedAlarmVH(private val mView: View) : RecyclerView.ViewHolder(mView) {
        fun bind(alarm: Alarm) = with(mView) {
            tv_alarm_title.text = alarm.title
            tv_alarm_time.text = alarm.getNextOccurrence().let {
                if (it == null) {
                    getDateTimeString(context, alarm.nextTime)
                } else {
                    context.getString(R.string.msg_missed_alarm_next_time,
                            getDateTimeString(context, alarm.nextTime),
                            getDateTimeString(context, it))
                }
            }
        }
    }
}