package com.elliottsj.ftw.stops

import android.app.Fragment
import android.content.Intent
import android.os.Bundle
import android.support.v7.widget.{LinearLayoutManager, RecyclerView}
import android.view._
import com.devspark.progressfragment.ProgressFragment
import com.elliottsj.ftw.R
import com.elliottsj.ftw.activities.AddStopActivity
import com.elliottsj.ftw.agencies.{StopAdapter, AgencyAdapter}
import com.elliottsj.ftw.protobus.Protobus
import com.elliottsj.ftw.util.{AsyncTaskContext, DelayedTask}
import com.elliottsj.protobus.Stop
import org.scaloid.common._

import scala.util.{Failure, Success}
import scala.concurrent.duration._

class SavedStopListFragment extends ProgressFragment with TagUtil with Logger with AsyncTaskContext {

  private var mContentView: View = _
  private var mRecyclerView: RecyclerView = _
  private var mLayoutManager: RecyclerView.LayoutManager = _

  override def onCreate(savedInstanceState: Bundle): Unit = {
    super.onCreate(savedInstanceState)
    mLayoutManager = new LinearLayoutManager(getActivity)
  }

  override def onCreateView(inflater: LayoutInflater, container: ViewGroup, savedInstanceState: Bundle): View = {
    // Inflate the content view without a parent ViewGroup
    mContentView = inflater.inflate(R.layout.fragment_stop_list, null)

    mRecyclerView = mContentView.findViewById(android.R.id.list).asInstanceOf[RecyclerView]
    mRecyclerView.setLayoutManager(mLayoutManager)

    super.onCreateView(inflater, container, savedInstanceState)
  }

  override def onActivityCreated(savedInstanceState: Bundle): Unit = {
    super.onActivityCreated(savedInstanceState)

    // Set the content view and empty text
    setContentView(mContentView)
    setEmptyText("No stops saved") // TODO: use string resource

    // Hide the content view while loading agencies
    setContentShown(false)

    // TODO: Load saved stops + predictions
    DelayedTask(delayBy = 3.seconds) {
      setContentShown(true)
      mRecyclerView.setAdapter(new StopAdapter(
        getActivity,
        Array[Stop](Stop(nextbusFields = Option(Stop.Nextbus("5292", "High Park Loop", None, 43.6477899f, -79.45813f, None)))),
        onStopClick)
      )
    }

//    Protobus(getActivity).getAgencies onComplete {
//      case Success(agencies) => runOnUiThread {
//        setContentShown(true)
//        mRecyclerView.setAdapter(new AgencyAdapter(getActivity, agencies, onAgencyClick))
//      }
//      case Failure(err) =>
//        error("Failed to fetch Protobus agencies", err)
//    }
  }

  def onStopClick(stop: Stop) {
  }

}
