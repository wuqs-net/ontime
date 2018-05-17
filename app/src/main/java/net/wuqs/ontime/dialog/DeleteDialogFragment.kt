package net.wuqs.ontime.dialog

import android.app.AlertDialog
import android.app.Dialog
import android.content.Context
import android.content.DialogInterface
import android.os.Bundle
import android.support.v4.app.DialogFragment
import net.wuqs.ontime.R

class DeleteDialogFragment : DialogFragment() {

    private lateinit var mListener: DialogInterface.OnClickListener

    override fun onCreateDialog(savedInstanceState: Bundle?): Dialog {
        val builder = AlertDialog.Builder(activity)
        builder.apply {
            setMessage(R.string.prompt_delete_alarm)
            setPositiveButton(R.string.action_delete, mListener)
            setNegativeButton(R.string.action_cancel, mListener)
        }
        return builder.create()
    }

    override fun onAttach(context: Context?) {
        super.onAttach(context)
        if (context is DialogInterface.OnClickListener) {
            mListener = context
        } else {
            throw RuntimeException("$context must implement OnClickListener")
        }
    }

    companion object {
        const val TAG_DELETE_ALARM = "DELETE_ALARM"
    }

}