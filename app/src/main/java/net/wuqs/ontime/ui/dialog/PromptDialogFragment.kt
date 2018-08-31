package net.wuqs.ontime.ui.dialog

import android.app.AlertDialog
import android.app.Dialog
import android.os.Bundle
import android.support.annotation.StringRes
import android.support.v4.app.DialogFragment
import android.support.v4.app.FragmentActivity

/**
 * DialogFragment used to show a prompt.
 */
class PromptDialogFragment : DialogFragment() {

    override fun onCreateDialog(savedInstanceState: Bundle?): Dialog {
        val listener = activity as OnClickListener
        val args = arguments!!
        return AlertDialog.Builder(activity).run {
            setMessage(args.getInt(ARG_MESSAGE))
            setPositiveButton(args.getInt(ARG_POSITIVE)) { _, _ ->
                listener.onDialogPositiveClick(this@PromptDialogFragment)
            }
            setNegativeButton(args.getInt(ARG_NEGATIVE)) { _, _ ->
                listener.onDialogNegativeClick(this@PromptDialogFragment)
            }
            create()
        }
    }

    interface OnClickListener {
        fun onDialogPositiveClick(dialogFragment: DialogFragment)
        fun onDialogNegativeClick(dialogFragment: DialogFragment)
    }

    companion object {

        /**
         * Shows a prompt dialog with the specified associated activity, message, and actions.
         *
         * @param parentActivity the [FragmentActivity] for this fragment to associate with.
         * @param messageId the resource id of the message to display.
         * @param positiveTextId the resource id of the text to display in the positive button.
         * @param negativeTextId the resource id of the text to display in the negative button.
         * @param tag the tag for this fragment.
         */
        @JvmStatic
        fun show(
            parentActivity: FragmentActivity,
            @StringRes messageId: Int,
            @StringRes positiveTextId: Int,
            @StringRes negativeTextId: Int,
            tag: String?
        ) {
            if (parentActivity !is OnClickListener) {
                throw IllegalArgumentException("$parentActivity must implement OnClickListener")
            }
            val fragment = PromptDialogFragment().apply {
                arguments = Bundle().apply {
                    putInt(ARG_MESSAGE, messageId)
                    putInt(ARG_POSITIVE, positiveTextId)
                    putInt(ARG_NEGATIVE, negativeTextId)
                }
            }
            fragment.show(parentActivity.supportFragmentManager, tag)
        }

        private const val ARG_MESSAGE = "message"
        private const val ARG_POSITIVE = "positive"
        private const val ARG_NEGATIVE = "negative"
    }
}