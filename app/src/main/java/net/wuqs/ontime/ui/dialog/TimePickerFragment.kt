package net.wuqs.ontime.ui.dialog

import android.app.Dialog
import android.app.TimePickerDialog
import android.content.Context
import android.os.Bundle
import android.support.v4.app.DialogFragment
import android.support.v7.app.AlertDialog
import android.text.format.DateFormat
import android.widget.TimePicker
import net.wuqs.ontime.util.ApiUtil

class TimePickerFragment : DialogFragment(), TimePickerDialog.OnTimeSetListener {

    private lateinit var mListener: TimeSetListener

    @Suppress("DEPRECATION")
    override fun onCreateDialog(savedInstanceState: Bundle?): Dialog {
        val context = activity!!
        val args = arguments!!
        val hour = args.getInt(ARG_HOUR)
        val minute = args.getInt(ARG_MINUTE)
        val is24HourView = DateFormat.is24HourFormat(activity)
        return if (ApiUtil.isLOrLater()) {
            TimePickerDialog(activity, this, hour, minute, is24HourView)
        } else {
            AlertDialog.Builder(context).run {
                val timePicker = TimePicker(context).apply {
                    currentHour = hour
                    currentMinute = minute
                    setIs24HourView(is24HourView)
                }
                setView(timePicker)
                setPositiveButton(android.R.string.ok) { dialog, which ->
                    mListener.onTimeSet(this@TimePickerFragment,
                            timePicker.currentHour, timePicker.currentMinute)
                }
                setNegativeButton(android.R.string.cancel, null)
                create()
            }
        }

    }

    override fun onAttach(context: Context?) {
        super.onAttach(context)
        if (context is TimeSetListener) {
            mListener = context
        } else {
            throw RuntimeException("$context must implement TimeSetListener")
        }
    }

    override fun onTimeSet(view: TimePicker?, hourOfDay: Int, minute: Int) {
        mListener.onTimeSet(this, hourOfDay, minute)
    }

    interface TimeSetListener {

        /**
         * Called when user sets a time with the dialog.
         *
         * @param fragment the fragment associated with this listener.
         */
        fun onTimeSet(fragment: TimePickerFragment, hourOfDay: Int, minute: Int)
    }

    companion object {

        /**
         * Create a new instance of [TimePickerFragment] with a specified [TimeSetListener] and a
         * default time.
         *
         * @param hourOfDay the default hour of the [TimePickerDialog].
         * @param minute the default minute of the [TimePickerDialog].
         */
        fun newInstance(hourOfDay: Int, minute: Int) = TimePickerFragment().apply {
            arguments = Bundle().apply {
                putInt(ARG_HOUR, hourOfDay)
                putInt(ARG_MINUTE, minute)
            }
        }

        const val NEW_ALARM = "NEW_ALARM"
        const val EDIT_ALARM = "EDIT_ALARM"

        private const val ARG_HOUR = "hour"
        private const val ARG_MINUTE = "minute"
    }
}
