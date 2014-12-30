package com.elliottsj.ftw.network;

import com.android.volley.AuthFailureError;
import com.android.volley.NetworkResponse;
import com.android.volley.Request;
import com.android.volley.Response;
import com.android.volley.Response.ErrorListener;
import com.android.volley.Response.Listener;
import com.android.volley.toolbox.HttpHeaderParser;

import java.util.Map;

/**
 * A canned request for retrieving the response body at a given URL as raw bytes.
 */
public class ByteRequest extends Request<byte[]> {

    private final Listener<byte[]> mListener;
    private Map<String, String> mPostParams;

    /**
     * Creates a new request with the given method.
     *
     * @param method the request {@link Method} to use
     * @param url URL to fetch the bytes at
     * @param listener Listener to receive the bytes response
     * @param errorListener Error listener, or null to ignore errors
     */
    public ByteRequest(int method, String url, Listener<byte[]> listener, ErrorListener errorListener) {
        super(method, url, errorListener);
        mListener = listener;
    }

    /**
     * Creates a new POST request.
     *
     * @param url URL to fetch the bytes at
     * @param postParams post parameters
     * @param listener Listener to receive the bytes response
     * @param errorListener Error listener, or null to ignore errors
     */
    public ByteRequest(String url, Map<String, String> postParams, Listener<byte[]> listener, ErrorListener errorListener) {
        this(Method.POST, url, listener, errorListener);
        mPostParams = postParams;
    }

    /**
     * Creates a new GET request.
     *
     * @param url URL to fetch the bytes at
     * @param listener Listener to receive the bytes response
     * @param errorListener Error listener, or null to ignore errors
     */
    public ByteRequest(String url, Listener<byte[]> listener, ErrorListener errorListener) {
        this(Method.GET, url, listener, errorListener);
    }

    @Override
    protected Map<String, String> getParams() throws AuthFailureError {
        return mPostParams;
    }

    @Override
    protected void deliverResponse(byte[] response) {
        mListener.onResponse(response);
    }

    @Override
    protected Response<byte[]> parseNetworkResponse(NetworkResponse response) {
        return Response.success(response.data, HttpHeaderParser.parseCacheHeaders(response));
    }
}