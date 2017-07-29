package com.example.wind.mycomic.custom;
import com.example.wind.mycomic.R;

import android.content.Context;
import android.support.annotation.ColorInt;
import android.support.v17.leanback.widget.ImageCardView;
import android.util.AttributeSet;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;

/**
 * Created by wind on 2017/1/2.
 */

public class CustomImageCardView extends ImageCardView {
    private ViewGroup mInfoArea;
    private TextView mHitsView;

    public CustomImageCardView(Context context) {
        this(context, null);
    }

    public CustomImageCardView(Context context, AttributeSet attrs) {
        this(context, attrs, R.attr.imageCardViewStyle);
    }

    public CustomImageCardView(Context context, AttributeSet attrs, int defStyle) {
        super(context, attrs, defStyle);

        LayoutInflater inflater = LayoutInflater.from(getContext());

        mInfoArea = (ViewGroup) findViewById(R.id.info_field);
        mHitsView = (TextView) inflater.inflate(R.layout.view_number,
                mInfoArea, false);
        mInfoArea.addView(mHitsView);
    }

    /**
     * Sets the hists text.
     */
    public void setHitsText(CharSequence text) {
        if (mHitsView == null) {
            return;
        }
        mHitsView.setText(text);
    }

    /**
     * Returns the hists text.
     */
    public CharSequence getHitsText() {
        if (mHitsView == null) {
            return null;
        }
        return mHitsView.getText();
    }

    /**
     * Sets the info area background color.
     */
    public void setHitsColor(@ColorInt int color) {
        if (mInfoArea != null) {
            mHitsView.setTextColor(color);
        }
    }

    public void showHits() {
        if(mHitsView != null) {
            mHitsView.setVisibility(View.VISIBLE);
        }
    }

    public void hideHits() {
        if(mHitsView != null) {
            mHitsView.setVisibility(View.GONE);
        }
    }
}
