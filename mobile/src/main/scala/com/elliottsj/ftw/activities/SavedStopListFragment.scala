package com.elliottsj.ftw.activities

import android.app.Fragment
import android.content.Intent
import android.os.Bundle
import android.view._
import com.elliottsj.ftw.R
import org.scaloid.common.Logger

class SavedStopListFragment extends Fragment with Logger {

  override def onCreate(savedInstanceState: Bundle) {
    super.onCreate(savedInstanceState)
    //setHasOptionsMenu(true)
  }

  override def onCreateView(inflater: LayoutInflater, container: ViewGroup, savedInstanceState: Bundle): View = {
    inflater.inflate(R.layout.fragment_stops, container, false)
  }

  override def onActivityCreated(savedInstanceState: Bundle) {
    super.onActivityCreated(savedInstanceState)
  }

  override def onCreateOptionsMenu(menu: Menu, inflater: MenuInflater) {
    inflater.inflate(R.menu.saved_stops, menu)
    super.onCreateOptionsMenu(menu, inflater)
  }

  override def onOptionsItemSelected(item: MenuItem): Boolean = {
    item.getItemId match {
      case R.id.add_stop =>
        val intent: Intent = new Intent(getActivity, classOf[AddStopActivity])
        startActivity(intent)
        true
      case R.id.refresh =>
        loadPredictions()
        true
      case _ =>
        super.onOptionsItemSelected(item)
    }
  }

  private def loadPredictions(): Unit = {

  }

}
