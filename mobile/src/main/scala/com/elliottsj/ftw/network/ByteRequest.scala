package com.elliottsj.ftw.network

import com.android.volley.{NetworkResponse, Request, Response}
import com.android.volley.toolbox.HttpHeaderParser

/**
 * A canned request for retrieving the response body at a given URL as raw bytes.
 *
 * @param method the request { @link Method} to use
 * @param url URL to fetch the bytes at
 * @param listener Listener to receive the bytes response
 * @param errorListener Error listener, or null to ignore errors
 */
class ByteRequest(method: Int, url: String, listener: Response.Listener[Array[Byte]], errorListener: Response.ErrorListener)
  extends Request[Array[Byte]](method, url, errorListener) {

  private val mListener: Response.Listener[Array[Byte]] = listener

  protected def deliverResponse(response: Array[Byte]): Unit = mListener.onResponse(response)

  protected def parseNetworkResponse(response: NetworkResponse): Response[Array[Byte]] =
    Response.success(response.data, HttpHeaderParser.parseCacheHeaders(response))

}
