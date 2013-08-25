package com.afollestad.silk.http;

import org.apache.http.HttpResponse;
import org.apache.http.StatusLine;

/**
 * @author Aidan Follestad (afollestad)
 */
public class SilkHttpException extends Exception {

    SilkHttpException(String msg) {
        super(msg);
    }

    SilkHttpException(Exception e) {
        super(e);
        mIsResponse = true;
    }

    SilkHttpException(HttpResponse response) {
        StatusLine stat = response.getStatusLine();
        mStatus = stat.getStatusCode();
        mReason = stat.getReasonPhrase();
    }

    private int mStatus;
    private String mReason;
    private boolean mIsResponse;

    /**
     * Gets the status code returned from the HTTP request, this will only be set if {@link #isServerResponse()} returns true;.
     */
    public int getStatusCode() {
        return mStatus;
    }

    /**
     * Gets the reason phrase for the value of {@link #getStatusCode()}. this will only be set if {@link #isServerResponse()} returns true.
     */
    public String getReasonPhrase() {
        return mReason;
    }

    /**
     * Gets whether or not this exception was thrown for a non-200 HTTP response code, or if it was thrown for a code level Exception.
     */
    public boolean isServerResponse() {
        return mIsResponse;
    }

    @Override
    public String getMessage() {
        if (isServerResponse())
            return getStatusCode() + " " + getReasonPhrase();
        return super.getMessage();
    }
}
