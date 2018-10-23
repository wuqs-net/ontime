package net.wuqs.ontime.feature.shared.dialog

import android.app.DatePickerDialog
import android.app.Dialog
import android.os.Build
import android.os.Bundle
import android.support.v4.app.DialogFragment
import android.support.v4.app.Fragment
import android.support.v4.app.FragmentActivity
import android.support.v7.app.AlertDialog
import android.widget.DatePicker
import java.util.*

class DatePickerDialogFragment : DialogFragment() {

    override fun onCreateDialog(savedInstanceState: Bundle?): Dialog {
        val listener: OnDateSetListener = parentFragment as? OnDateSetListener
                ?: activity as? OnDateSetListener
                ?: throw RuntimeException("No associated OnDateSetListener")
        val context = parentFragment?.context
                ?: activity
                ?: throw RuntimeException("Context is null, cannot create dialog")
        val args = arguments!!
        val y = args.getInt(ARG_YEAR)
        val m = args.getInt(ARG_MONTH)
        val d = args.getInt(ARG_DAY_OF_MONTH)
        val minDate = args[ARG_MIN_DATE] as? Long
        return if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.LOLLIPOP) {
            DatePickerDialog(
                    context,
                    { _, year, month, dayOfMonth ->
                        listener.onDateSet(this@DatePickerDialogFragment, year,
                                month, dayOfMonth)
                    },
                    y, m, d
            ).apply {
                minDate?.let { datePicker.minDate = it }
            }
        } else {
            AlertDialog.Builder(context).run {
                val datePicker = DatePicker(context).apply {
                    init(y, m, d, null)
                    minDate?.let { this.minDate = it }
                    calendarViewShown = false
                }
                setView(datePicker)
                setPositiveButton(android.R.string.ok) { _, _ ->
                    listener.onDateSet(this@DatePickerDialogFragment, datePicker.year,
                            datePicker.month, datePicker.dayOfMonth)
                }
                setNegativeButton(android.R.string.cancel, null)
                create()
            }
        }
    }

    interface OnDateSetListener {

        /**
         * Called when user sets a date with the dialog.
         *
         * @param fragment the fragment associated with this listener.
         * @param year the year that was set.
         * @param month the month that was set.
         * @param dayOfMonth the day of month that was set.
         */
        fun onDateSet(fragment: DatePickerDialogFragment, year: Int, month: Int, dayOfMonth: Int)
    }

    companion object {

        /**
         * Shows a [DatePickerDialog] with the specified parent fragment, default date and optional
         * minimal date.
         *
         * @param parentFragment the parent fragment of this fragment.
         * @param date the default date for the DatePicker.
         * @param tag the tag for this fragment.
         * @param minDate the minimal date for the DatePicker, optional.
         */
        @JvmStatic
        fun show(
            parentFragment: Fragment,
            date: Calendar,
            tag: String?,
            minDate: Calendar? = null
        ) {
            if (parentFragment !is OnDateSetListener) {
                throw IllegalArgumentException("$parentFragment must implement OnDateSetListener")
            }
            newInstance(date, minDate).show(parentFragment.childFragmentManager, tag)
        }

        /**
         * Shows a [DatePickerDialog] with the specified associated activity, default date and
         * optional minimal date.
         *
         * @param parentActivity the [FragmentActivity] for this fragment to associate with.
         * @param date the default date for the DatePicker.
         * @param tag the tag for this fragment.
         * @param minDate the minimal date for the DatePicker, optional.
         */
        @JvmStatic
        fun show(
            parentActivity: FragmentActivity,
            date: Calendar,
            tag: String?,
            minDate: Calendar? = null
        ) {
            if (parentActivity !is OnDateSetListener) {
                throw IllegalArgumentException("$parentActivity must implement OnDateSetListener")
            }
            newInstance(date, minDate).show(parentActivity.supportFragmentManager, tag)
        }

        /**
         * Creates a new instance of [DatePickerDialogFragment] with a specified [OnDateSetListener] and
         * a default date.
         *
         * @param date the default date for the DatePicker.
         * @param minDate the minimal date for the DatePicker.
         */
        @JvmStatic
        private fun newInstance(date: Calendar, minDate: Calendar? = null): DatePickerDialogFragment {
            return DatePickerDialogFragment().apply {
                arguments = Bundle().apply {
                    putInt(ARG_YEAR, date[Calendar.YEAR])
                    putInt(ARG_MONTH, date[Calendar.MONTH])
                    putInt(ARG_DAY_OF_MONTH, date[Calendar.DAY_OF_MONTH])
                    minDate?.let { putLong(ARG_MIN_DATE, it.timeInMillis) }
                }
            }
        }

        private const val ARG_YEAR = "year"
        private const val ARG_MONTH = "month"
        private const val ARG_DAY_OF_MONTH = "dayOfMonth"
        private const val ARG_MIN_DATE = "minDate"
    }
}