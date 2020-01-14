package com.example.wind.mycomic.custom;

import android.os.Build;
import android.view.View;

/**
 * Created by wind on 2016/12/31.
 */

final class RoundedRectHelper {

    private final static RoundedRectHelper sInstance = new RoundedRectHelper();
    private final RoundedRectHelper.Impl mImpl;

    /**
     * Returns an instance of the helper.
     */
    public static RoundedRectHelper getInstance() {
        return sInstance;
    }

    public static boolean supportsRoundedCorner() {
        return Build.VERSION.SDK_INT >= 21;
    }

    /**
     * Sets or removes a rounded rectangle outline on the given view.
     */
    public void setClipToRoundedOutline(View view, boolean clip, int radius) {
        mImpl.setClipToRoundedOutline(view, clip, radius);
    }

    /**
     * Sets or removes a rounded rectangle outline on the given view.
     */
    public void setClipToRoundedOutline(View view, boolean clip) {
        mImpl.setClipToRoundedOutline(view, clip, view.getResources().getDimensionPixelSize(
                android.support.v17.leanback.R.dimen.lb_rounded_rect_corner_radius));
    }

    static interface Impl {
        public void setClipToRoundedOutline(View view, boolean clip, int radius);
    }

    /**
     * Implementation used prior to L.
     */
    private static final class StubImpl implements RoundedRectHelper.Impl {
        StubImpl() {
        }

        @Override
        public void setClipToRoundedOutline(View view, boolean clip, int radius) {
            // Not supported
        }
    }

    /**
     * Implementation used on api 21 (and above).
     */
    private static final class Api21Impl implements RoundedRectHelper.Impl {
        Api21Impl() {
        }

        @Override
        public void setClipToRoundedOutline(View view, boolean clip, int radius) {
            RoundedRectHelperApi21.setClipToRoundedOutline(view, clip, radius);
        }
    }

    private RoundedRectHelper() {
        if (supportsRoundedCorner()) {
            mImpl = new RoundedRectHelper.Api21Impl();
        } else {
            mImpl = new RoundedRectHelper.StubImpl();
        }
    }
}