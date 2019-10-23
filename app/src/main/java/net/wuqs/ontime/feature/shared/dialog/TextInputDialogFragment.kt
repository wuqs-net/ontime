package net.wuqs.ontime.feature.shared.dialog

import android.app.Dialog
import android.os.Bundle
import android.view.WindowManager.LayoutParams.SOFT_INPUT_STATE_VISIBLE
import androidx.appcompat.app.AlertDialog
import androidx.core.os.bundleOf
import androidx.fragment.app.DialogFragment
import kotlinx.android.synthetic.main.dialog_text_input.view.*
import net.wuqs.ontime.R

class TextInputDialogFragment : DialogFragment() {

    private lateinit var listener: TextInputDialogListener

    override fun onCreateDialog(savedInstanceState: Bundle?): Dialog {
        return activity?.let {
            listener = it as TextInputDialogListener
            val builder = AlertDialog.Builder(it)
            val inflater = requireActivity().layoutInflater
            val view = inflater.inflate(R.layout.dialog_text_input, null).apply {
                til.hint = arguments?.getString("hint")
                et.setText(arguments?.getString("text"))
                til.requestFocus()
            }
            builder.run {
                setView(view)
                setPositiveButton(R.string.ok) { _, _ ->
                    listener.onTextOK(tag, view.et.text.toString())
                }
                setNegativeButton(android.R.string.cancel) { _, _ ->
                    listener.onCancel(tag)
                }
            }
            builder.create().apply {
                window?.setSoftInputMode(SOFT_INPUT_STATE_VISIBLE)
            }
        } ?: throw IllegalStateException("Activity cannot be null")
    }

    interface TextInputDialogListener {
        fun onTextOK(tag: String?, text: String)
        fun onCancel(tag: String?)
    }

    companion object {

        fun newInstance(hint: String, text: String): TextInputDialogFragment {
            return TextInputDialogFragment().apply {
                arguments = bundleOf("hint" to hint, "text" to text)
            }
        }
    }
}