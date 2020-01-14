package com.example.wind.mycomic.utils;

import android.support.v17.leanback.widget.AbstractDetailsDescriptionPresenter;

/**
 * Created by wind on 2017/1/7.
 */

public class DescriptionPresenter extends AbstractDetailsDescriptionPresenter {

    private static final String TAG = DescriptionPresenter.class.getSimpleName();

    @Override
    protected void onBindDescription(ViewHolder viewHolder, Object item) {
        viewHolder.getTitle().setText(((PlayMovie) item).getVideo_title());
        viewHolder.getSubtitle().setText(((PlayMovie) item).getMovie_category());
    }
}