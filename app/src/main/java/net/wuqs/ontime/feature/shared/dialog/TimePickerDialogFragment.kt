package net.wuqs.ontime.feature.shared.dialog

import android.app.Dialog
import android.app.TimePickerDialog
import android.os.Build
import android.os.Bundle
import androidx.fragment.app.DialogFragment
import androidx.fragment.app.FragmentActivity
import androidx.appcompat.app.AlertDialog
import android.text.format.DateFormat
import android.widget.TimePicker

class TimePickerDialogFragment : DialogFragment() {

    @Suppress("DEPRECATION")
    override fun onCreateDialog(savedInstanceState: Bundle?): Dialog {
        val listener = activity as OnTimeSetListener
        val context = activity!!
        val args = arguments!!
        val h = args.getInt(ARG_HOUR)
        val m = args.getInt(ARG_MINUTE)
        val is24HourView = DateFormat.is24HourFormat(activity)
        return if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.LOLLIPOP) {
            TimePickerDialog(
                    activity,
                    { _, hourOfDay, minute ->
                        listener.onTimeSet(this, hourOfDay, minute)
                    },
                    h,
                    m,
                    is24HourView
            )
        } else {
            AlertDialog.Builder(context).run {
                val timePicker = TimePicker(context).apply {
                    currentHour = h
                    currentMinute = m
                    setIs24HourView(is24HourView)
                }
                setView(timePicker)
                setPositiveButton(android.R.string.ok) { dialog, which ->
                    listener.onTimeSet(this@TimePickerDialogFragment,
                            timePicker.currentHour, timePicker.currentMinute)
                }
                setNegativeButton(android.R.string.cancel, null)
                create()
            }
        }

    }

    interface OnTimeSetListener {

        /**
         * Called when the user sets a time with the dialog.
         *
         * @param fragment the fragment associated with this listener.
         * @param hourOfDay the hour that was set
         * @param minute the minute that was set
         */
        fun onTimeSet(fragment: TimePickerDialogFragment, hourOfDay: Int, minute: Int)
    }

    companion object {

        /**
         * Shows a [TimePickerDialog] with the specified default time.
         *
         * @param parentActivity the [FragmentActivity] for this fragment to associate with.
         * @param hourOfDay the default hour of the [TimePickerDialog].
         * @param minute the default minute of the [TimePickerDialog].
         * @param tag the tag for this fragment.
         */
        @JvmStatic
        fun show(
            parentActivity: FragmentActivity,
            hourOfDay: Int,
            minute: Int,
            tag: String?
        ) {
            if (parentActivity !is OnTimeSetListener) {
                throw IllegalArgumentException("$parentActivity must implement OnTimeSetListener")
            }
            val fragment = TimePickerDialogFragment().apply {
                arguments = Bundle().apply {
                    putInt(ARG_HOUR, hourOfDay)
                    putInt(ARG_MINUTE, minute)
                }
            }
            fragment.show(parentActivity.supportFragmentManager, tag)
        }

        private const val ARG_HOUR = "hour"
        private const val ARG_MINUTE = "minute"
    }
}
