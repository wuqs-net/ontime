package net.wuqs.ontime

import android.os.Bundle
import android.support.v7.util.DiffUtil
import android.util.Log
import net.wuqs.ontime.db.Alarm

class AlarmDiffCallback(val oldAlarms: List<Alarm>, val newAlarms: List<Alarm>) : DiffUtil.Callback() {

    override fun areItemsTheSame(oldItemPosition: Int, newItemPosition: Int): Boolean {
        val b = oldAlarms[oldItemPosition].id == newAlarms[newItemPosition].id
//        Log.v("AlarmDiffCallback", b.toString() + "item" + oldAlarms[oldItemPosition].id)
        return b
    }

    override fun getOldListSize(): Int {
        return oldAlarms.size
    }

    override fun getNewListSize(): Int {
        return newAlarms.size
    }

    override fun areContentsTheSame(oldItemPosition: Int, newItemPosition: Int): Boolean {
        val b = oldAlarms[oldItemPosition].sameSummary(newAlarms[newItemPosition])
//        Log.v("AlarmDiffCallback", "$b content ${oldAlarms[oldItemPosition].nextOccurrence.time} ${newAlarms[newItemPosition].nextOccurrence.time}")
        return b
    }

    override fun getChangePayload(oldItemPosition: Int, newItemPosition: Int): Any? {
//        val o = oldAlarms[oldItemPosition]
//        val n = newAlarms[newItemPosition]
//        val diff = Bundle()
//        if (o.title != n.title) diff.putString("title", n.title)
//        if (o.isEnabled != n.isEnabled) diff.putBoolean("isEnabled", n.isEnabled)
//        if (o.repeatType != n.repeatType) diff.putInt("repeatType", n.repeatType)
        return newAlarms[newItemPosition]
    }

}