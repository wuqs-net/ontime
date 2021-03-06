package net.wuqs.ontime.feature.editalarm

import android.content.Context
import android.util.AttributeSet
import android.view.LayoutInflater
import androidx.constraintlayout.widget.ConstraintLayout
import kotlinx.android.synthetic.main.view_option_item.view.*
import net.wuqs.ontime.R

class OptionItemView(context: Context, attrs: AttributeSet) : ConstraintLayout(context, attrs) {

    private var iconResId: Int = 0
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

    override fun onMeasure(widthMeasureSpec: Int, heightMeasureSpec: Int) {
        if (MeasureSpec.getMode(widthMeasureSpec) == MeasureSpec.EXACTLY) {
            super.onMeasure(widthMeasureSpec, heightMeasureSpec)
            return
        }
        val widthSize = MeasureSpec.getSize(widthMeasureSpec)
        val newWidthMeasureSpec = MeasureSpec.makeMeasureSpec(widthSize, MeasureSpec.EXACTLY)
        super.onMeasure(newWidthMeasureSpec, heightMeasureSpec)
    }
}