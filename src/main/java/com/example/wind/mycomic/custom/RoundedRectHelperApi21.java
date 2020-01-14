package com.example.wind.mycomic.custom;

/**
 * Created by wind on 2016/12/31.
 */
import android.graphics.Outline;
import android.util.SparseArray;
import android.view.ViewOutlineProvider;
import android.view.View;

final class RoundedRectHelperApi21 {

    private static SparseArray<ViewOutlineProvider> sRoundedRectProvider;
    private static final int MAX_CACHED_PROVIDER = 32;

    static final class RoundedRectOutlineProvider extends ViewOutlineProvider {

        private int mRadius;

        RoundedRectOutlineProvider(int radius) {
            mRadius = radius;
        }

        @Override
        public void getOutline(View view, Outline outline) {
            outline.setRoundRect(0, 0, view.getWidth(), view.getHeight(), mRadius);
            outline.setAlpha(1f);
        }
    };

    public static void setClipToRoundedOutline(View view, boolean clip, int roundedCornerRadius) {
        if (clip) {
            if (sRoundedRectProvider == null) {
                sRoundedRectProvider = new SparseArray<ViewOutlineProvider>();
            }
            ViewOutlineProvider provider = sRoundedRectProvider.get(roundedCornerRadius);
            if (provider == null) {
                provider = new RoundedRectHelperApi21.RoundedRectOutlineProvider(roundedCornerRadius);
                if (sRoundedRectProvider.size() < MAX_CACHED_PROVIDER) {
                    sRoundedRectProvider.put(roundedCornerRadius, provider);
                }
            }
            view.setOutlineProvider(provider);
        } else {
            view.setOutlineProvider(ViewOutlineProvider.BACKGROUND);
        }
        view.setClipToOutline(clip);
    }
}
