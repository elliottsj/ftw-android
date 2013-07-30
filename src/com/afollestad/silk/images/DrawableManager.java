package com.afollestad.silk.images;

import android.content.Context;
import android.graphics.drawable.Drawable;
import android.net.Uri;
import android.os.Handler;
import android.os.Message;
import android.util.Log;
import android.widget.ImageView;
import org.apache.http.HttpResponse;
import org.apache.http.client.methods.HttpGet;
import org.apache.http.impl.client.DefaultHttpClient;

import java.io.File;
import java.io.FileInputStream;
import java.io.InputStream;
import java.util.HashMap;
import java.util.Map;

public class DrawableManager {

    private final Map<String, Drawable> drawableMap;

    public DrawableManager() {
        drawableMap = new HashMap<String, Drawable>();
    }

    public Drawable fetchDrawable(Context context, String uri) {
        if (drawableMap.containsKey(uri)) {
            return drawableMap.get(uri);
        }

        Log.d(this.getClass().getSimpleName(), "image url:" + uri);
        try {
            InputStream is = fetch(context, uri);
            Drawable drawable = Drawable.createFromStream(is, "src");


            if (drawable != null) {
                drawableMap.put(uri, drawable);
                Log.d(this.getClass().getSimpleName(), "got a thumbnail drawable: " + drawable.getBounds() + ", "
                        + drawable.getIntrinsicHeight() + "," + drawable.getIntrinsicWidth() + ", "
                        + drawable.getMinimumHeight() + "," + drawable.getMinimumWidth());
            } else {
                Log.w(this.getClass().getSimpleName(), "could not get thumbnail");
            }

            return drawable;
        } catch (Exception e) {
            Log.e(this.getClass().getSimpleName(), "fetchDrawable failed", e);
            return null;
        }
    }

    public void fetchDrawableOnThread(final Context context, final String uri, final ImageView imageView) {
        if (drawableMap.containsKey(uri)) {
            imageView.setImageDrawable(drawableMap.get(uri));
            return;
        }

        final Handler handler = new Handler() {
            @Override
            public void handleMessage(Message message) {
                imageView.setImageDrawable((Drawable) message.obj);
            }
        };

        Thread thread = new Thread() {
            @Override
            public void run() {
                //TODO : set imageView to a "pending" image
                Drawable drawable = fetchDrawable(context, uri);
                Message message = handler.obtainMessage(1, drawable);
                handler.sendMessage(message);
            }
        };
        thread.setPriority(Thread.MAX_PRIORITY);
        thread.start();
    }

    private InputStream fetch(Context context, String uri) throws Exception {
        InputStream is;
        if (uri.startsWith("http")) {
            DefaultHttpClient httpClient = new DefaultHttpClient();
            HttpGet request = new HttpGet(uri);
            HttpResponse response = httpClient.execute(request);
            is = response.getEntity().getContent();
        } else if (uri.startsWith("content")) {
            is = context.getContentResolver().openInputStream(Uri.parse(uri));
        } else {
            is = new FileInputStream(new File(uri));
        }
        return is;
    }
}