package net.wuqs.ontime.dialog

import android.app.Dialog
import android.app.TimePickerDialog
import android.content.Context
import android.os.Bundle
import android.support.v4.app.DialogFragment
import android.text.format.DateFormat
import android.widget.TimePicker

class TimePickerFragment : DialogFragment(), TimePickerDialog.OnTimeSetListener {

    private lateinit var mListener: TimeSetListener

    override fun onCreateDialog(savedInstanceState: Bundle?): Dialog {
        val args = arguments!!
        val hour = args.getInt(ARG_HOUR)
        val minute = args.getInt(ARG_MINUTE)
        return TimePickerDialog(activity, this, hour, minute,
                DateFormat.is24HourFormat(activity))
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
        mListener.onTimeSet(tag, hourOfDay, minute)
    }

    interface TimeSetListener {

        /**
         * Called when user sets a time with the dialog.
         *
         * @param tag the tag of this [DialogFragment].
         */
        fun onTimeSet(tag: String?, hourOfDay: Int, minute: Int)
    }

    companion object {

        /**
         * Create a new instance of [TimePickerFragment] with a specified [TimeSetListener] and a
         * default time.
         *
         * @param hourOfDay the default hour of the [TimePickerDialog].
         * @param minute the default minute of the [TimePickerDialog].
         */
        fun newInstance(hourOfDay: Int, minute: Int): TimePickerFragment {
            val fragment = TimePickerFragment()
            val args = Bundle().apply {
                putInt(ARG_HOUR, hourOfDay); putInt(ARG_MINUTE, minute)
            }
            fragment.arguments = args
            return fragment
        }

        const val NEW_ALARM = "NEW_ALARM"
        const val EDIT_ALARM = "EDIT_ALARM"

        private const val ARG_HOUR = "hour"
        private const val ARG_MINUTE = "minute"
    }
}
