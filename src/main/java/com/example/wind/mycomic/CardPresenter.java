/*
 * Copyright (C) 2014 The Android Open Source Project
 *
 * Licensed under the Apache License, Version 2.0 (the "License"); you may not use this file except
 * in compliance with the License. You may obtain a copy of the License at
 *
 * http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software distributed under the License
 * is distributed on an "AS IS" BASIS, WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express
 * or implied. See the License for the specific language governing permissions and limitations under
 * the License.
 */

package com.example.wind.mycomic;

import android.graphics.drawable.Drawable;
import android.support.v17.leanback.widget.Presenter;
import android.util.Log;
import android.view.ViewGroup;

import com.bumptech.glide.Glide;
import com.example.wind.mycomic.custom.CustomImageCardView;
import com.example.wind.mycomic.object.Movie;

/*
 * A CardPresenter is used to generate Views and bind Objects to them on demand.
 * It contains an Image CardView
 */
public class CardPresenter extends Presenter {
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
        Movie movie = (Movie) item;
        CustomImageCardView cardView = (CustomImageCardView) viewHolder.view;

        if (movie.getCardImageUrl() != null) {
            // Set Hist text
            if(movie.getViewNumber().compareTo("0") == 0) {
                cardView.hideHits();
            } else {
                cardView.showHits();
                cardView.setHitsText("查看數: " + movie.getViewNumber());
            }
            setHitsColor(movie.getCategory(), Integer.parseInt(movie.getViewNumber()), cardView);
            // Others
            cardView.setTitleText(movie.getTitle());
            cardView.setContentText(movie.getStudio());
            cardView.setMainImageDimensions(CARD_WIDTH, CARD_HEIGHT);
            Glide.with(viewHolder.view.getContext())
                    .load(movie.getCardImageUrl())
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

    private void setHitsColor(String movie_type, int numberOfHits, CustomImageCardView cardView) {
        double maxHits = ShareDataClass.getInstance().movieMaxHiits.get(movie_type).doubleValue();
        double[] hits_level = {
                maxHits * 0.7,
                maxHits * 0.3
        };

        if(numberOfHits >= hits_level[0]) {
            cardView.setHitsColor(sHitsHighTextColor);
        } else if(numberOfHits < hits_level[0] && numberOfHits >= hits_level[1]) {
            cardView.setHitsColor(sHitsMediumTextColor);
        } else if(numberOfHits < hits_level[1]) {
            cardView.setHitsColor(sHitsLowTextColor);
        }
    }
}
