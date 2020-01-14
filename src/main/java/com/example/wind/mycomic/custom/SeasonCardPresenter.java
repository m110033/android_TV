package com.example.wind.mycomic.custom;

import android.graphics.drawable.Drawable;
import android.support.v17.leanback.widget.Presenter;
import android.util.Log;
import android.view.ViewGroup;

import com.bumptech.glide.Glide;
import com.example.wind.mycomic.R;
import com.example.wind.mycomic.object.SeasonMovie;

/**
 * Created by wind on 2017/1/13.
 */

public class SeasonCardPresenter extends Presenter {
    private static final String TAG = "CardPresenter";

    private static final int CARD_WIDTH = 313;
    private static final int CARD_HEIGHT = 176;
    private static int sSelectedBackgroundColor;
    private static int sDefaultBackgroundColor;
    private static int sHitsHighTextColor;
    private static int sHitsMediumTextColor;
    private static int sHitsLowTextColor;
    private Drawable mDefaultCardImage;

    private static void updateCardBackgroundColor(CustomImageCardView view, boolean selected) {
        int color = selected ? sSelectedBackgroundColor : sDefaultBackgroundColor;
        // Both background colors should be set because the view's background is temporarily visible
        // during animations.
        view.setBackgroundColor(color);
        view.findViewById(R.id.info_field).setBackgroundColor(color);
    }

    @Override
    public ViewHolder onCreateViewHolder(ViewGroup parent) {
        Log.d(TAG, "onCreateViewHolder");

        sHitsHighTextColor = parent.getResources().getColor(R.color.high_viewnumber);
        sHitsMediumTextColor = parent.getResources().getColor(R.color.medium_viewnumber);
        sHitsLowTextColor = parent.getResources().getColor(R.color.low_viewnumber);
        sDefaultBackgroundColor = parent.getResources().getColor(R.color.default_background);
        sSelectedBackgroundColor = parent.getResources().getColor(R.color.selected_background);
        mDefaultCardImage = parent.getResources().getDrawable(R.drawable.movie);

        CustomImageCardView cardView = new CustomImageCardView(parent.getContext()) {
            @Override
            public void setSelected(boolean selected) {
                updateCardBackgroundColor(this, selected);
                super.setSelected(selected);
            }
        };

        cardView.setFocusable(true);
        cardView.setFocusableInTouchMode(true);
        updateCardBackgroundColor(cardView, false);
        return new ViewHolder(cardView);
    }

    @Override
    public void onBindViewHolder(Presenter.ViewHolder viewHolder, Object item) {
        SeasonMovie movie = (SeasonMovie) item;
        CustomImageCardView cardView = (CustomImageCardView) viewHolder.view;

        if (movie.getSeasonImg() != null) {
            cardView.hideHits();
            // Others
            cardView.setTitleText(movie.getSeasonName());
            cardView.setContentText("");
            cardView.setMainImageDimensions(CARD_WIDTH, CARD_HEIGHT);
            Glide.with(viewHolder.view.getContext())
                    .load(movie.getSeasonImg())
                    .centerCrop()
                    .error(mDefaultCardImage)
                    .into(cardView.getMainImageView());
        }
    }

    @Override
    public void onUnbindViewHolder(Presenter.ViewHolder viewHolder) {
        Log.d(TAG, "onUnbindViewHolder");
        CustomImageCardView cardView = (CustomImageCardView) viewHolder.view;
        // Remove references to images so that the garbage collector can free up memory
        cardView.setBadgeImage(null);
        cardView.setMainImage(null);
    }
}
