package net.wuqs.ontime.feature.editalarm

import androidx.recyclerview.widget.RecyclerView
import android.view.View
import android.widget.CheckBox
import kotlinx.android.synthetic.main.item_day_of_month.view.*

class DayItemHolder(view: View) : RecyclerView.ViewHolder(view) {
    val toggleBtn: CheckBox = view.cb_day
}