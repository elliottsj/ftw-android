package com.elliottsj.ftw.activities

import android.app.FragmentManager
import android.os.Bundle
import android.support.v13.app.FragmentPagerAdapter
import android.support.v4.view.ViewPager
import android.view.{Menu, MenuItem}
import com.astuetz.PagerSlidingTabStrip
import com.elliottsj.ftw.R
import org.scaloid.common.{Logger, SActivity}

class StopListActivity extends SActivity with Logger {

  protected override def onCreate(savedInstanceState: Bundle): Unit = {
    super.onCreate(savedInstanceState)
    setContentView(R.layout.activity_stop_list)

    // Get the ViewPager and set its PagerAdapter so that it can display items
    val viewPager = find[ViewPager](R.id.pager)
    viewPager.setAdapter(new StopsTabsAdapter(getFragmentManager))

    // Bind the tabs to the pager
    val tabs = find[PagerSlidingTabStrip](R.id.tabs)
    tabs.setViewPager(viewPager)

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
