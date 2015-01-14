package com.elliottsj.ftw.activities

import java.util

import android.content.res.Configuration
import android.os.Bundle
import android.support.v4.widget.DrawerLayout
import android.support.v7.app.ActionBarDrawerToggle
import android.view.{MenuItem, View, ViewGroup}
import android.widget._
import com.elliottsj.ftw.R
import com.elliottsj.ftw.agencies.AddAgencyActivity
import com.elliottsj.ftw.preferences.Preferences
import com.elliottsj.ftw.stops.StopTabsFragment
import com.elliottsj.protobus.Agency
import org.scaloid.common.{Preferences => _, _}

import scala.collection.JavaConverters._
import scala.collection.mutable
import scala.collection.mutable.ArrayBuffer

class MainActivity extends SActivity with Logger {

  private var mDrawerLayout: DrawerLayout = _
  private var mDrawerPanel: RelativeLayout = _
  private var mDrawerList: ListView = _
  private var mDrawerAdapter: ArrayAdapter[Agency] = _
  private var mDrawerToggle: ActionBarDrawerToggle = _
  private var mAddAgencyButton: TextView = _

  protected override def onCreate(savedInstanceState: Bundle): Unit = {
    super.onCreate(savedInstanceState)
    setContentView(R.layout.activity_main)

    // Enable the menu button
    getActionBar.setDisplayHomeAsUpEnabled(true)

    mDrawerLayout = find[DrawerLayout](R.id.drawer_layout)
    mDrawerPanel = find[RelativeLayout](R.id.drawer_panel)
    mDrawerList = find[ListView](android.R.id.list)
    mAddAgencyButton = find[TextView](R.id.add_agency)

    // Create an array adapter for the drawer and initialize with an empty list
    mDrawerAdapter = new ArrayAdapter[Agency](this, R.layout.drawer_item, new util.ArrayList[Agency]()) {
      /**
       * Set the TextView to contain the agency short title if it exists,
       * otherwise the full title.
       */
      override def getView(position: Int, convertView: View, parent: ViewGroup): View = {
        val nb = getItem(position).getNextbusFields
        val view = super.getView(position, convertView, parent)
        view.asInstanceOf[TextView].setText(nb.agencyShortTitle getOrElse nb.agencyTitle)
        view
      }
    }
    mDrawerList.setAdapter(mDrawerAdapter)
    mDrawerList.onItemClick(onDrawerItemClick _)

    // ActionBarDrawerToggle ties together the the proper interactions
    // between the sliding drawer and the action bar app icon
    mDrawerToggle = new ActionBarDrawerToggle(this, mDrawerLayout, R.string.drawer_open, R.string.drawer_close)
    mDrawerLayout.setDrawerListener(mDrawerToggle)

    // Upon clicking on 'Add transit agency', start the activity
    mAddAgencyButton.onClick(startActivity[AddAgencyActivity])
  }

  override def onPostCreate(savedInstanceState: Bundle): Unit = {
    super.onPostCreate(savedInstanceState)
    // Sync the toggle state after onRestoreInstanceState has occurred.
    mDrawerToggle.syncState()
  }


  override def onResume(): Unit = {
    super.onResume()
    mDrawerAdapter.clear()
    val agencies: util.Collection[Agency] = Preferences(this).getAgencies.asJavaCollection
    mDrawerAdapter.addAll(agencies)
  }

  override def onConfigurationChanged(newConfig: Configuration): Unit = {
    super.onConfigurationChanged(newConfig)
    // Pass any configuration change to the drawer toggles
    mDrawerToggle.onConfigurationChanged(newConfig)
  }

  override def onOptionsItemSelected(item: MenuItem): Boolean = {
    if (mDrawerToggle.onOptionsItemSelected(item))
      return true
    super.onOptionsItemSelected(item)
  }

  def onDrawerItemClick(parent: AdapterView[_], view: View, position: Int, id: Long): Unit = {
    getFragmentManager.beginTransaction().replace(R.id.content_frame, {
      val f = new StopTabsFragment()
      val args = new Bundle()
      args.putSerializable(StopTabsFragment.ARG_AGENCY, mDrawerAdapter.getItem(position))
      f.setArguments(args)
      f
    }).commit()

    mDrawerList.setItemChecked(position, true)
    mDrawerLayout.closeDrawer(mDrawerPanel)
  }

}
