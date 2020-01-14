package com.example.wind.mycomic;

import android.content.Intent;
import android.graphics.drawable.Drawable;
import android.os.Bundle;
import android.support.v17.leanback.app.BackgroundManager;
import android.support.v17.leanback.app.VerticalGridFragment;
import android.support.v17.leanback.widget.ArrayObjectAdapter;
import android.support.v17.leanback.widget.OnItemViewClickedListener;
import android.support.v17.leanback.widget.OnItemViewSelectedListener;
import android.support.v17.leanback.widget.Presenter;
import android.support.v17.leanback.widget.Row;
import android.support.v17.leanback.widget.RowPresenter;
import android.support.v17.leanback.widget.VerticalGridPresenter;
import android.util.DisplayMetrics;
import android.util.Log;

import com.bumptech.glide.Glide;
import com.bumptech.glide.load.resource.drawable.GlideDrawable;
import com.bumptech.glide.request.animation.GlideAnimation;
import com.bumptech.glide.request.target.SimpleTarget;
import com.example.wind.mycomic.custom.SeasonCardPresenter;
import com.example.wind.mycomic.object.Movie;
import com.example.wind.mycomic.object.SeasonMovie;

import java.util.ArrayList;

/**
 * Created by User on 2017/1/11.
 */

public class SeasonFragment extends android.support.v17.leanback.app.VerticalGridFragment {

    private static final String TAG = VerticalGridFragment.class.getSimpleName();
    private static final int BACKGROUND_UPDATE_DELAY = 300;
    private static final int NUM_COLUMNS = 4;

    private ArrayObjectAdapter mAdapter;

    private Drawable mDefaultBackground;
    private DisplayMetrics mMetrics;
    private BackgroundManager mBackgroundManager;
    private Movie mSelectedMovie;

    @Override
    public void onCreate(Bundle savedInstanceState) {
        Log.d(TAG, "onCreate");
        super.onCreate(savedInstanceState);

        prepareBackgroundManager();

        String site_type = (String) getActivity().getIntent().getSerializableExtra(DetailsActivity.MOVIE_CATEGORY);
        String movie_uuid = (String) getActivity().getIntent().getSerializableExtra(DetailsActivity.MOVIE_UUID);

        ArrayList<Movie> movieList = ShareDataClass.getInstance().movieList.get(site_type);
        for (int i = 0; i < movieList.size(); i++) {
            if (movieList.get(i).getUUID().compareTo(movie_uuid) == 0) {
                mSelectedMovie = movieList.get(i);
                break;
            }
        }

        //Fake a seasonMovie obj to make the seasonCardPresenter could work
        mSelectedMovie.getSeasonMovieList().clear();
        SeasonMovie seasonMovie = new SeasonMovie();
        seasonMovie.setSeasonImg(mSelectedMovie.getCardImageUrl());
        seasonMovie.setSeasonName(mSelectedMovie.getTitle());
        seasonMovie.setSeasonPageLink(mSelectedMovie.getVideoPage());
        mSelectedMovie.setSeasonMovieList(seasonMovie);

        //setTitle("");
        //setBadgeDrawable(getResources().getDrawable(R.drawable.app_icon_your_company));

        setupFragment();
        setupEventListeners();

        // it will move current focus to specified position. Comment out it to see the behavior.
        // setSelectedPosition(5);
    }

    private void prepareBackgroundManager() {
        mBackgroundManager = BackgroundManager.getInstance(getActivity());
        mBackgroundManager.attach(getActivity().getWindow());
        mDefaultBackground = getResources().getDrawable(R.drawable.default_background);
        mMetrics = new DisplayMetrics();
        getActivity().getWindowManager().getDefaultDisplay().getMetrics(mMetrics);
    }

    private void setupFragment() {
        VerticalGridPresenter gridPresenter = new VerticalGridPresenter();
        gridPresenter.setNumberOfColumns(NUM_COLUMNS);
        setGridPresenter(gridPresenter);

        mAdapter = new ArrayObjectAdapter(new SeasonCardPresenter());

        for(int i = 0; i < mSelectedMovie.getSeasonMovieList().size(); i++) {
            SeasonMovie seasonMovie = mSelectedMovie.getSeasonMovieList().get(i);
            seasonMovie.setSeasonImg(mSelectedMovie.getCardImageUrl());
            mAdapter.add(seasonMovie);
        }

        setAdapter(mAdapter);
    }

    private void setupEventListeners() {
        setOnItemViewClickedListener(new ItemViewClickedListener());
        setOnItemViewSelectedListener(new ItemViewSelectedListener());
    }

    protected void updateBackground(String uri) {
        Glide.with(getActivity())
                .load(uri)
                .centerCrop()
                .error(mDefaultBackground)
                .into(new SimpleTarget<GlideDrawable>(mMetrics.widthPixels, mMetrics.heightPixels) {
                    @Override
                    public void onResourceReady(GlideDrawable resource,
                                                GlideAnimation<? super GlideDrawable> glideAnimation) {
                        mBackgroundManager.setDrawable(resource);
                    }
                });
    }

    private final class ItemViewClickedListener implements OnItemViewClickedListener {
        @Override
        public void onItemClicked(Presenter.ViewHolder itemViewHolder, Object item,
                                  RowPresenter.ViewHolder rowViewHolder, Row row) {
            if (item instanceof SeasonMovie) {
                SeasonMovie movie = (SeasonMovie) item;

                Intent intent = new Intent(getActivity(), DetailsActivity.class);

                intent.putExtra(DetailsActivity.MOVIE_CATEGORY, mSelectedMovie.getCategory());
                intent.putExtra(DetailsActivity.MOVIE_UUID, mSelectedMovie.getUUID());
                intent.putExtra(DetailsActivity.MOVIE_SEASON_NAME, movie.getSeasonName());

                startActivity(intent);
            }
        }
    }

    private final class ItemViewSelectedListener implements OnItemViewSelectedListener {
        @Override
        public void onItemSelected(Presenter.ViewHolder itemViewHolder, Object item,
                                   RowPresenter.ViewHolder rowViewHolder, Row row) {
            if (item instanceof SeasonMovie) {
                //updateBackground(ShareDataClass.getInstance().default_fragment_background);
            }
        }
    }

}