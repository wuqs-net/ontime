package net.wuqs.ontime.dialog

import android.app.DatePickerDialog
import android.app.Dialog
import android.os.Bundle
import android.support.v4.app.DialogFragment
import android.widget.DatePicker
import java.util.Calendar

class DatePickerFragment : DialogFragment(), DatePickerDialog.OnDateSetListener {

    private var mListener: OnDateSetListener? = null

    override fun onCreateDialog(savedInstanceState: Bundle?): Dialog {
        val args = arguments!!
        val year = args.getInt(ARGS_YEAR)
        val month = args.getInt(ARGS_MONTH)
        val day = args.getInt(ARGS_DAY_OF_MONTH)
        return DatePickerDialog(activity, this, year, month, day)
    }

    override fun onDateSet(view: DatePicker?, year: Int, month: Int, dayOfMonth: Int) {
        mListener!!.onDateSet(tag, year, month, dayOfMonth)
    }

    interface OnDateSetListener {

        /**
         * Called when user sets a date with the dialog.
         *
         * @param tag the tag of this [DialogFragment].
         */
        fun onDateSet(tag: String?, year: Int, month: Int, dayOfMonth: Int)
    }

    companion object {

        /**
         * Create a new instance of [DatePickerFragment] with a specified [OnDateSetListener] and
         * a default date.
         *
         * @param c the default date of the [DatePickerDialog].
         */
        fun newInstance(listener: OnDateSetListener, c: Calendar): DatePickerFragment {
            val fragment = DatePickerFragment()
            fragment.mListener = listener
            val args = Bundle()
            args.apply {
                putInt(ARGS_YEAR, c[Calendar.YEAR])
                putInt(ARGS_MONTH, c[Calendar.MONTH])
                putInt(ARGS_DAY_OF_MONTH, c[Calendar.DAY_OF_MONTH])
            }
            fragment.arguments = args
            return fragment
        }

        const val SET_ACTIVATE_DATE = "SET_ACTIVATE_DATE"

        private const val ARGS_YEAR = "year"
        private const val ARGS_MONTH = "month"
        private const val ARGS_DAY_OF_MONTH = "dayOfMonth"
    }
}