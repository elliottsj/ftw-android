package com.elliottsj.ftw.activities

import android.app.FragmentManager
import android.os.Bundle
import android.support.v13.app.FragmentPagerAdapter
import android.support.v4.view.ViewPager
import android.view.{Menu, MenuItem}
import com.elliottsj.ftw.R
import com.elliottsj.ftw.views.SlidingTabLayout
import org.scaloid.common.SActivity

class StopListActivity extends SActivity {

  protected override def onCreate(savedInstanceState: Bundle): Unit = {
    super.onCreate(savedInstanceState)
    setContentView(R.layout.activity_stop_list)

    // Get the ViewPager and set it's PagerAdapter so that it can display items
    val viewPager = find[ViewPager](R.id.pager)
    viewPager.setAdapter(new StopsTabsAdapter(getFragmentManager))

    // Give the SlidingTabLayout the ViewPager
    val slidingTabLayout = find[SlidingTabLayout](R.id.sliding_tabs)
    slidingTabLayout.setViewPager(viewPager)
  }

  override def onDestroy(): Unit = super.onDestroy()

  override def onCreateOptionsMenu(menu: Menu): Boolean = {
    getMenuInflater.inflate(R.menu.stops, menu)
    super.onCreateOptionsMenu(menu)
  }

  override def onOptionsItemSelected(item: MenuItem): Boolean = item.getItemId match {
    case R.id.settings => true
//    case R.id.sync => {
//      // Force manual sync
//      Bundle bundle = new Bundle()
//      bundle.putBoolean(ContentResolver.SYNC_EXTRAS_MANUAL, true)
//      bundle.putBoolean(ContentResolver.SYNC_EXTRAS_EXPEDITED, true)
//      ContentResolver.requestSync(mAccount, AUTHORITY, bundle)
//      return true
//    }
    case R.id.about => true
    case _ => super.onOptionsItemSelected(item)
  }

  class StopsTabsAdapter(fm: FragmentManager) extends FragmentPagerAdapter(fm) {

    val TITLES = Seq("Nearby", "Saved")

    override def getCount: Int = TITLES.length

    override def getPageTitle(position: Int): CharSequence = TITLES(position)

    override def getItem(position: Int): SavedStopListFragment = position match {
      case 0 => new SavedStopListFragment()
      case 1 => new SavedStopListFragment()
    }

  }

}
