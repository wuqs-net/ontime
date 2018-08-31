package net.wuqs.ontime.ui.feature.main

import android.support.v7.util.DiffUtil
import net.wuqs.ontime.db.Alarm

class AlarmDiffCallback(private val oldAlarms: List<Alarm>,
                        private val newAlarms: List<Alarm>) : DiffUtil.Callback() {

    override fun areItemsTheSame(oldItemPosition: Int, newItemPosition: Int): Boolean =
            oldAlarms[oldItemPosition].id == newAlarms[newItemPosition].id

    override fun getOldListSize(): Int = oldAlarms.size

    override fun getNewListSize(): Int = newAlarms.size

    override fun areContentsTheSame(oldItemPosition: Int, newItemPosition: Int): Boolean =
            oldAlarms[oldItemPosition] == newAlarms[newItemPosition]

    //    override fun getChangePayload(oldItemPosition: Int, newItemPosition: Int): Any? {
////        val o = oldAlarms[oldItemPosition]
////        val n = newAlarms[newItemPosition]
////        val diff = Bundle()
////        if (o.title != n.title) diff.putString("title", n.title)
////        if (o.isEnabled != n.isEnabled) diff.putBoolean("isEnabled", n.isEnabled)
////        if (o.repeatType != n.repeatType) diff.putInt("repeatType", n.repeatType)
//        return newAlarms[newItemPosition]
//    }

}