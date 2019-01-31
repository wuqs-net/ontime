package net.wuqs.ontime.feature.home

import androidx.lifecycle.LiveData
import androidx.lifecycle.Observer
import androidx.lifecycle.ViewModelProviders
import android.content.Context
import android.os.Bundle
import androidx.fragment.app.Fragment
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import android.view.*
import kotlinx.android.synthetic.main.fragment_alarm_list.*
import kotlinx.android.synthetic.main.fragment_alarm_list.view.*
import net.wuqs.ontime.R
import net.wuqs.ontime.db.Alarm
import net.wuqs.ontime.db.AlarmDataModel
import net.wuqs.ontime.util.Logger

/**
 * A fragment representing a list of Items.
 * Activities containing this fragment MUST implement the
 * [AlarmListFragment.OnListFragmentActionListener] interface.
 */
class AlarmListFragment : Fragment() {

    private var mListener: OnListFragmentActionListener? = null

    private lateinit var mRecyclerView: RecyclerView
    private var mAdapter: AlarmRecyclerViewAdapter? = null

    // Data
    private lateinit var alarmDataModel: AlarmDataModel
    private lateinit var mAlarms: LiveData<List<Alarm>>
    private lateinit var mAlarmsObserver: Observer<List<Alarm>>

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setHasOptionsMenu(true)
        alarmDataModel = ViewModelProviders.of(this)[AlarmDataModel::class.java]
        mAlarms = alarmDataModel.alarms
        mAlarmsObserver = Observer { onDataChange(it!!) }
        mAlarms.observe(this, mAlarmsObserver)
        mLogger.v("onCreate")
    }

    override fun onCreateView(inflater: LayoutInflater, container: ViewGroup?,
                              savedInstanceState: Bundle?): View? {
        val view = inflater.inflate(R.layout.fragment_alarm_list, container, false)
        mAdapter = AlarmRecyclerViewAdapter(mutableListOf(), mListener)

        mRecyclerView = (view.rv_alarm_list as RecyclerView).apply {
            layoutManager = LinearLayoutManager(context)
            adapter = mAdapter
        }
        return view
    }

    override fun onAttach(context: Context) {
        super.onAttach(context)
        mListener = if (context is OnListFragmentActionListener) {
            context
        } else {
            throw RuntimeException("$context must implement OnListFragmentActionListener")
        }
    }

    override fun onDetach() {
        super.onDetach()
        mListener = null
    }

    override fun onCreateOptionsMenu(menu: Menu, inflater: MenuInflater) {
        inflater.inflate(R.menu.menu_alarm_list, menu)
    }

    override fun onOptionsItemSelected(item: MenuItem): Boolean {
        if (item.groupId == R.id.group_filter) {
            item.isChecked = true
            alarmDataModel.dataState = when (item.itemId) {
                R.id.item_all_alarms -> AlarmDataModel.ALARMS_ALL
                R.id.item_today_alarms -> AlarmDataModel.ALARMS_TODAY
                R.id.item_daily_alarms -> AlarmDataModel.ALARMS_DAILY
                R.id.item_weekly_alarms -> AlarmDataModel.ALARMS_WEEKLY
                R.id.item_monthly_alarms -> AlarmDataModel.ALARMS_MONTHLY
                R.id.item_history -> AlarmDataModel.ALARMS_HISTORY
                else -> throw IllegalArgumentException("Illegal filter type")
            }
        }
        return super.onOptionsItemSelected(item)
    }

    private fun onDataChange(data: List<Alarm>) {
        mAdapter?.setAlarms(data)
        mListener?.onListUpdate()
        if (data.isEmpty()) {
            rv_alarm_list.visibility = View.INVISIBLE
            tv_hint_add_alarm.visibility = View.VISIBLE
            when (alarmDataModel.dataState) {
                AlarmDataModel.ALARMS_ALL -> {
                    tv_hint_add_alarm.setText(R.string.msg_click_add)
                }
                AlarmDataModel.ALARMS_HISTORY -> {
                    tv_hint_add_alarm.setText(R.string.msg_no_history)
                }
                else -> {
                    tv_hint_add_alarm.setText(R.string.msg_click_add)
                }
            }
        } else {
            rv_alarm_list.visibility = View.VISIBLE
            tv_hint_add_alarm.visibility = View.GONE
        }
    }

    /**
     * This interface must be implemented by activities that contain this
     * fragment to allow an interaction in this fragment to be communicated
     * to the activity and potentially other fragments contained in that
     * activity.
     */
    interface OnListFragmentActionListener {

        /**
         * Called when an item in the [RecyclerView] is clicked.
         *
         * @param item the [Alarm] attached to the [RecyclerView] item.
         */
        fun onListItemClick(item: Alarm)

        /**
         * Called when the switch in an item in the [RecyclerView] is clicked.
         *
         * @param item the [Alarm] attached to the [RecyclerView] item.
         * @param isChecked whether the switch is checked.
         */
        fun onAlarmSwitchClick(item: Alarm, isChecked: Boolean)

        /**
         * Called when an option in the context menu of an item in the [RecyclerView] is selected.
         *
         * @param item the [Alarm] attached to the [RecyclerView] item.
         * @param menuItem the [MenuItem] selected.
         */
        fun onContextMenuItemSelected(item: Alarm, menuItem: MenuItem)

        /**
         * Called when the contents of the [RecyclerView] is updated.
         */
        fun onListUpdate()
    }

    private val mLogger = Logger("AlarmListFragment")
}
