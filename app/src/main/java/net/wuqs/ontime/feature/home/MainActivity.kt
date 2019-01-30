package net.wuqs.ontime.feature.home

import android.Manifest
import android.app.AlertDialog
import android.content.BroadcastReceiver
import android.content.Context
import android.content.Intent
import android.content.IntentFilter
import android.content.pm.PackageManager
import android.media.AudioManager
import android.os.Build
import android.os.Bundle
import android.support.design.widget.Snackbar
import android.support.v4.app.ActivityCompat
import android.support.v4.content.ContextCompat
import android.support.v4.content.LocalBroadcastManager
import android.support.v4.text.TextUtilsCompat
import android.support.v7.app.AppCompatActivity
import android.text.Html
import android.text.TextUtils
import android.view.Menu
import android.view.MenuItem
import android.widget.Toast
import kotlinx.android.synthetic.main.activity_main.*
import net.wuqs.ontime.BuildConfig
import net.wuqs.ontime.R
import net.wuqs.ontime.alarm.*
import net.wuqs.ontime.db.Alarm
import net.wuqs.ontime.db.BackupDbTask
import net.wuqs.ontime.db.RestoreDbTask
import net.wuqs.ontime.feature.about.AboutActivity
import net.wuqs.ontime.feature.editalarm.EditAlarmActivity
import net.wuqs.ontime.feature.missedalarms.MissedAlarmsActivity
import net.wuqs.ontime.feature.shared.dialog.TimePickerDialogFragment
import net.wuqs.ontime.util.Logger
import net.wuqs.ontime.util.changeTaskDescription
import java.util.*


class MainActivity : AppCompatActivity(),
        AlarmListFragment.OnListFragmentActionListener,
        TimePickerDialogFragment.OnTimeSetListener {

    private lateinit var alarmUpdateHandler: AlarmUpdateHandler

    private val receiver = object : BroadcastReceiver() {
        override fun onReceive(context: Context?, intent: Intent?) {
            logger.d("onReceive(): ${intent?.action}")
            when (intent!!.action) {
                ACTION_SHOW_MISSED_ALARMS -> {
                    // Start the activity to show missed alarms.
                    if (!intent.hasExtra(EXTRA_MISSED_ALARMS)) return
                    logger.i("Show missed alarms")
                    val missedAlarmsIntent = Intent(
                            this@MainActivity,
                            MissedAlarmsActivity::class.java
                    ).apply {
                        putExtras(intent)
                    }
                    startActivity(missedAlarmsIntent)
                }
            }
        }
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_main)
        logger.v("onCreate")

        createAlarmNotificationChannels()

        changeTaskDescription()

        alarmUpdateHandler = AlarmUpdateHandler(this)

        val filter = IntentFilter(ACTION_SHOW_MISSED_ALARMS)
        val lbm = LocalBroadcastManager.getInstance(this)
        lbm.registerReceiver(receiver, filter)

        volumeControlStream = AudioManager.STREAM_ALARM
        fabCreateAlarm.setOnClickListener { onFabCreateAlarmClick() }
        sendBroadcast(AlarmStateManager.createScheduleAllAlarmsIntent(this))
    }

    override fun onActivityResult(requestCode: Int, resultCode: Int, data: Intent?) {
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
            else -> return super.onOptionsItemSelected(item)
        }
        return true
    }

    override fun onRequestPermissionsResult(requestCode: Int, permissions: Array<out String>, grantResults: IntArray) {
        when (requestCode) {
            PERMISSION_REQUEST_BACKUP_DB -> {
                if (grantResults.isNotEmpty() && grantResults[0]
                        == PackageManager.PERMISSION_GRANTED) {
                    backupDb()
                }
            }
            PERMISSION_REQUEST_RESTORE_DB -> {
                if (grantResults.isNotEmpty() && grantResults[0]
                        == PackageManager.PERMISSION_GRANTED) {
                    restoreDb()
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
        logger.v("onListItemClick: $item")
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
                AlertDialog.Builder(this).run {
                    setMessage(R.string.prompt_delete_alarm)
                    setPositiveButton(R.string.action_delete) { _, _ ->
                        alarmUpdateHandler.asyncDeleteAlarm(item)
                    }
                    setNegativeButton(android.R.string.cancel, null)
                    create()
                }.show()
            }
        }
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
                val text = getString(R.string.msg_titled_alarm_skipped,
                        it.createDateString(), title)
                if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.N) {
                    Html.fromHtml(text, Html.FROM_HTML_MODE_COMPACT)
                } else {
                    @Suppress("DEPRECATION")
                    Html.fromHtml(text)
                }
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

    /**
     * Backup database.
     *
     * This function is NOT for production use.
     */
    private fun backupDb() {
        if (ContextCompat.checkSelfPermission(this, Manifest.permission.WRITE_EXTERNAL_STORAGE)
                != PackageManager.PERMISSION_GRANTED) {
            ActivityCompat.requestPermissions(this,
                    arrayOf(Manifest.permission.WRITE_EXTERNAL_STORAGE),
                    PERMISSION_REQUEST_BACKUP_DB)
        } else {
            BackupDbTask(this).execute()
        }
    }

    /**
     * Restore database.
     *
     * This function is NOT for production use.
     */
    private fun restoreDb() {
        if (ContextCompat.checkSelfPermission(this, Manifest.permission.WRITE_EXTERNAL_STORAGE)
                != PackageManager.PERMISSION_GRANTED) {
            ActivityCompat.requestPermissions(this,
                    arrayOf(Manifest.permission.WRITE_EXTERNAL_STORAGE),
                    PERMISSION_REQUEST_RESTORE_DB)
        } else {
            RestoreDbTask(this).execute()
        }
    }

    override fun onDestroy() {
        val lbm = LocalBroadcastManager.getInstance(this)
        lbm.unregisterReceiver(receiver)
        super.onDestroy()
    }

    private val logger = Logger("MainActivity")
}

private const val TAG_NEW_ALARM = "NEW_ALARM"

const val RESULT_SAVE_ALARM = 1
const val RESULT_DELETE_ALARM = 2

private const val PERMISSION_REQUEST_BACKUP_DB = 1
private const val PERMISSION_REQUEST_RESTORE_DB = 2
