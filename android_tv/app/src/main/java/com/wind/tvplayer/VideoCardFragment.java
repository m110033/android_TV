package com.wind.tvplayer;

import android.content.Intent;
import android.graphics.drawable.Drawable;
import android.os.Bundle;
import android.util.DisplayMetrics;
import android.util.Log;

import androidx.leanback.app.BackgroundManager;
import androidx.leanback.app.VerticalGridFragment;
import androidx.leanback.widget.ArrayObjectAdapter;
import androidx.leanback.widget.OnItemViewClickedListener;
import androidx.leanback.widget.OnItemViewSelectedListener;
import androidx.leanback.widget.Presenter;
import androidx.leanback.widget.Row;
import androidx.leanback.widget.RowPresenter;
import androidx.leanback.widget.VerticalGridPresenter;

import com.wind.tvplayer.common.ShareVideo;
import com.wind.tvplayer.model.video.Movie;

import java.util.ArrayList;

public class VideoCardFragment extends VerticalGridFragment {

    private static final String TAG = VerticalGridFragment.class.getSimpleName();
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

        String movie_uuid = (String) getActivity().getIntent().getSerializableExtra(DetailsActivity.MOVIE_UUID);

        ArrayList<Movie> movieList = ShareVideo.selectedSite.getMovie_list();

        for (int i = 0; i < movieList.size(); i++) {
            if (movieList.get(i).getUuid().compareTo(movie_uuid) == 0) {
                mSelectedMovie = movieList.get(i);
                break;
            }
        }

        setupFragment();
        setupEventListeners();
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
        mAdapter = new ArrayObjectAdapter(new CardPresenter());
        Movie movie = new Movie();
        movie.setCardImageUrl(mSelectedMovie.getCardImageUrl());
        movie.setTitle(mSelectedMovie.getTitle());
        // movie.setBackgroundImageUrl(mSelectedMovie.getVideoPage());
        mAdapter.add(movie);
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
            if (item instanceof Movie && mSelectedMovie != null) {
                Intent intent = new Intent(getActivity(), DetailsActivity.class);
                intent.putExtra(DetailsActivity.MOVIE_CATEGORY, mSelectedMovie.getCategory());
                intent.putExtra(DetailsActivity.MOVIE_UUID, mSelectedMovie.getUuid());
                startActivity(intent);
            }
        }
    }

    private final class ItemViewSelectedListener implements OnItemViewSelectedListener {
        @Override
        public void onItemSelected(Presenter.ViewHolder itemViewHolder, Object item,
                                   RowPresenter.ViewHolder rowViewHolder, Row row) {
            if (item instanceof Movie) {
                // TBD...
            }
        }
    }

}