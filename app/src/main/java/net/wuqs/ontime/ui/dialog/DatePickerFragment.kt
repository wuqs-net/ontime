package net.wuqs.ontime.ui.dialog

import android.app.DatePickerDialog
import android.app.Dialog
import android.os.Bundle
import android.support.v4.app.DialogFragment
import android.widget.DatePicker
import java.util.Calendar

class DatePickerFragment : DialogFragment(), DatePickerDialog.OnDateSetListener {

    private var mListener: DateSetListener? = null

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        targetFragment.let {
            if (it is DateSetListener) mListener = it
        }
    }

    override fun onCreateDialog(savedInstanceState: Bundle?): Dialog {
        val args = arguments!!
        val year = args.getInt(ARG_YEAR)
        val month = args.getInt(ARG_MONTH)
        val day = args.getInt(ARG_DAY_OF_MONTH)
        return DatePickerDialog(activity, this, year, month, day).apply {
            if (args.containsKey(ARG_MIN_DATE)) datePicker.minDate = args.getLong(ARG_MIN_DATE)
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
        mListener?.onDateSet(tag, year, month, dayOfMonth)
    }

    interface DateSetListener {

        /**
         * Called when user sets a date with the dialog.
         *
         * @param tag the tag of this [DialogFragment].
         */
        fun onDateSet(tag: String?, year: Int, month: Int, dayOfMonth: Int)
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