package net.wuqs.ontime.feature.shared.dialog

import android.app.Dialog
import android.os.Bundle
import androidx.annotation.StringRes
import androidx.appcompat.app.AlertDialog
import androidx.fragment.app.DialogFragment
import androidx.fragment.app.FragmentActivity

/**
 * DialogFragment used to show a prompt.
 */
class PromptDialogFragment : DialogFragment() {

    override fun onCreateDialog(savedInstanceState: Bundle?): Dialog {
        val listener = activity as OnClickListener
        val args = arguments!!
        return AlertDialog.Builder(activity!!).run {
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

/**
 * Shows a simple prompt dialog.
 *
 * @param messageId the resource id of the message to display.
 * @param positiveTextId the resource id of the text to display in the positive button.
 * @param negativeTextId the resource id of the text to display in the negative button.
 * @param listener the listener to use.
 */
fun FragmentActivity.prompt(
    @StringRes messageId: Int,
    @StringRes positiveTextId: Int,
    @StringRes negativeTextId: Int,
    listener: (which: Int) -> Unit
) {
    val dialog = AlertDialog.Builder(this).run {
        setMessage(messageId)
        setPositiveButton(positiveTextId) { _, which -> listener(which) }
        setNegativeButton(negativeTextId) { _, which -> listener(which) }
        create()
    }
    dialog.show()
}

/**
 * Shows a simple prompt dialog.
 *
 * @param message the message to display.
 * @param positiveTextId the resource id of the text to display in the positive button.
 * @param negativeTextId the resource id of the text to display in the negative button.
 * @param listener the listener to use.
 */
fun FragmentActivity.prompt(
    message: CharSequence,
    @StringRes positiveTextId: Int,
    @StringRes negativeTextId: Int,
    listener: (which: Int) -> Unit
) {
    val dialog = AlertDialog.Builder(this).run {
        setMessage(message)
        setPositiveButton(positiveTextId) { _, which -> listener(which) }
        setNegativeButton(negativeTextId) { _, which -> listener(which) }
        create()
    }
    dialog.show()
}