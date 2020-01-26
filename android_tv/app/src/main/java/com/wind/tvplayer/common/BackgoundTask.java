package com.wind.tvplayer.common;

import android.app.Activity;
import android.graphics.drawable.Drawable;
import android.os.Handler;
import android.util.DisplayMetrics;

import androidx.leanback.app.BackgroundManager;

import com.bumptech.glide.Glide;
import com.bumptech.glide.load.resource.drawable.GlideDrawable;
import com.bumptech.glide.request.animation.GlideAnimation;
import com.bumptech.glide.request.target.SimpleTarget;
import com.wind.tvplayer.R;

import java.net.URI;
import java.net.URISyntaxException;
import java.util.Timer;
import java.util.TimerTask;

public class BackgoundTask {
    private Activity activity;
    private final Handler mHandler = new Handler();
    private Drawable mDefaultBackground;
    private DisplayMetrics mMetrics;
    private Timer mBackgroundTimer;
    private URI mBackgroundURI;
    private BackgroundManager mBackgroundManager;

    private int BACKGROUND_UPDATE_DELAY = 300;

    public BackgoundTask(Activity activity) {
        this.activity = activity;
        this.prepareBackgroundManager();
    }

    private void prepareBackgroundManager() {
        mBackgroundManager = BackgroundManager.getInstance(activity);
        mBackgroundManager.attach(activity.getWindow());
        mDefaultBackground = activity.getResources().getDrawable(R.drawable.default_background);
        mMetrics = new DisplayMetrics();
        activity.getWindowManager().getDefaultDisplay().getMetrics(mMetrics);
    }

    public void Destroy() {
        if (null != mBackgroundTimer) {
            mBackgroundTimer.cancel();
        }
    }

    private class ThreadBackgoundTask extends TimerTask {
        @Override
        public void run() {
            mHandler.post(new Runnable() {
                @Override
                public void run() {
                    if (mBackgroundURI != null) {
                        updateBackground(mBackgroundURI.toString());
                    }
                }
            });
        }
    }

    private void updateBackground(String uri) {
        int width = mMetrics.widthPixels;
        int height = mMetrics.heightPixels;
        Glide.with(activity)
                .load(uri)
                .centerCrop()
                .error(mDefaultBackground)
                .into(new SimpleTarget<GlideDrawable>(width, height) {
                    @Override
                    public void onResourceReady(GlideDrawable resource,
                                                GlideAnimation<? super GlideDrawable>
                                                        glideAnimation) {
                        mBackgroundManager.setDrawable(resource);
                    }
                });
        mBackgroundTimer.cancel();
    }

    public void startBackgroundTimer(String uri_str) {
        try {
            URI uri = new URI(uri_str);
            startBackgroundTimer(uri);
        } catch (URISyntaxException e) {
            //
        }
    }

    public void startBackgroundTimer(URI uri) {
        this.mBackgroundURI = uri;

        if (null != mBackgroundTimer) {
            mBackgroundTimer.cancel();
        }
        mBackgroundTimer = new Timer();
        mBackgroundTimer.schedule(new ThreadBackgoundTask(), BACKGROUND_UPDATE_DELAY);
    }
}
