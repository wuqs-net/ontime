package net.wuqs.ontime.feature.editalarm

import android.app.Dialog
import android.os.Bundle
import androidx.appcompat.app.AlertDialog
import androidx.core.os.bundleOf
import androidx.fragment.app.DialogFragment
import androidx.fragment.app.Fragment
import net.wuqs.ontime.R
import net.wuqs.ontime.db.Alarm

class SilenceAfterDialogFragment : DialogFragment() {

    private var listener: OnRingtoneDurationSetListener? = null

    override fun onCreateDialog(savedInstanceState: Bundle?): Dialog {
        listener = parentFragment as? OnRingtoneDurationSetListener
        if (listener == null) {
            throw ClassCastException("$activity must implement OnRingtoneDurationSetListener")
        }

        val displayOptions = resources.getStringArray(R.array.msg_ringtone_durations)

        val checked = arguments!!.getInt(Alarm.Columns.SILENCE_AFTER)

        return parentFragment?.context?.let {
            val builder = AlertDialog.Builder(it).apply {
                setTitle(R.string.msg_silence_after)
                setSingleChoiceItems(displayOptions, checked) { _, which ->
                    listener!!.onRingtoneDurationSet(which)
                    dismiss()
                }
                setNegativeButton(R.string.action_cancel, null)
            }
            builder.create()
        } ?: throw IllegalStateException("Activity cannot be null")
    }

    interface OnRingtoneDurationSetListener {

        fun onRingtoneDurationSet(which: Int)
    }

    companion object {
        fun show(
            parentFragment: Fragment,
            checked: Int,
            tag: String
        ) {
            if (parentFragment !is OnRingtoneDurationSetListener) {
                throw IllegalArgumentException("$parentFragment must implement OnDateSetListener")
            }
            SilenceAfterDialogFragment().apply {
                arguments = bundleOf(Alarm.Columns.SILENCE_AFTER to checked)
            }.show(parentFragment.childFragmentManager, tag)
        }
    }
}