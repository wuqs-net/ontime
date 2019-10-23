package net.wuqs.ontime.feature.history

import android.os.Bundle
import android.view.View
import androidx.appcompat.app.AppCompatActivity
import androidx.lifecycle.LiveData
import androidx.lifecycle.Observer
import androidx.lifecycle.ViewModelProviders
import androidx.recyclerview.widget.LinearLayoutManager
import kotlinx.android.synthetic.main.activity_history_list.*
import net.wuqs.ontime.R
import net.wuqs.ontime.alarm.AlarmUpdateHandler
import net.wuqs.ontime.db.Alarm
import net.wuqs.ontime.db.AlarmDataModel
import net.wuqs.ontime.feature.shared.dialog.TextInputDialogFragment
import net.wuqs.ontime.util.logD

private const val TAG_EDIT_NOTES = "EDIT_NOTES"

class HistoryListActivity : AppCompatActivity(), HistoryRVAdapter.OnListItemActionListener,
        TextInputDialogFragment.TextInputDialogListener {

    private lateinit var alarmDataModel: AlarmDataModel

    private lateinit var historyEntries: LiveData<List<Alarm>>
    private lateinit var entriesObserver: Observer<List<Alarm>>

    private lateinit var adapter: HistoryRVAdapter

    private lateinit var alarmUpdateHandler: AlarmUpdateHandler

    private var editingPosition: Int? = null

    private var editingAlarm: Alarm? = null

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_history_list)
        title = getString(R.string.option_history)

        alarmUpdateHandler = AlarmUpdateHandler(this)

        alarmDataModel = ViewModelProviders.of(this)[AlarmDataModel::class.java]
        historyEntries = alarmDataModel.history
        entriesObserver = Observer { onDataChange(it) }
        historyEntries.observe(this, entriesObserver)
        adapter = HistoryRVAdapter(emptyList(), this)

        rv_history_entries.adapter = adapter
        rv_history_entries.layoutManager = LinearLayoutManager(this)
    }

    override fun onItemNotesClick(position: Int, alarm: Alarm) {
        val dialog = TextInputDialogFragment.newInstance(getString(R.string.hint_records),
            alarm.notes)
        dialog.show(supportFragmentManager, TAG_EDIT_NOTES)
        editingPosition = position
        editingAlarm = alarm
    }

    override fun onTextOK(tag: String?, text: String) {
        if (tag == TAG_EDIT_NOTES) {
            editingAlarm?.let {
                it.notes = text
                alarmUpdateHandler.asyncUpdateAlarm(it)
            }
            editingAlarm = null
            editingPosition?.let { adapter.notifyItemChanged(it) }
            editingPosition = null
        }
    }

    override fun onCancel(tag: String?) {
        if (tag == TAG_EDIT_NOTES) {
            editingAlarm = null
            editingPosition = null
        }
    }

    private fun onDataChange(data: List<Alarm>) {
        logD("Data changed: new list length is ${data.size}")
        if (data.isEmpty()) {
            tv_no_history.visibility = View.VISIBLE
            rv_history_entries.visibility = View.INVISIBLE
        } else {
            tv_no_history.visibility = View.INVISIBLE
            rv_history_entries.visibility = View.VISIBLE
        }
        adapter.setData(data)
    }
}
