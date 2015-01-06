package com.elliottsj.ftw.agencies

import android.os.Bundle
import com.elliottsj.ftw.R
import org.scaloid.common._

/**
 * An activity where the user can select a transit agency to add to his/her list of saved
 * transit agencies.
 */
class AddAgencyActivity extends SActivity with Logger {

  override def onCreate(savedInstanceState: Bundle): Unit = {
    super.onCreate(savedInstanceState)
    setContentView(R.layout.activity_add_agency)

    // TODO: check network connection
  }

  override def onResume(): Unit = {
    super.onResume()

    // Load the agency list fragment
    getFragmentManager
      .beginTransaction()
      .add(R.id.fragment_container, new AgencyListFragment)
      .commit()
  }

}
