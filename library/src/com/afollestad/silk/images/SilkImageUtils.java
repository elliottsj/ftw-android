package com.afollestad.silk.images;

import android.graphics.Bitmap;
import android.graphics.BitmapFactory;

import java.io.UnsupportedEncodingException;
import java.net.URLEncoder;

public class SilkImageUtils {

    public static int calculateInSampleSize(BitmapFactory.Options options, Dimension dimension) {
        // Raw height and width of image
        final int height = options.outHeight;
        final int width = options.outWidth;
        int inSampleSize = 1;

        if (height > dimension.getHeight() || width > dimension.getWidth()) {

            // Calculate ratios of height and width to requested height and width
            final int heightRatio = Math.round((float) height / (float) dimension.getHeight());
            final int widthRatio = Math.round((float) width / (float) dimension.getWidth());

            // Choose the smallest ratio as inSampleSize value, this will guarantee
            // a final image with both dimensions larger than or equal to the
            // requested height and width.
            inSampleSize = heightRatio < widthRatio ? heightRatio : widthRatio;
        }

        return inSampleSize;
    }

    public static BitmapFactory.Options getBitmapFactoryOptions(Dimension dimension) {
        BitmapFactory.Options options = new BitmapFactory.Options();
        options.inPurgeable = true;
        options.inInputShareable = true;
        options.inPreferredConfig = Bitmap.Config.ARGB_8888;
        if (dimension != null)
            options.inSampleSize = calculateInSampleSize(options, dimension);
        return options;
    }

    public static Bitmap decodeByteArray(byte[] byteArray, Dimension dimension) {
        try {
            BitmapFactory.Options bitmapFactoryOptions = getBitmapFactoryOptions(dimension);
            return BitmapFactory.decodeByteArray(byteArray, 0, byteArray.length, bitmapFactoryOptions);
        } catch (Throwable t) {
            t.printStackTrace();
        }
        return null;
    }

    public static String getKey(String source, Dimension dimension) {
        if (source == null) {
            return null;
        }
        if (dimension != null)
            source += "_" + dimension.toString();
        try {
            return URLEncoder.encode(source, "UTF-8");
        } catch (UnsupportedEncodingException e) {
            e.printStackTrace();
        }
        return null;
    }
}
