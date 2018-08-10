package net.wuqs.ontime.ui.alarmeditscreen

import android.content.Context
import android.support.constraint.ConstraintLayout
import android.support.constraint.ConstraintSet
import android.support.constraint.Constraints
import android.util.AttributeSet
import android.util.Log
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import kotlinx.android.synthetic.main.view_option_item.view.*
import net.wuqs.ontime.R

class OptionItemView(context: Context, attrs: AttributeSet) : ConstraintLayout(context, attrs) {

    var iconResId: Int = 0
        set(value) {
            field = value
            iv_icon.setImageResource(value)
        }

    var captionText: CharSequence? = null
        set(value) {
            field = value
            tv_option_caption.text = value
        }

    var valueText: CharSequence? = null
        set(value) {
            field = value
            tv_option_value.text = value
        }

    init {
        LayoutInflater.from(context).inflate(R.layout.view_option_item, this)
        val a = context.theme.obtainStyledAttributes(attrs, R.styleable.OptionItemView, 0, 0)
        iconResId = a.getResourceId(R.styleable.OptionItemView_iconImage, 0)
        captionText = a.getText(R.styleable.OptionItemView_captionText)
        valueText = a.getText(R.styleable.OptionItemView_valueText)

    }
}