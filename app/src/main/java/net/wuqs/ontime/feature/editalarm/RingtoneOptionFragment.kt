package net.wuqs.ontime.feature.editalarm

import android.Manifest
import android.app.Activity
import android.content.Context
import android.content.Intent
import android.content.pm.PackageManager
import android.media.RingtoneManager
import android.net.Uri
import android.os.Bundle
import android.provider.Settings
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.appcompat.app.AlertDialog
import androidx.core.app.ActivityCompat
import androidx.core.os.bundleOf
import androidx.fragment.app.Fragment
import kotlinx.android.synthetic.main.fragment_ringtone_option.*
import net.wuqs.ontime.R
import net.wuqs.ontime.db.Alarm
import net.wuqs.ontime.feature.shared.dialog.prompt
import net.wuqs.ontime.util.getIntArray
import net.wuqs.ontime.util.getStringArray
import net.wuqs.ontime.util.logI
import net.wuqs.ontime.util.logV
import java.io.File
import java.io.IOException
import java.lang.ClassCastException

private const val PERMISSION_REQUEST_RINGTONE = 2
private const val TAG_RINGTONE_DURATION = "RINGTONE_DURATION"

class RingtoneOptionFragment : Fragment(),
        SilenceAfterDialogFragment.OnRingtoneDurationSetListener {

    private var listener: OnOptionChangeListener? = null

    private var ringtoneUri: Uri? = null
    private var vibrate: Boolean = false
    private var silenceAfter: Int = -1

    override fun onAttach(context: Context?) {
        super.onAttach(context)
        listener = context as? OnOptionChangeListener
        if (listener == null) {
            throw ClassCastException("$context must implement OnOptionChangeListener")
        }
    }

    override fun onCreateView(inflater: LayoutInflater, container: ViewGroup?,
                              savedInstanceState: Bundle?): View? {
        arguments?.let {
            ringtoneUri = it.getParcelable(Alarm.Columns.RINGTONE_URI)
            vibrate = it.getBoolean(Alarm.Columns.VIBRATE)
            silenceAfter = it.getInt(Alarm.Columns.SILENCE_AFTER)
        }
        return inflater.inflate(R.layout.fragment_ringtone_option, container, false)
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        updateRingtoneDisplay()
        oiv_ringtone.setOnClickListener { showRingtonePicker() }
        tv_perm_warn.setOnClickListener { requestRingtonePerm() }
        oiv_silence_after.setOnClickListener { showRingtoneDurationPicker() }
    }

    override fun onActivityResult(requestCode: Int, resultCode: Int, data: Intent?) {
        if (requestCode == 1 && resultCode == Activity.RESULT_OK && data != null) {
            ringtoneUri = data.getParcelableExtra(RingtoneManager.EXTRA_RINGTONE_PICKED_URI)
            logV("Ringtone picked: $ringtoneUri")
            updateRingtoneDisplay()
            listener!!.onRingtoneUriSet(ringtoneUri)
        }
    }

    override fun onRequestPermissionsResult(requestCode: Int, permissions: Array<out String>, grantResults: IntArray) {
        permissions.forEachIndexed { index, perm ->
            if (grantResults[index] == PackageManager.PERMISSION_GRANTED) {
                logI("$perm granted")
            } else {
                logI("$perm denied")
            }
        }
        if (requestCode != PERMISSION_REQUEST_RINGTONE) return

        updateRingtoneDisplay()
        if (grantResults[0] == PackageManager.PERMISSION_DENIED &&
                !ActivityCompat.shouldShowRequestPermissionRationale(activity!!,
                        Manifest.permission.READ_EXTERNAL_STORAGE)) {
            activity!!.prompt(R.string.msg_ask_for_perm, R.string.action_go_to_settings,
                    R.string.action_cancel) { which ->
                if (which == AlertDialog.BUTTON_POSITIVE) {
                    // Redirect user to app setting screen to allow permission
                    val intent = Intent(Settings.ACTION_APPLICATION_DETAILS_SETTINGS).apply {
                        data = Uri.fromParts("package", activity!!.packageName, null)
                    }
                    startActivity(intent)
                }
            }
        }
    }

    override fun onRingtoneDurationSet(which: Int) {
        silenceAfter = getIntArray(R.array.ringtone_durations)[which]
        updateRingtoneDurationDisplay(which)
        listener!!.onSilenceAfterSet(silenceAfter)
    }

    /**
     * Shows the system ringtone picker.
     */
    private fun showRingtonePicker() {
        val intent = Intent(RingtoneManager.ACTION_RINGTONE_PICKER).apply {
            putExtra(RingtoneManager.EXTRA_RINGTONE_TYPE, RingtoneManager.TYPE_ALARM)
            putExtra(RingtoneManager.EXTRA_RINGTONE_DEFAULT_URI,
                    RingtoneManager.getDefaultUri(RingtoneManager.TYPE_ALARM))
            putExtra(RingtoneManager.EXTRA_RINGTONE_EXISTING_URI, ringtoneUri)
        }
        startActivityForResult(intent, 1)
    }

    private fun showRingtoneDurationPicker() {
        val checked = getIntArray(R.array.ringtone_durations).indexOf(silenceAfter)
        SilenceAfterDialogFragment.show(this, checked, TAG_RINGTONE_DURATION)
    }

    /**
     * Requests for the permission to access ringtone.
     */
    private fun requestRingtonePerm() {
        ActivityCompat.requestPermissions(activity!!,
                arrayOf(Manifest.permission.READ_EXTERNAL_STORAGE),
                PERMISSION_REQUEST_RINGTONE)
    }

    /**
     * Checks if the app has the permission to access ringtone.
     */
    private fun Uri?.hasRingtonePerm(): Boolean {
        if (this == null) return true
        when (scheme) {
            "file" -> return try {
                logV("Ringtone path: ${path}")
                File(path).canRead()
            } catch (e: IOException) {
                false
            }
            "content" -> return try {
                context?.contentResolver?.let {
                    it.query(this, null, null, null, null)?.close()
                    true
                } ?: false
            } catch (e: SecurityException) {
                false
            }
            else -> return false
        }
    }

    private fun updateRingtoneDisplay() {
        val ringtone = RingtoneManager.getRingtone(context, ringtoneUri)
        oiv_ringtone.valueText = ringtone.getTitle(context)
        cl_perm_warn.visibility = if (ringtoneUri.hasRingtonePerm()) {
            View.GONE
        } else {
            View.VISIBLE
        }
        cb_vibrate.isChecked = vibrate
        updateRingtoneDurationDisplay()
    }

    private fun updateRingtoneDurationDisplay(which: Int = -1) {
        val index = when (which) {
            -1 -> getIntArray(R.array.ringtone_durations).indexOf(silenceAfter)
            else -> which
        }
        oiv_silence_after.valueText = getStringArray(R.array.msg_ringtone_durations)[index]
    }

    interface OnOptionChangeListener {

        /**
         * Called when the ringtone [Uri] of an [Alarm] is changed by the user.
         */
        fun onRingtoneUriSet(uri: Uri?)

        /**
         * Called when the duration before an [Alarm] becomes silence is changed by the user.
         */
        fun onSilenceAfterSet(silenceAfter: Int)
    }

    companion object {
        fun newInstance(alarm: Alarm): RingtoneOptionFragment {
            return RingtoneOptionFragment().apply {
                arguments = bundleOf(
                        Alarm.Columns.RINGTONE_URI to alarm.ringtoneUri,
                        Alarm.Columns.VIBRATE to alarm.vibrate,
                        Alarm.Columns.SILENCE_AFTER to alarm.silenceAfter
                )
            }
        }
    }
}

