package com.elliottsj.ftw.activities

import android.content.res.Configuration
import android.os.Bundle
import android.support.v4.widget.DrawerLayout
import android.support.v7.app.ActionBarDrawerToggle
import android.view.{MenuItem, View}
import android.widget.{ArrayAdapter, AdapterView, ListView, TextView}
import com.elliottsj.ftw.R
import org.scaloid.common._

class MainActivity extends SActivity with Logger {

  private var mDrawerLayout: DrawerLayout = _
  private var mDrawerList: ListView = _
  private var mDrawerToggle: ActionBarDrawerToggle = _
  private var mAddAgencyButton: TextView = _

  protected override def onCreate(savedInstanceState: Bundle): Unit = {
    super.onCreate(savedInstanceState)
    setContentView(R.layout.activity_main)

    // Enable the menu button
    getActionBar.setDisplayHomeAsUpEnabled(true)

    mDrawerLayout = find[DrawerLayout](R.id.drawer_layout)
    mDrawerList = find[ListView](android.R.id.list)
    mAddAgencyButton = find[TextView](R.id.add_agency)

    mDrawerList.setAdapter(new ArrayAdapter(this, R.layout.drawer_item, Array("TTC", "MBTA")))
    // SArrayAdapter(R.layout.drawer_item, /* TODO: get agency data */ Array("TTC", "MBTA"))
    mDrawerList.onItemClick(onDrawerItemClick _)

    // ActionBarDrawerToggle ties together the the proper interactions
    // between the sliding drawer and the action bar app icon
    mDrawerToggle = new ActionBarDrawerToggle(this, mDrawerLayout, R.string.drawer_open, R.string.drawer_close)
    mDrawerLayout.setDrawerListener(mDrawerToggle)

    mAddAgencyButton.onClick(toast("Adding agency"))
  }

  override def onPostCreate(savedInstanceState: Bundle): Unit = {
    super.onPostCreate(savedInstanceState)
    // Sync the toggle state after onRestoreInstanceState has occurred.
    mDrawerToggle.syncState()
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

  def onDrawerItemClick(parent: AdapterView[_], view: View, position: Int, id: Long): Unit = position match {
    case 0 => getFragmentManager.beginTransaction().replace(R.id.content_frame, new StopListFragment())
    case 1 => getFragmentManager.beginTransaction().replace(R.id.content_frame, new StopListFragment())
  }
}
