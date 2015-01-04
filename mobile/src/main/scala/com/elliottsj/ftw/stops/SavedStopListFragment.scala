package com.elliottsj.ftw.stops

import android.app.Fragment
import android.content.Intent
import android.os.Bundle
import android.view._
import com.elliottsj.ftw.R
import com.elliottsj.ftw.activities.AddStopActivity
import org.scaloid.common.Logger

class SavedStopListFragment extends Fragment with Logger {

  override def onCreateView(inflater: LayoutInflater, container: ViewGroup, savedInstanceState: Bundle): View = {
    inflater.inflate(R.layout.fragment_stop_list, container, false)
  }

  private def loadPredictions(): Unit = {

  }

}
