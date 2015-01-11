package com.elliottsj.ftw.agencies

import android.os.Bundle
import android.support.v7.widget.{LinearLayoutManager, RecyclerView}
import android.view.{LayoutInflater, View, ViewGroup}
import com.devspark.progressfragment.ProgressFragment
import com.elliottsj.ftw.R
import com.elliottsj.ftw.preferences.Preferences
import com.elliottsj.ftw.protobus.Protobus
import com.elliottsj.ftw.util.AsyncTaskContext
import com.elliottsj.protobus.Agency
import org.scaloid.common.{Logger, TagUtil, runOnUiThread}

class AgencyListFragment extends ProgressFragment with TagUtil with Logger with AsyncTaskContext {

  private var mContentView: View = _
  private var mRecyclerView: RecyclerView = _
  private var mLayoutManager: RecyclerView.LayoutManager = _

  override def onCreate(savedInstanceState: Bundle): Unit = {
    super.onCreate(savedInstanceState)
    mLayoutManager = new LinearLayoutManager(getActivity)
  }

  override def onCreateView(inflater: LayoutInflater, container: ViewGroup, savedInstanceState: Bundle): View = {
    // Inflate the content view without a parent ViewGroup
    mContentView = inflater.inflate(R.layout.fragment_agency_list, null)

    mRecyclerView = mContentView.findViewById(android.R.id.list).asInstanceOf[RecyclerView]
    mRecyclerView.setLayoutManager(mLayoutManager)
    
    super.onCreateView(inflater, container, savedInstanceState)
  }

  override def onActivityCreated(savedInstanceState: Bundle): Unit = {
    super.onActivityCreated(savedInstanceState)

    // Set the content view and empty text
    setContentView(mContentView)
    setEmptyText("No transit agencies available") // TODO: use string resource

    // Hide the content view while loading agencies
    setContentShown(false)

    // Load agencies
    Protobus(getActivity).getAgencies onSuccess { case agencies => runOnUiThread {
      setContentShown(true)
      mRecyclerView.setAdapter(new AgencyAdapter(getActivity, agencies, onAgencyClick))
    }}
  }

  def onAgencyClick(agency: Agency): Unit = {
    // Add the selected agency to preferences and exit the activity
    Preferences(getActivity).saveAgency(agency)
    getActivity.finish()
  }

}
