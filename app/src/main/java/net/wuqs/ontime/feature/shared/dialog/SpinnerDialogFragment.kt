package net.wuqs.ontime.feature.shared.dialog

import android.app.AlertDialog
import android.app.Dialog
import android.os.Bundle
import android.support.annotation.ArrayRes
import android.support.v4.app.DialogFragment
import android.support.v4.app.FragmentActivity

class SpinnerDialogFragment : DialogFragment() {

    override fun onCreateDialog(savedInstanceState: Bundle?): Dialog {
        val listener = activity as OptionListener
        val itemsId = arguments?.getInt(ARG_ITEMS_ID)!!
        return AlertDialog.Builder(activity).run {
            setItems(itemsId) { _, which ->
                listener.onOptionClick(this@SpinnerDialogFragment, which)
            }
            create()
        }
    }

    interface OptionListener {
        fun onOptionClick(dialog: DialogFragment, which: Int)
    }

    companion object {
        /**
         * Shows a dialog with a list of options.
         *
         * @param parentActivity the [FragmentActivity] for this fragment to associate with.
         * @param itemsId the resource id of the list of options, should be an array type
         * (`R.array.*`).
         * @param tag the tag for this fragment.
         */
        @JvmStatic
        fun show(parentActivity: FragmentActivity, @ArrayRes itemsId: Int, tag: String?) {
            if (parentActivity !is OptionListener) {
                throw IllegalArgumentException("$parentActivity must implement OptionListener")
            }
            SpinnerDialogFragment().run {
                arguments = Bundle().apply {
                    putInt(ARG_ITEMS_ID, itemsId)
                }
                show(parentActivity.supportFragmentManager, tag)
            }
        }

    }
}

private const val ARG_ITEMS_ID = "itemsId"