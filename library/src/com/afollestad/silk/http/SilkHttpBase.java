package com.afollestad.silk.http;

import android.os.Handler;
import org.apache.http.HttpResponse;
import org.apache.http.client.HttpClient;
import org.apache.http.client.methods.HttpUriRequest;
import org.apache.http.conn.ClientConnectionManager;
import org.apache.http.conn.scheme.PlainSocketFactory;
import org.apache.http.conn.scheme.Scheme;
import org.apache.http.conn.scheme.SchemeRegistry;
import org.apache.http.impl.client.DefaultHttpClient;
import org.apache.http.impl.conn.PoolingClientConnectionManager;

import java.util.ArrayList;
import java.util.List;

/**
 * @author Aidan Follestad (afollestad)
 */
class SilkHttpBase {

    public SilkHttpBase(Handler handler) {
        mHeaders = new ArrayList<SilkHttpHeader>();
        mHandler = handler;
        init();
    }

    public SilkHttpBase() {
        this(new Handler());
    }

    private void init() {
        SchemeRegistry registry = new SchemeRegistry();
        registry.register(new Scheme("http", 80, PlainSocketFactory.getSocketFactory()));
        ClientConnectionManager cm = new PoolingClientConnectionManager(registry);
        mClient = new DefaultHttpClient(cm);
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