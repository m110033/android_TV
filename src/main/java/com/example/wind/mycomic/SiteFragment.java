package com.example.wind.mycomic;

import android.content.Intent;
import android.graphics.drawable.Drawable;
import android.os.Bundle;
import android.support.v17.leanback.app.VerticalGridFragment;
import android.support.v17.leanback.widget.ArrayObjectAdapter;
import android.support.v17.leanback.widget.OnItemViewClickedListener;
import android.support.v17.leanback.widget.OnItemViewSelectedListener;
import android.support.v17.leanback.widget.Presenter;
import android.support.v17.leanback.widget.Row;
import android.support.v17.leanback.widget.RowPresenter;
import android.support.v17.leanback.widget.VerticalGridPresenter;
import android.util.Log;
import android.view.ViewGroup;

import com.bumptech.glide.Glide;
import com.example.wind.mycomic.custom.BackgoundTask;
import com.example.wind.mycomic.custom.CustomImageCardView;
import com.example.wind.mycomic.service.AppUtils;


/**
 * Created by wind on 2017/1/14.
 */

public class SiteFragment extends VerticalGridFragment {
    private static final String TAG = VerticalGridFragment.class.getSimpleName();
    private static final int BACKGROUND_UPDATE_DELAY = 300;
    private static final int NUM_COLUMNS = 4;

    private ArrayObjectAdapter mAdapter;
    private BackgoundTask backgoundTask;

    @Override
    public void onCreate(Bundle savedInstanceState) {
        Log.d(TAG, "onCreate");
        super.onCreate(savedInstanceState);

        backgoundTask = new BackgoundTask(getActivity());

        Integer versionCode = AppUtils.getVersionCode(this.getActivity().getApplicationContext());

        setTitle("Version: " + versionCode.toString());
        //setBadgeDrawable(getResources().getDrawable(R.drawable.app_icon_your_company));

        setupFragment();
        setupEventListeners();

        // it will move current focus to specified position. Comment out it to see the behavior.
        // setSelectedPosition(5);
        backgoundTask.startBackgroundTimer(ShareDataClass.getInstance().default_fragment_background);
    }

    @Override
    public void onDestroy() {
        super.onDestroy();
        backgoundTask.Destroy();
    }

    private void setupFragment() {
        VerticalGridPresenter gridPresenter = new VerticalGridPresenter();
        gridPresenter.setNumberOfColumns(NUM_COLUMNS);
        setGridPresenter(gridPresenter);

        mAdapter = new ArrayObjectAdapter(new CardPresenter());

        for(int i = 0; i < ShareDataClass.getInstance().site_json_list.length; i++) {
            MovieCard movieCard = new MovieCard();
            movieCard.setTitle(ShareDataClass.getInstance().site_name_list[i]);
            movieCard.setSub_title("");
            movieCard.setImg(ShareDataClass.getInstance().site_img_list[i]);
            movieCard.setMoveIndex(i);

            mAdapter.add(movieCard);
        }

        setAdapter(mAdapter);
    }

    private void setupEventListeners() {
        setOnItemViewClickedListener(new ItemViewClickedListener());
        setOnItemViewSelectedListener(new ItemViewSelectedListener());
    }

    private final class ItemViewClickedListener implements OnItemViewClickedListener {
        @Override
        public void onItemClicked(Presenter.ViewHolder itemViewHolder, Object item,
                                  RowPresenter.ViewHolder rowViewHolder, Row row) {
            if (item instanceof MovieCard) {
                MovieCard movie = (MovieCard) item;

                Intent intent = new Intent(getActivity(), MainActivity.class);

                intent.putExtra(DetailsActivity.MOVIE_UUID, movie.getMoveIndex());

                startActivity(intent);
            }
        }
    }

    private final class ItemViewSelectedListener implements OnItemViewSelectedListener {
        @Override
        public void onItemSelected(Presenter.ViewHolder itemViewHolder, Object item,
                                   RowPresenter.ViewHolder rowViewHolder, Row row) {
            if (item instanceof MovieCard) {
                //backgoundTask.startBackgroundTimer(ShareDataClass.getInstance().default_fragment_background);
            }
        }
    }

    private class MovieCard {
        public String getTitle() {
            return title;
        }

        public void setTitle(String title) {
            this.title = title;
        }

        public String getImg() {
            return img;
        }

        public void setImg(String img) {
            this.img = img;
        }

        public String getSub_title() {
            return sub_title;
        }

        public void setSub_title(String sub_title) {
            this.sub_title = sub_title;
        }

        public int getMoveIndex() {
            return moveIndex;
        }

        public void setMoveIndex(int moveIndex) {
            this.moveIndex = moveIndex;
        }

        private int moveIndex;
        private String title;
        private String sub_title;
        private String img;
    }

    private class CardPresenter extends Presenter {
        private static final String TAG = "CardPresenter";

        private final int CARD_WIDTH = 313;
        private final int CARD_HEIGHT = 176;
        private int sSelectedBackgroundColor;
        private int sDefaultBackgroundColor;
        private Drawable mDefaultCardImage;

        private void updateCardBackgroundColor(CustomImageCardView view, boolean selected) {
            int color = selected ? sSelectedBackgroundColor : sDefaultBackgroundColor;
            // Both background colors should be set because the view's background is temporarily visible
            // during animations.
            view.setBackgroundColor(color);
            view.findViewById(R.id.info_field).setBackgroundColor(color);
        }

        @Override
        public ViewHolder onCreateViewHolder(ViewGroup parent) {
            Log.d(TAG, "onCreateViewHolder");
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
            MovieCard movie = (MovieCard) item;
            CustomImageCardView cardView = (CustomImageCardView) viewHolder.view;

            if (movie.getImg() != null) {
                cardView.hideHits();

                // Others
                cardView.setTitleText(movie.getTitle());
                cardView.setContentText(movie.getSub_title());
                cardView.setMainImageDimensions(CARD_WIDTH, CARD_HEIGHT);
                Glide.with(viewHolder.view.getContext())
                        .load(movie.getImg())
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
}
