package net.wuqs.ontime.feature.missedalarms

import android.os.Bundle
import androidx.appcompat.app.AppCompatActivity
import androidx.recyclerview.widget.LinearLayoutManager
import kotlinx.android.synthetic.main.activity_missed_alarms.*
import net.wuqs.ontime.R
import net.wuqs.ontime.alarm.ACTION_DISMISS_ALL_MISSED_ALARMS
import net.wuqs.ontime.alarm.AlarmStateManager
import net.wuqs.ontime.alarm.EXTRA_MISSED_ALARMS
import net.wuqs.ontime.db.Alarm
import net.wuqs.ontime.util.Logger
import net.wuqs.ontime.util.changeTaskDescription

class MissedAlarmsActivity : AppCompatActivity(),
        MissedAlarmsAdapter.OnListInteractListener {

    private lateinit var alarms: ArrayList<Alarm>

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_missed_alarms)
        title = getText(R.string.title_missed_alarms)
        mLogger.v("onCreate")

        changeTaskDescription()

        alarms = intent.getParcelableArrayListExtra(EXTRA_MISSED_ALARMS)
        rv_missed_alarms.layoutManager = LinearLayoutManager(this)
        rv_missed_alarms.adapter = MissedAlarmsAdapter(this, alarms)

        btn_dismiss_all.setOnClickListener { dismissAll() }
    }

    override fun onListItemClick(alarm: Alarm) {
        TODO("not implemented") //To change body of created functions use File | Settings | File Templates.
    }

    override fun onBackPressed() {
    }

    private fun dismissAll() {
        val mIntent = AlarmStateManager.createIntent(ACTION_DISMISS_ALL_MISSED_ALARMS,
                this).apply {
            putParcelableArrayListExtra(EXTRA_MISSED_ALARMS, alarms)
        }
        sendBroadcast(mIntent)
        finish()
    }

    private val mLogger = Logger("MissedAlarmActivity")
}