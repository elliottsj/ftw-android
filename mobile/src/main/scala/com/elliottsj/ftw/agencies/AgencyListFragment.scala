package com.elliottsj.ftw.agencies

import android.database.sqlite.SQLiteConstraintException
import android.os.Bundle
import android.support.v7.widget.{LinearLayoutManager, RecyclerView}
import android.view.{LayoutInflater, View, ViewGroup}
import android.widget.Toast
import com.devspark.progressfragment.ProgressFragment
import com.elliottsj.ftw.R
import com.elliottsj.ftw.preferences.Preferences
import com.elliottsj.ftw.protobus.Protobus
import com.elliottsj.ftw.util.AsyncTaskContext
import com.elliottsj.protobus.Agency
import org.scaloid.common.{Logger, TagUtil, runOnUiThread}

import scala.util.{Failure, Success, Try}

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
    Protobus(getActivity).getAgencies onComplete {
      case Success(agencies) => runOnUiThread {
        setContentShown(true)
        mRecyclerView.setAdapter(new AgencyAdapter(getActivity, agencies, onAgencyClick))
      }
      case Failure(err) =>
        error("Failed to fetch Protobus agencies", err)
    }
  }

  /**
   * Handle when the user clicks on a transit agency
   *
   * @param agency the clicked agency
   */
  def onAgencyClick(agency: Agency): Unit = {
    // Add the selected agency to preferences
    Try(Preferences(getActivity).saveAgency(agency)) match {
      case Success(_) =>
        // Successfully added the agency; finish the activity
        getActivity.finish()
      case Failure(err) =>
        // Failed to add the agency; display the error
        Toast.makeText(getActivity, err.getCause match {
          case cause: SQLiteConstraintException => "Selected agency is already saved"
          case cause => s"Cannot add agency: ${cause.getMessage}"
        }, Toast.LENGTH_SHORT).show()
    }
  }

}
