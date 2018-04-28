package net.wuqs.ontime

import android.arch.lifecycle.Observer
import android.arch.lifecycle.ViewModelProviders
import android.content.Context
import android.content.Intent
import android.os.Bundle
import android.support.v4.app.Fragment
import android.support.v7.widget.LinearLayoutManager
import android.support.v7.widget.RecyclerView
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import net.wuqs.ontime.db.AlarmDataModel
import net.wuqs.ontime.db.Alarm
import net.wuqs.ontime.utils.LogUtils

/**
 * A fragment representing a list of Items.
 * Activities containing this fragment MUST implement the
 * [AlarmListFragment.OnListFragmentInteractionListener] interface.
 */
class AlarmListFragment : Fragment() {

    private var listener: OnListFragmentInteractionListener? = null

    private var recyclerView: RecyclerView? = null

    override fun onCreateView(inflater: LayoutInflater, container: ViewGroup?,
                              savedInstanceState: Bundle?): View? {
        val view = inflater.inflate(R.layout.fragment_alarm_list, container, false)
        val mAdapter = AlarmRecyclerViewAdapter(mutableListOf(), listener)

        // Set the adapter
        recyclerView = view as RecyclerView
        recyclerView!!.apply {
            layoutManager = LinearLayoutManager(context)
            adapter = mAdapter
        }
        val alarms = ViewModelProviders.of(this)[AlarmDataModel::class.java].alarms
        alarms.observe(this, Observer { mAdapter.setAlarms(it!!) })
        return view
    }

    override fun onAttach(context: Context) {
        super.onAttach(context)
        if (context is OnListFragmentInteractionListener) listener = context
        else throw RuntimeException("$context must implement OnListFragmentInteractionListener")
    }

    override fun onDetach() {
        super.onDetach()
        listener = null
    }

//    override fun onResume() {
//        super.onResume()
//        recyclerView?.adapter?.notifyDataSetChanged()
//    }

    /**
     * This interface must be implemented by activities that contain this
     * fragment to allow an interaction in this fragment to be communicated
     * to the activity and potentially other fragments contained in that
     * activity.
     */
    interface OnListFragmentInteractionListener {

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
