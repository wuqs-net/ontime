package net.wuqs.ontime.feature.history

import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.recyclerview.widget.RecyclerView
import kotlinx.android.synthetic.main.item_history_expanded.view.*
import kotlinx.android.synthetic.main.partial_history_summary.view.*
import net.wuqs.ontime.R
import net.wuqs.ontime.alarm.createDateString
import net.wuqs.ontime.alarm.createTimeString
import net.wuqs.ontime.db.Alarm

private const val VIEW_TYPE_NORMAL = 1
private const val VIEW_TYPE_EXPANDED = 2

class HistoryRVAdapter(
    private var data: List<Alarm>,
    private val listener: OnListItemActionListener
) : RecyclerView.Adapter<HistoryEntryVH>() {

    private var expandedPos = -1

    override fun getItemViewType(position: Int): Int {
        return if (position == expandedPos) VIEW_TYPE_EXPANDED else VIEW_TYPE_NORMAL
    }

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): HistoryEntryVH {
        return if (viewType == VIEW_TYPE_EXPANDED) {
            val view = LayoutInflater.from(parent.context).inflate(R.layout.item_history_expanded,
                parent, false)
            HistoryEntryVH(view)
        } else {
            val view = LayoutInflater.from(parent.context).inflate(R.layout.item_history_expanded,
                parent, false)
            HistoryEntryVH(view)
        }
    }

    override fun getItemCount() = data.size

    override fun onBindViewHolder(holder: HistoryEntryVH, position: Int) {
        val alarm = data[position]
        holder.view.run {
            tv_alarm_time.text = alarm.activateDate.createTimeString(context, hairSpace = true)
            tv_alarm_title.run {
                if (alarm.title.isNullOrEmpty()) {
                    visibility = View.GONE
                } else {
                    visibility = View.VISIBLE
                    text = alarm.title
                }
            }
            tv_done_time.text = context.getString(R.string.msg_done_time,
                alarm.nextTime.createTimeString(context), alarm.nextTime.createDateString())
            val expandListener = { _: View ->
                if (getItemViewType(position) == VIEW_TYPE_EXPANDED) {
                    expandedPos = -1
                } else {
                    val previousExpandedId = expandedPos
                    expandedPos = position
                    if (previousExpandedId != -1) notifyItemChanged(previousExpandedId)
                }
                notifyItemChanged(position)
            }
            iv_expand.setOnClickListener(expandListener)
            this.setOnClickListener(expandListener)

            if (getItemViewType(position) == VIEW_TYPE_EXPANDED) {
                cl_expanded.visibility = View.VISIBLE
                iv_expand.setImageResource(R.drawable.ic_expand_less_black_24dp)
                tv_records.text = alarm.notes
                tv_records.visibility = View.VISIBLE
                cl_records.setOnClickListener { listener.onItemNotesClick(position, alarm) }
            } else {
                cl_expanded.visibility = View.GONE
            }
        }
    }

    fun setData(data: List<Alarm>) {
        this.data = data
        notifyDataSetChanged()
    }

    interface OnListItemActionListener {
        fun onItemNotesClick(position: Int, alarm: Alarm)
    }

}