package com.afollestad.silk.http;

import android.text.Html;
import ch.boye.httpclientandroidlib.HttpResponse;
import ch.boye.httpclientandroidlib.StatusLine;
import ch.boye.httpclientandroidlib.util.EntityUtils;

import java.io.IOException;

/**
 * @author Aidan Follestad (afollestad)
 */
public class SilkHttpException extends Exception {

    SilkHttpException(Exception e) {
        super(e);
    }

    SilkHttpException(HttpResponse response) {
        mIsResponse = true;
        StatusLine stat = response.getStatusLine();
        mStatus = stat.getStatusCode();
        mReason = stat.getReasonPhrase();
        try {
            mBody = EntityUtils.toString(response.getEntity());
        } catch (IOException e) {
            e.printStackTrace();
            mBody = null;
        }
    }

    private int mStatus;
    private String mReason;
    private boolean mIsResponse;
    private String mBody;

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
        if (isServerResponse()) {
            String msg = getStatusCode() + " " + getReasonPhrase();
            if (mBody != null) msg += ":\n" + Html.fromHtml(mBody).toString();
            return msg;
        }
        return super.getMessage();
    }
}
