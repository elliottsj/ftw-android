package com.afollestad.silk.images;

import android.content.Context;
import android.widget.ImageView;

/**
 * @author Aidan Follestad (afollestad)
 */
public class SilkImageView extends ImageView {

    public SilkImageView(Context context) {
        super(context);
    }

    private String mUrl;
    private SilkImageManager mImageManager;

    @Override
    protected void onSizeChanged(int w, int h, int oldw, int oldh) {
        /**
         * This method allows the view to wait until it has been measured (a view won't be measured until
         * right before it becomes visible, which is usually after your code first starts executing. This
         * insures that correct dimensions will be used for the image loading size to optimize memory.
         */
        super.onSizeChanged(w, h, oldw, oldh);
        load();
    }

    public void load() {
        if (getMeasuredWidth() == 0 || getMeasuredHeight() == 0) return;
        else if (mUrl == null || mUrl.trim().isEmpty()) return;
        else if (mImageManager == null) return;
        mImageManager.fetchImageOnThread(getContext(), mUrl, this);
    }

    public void setImageURL(String url) {
        mUrl = url;
    }

    public void setImageManager(SilkImageManager manager) {
        mImageManager = manager;
    }
}
