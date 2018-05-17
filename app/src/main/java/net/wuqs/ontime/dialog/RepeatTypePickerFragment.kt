package net.wuqs.ontime.dialog

import android.app.AlertDialog
import android.app.Dialog
import android.content.Context
import android.os.Bundle
import android.support.v4.app.DialogFragment
import net.wuqs.ontime.R

class RepeatTypePickerFragment : DialogFragment() {

    private lateinit var mListener: RepeatDialogListener

    override fun onCreateDialog(savedInstanceState: Bundle?): Dialog {
        val builder = AlertDialog.Builder(activity).apply {
            setItems(R.array.repeat_types, { dialog, which ->
                mListener.onItemChoose(this@RepeatTypePickerFragment, which)
            })
        }
        return builder.create()
    }

    override fun onAttach(context: Context?) {
        super.onAttach(context)
        mListener = if (context is RepeatDialogListener) {
            context
        } else {
            throw RuntimeException("$context must implement RepeatDialogListener")
        }
    }

    interface RepeatDialogListener {
        fun onItemChoose(dialog: DialogFragment, which: Int)
    }

    companion object {
        const val CHOOSE_REPEAT_TYPE = "CHOOSE_REPEAT_TYPE"
    }
}