package net.wuqs.ontime.ui.dialog

import android.app.DatePickerDialog
import android.app.Dialog
import android.os.Bundle
import android.support.v4.app.DialogFragment
import android.support.v7.app.AlertDialog
import android.widget.DatePicker
import net.wuqs.ontime.util.ApiUtil
import java.util.*

class DatePickerFragment : DialogFragment(), DatePickerDialog.OnDateSetListener {

    private var mListener: DateSetListener? = null

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        targetFragment.let {
            if (it is DateSetListener) mListener = it
        }
    }

    override fun onCreateDialog(savedInstanceState: Bundle?): Dialog {
        val context = activity!!
        val args = arguments!!
        val year = args.getInt(ARG_YEAR)
        val month = args.getInt(ARG_MONTH)
        val day = args.getInt(ARG_DAY_OF_MONTH)
        val minDate = args[ARG_MIN_DATE] as? Long
        return if (ApiUtil.isLOrLater()) {
            DatePickerDialog(context, this, year, month, day).apply {
                minDate?.let { datePicker.minDate = it }
            }
        } else {
            AlertDialog.Builder(context).run {
                val datePicker = DatePicker(context).apply {
                    init(year, month, day, null)
                    minDate?.let { this.minDate = it }
                    calendarViewShown = false
                }
                setView(datePicker)
                setPositiveButton(android.R.string.ok) { dialog, which ->
                    mListener?.onDateSet(this@DatePickerFragment, datePicker.year,
                            datePicker.month, datePicker.dayOfMonth)
                }
                setNegativeButton(android.R.string.cancel, null)
                create()
            }
        }
    }

//    override fun onAttach(context: Context?) {
//        super.onAttach(context)
//        mListener = if (context is DateSetListener) {
//            context
//        } else {
//            throw RuntimeException("$context must implement DateSetListener")
//        }
//    }

    override fun onDateSet(view: DatePicker?, year: Int, month: Int, dayOfMonth: Int) {
        mListener?.onDateSet(this, year, month, dayOfMonth)
    }

    interface DateSetListener {

        /**
         * Called when user sets a date with the dialog.
         *
         * @param fragment the tag of this [DialogFragment].
         */
        fun onDateSet(fragment: DatePickerFragment, year: Int, month: Int, dayOfMonth: Int)
    }

    companion object {

        /**
         * Create a new instance of [DatePickerFragment] with a specified [DateSetListener] and
         * a default date.
         *
         * @param date the default date of the [DatePickerDialog].
         */
        fun newInstance(date: Calendar, minDate: Calendar? = null): DatePickerFragment {
            val fragment = DatePickerFragment()
            fragment.arguments = Bundle().apply {
                putInt(ARG_YEAR, date[Calendar.YEAR])
                putInt(ARG_MONTH, date[Calendar.MONTH])
                putInt(ARG_DAY_OF_MONTH, date[Calendar.DAY_OF_MONTH])
                minDate?.let { putLong(ARG_MIN_DATE, it.timeInMillis) }
            }
            return fragment
        }

        const val TAG_ACTIVATE_DATE = "activateDate"

        private const val ARG_YEAR = "year"
        private const val ARG_MONTH = "month"
        private const val ARG_DAY_OF_MONTH = "dayOfMonth"
        private const val ARG_MIN_DATE = "minDate"
    }
}

const val TAG_ACTIVATE_DATE = "activateDate"