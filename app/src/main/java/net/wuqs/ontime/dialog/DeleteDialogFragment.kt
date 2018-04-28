package net.wuqs.ontime.dialog

import android.app.AlertDialog
import android.app.Dialog
import android.content.DialogInterface
import android.os.Bundle
import android.support.v4.app.DialogFragment
import net.wuqs.ontime.R

class DeleteDialogFragment : DialogFragment() {

    private var mListener: DialogInterface.OnClickListener? = null

    override fun onCreateDialog(savedInstanceState: Bundle?): Dialog {
        val builder = AlertDialog.Builder(activity)
        builder.apply {
            setMessage(R.string.prompt_delete_alarm)
            setPositiveButton(R.string.action_delete, mListener)
            setNegativeButton(R.string.action_cancel, mListener)
        }
        return builder.create()
    }

    companion object {

        /**
         * Creates a new instance of [DeleteDialogFragment]
         * with a specified [DialogInterface.OnClickListener].
         */
        fun newInstance(listener: DialogInterface.OnClickListener): DeleteDialogFragment {
            val fragment = DeleteDialogFragment()
            fragment.mListener = listener
            return fragment
        }

        const val TAG_DELETE_ALARM = "DELETE_ALARM"
    }

}