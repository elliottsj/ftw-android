package com.afollestad.silk.http;

import android.os.Handler;
import org.apache.http.HttpResponse;
import org.apache.http.client.HttpClient;
import org.apache.http.client.methods.HttpUriRequest;
import org.apache.http.impl.client.DefaultHttpClient;

import java.util.ArrayList;
import java.util.List;

/**
 * @author Aidan Follestad (afollestad)
 */
class SilkHttpBase {

    public SilkHttpBase(Handler handler) {
        mClient = new DefaultHttpClient();
        mHeaders = new ArrayList<SilkHttpHeader>();
        mHandler = handler;
    }

    public SilkHttpBase() {
        this(new Handler());
    }

    private HttpClient mClient;
    protected List<SilkHttpHeader> mHeaders;
    private Handler mHandler;

    protected void reset() {
        mHeaders.clear();
    }

    protected Handler getHandler() {
        return mHandler;
    }

    protected void runOnPriorityThread(Runnable runnable) {
        Thread t = new Thread(runnable);
        t.setPriority(Thread.MAX_PRIORITY);
        t.start();
    }

    protected SilkHttpResponse performRequest(final HttpUriRequest request) throws SilkHttpException {
        if (mHeaders.size() > 0) {
            for (SilkHttpHeader header : mHeaders)
                request.setHeader(header.getName(), header.getValue());
        }
        HttpResponse response;
        try {
            response = mClient.execute(request);
        } catch (Exception e) {
            reset();
            throw new SilkHttpException(e);
        }
        int status = response.getStatusLine().getStatusCode();
        if (status != 200) {
            reset();
            throw new SilkHttpException(response);
        }
        reset();
        return new SilkHttpResponse(response);
    }
}