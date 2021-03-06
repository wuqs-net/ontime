package net.wuqs.ontime.feature.home

import android.Manifest
import android.content.BroadcastReceiver
import android.content.Context
import android.content.Intent
import android.content.IntentFilter
import android.content.pm.PackageManager
import android.media.AudioManager
import android.media.RingtoneManager
import android.os.Bundle
import android.text.Html
import android.view.Menu
import android.view.MenuItem
import android.widget.Toast
import androidx.appcompat.app.AlertDialog
import androidx.appcompat.app.AppCompatActivity
import androidx.core.app.ActivityCompat
import androidx.core.content.ContextCompat
import androidx.core.text.HtmlCompat
import androidx.localbroadcastmanager.content.LocalBroadcastManager
import com.google.android.material.snackbar.Snackbar
import kotlinx.android.synthetic.main.activity_main.*
import net.wuqs.ontime.BuildConfig
import net.wuqs.ontime.R
import net.wuqs.ontime.feature.settings.SettingsActivity
import net.wuqs.ontime.alarm.*
import net.wuqs.ontime.db.Alarm
import net.wuqs.ontime.db.BackupDbTask
import net.wuqs.ontime.db.RestoreDbTask
import net.wuqs.ontime.feature.about.AboutActivity
import net.wuqs.ontime.feature.currentalarm.DelayOptionFragment
import net.wuqs.ontime.feature.editalarm.EditAlarmActivity
import net.wuqs.ontime.feature.missedalarms.MissedAlarmsActivity
import net.wuqs.ontime.feature.shared.dialog.TimePickerDialogFragment
import net.wuqs.ontime.feature.shared.dialog.prompt
import net.wuqs.ontime.util.changeTaskDescription
import net.wuqs.ontime.util.logD
import net.wuqs.ontime.util.logV
import java.util.*

/** Activity result code for saving an alarm. */
const val RESULT_SAVE_ALARM = 1

/** Activity result code for deleting an alarm. */
const val RESULT_DELETE_ALARM = 2

/** [TimePickerDialogFragment] tag for creating a new alarm. */
private const val TAG_NEW_ALARM = "NEW_ALARM"

/** Permission request code for database backup. */
private const val PERMISSION_REQUEST_BACKUP_DB = 1

/** Permission request code for database restore. */
private const val PERMISSION_REQUEST_RESTORE_DB = 2


class MainActivity : AppCompatActivity(),
        AlarmListFragment.OnListFragmentActionListener,
        TimePickerDialogFragment.OnTimeSetListener,
        DelayOptionFragment.DelayOptionListener {

    private lateinit var alarmUpdateHandler: AlarmUpdateHandler

    /**
     * Local [BroadcastReceiver] to interact with other components of the app.
     */
    private val receiver = object : BroadcastReceiver() {
        override fun onReceive(context: Context?, intent: Intent?) {
            logD("onReceive(): ${intent?.action}")
            when (intent!!.action) {
                ACTION_SHOW_MISSED_ALARMS -> {
                    // Start the activity to show missed alarms.
                    if (!intent.hasExtra(EXTRA_MISSED_ALARMS)) return
                    logD("Show missed alarms")
                    val missedAlarmsIntent = Intent(
                        this@MainActivity,
                        MissedAlarmsActivity::class.java
                    ).apply {
                        putExtras(intent)
                    }
                    startActivity(missedAlarmsIntent)
                }
                ACTION_ALARM_START -> {
                    // Close any context menu when an alarm starts.
                    closeContextMenu()
                }
            }
        }
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_main)

        createAlarmNotificationChannels()

        changeTaskDescription()

        alarmUpdateHandler = AlarmUpdateHandler(this)

        volumeControlStream = AudioManager.STREAM_ALARM
        fabCreateAlarm.setOnClickListener { onFabCreateAlarmClick() }
        fabCreateAlarm.setOnLongClickListener { onFabCreateAlarmLongClick() }
        sendBroadcast(AlarmStateManager.createScheduleAllAlarmsIntent(this))
    }

    override fun onResume() {
        super.onResume()

        val filter = IntentFilter().apply {
            addAction(ACTION_SHOW_MISSED_ALARMS)
            addAction(ACTION_ALARM_START)
        }
        val lbm = LocalBroadcastManager.getInstance(this)
        lbm.registerReceiver(receiver, filter)
    }

    override fun onActivityResult(requestCode: Int, resultCode: Int, data: Intent?) {
        super.onActivityResult(requestCode, resultCode, data)
        when (requestCode) {
            CREATE_ALARM_REQUEST, EDIT_ALARM_REQUEST -> {
                if (resultCode == RESULT_SAVE_ALARM) {
                    val alarm = data!!.getParcelableExtra<Alarm>(EXTRA_ALARM_INSTANCE)
                    if (alarm.id == Alarm.INVALID_ID) {
                        alarmUpdateHandler.asyncAddAlarm(alarm)
                        showAlarmWillGoOffSnack(alarm)
                    } else {
                        alarmUpdateHandler.asyncUpdateAlarm(alarm)
                        showAlarmWillGoOffSnack(alarm)
                    }
                } else if (resultCode == RESULT_DELETE_ALARM) {
                    val alarm = data!!.getParcelableExtra<Alarm>(EXTRA_ALARM_INSTANCE)
                    alarmUpdateHandler.asyncDeleteAlarm(alarm)
                }
            }
        }
    }

    override fun onCreateOptionsMenu(menu: Menu?): Boolean {
        menuInflater.inflate(R.menu.menu_main, menu)
        return true
    }

    override fun onOptionsItemSelected(item: MenuItem?): Boolean {
        when (item?.itemId) {
            R.id.mi_backup -> backupDb()
            R.id.mi_restore -> restoreDb()
            R.id.mi_about -> startActivity(Intent(this, AboutActivity::class.java))
            R.id.mi_settings -> startActivity(Intent(this, SettingsActivity::class.java))
            else -> return super.onOptionsItemSelected(item)
        }
        return true
    }

    override fun onRequestPermissionsResult(requestCode: Int, permissions: Array<out String>, grantResults: IntArray) {
        when (requestCode) {
            PERMISSION_REQUEST_BACKUP_DB -> {
                if (grantResults.isNotEmpty() && grantResults[0]
                        == PackageManager.PERMISSION_GRANTED) {
                    backupDb(true)
                }
            }
            PERMISSION_REQUEST_RESTORE_DB -> {
                if (grantResults.isNotEmpty() && grantResults[0]
                        == PackageManager.PERMISSION_GRANTED) {
                    restoreDb(true)
                }
            }
        }
    }

    /**
     * Called when the user set a time using the TimePicker when creating a new alarm.
     */
    override fun onTimeSet(fragment: TimePickerDialogFragment, hourOfDay: Int, minute: Int) {
        if (fragment.tag == TAG_NEW_ALARM) {
            val alarm = Alarm(hour = hourOfDay, minute = minute).apply {
                if (isSetTimeEarlierThanNow()) activateDate!!.add(Calendar.DAY_OF_MONTH, 1)
                nextTime = getNextOccurrence()
            }
            val editAlarmIntent = EditAlarmActivity.createIntent(this)
                    .putExtra(EXTRA_ALARM_INSTANCE, alarm)
            startActivityForResult(editAlarmIntent, CREATE_ALARM_REQUEST)
        }
    }

    /**
     * Called when an alarm in the list is clicked.
     */
    override fun onListItemClick(item: Alarm) {
        if (BuildConfig.DEBUG) {
            // Show alarm info in debug builds.
            Toast.makeText(this, item.toString(), Toast.LENGTH_SHORT).show()
        }
        logV("onListItemClick(): $item")
        val editAlarmIntent = EditAlarmActivity.createIntent(this)
                .putExtra(EXTRA_ALARM_INSTANCE, item)
        startActivityForResult(editAlarmIntent, EDIT_ALARM_REQUEST)
    }

    /**
     * Called when the switch of an alarm in the list is clicked.
     */
    override fun onAlarmSwitchClick(item: Alarm, isChecked: Boolean) {
        item.isEnabled = isChecked
        alarmUpdateHandler.asyncUpdateAlarm(item)
        showAlarmWillGoOffSnack(item)
    }

    /**
     * Called when an alarm item in the list is long-pressed and an option is selected.
     */
    override fun onContextMenuItemSelected(item: Alarm, menuItem: MenuItem) {
        when (menuItem.itemId) {
            R.id.item_skip_once -> {
                val updated = Alarm(item).apply {
                    nextTime = getNextOccurrence(nextTime!!)
                    // Make sure previous snooze status is cleared.
                    snoozed = 0
                }
                alarmUpdateHandler.asyncUpdateAlarm(updated)
                showAlarmSkippedSnack(updated, item)
            }
            R.id.item_delete -> {
                // Prompt before deleting.
                val text = getString(
                    if (item.title.isNullOrBlank()) {
                        R.string.prompt_delete_untitled_alarm
                    } else {
                        R.string.prompt_delete_titled_alarm
                    },
                    Html.escapeHtml(item.getTitleOrTime(this@MainActivity))
                )
                val message = HtmlCompat.fromHtml(text, HtmlCompat.FROM_HTML_MODE_COMPACT)
                prompt(message, R.string.action_delete, android.R.string.cancel) { which ->
                    if (which == AlertDialog.BUTTON_POSITIVE) {
                        alarmUpdateHandler.asyncDeleteAlarm(item)
                    }
                }
            }
        }
    }

    override fun onListUpdate() {
        logV("onListUpdate()")
        // Close any context menu when the list is updated.
        closeContextMenu()
    }

    /**
     * Shows a Snackbar that displays the time left until the alarm goes off.
     */
    private fun showAlarmWillGoOffSnack(alarm: Alarm) {
        if (!alarm.isEnabled) return
        alarm.nextTime?.let {
            val msg = getString(
                R.string.msg_alarm_will_go_off,
                createTimeDifferenceString(this, it)
            )
            Snackbar.make(fabCreateAlarm, msg, Snackbar.LENGTH_SHORT).show()
        }
    }

    /**
     * Shows a Snackbar that tells user an alarm is skipped and allows them to undo.
     */
    private fun showAlarmSkippedSnack(updated: Alarm, old: Alarm) {
        updated.nextTime?.let {
            val msg = if (updated.title.isNullOrBlank()) {
                getString(R.string.msg_alarm_skipped, it.createDateString())
            } else {
                // If the alarm has a title, bold it and show.
                val title = Html.escapeHtml(updated.title!!)
                val text = getString(
                    R.string.msg_titled_alarm_skipped,
                    it.createDateString(),
                    title
                )
                HtmlCompat.fromHtml(text, HtmlCompat.FROM_HTML_MODE_COMPACT)
            }
            Snackbar.make(fabCreateAlarm, msg, Snackbar.LENGTH_LONG)
                    .setAction(R.string.action_undo) { alarmUpdateHandler.asyncUpdateAlarm(old) }
                    .show()
        }
    }


    /**
     * Called when the create alarm button is clicked.
     */
    private fun onFabCreateAlarmClick() {
        val calendar = Calendar.getInstance().apply {
            add(Calendar.HOUR_OF_DAY, if (this[Calendar.MINUTE] < 50) 1 else 2)
            this[Calendar.MINUTE] = 0
        }
        val hour = calendar[Calendar.HOUR_OF_DAY]
        val minute = calendar[Calendar.MINUTE]
        TimePickerDialogFragment.show(this, hour, minute, TAG_NEW_ALARM)
    }

    private fun onFabCreateAlarmLongClick(): Boolean {
        DelayOptionFragment.newInstance(null).show(supportFragmentManager, null)
        return true
    }

    override fun onDelayOptionClick(quantity: Int, unit: Int) {
        val calendar = Calendar.getInstance().apply {
            add(unit, quantity)
        }
        val intervalStr = when (unit) {
            Calendar.SECOND -> R.plurals.seconds_with_quan
            Calendar.MINUTE -> R.plurals.minutes_with_quan
            Calendar.HOUR_OF_DAY -> R.plurals.hours_with_quan
            Calendar.DATE -> R.plurals.days_with_quan
            Calendar.WEEK_OF_YEAR -> R.plurals.weeks_with_quan
            else -> 0
        }
        val title = resources.getQuantityString(intervalStr, quantity, quantity)
        logD(calendar.time.toString())
        val alarm = Alarm(
            hour = calendar[Calendar.HOUR_OF_DAY],
            minute = calendar[Calendar.MINUTE],
            title = "${getString(R.string.countdown)}${title}",
            nextTime = calendar,
            activateDate = calendar.clone() as Calendar,
            ringtoneUri = RingtoneManager.getDefaultUri(RingtoneManager.TYPE_ALARM)
        )
        alarmUpdateHandler.asyncAddAlarm(alarm)
        showAlarmWillGoOffSnack(alarm)
    }

    /**
     * Backup database.
     *
     * This function is NOT for production use.
     */
    private fun backupDb(permissionGranted: Boolean = false) {
        fun backup() {
            if (ContextCompat.checkSelfPermission(this, Manifest.permission.WRITE_EXTERNAL_STORAGE)
                    != PackageManager.PERMISSION_GRANTED) {
                ActivityCompat.requestPermissions(this,
                    arrayOf(Manifest.permission.WRITE_EXTERNAL_STORAGE),
                    PERMISSION_REQUEST_BACKUP_DB)
            } else {
                BackupDbTask(this).execute()
            }
        }
        if (permissionGranted) {
            backup()
            return
        }
        prompt(R.string.action_backup, R.string.action_backup, android.R.string.cancel) { which ->
            if (which == AlertDialog.BUTTON_POSITIVE) backup()
        }
    }

    /**
     * Restore database.
     *
     * This function is NOT for production use.
     */
    private fun restoreDb(permissionGranted: Boolean = false) {
        fun restore() {
            if (ContextCompat.checkSelfPermission(this, Manifest.permission.WRITE_EXTERNAL_STORAGE)
                    != PackageManager.PERMISSION_GRANTED) {
                ActivityCompat.requestPermissions(this,
                    arrayOf(Manifest.permission.WRITE_EXTERNAL_STORAGE),
                    PERMISSION_REQUEST_RESTORE_DB)
            } else {
                RestoreDbTask(this).execute()
            }
        }
        if (permissionGranted) {
            restore()
            return
        }
        prompt(R.string.action_restore, R.string.action_restore, android.R.string.cancel) { which ->
            if (which == AlertDialog.BUTTON_POSITIVE) restore()
        }
    }

    override fun onPause() {
        val lbm = LocalBroadcastManager.getInstance(this)
        lbm.unregisterReceiver(receiver)
        super.onPause()
    }
}