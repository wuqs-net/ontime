package net.wuqs.ontime

import android.arch.lifecycle.LiveData
import android.arch.lifecycle.Observer
import android.arch.lifecycle.ViewModelProviders
import android.content.Context
import android.os.Bundle
import android.support.v4.app.Fragment
import android.support.v7.widget.LinearLayoutManager
import android.support.v7.widget.RecyclerView
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import net.wuqs.ontime.db.Alarm
import net.wuqs.ontime.db.AlarmDataModel
import net.wuqs.ontime.utils.LogUtils

/**
 * A fragment representing a list of Items.
 * Activities containing this fragment MUST implement the
 * [AlarmListFragment.OnListFragmentActionListener] interface.
 */
class AlarmListFragment : Fragment() {

    private var listener: OnListFragmentActionListener? = null

    private lateinit var recyclerView: RecyclerView
    private var mAdapter: AlarmRecyclerViewAdapter? = null

    // Data
    private lateinit var alarmsData: LiveData<List<Alarm>>
    private lateinit var alarmObserver: Observer<List<Alarm>>

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        alarmsData = ViewModelProviders.of(this)[AlarmDataModel::class.java].alarms
        alarmObserver = Observer { onDataChange(it!!) }
        alarmsData.observe(this, alarmObserver)
        LOGGER.v("onCreate")
    }

    override fun onCreateView(inflater: LayoutInflater, container: ViewGroup?,
                              savedInstanceState: Bundle?): View? {
        val view = inflater.inflate(R.layout.fragment_alarm_list, container, false)
        mAdapter = AlarmRecyclerViewAdapter(mutableListOf(), listener)

        recyclerView = view as RecyclerView
        // Set the adapter
        return view.apply {
            layoutManager = LinearLayoutManager(context)
            adapter = mAdapter
        }
    }

    override fun onAttach(context: Context) {
        super.onAttach(context)
        listener = when (context) {
            is OnListFragmentActionListener -> context
            else -> throw RuntimeException("$context must implement OnListFragmentActionListener")
        }
    }

    override fun onDetach() {
        super.onDetach()
        listener = null
    }

    private fun onDataChange(data: List<Alarm>) {
        mAdapter?.setAlarms(data)
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
         * Called when the [RecyclerView] is updated.
         *
         * @param itemCount the number of items in the [RecyclerView].
         */
        fun onRecyclerViewUpdate(itemCount: Int)
    }

    private val LOGGER = LogUtils.Logger("AlarmListFragment")
}
