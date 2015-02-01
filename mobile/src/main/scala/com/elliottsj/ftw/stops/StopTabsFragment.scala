package com.elliottsj.ftw.stops

import android.app.{Activity, Fragment, FragmentManager}
import android.os.Bundle
import android.support.v13.app.{FragmentStatePagerAdapter, FragmentPagerAdapter}
import android.support.v4.view.ViewPager
import android.view._
import com.astuetz.PagerSlidingTabStrip
import com.elliottsj.ftw.R

object StopTabsFragment {
  final val ARG_AGENCY = "com.elliottsj.ftw.AGENCY"
}

class StopTabsFragment extends Fragment {

  override def onCreateView(inflater: LayoutInflater, container: ViewGroup, savedInstanceState: Bundle): View = {
    // Enable the options menu
    setHasOptionsMenu(true)

    val rootView: View = inflater.inflate(R.layout.fragment_stop_tabs, container, false)

    // Get the ViewPager and set its PagerAdapter so that it can display items
    val viewPager = rootView.findViewById(R.id.pager).asInstanceOf[ViewPager]
    viewPager.setAdapter(new StopsTabsAdapter(getFragmentManager))

    // Bind the tabs to the pager
    val tabs = rootView.findViewById(R.id.tabs).asInstanceOf[PagerSlidingTabStrip]
    tabs.setViewPager(viewPager)

    rootView
  }

  override def onCreateOptionsMenu(menu: Menu, inflater: MenuInflater): Unit = {
    inflater.inflate(R.menu.stops, menu)
  }

  class StopsTabsAdapter(fm: FragmentManager) extends FragmentStatePagerAdapter(fm) {
    val TITLES = Seq("Nearby", "Saved")
    override def getCount: Int = TITLES.length
    override def getPageTitle(position: Int): CharSequence = TITLES(position)
    override def getItem(position: Int): Fragment = position match {
      case 0 => new SavedStopListFragment()
      case 1 => new SavedStopListFragment()
    }
  }

}
