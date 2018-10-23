package net.wuqs.ontime.feature.currentalarm

import android.app.Dialog
import android.content.Context
import android.os.Bundle
import android.support.design.widget.BottomSheetDialog
import android.support.design.widget.BottomSheetDialogFragment
import android.support.v7.widget.GridLayoutManager
import android.support.v7.widget.RecyclerView
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.view.WindowManager
import kotlinx.android.synthetic.main.fragment_delay_option.view.*
import net.wuqs.ontime.R
import java.util.*

class DelayOptionFragment
    : BottomSheetDialogFragment() {

    private lateinit var listener: DelayOptionListener

    private var mNextTime: Calendar? = null

    override fun onCreateDialog(savedInstanceState: Bundle?): Dialog {
        return object : BottomSheetDialog(context!!, theme) {
            override fun onCreate(savedInstanceState: Bundle?) {
                super.onCreate(savedInstanceState)
                window.addFlags(WindowManager.LayoutParams.FLAG_KEEP_SCREEN_ON
                        or WindowManager.LayoutParams.FLAG_SHOW_WHEN_LOCKED
                        or WindowManager.LayoutParams.FLAG_DISMISS_KEYGUARD
                        or WindowManager.LayoutParams.FLAG_ALLOW_LOCK_WHILE_SCREEN_ON)
            }
        }
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        arguments?.let {
            if (it.containsKey(ARG_NEXT_TIME)) {
                mNextTime = Calendar.getInstance().apply {
                    timeInMillis = it.getLong(ARG_NEXT_TIME)
                }
            }
        }
    }

    override fun onCreateView(inflater: LayoutInflater, container: ViewGroup?, savedInstanceState: Bundle?): View? {
        return inflater.inflate(R.layout.fragment_delay_option, container, false)
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        var options = DelayOptionAdapter.ALL_INTERVALS
        if (mNextTime != null) {
            options = options.filter {
                Calendar.getInstance().apply { add(it.second, it.first) }.before(mNextTime)
            }
        }
        (view.rv_delay_options as RecyclerView).apply {
            layoutManager = GridLayoutManager(context, 3)
            adapter = DelayOptionAdapter(listener, options)
        }
    }

    override fun onAttach(context: Context?) {
        super.onAttach(context)
        if (context is DelayOptionListener) {
            listener = context
        } else {
            throw RuntimeException("$context must implement DelayOptionListener")
        }
    }

    interface DelayOptionListener {
        fun onDelayOptionClick(quantity: Int, unit: Int)
    }

    companion object {
        @JvmStatic
        fun newInstance(nextTime: Calendar?) = DelayOptionFragment().apply {
            arguments = Bundle().apply {
                nextTime?.let { putLong(ARG_NEXT_TIME, it.timeInMillis) }
            }
        }

        private const val ARG_NEXT_TIME = "nextTime"
    }
}