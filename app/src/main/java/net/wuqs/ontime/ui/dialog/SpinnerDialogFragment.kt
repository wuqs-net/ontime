package net.wuqs.ontime.ui.dialog

import android.app.AlertDialog
import android.app.Dialog
import android.content.Context
import android.os.Bundle
import android.support.v4.app.DialogFragment

class SpinnerDialogFragment : DialogFragment() {

    private lateinit var mListener: SpinnerDialogListener

    override fun onCreateDialog(savedInstanceState: Bundle?): Dialog {
        val itemsId = arguments?.getInt(ARG_ITEMS_ID)!!
        val builder = AlertDialog.Builder(activity).apply {
            setItems(itemsId) { dialog, which ->
                mListener.onOptionClick(this@SpinnerDialogFragment, which)
            }
        }
        return builder.create()
    }

    override fun onAttach(context: Context?) {
        super.onAttach(context)
        mListener = if (context is SpinnerDialogListener) {
            context
        } else {
            throw RuntimeException("$context must implement SpinnerDialogListener")
        }
    }

    interface SpinnerDialogListener {
        fun onOptionClick(dialog: DialogFragment, which: Int)
    }

    companion object {
        @JvmStatic
        fun newInstance(itemsId: Int) =
                SpinnerDialogFragment().apply {
                    arguments = Bundle().apply {
                        putInt(ARG_ITEMS_ID, itemsId)
                    }
                }

        const val CHOOSE_REPEAT_TYPE = "CHOOSE_REPEAT_TYPE"
    }
}

private const val ARG_ITEMS_ID = "itemsId"