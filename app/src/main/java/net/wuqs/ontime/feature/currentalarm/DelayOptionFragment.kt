package net.wuqs.ontime.feature.currentalarm

import android.app.Dialog
import android.content.Context
import android.os.Bundle
import com.google.android.material.bottomsheet.BottomSheetDialog
import com.google.android.material.bottomsheet.BottomSheetDialogFragment
import androidx.recyclerview.widget.GridLayoutManager
import androidx.recyclerview.widget.RecyclerView
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.view.WindowManager
import androidx.core.content.edit
import androidx.preference.PreferenceManager
import kotlinx.android.synthetic.main.fragment_delay_option.view.*
import net.wuqs.ontime.R
import net.wuqs.ontime.data.SnoozeLength
import net.wuqs.ontime.data.add
import java.util.*

class DelayOptionFragment
    : BottomSheetDialogFragment() {

    private lateinit var listener: DelayOptionListener

    private var mNextTime: Calendar? = null

    override fun onCreateDialog(savedInstanceState: Bundle?): Dialog {
        return object : BottomSheetDialog(context!!, theme) {
            override fun onCreate(savedInstanceState: Bundle?) {
                super.onCreate(savedInstanceState)
                window?.addFlags(WindowManager.LayoutParams.FLAG_KEEP_SCREEN_ON
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
        var options = getSnoozeOptions()
        if (mNextTime != null) {
            options = options.filter {
                Calendar.getInstance().apply { add(it) }.before(mNextTime)
            }
        }
        (view.rv_delay_options as RecyclerView).apply {
            layoutManager = GridLayoutManager(context, 3).apply {
                reverseLayout = true
            }
            adapter = DelayOptionAdapter(listener, options)
        }
    }

    override fun onAttach(context: Context) {
        super.onAttach(context)
        if (context is DelayOptionListener) {
            listener = context
        } else {
            throw RuntimeException("$context must implement DelayOptionListener")
        }
    }

    private fun getSnoozeOptions(): List<SnoozeLength> {
        val sharedPreferences = PreferenceManager.getDefaultSharedPreferences(context)
        var str = sharedPreferences.getString("snooze_lengths", "").ifBlank {
            getString(R.string.default_snooze_lengths).also {
                sharedPreferences.edit { putString("snooze_lengths", it) }
            }
        }
        str = str.toLowerCase().replace(';', ',')
        val list = str.split(',')
        return list.mapNotNull {
            try {
                SnoozeLength(it)
            } catch (e: Exception) {
                null
            }
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