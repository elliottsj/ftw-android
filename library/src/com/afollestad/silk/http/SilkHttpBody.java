package com.afollestad.silk.http;

import org.apache.http.HttpEntity;
import org.apache.http.entity.*;
import org.json.JSONObject;

import java.io.File;
import java.io.InputStream;
import java.io.Serializable;

/**
 * Represents the content of a POST or PUT request.
 *
 * @author Aidan Follestad (afollestad)
 */
public class SilkHttpBody {

    public SilkHttpBody(byte[] bytes) {
        mEntity = new ByteArrayEntity(bytes);
    }

    public SilkHttpBody(File file, String contentType) {
        mEntity = new FileEntity(file, contentType);
    }

    public SilkHttpBody(InputStream stream, long length) {
        mEntity = new InputStreamEntity(stream, length);
    }

    public SilkHttpBody(Serializable serializable, boolean bufferize) throws Exception {
        mEntity = new SerializableEntity(serializable, bufferize);
    }

    public SilkHttpBody(String body, String charset) throws Exception {
        mEntity = new StringEntity(body, charset);
    }

    public SilkHttpBody(JSONObject json) throws Exception {
        this(json.toString(), "UTF-8");
    }

    private HttpEntity mEntity;

    public HttpEntity getEntity() {
        return mEntity;
    }
}
