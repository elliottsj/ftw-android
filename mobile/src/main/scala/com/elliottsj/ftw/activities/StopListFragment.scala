package com.elliottsj.ftw.activities

import android.app.{Fragment, FragmentManager}
import android.os.Bundle
import android.support.v13.app.FragmentPagerAdapter
import android.support.v4.view.ViewPager
import android.support.v4.widget.DrawerLayout
import android.view._
import com.astuetz.PagerSlidingTabStrip
import com.elliottsj.ftw.R

class StopListFragment extends Fragment {

  protected override def onCreate(savedInstanceState: Bundle): Unit = {
    super.onCreate(savedInstanceState)


//    // Instantiate the RequestQueue
//    val queue: RequestQueue = Volley.newRequestQueue(this)
//    val url: String = "http://192.168.1.142:9000/agencies"
//
//    // Request a string response from the provided URL.
//    val byteRequest: ByteRequest = new ByteRequest(Request.Method.GET, url, new Listener[Array[Byte]] {
//      override def onResponse(response: Array[Byte]): Unit = {
//        val message = FeedMessage.parseFrom(response)
//        info("Received " + message.entity.size + " agencies")
//      }
//    }, new ErrorListener {
//      override def onErrorResponse(err: VolleyError): Unit = error(err.toString)
//    })
//    // Add the request to the RequestQueue.
//    queue.add(byteRequest)



  }


  override def onCreateView(inflater: LayoutInflater, container: ViewGroup, savedInstanceState: Bundle): View = {
    val rootView: View = inflater.inflate(R.layout.fragment_stop_list, container, false)

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
