package net.wuqs.ontime.feature.history

import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import android.view.View
import androidx.lifecycle.LiveData
import androidx.lifecycle.Observer
import androidx.lifecycle.ViewModelProviders
import androidx.recyclerview.widget.LinearLayoutManager
import kotlinx.android.synthetic.main.activity_history_list.*
import net.wuqs.ontime.R
import net.wuqs.ontime.db.Alarm
import net.wuqs.ontime.db.AlarmDataModel
import net.wuqs.ontime.feature.shared.dialog.TextInputDialogFragment
import net.wuqs.ontime.util.logD
import net.wuqs.ontime.util.logV

class HistoryListActivity : AppCompatActivity(), HistoryRVAdapter.OnListItemActionListener,
        TextInputDialogFragment.TextInputDialogListener {

    private lateinit var alarmDataModel: AlarmDataModel

    private lateinit var historyEntries: LiveData<List<Alarm>>
    private lateinit var entriesObserver: Observer<List<Alarm>>

    private lateinit var adapter: HistoryRVAdapter

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_history_list)
        title = getString(R.string.option_history)

        alarmDataModel = ViewModelProviders.of(this)[AlarmDataModel::class.java]
        historyEntries = alarmDataModel.history
        entriesObserver = Observer { onDataChange(it) }
        historyEntries.observe(this, entriesObserver)
        adapter = HistoryRVAdapter(emptyList(), this)

        rv_history_entries.adapter = adapter
        rv_history_entries.layoutManager = LinearLayoutManager(this)
    }

    override fun onItemNotesClick(position: Int, alarm: Alarm) {
        val dialog = TextInputDialogFragment.newInstance(getString(R.string.hint_alarm_notes),
            alarm.notes)
        dialog.show(supportFragmentManager, "editNotes")
    }

    override fun onTextOK(tag: String?, text: String) {
        if (tag == "editNotes") {
            logV("New notes: $text")
            TODO("Change notes in alarm")
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
