package com.elliottsj.ftw.stops

import android.app.{Fragment, FragmentManager}
import android.os.Bundle
import android.support.v13.app.FragmentPagerAdapter
import android.support.v4.view.ViewPager
import android.view._
import com.astuetz.PagerSlidingTabStrip
import com.elliottsj.ftw.R

class StopTabsFragment extends Fragment {

  override def onCreateView(inflater: LayoutInflater, container: ViewGroup, savedInstanceState: Bundle): View = {
    val rootView: View = inflater.inflate(R.layout.fragment_stop_tabs, container, false)

    // Get the ViewPager and set its PagerAdapter so that it can display items
    val viewPager = rootView.findViewById(R.id.pager).asInstanceOf[ViewPager]
    viewPager.setAdapter(new StopsTabsAdapter(getFragmentManager))

    // Bind the tabs to the pager
    val tabs = rootView.findViewById(R.id.tabs).asInstanceOf[PagerSlidingTabStrip]
    tabs.setViewPager(viewPager)

    rootView
  }

  class StopsTabsAdapter(fm: FragmentManager) extends FragmentPagerAdapter(fm) {
    val TITLES = Seq("Nearby", "Saved")
    override def getCount: Int = TITLES.length
    override def getPageTitle(position: Int): CharSequence = TITLES(position)
    override def getItem(position: Int): Fragment = position match {
      case 0 => new SavedStopListFragment()
      case 1 => new SavedStopListFragment()
    }
  }

}
