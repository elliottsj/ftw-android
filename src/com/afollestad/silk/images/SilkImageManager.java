package com.afollestad.silk.images;

import android.content.Context;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
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

public class SilkImageManager {

    private final Map<String, Bitmap> imageMap;

    public SilkImageManager() {
        imageMap = new HashMap<String, Bitmap>();
    }

    private void log(String message) {
        Log.d("SilkImageManager", message);
    }

    public Bitmap fetchImage(Context context, String uri, int reqWidth, int reqHeight) {
        if (imageMap.containsKey(uri)) {
            return imageMap.get(uri);
        }
        log("Fetching URL (" + reqWidth + ", " + reqHeight + "):" + uri);
        try {
            InputStream is = fetch(context, uri);
            final BitmapFactory.Options options = new BitmapFactory.Options();
            options.inJustDecodeBounds = true;
            BitmapFactory.decodeStream(is, null, options);
            options.inSampleSize = calculateInSampleSize(options, reqWidth, reqHeight);
            options.inJustDecodeBounds = false;

            Bitmap image = BitmapFactory.decodeStream(is, null, options);
            if (image != null) {
                imageMap.put(uri, image);
            } else {
                log("Could not get thumbnail");
            }

            return image;
        } catch (Exception e) {
            log("Failed to fetch: " + e.getMessage());
            return null;
        }
    }

    public void fetchImageOnThread(final Context context, final String uri, final ImageView imageView) {
        if (imageMap.containsKey(uri)) {
            imageView.setImageBitmap(imageMap.get(uri));
            return;
        }

        final Handler handler = new Handler() {
            @Override
            public void handleMessage(Message message) {
                imageView.setImageBitmap((Bitmap) message.obj);
            }
        };

        Thread thread = new Thread() {
            @Override
            public void run() {
                //TODO : set imageView to a "pending" image
                Bitmap drawable = fetchImage(context, uri, imageView.getWidth(), imageView.getHeight());
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

    public void clear() {
        imageMap.clear();
    }

    private int calculateInSampleSize(BitmapFactory.Options options, int reqWidth, int reqHeight) {
        // Raw height and width of image
        final int height = options.outHeight;
        final int width = options.outWidth;
        int inSampleSize = 1;

        if (height > reqHeight || width > reqWidth) {

            // Calculate ratios of height and width to requested height and width
            final int heightRatio = Math.round((float) height / (float) reqHeight);
            final int widthRatio = Math.round((float) width / (float) reqWidth);

            // Choose the smallest ratio as inSampleSize value, this will guarantee
            // a final image with both dimensions larger than or equal to the
            // requested height and width.
            inSampleSize = heightRatio < widthRatio ? heightRatio : widthRatio;
        }

        return inSampleSize;
    }
}