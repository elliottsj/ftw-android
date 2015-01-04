package com.elliottsj.ftw.agencies

import android.os.Bundle
import android.support.v7.widget.RecyclerView.Adapter
import android.support.v7.widget.{LinearLayoutManager, RecyclerView}
import android.view.{LayoutInflater, View, ViewGroup}
import com.android.volley.Response.{ErrorListener, Listener}
import com.android.volley.toolbox.Volley
import com.android.volley.{Request, RequestQueue, VolleyError}
import com.devspark.progressfragment.ProgressFragment
import com.elliottsj.ftw.R
import com.elliottsj.ftw.network.ByteRequest
import com.elliottsj.ftw.util.AsyncTaskContext
import com.elliottsj.protobus.{Agency, FeedMessage}
import org.scaloid.common.{Logger, TagUtil, runOnUiThread}

import scala.concurrent.{Future, Promise}

class AgencyListFragment extends ProgressFragment with TagUtil with Logger with AsyncTaskContext {

  private var mContentView: View = _
  private var mRootView: ViewGroup = _
  private var mRecyclerView: RecyclerView = _
  private var mLayoutManager: RecyclerView.LayoutManager = _

  override def onCreate(savedInstanceState: Bundle): Unit = {
    super.onCreate(savedInstanceState)
    mLayoutManager = new LinearLayoutManager(getActivity)
  }

  override def onCreateView(inflater: LayoutInflater, container: ViewGroup, savedInstanceState: Bundle): View = {
    // Inflate the content view without a parent ViewGroup
    mContentView = inflater.inflate(R.layout.fragment_agency_list, null)

    mRecyclerView = mContentView.findViewById(android.R.id.list).asInstanceOf[RecyclerView]
    mRecyclerView.setLayoutManager(mLayoutManager)

    super.onCreateView(inflater, container, savedInstanceState)
  }

  override def onActivityCreated(savedInstanceState: Bundle): Unit = {
    super.onActivityCreated(savedInstanceState)

    // Set the content view and empty text
    setContentView(mContentView)
    setEmptyText("No transit agencies available") // TODO: use string resource

    // Hide the content view while loading agencies
    setContentShown(false)

    // Load agencies
    loadAgencies() onSuccess { case agencies => runOnUiThread {
      setContentShown(true)
      mRecyclerView.setAdapter(new AgencyAdapter(getActivity, agencies))
    }}
  }

  def loadAgencies(): Future[Array[Agency]] = {
    val p = Promise[Array[Agency]]()

    // Instantiate the RequestQueue
    val queue: RequestQueue = Volley.newRequestQueue(getActivity)
    val url: String = "http://protobus.fasterthanwalking.com/agencies"

    // Request a string response from the provided URL.
    val byteRequest: ByteRequest = new ByteRequest(Request.Method.GET, url, new Listener[Array[Byte]] {
      override def onResponse(response: Array[Byte]): Unit = {
        val message = FeedMessage.parseFrom(response)
        info("Received " + message.entity.size + " agencies")

        val agencies = message.entity.map { e => e.getAgency }
        p success agencies.toArray
      }
    }, new ErrorListener {
      override def onErrorResponse(err: VolleyError): Unit = error(err.toString)
    })
    // Add the request to the RequestQueue.
    queue.add(byteRequest)

    p.future
  }

}
