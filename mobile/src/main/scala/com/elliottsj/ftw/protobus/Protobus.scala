package com.elliottsj.ftw.protobus

import android.content.Context
import com.android.volley.Response.{ErrorListener, Listener}
import com.android.volley.toolbox.Volley
import com.android.volley.{Request, RequestQueue, VolleyError}
import com.elliottsj.ftw.R
import com.elliottsj.ftw.network.ByteRequest
import com.elliottsj.ftw.util.AsyncTaskContext
import com.elliottsj.protobus.{Stop, Agency, FeedMessage}

import scala.concurrent.{Future, Promise}
import scala.util.Try

/**
 * A lightweight wrapper for calling the Protobus HTTP API
 *
 * @param context a context from which to construct a request queue
 */
class Protobus(context: Context) extends AsyncTaskContext {
  final val API_HOST = context.getString(R.string.api_host)

  // Instantiate the RequestQueue
  val queue: RequestQueue = Volley.newRequestQueue(context)

  private def get(path: String): Future[FeedMessage] = {
    val p = Promise[FeedMessage]()

    // Add a byte request to the RequestQueue
    queue.add(new ByteRequest(Request.Method.GET, API_HOST + path, new Listener[Array[Byte]] {
      override def onResponse(response: Array[Byte]): Unit = p complete Try(FeedMessage.parseFrom(response))
    }, new ErrorListener {
      override def onErrorResponse(err: VolleyError): Unit = p failure err
    }))

    p.future
  }

  def getAgencies: Future[Array[Agency]] =
    for(message <- get("/agencies")) yield
      message.entities.map(_.getAgency).toArray

  def getStops(agency: Agency): Future[Array[Stop]] =
    for(message <- get(s"/agencies/${agency.getNextbusFields.agencyTag}")) yield
      message.entities.map(_.getStop).toArray
}

object Protobus {
  def apply(context: Context) = new Protobus(context)
}
