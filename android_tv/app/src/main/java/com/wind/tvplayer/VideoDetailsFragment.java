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

package com.wind.tvplayer;

import android.content.Context;
import android.content.Intent;
import android.os.Bundle;
import android.util.Log;
import android.view.MenuItem;

import androidx.annotation.Nullable;
import androidx.core.content.ContextCompat;
import androidx.leanback.app.DetailsFragment;
import androidx.leanback.widget.Action;
import androidx.leanback.widget.ArrayObjectAdapter;
import androidx.leanback.widget.ClassPresenterSelector;
import androidx.leanback.widget.DetailsOverviewRow;
import androidx.leanback.widget.FullWidthDetailsOverviewRowPresenter;
import androidx.leanback.widget.FullWidthDetailsOverviewSharedElementHelper;
import androidx.leanback.widget.OnActionClickedListener;
import androidx.leanback.widget.OnItemViewClickedListener;
import androidx.leanback.widget.Presenter;
import androidx.leanback.widget.Row;
import androidx.leanback.widget.RowPresenter;

import com.bumptech.glide.Glide;
import com.bumptech.glide.load.resource.drawable.GlideDrawable;
import com.bumptech.glide.request.animation.GlideAnimation;
import com.bumptech.glide.request.target.SimpleTarget;
import com.wind.tvplayer.common.BackgoundTask;
import com.wind.tvplayer.common.ShareVideo;
import com.wind.tvplayer.controller.parser.ExoPlayer.PlayerActivity;
import com.wind.tvplayer.controller.parser.Site;
import com.wind.tvplayer.model.video.Movie;
import com.wind.tvplayer.model.video.PlayMovie;
import com.wind.tvplayer.model.video.Video;

import java.util.ArrayList;
import java.util.Objects;

/*
 * LeanbackDetailsFragment extends DetailsFragment, a Wrapper fragment for leanback details screens.
 * It shows a detailed view of video and its meta plus related videos.
 */
public class VideoDetailsFragment extends DetailsFragment {
    private static final String TAG = "VideoDetailsFragment";
    private static final int DETAIL_THUMB_WIDTH = 274;
    private static final int DETAIL_THUMB_HEIGHT = 274;

    private Movie mSelectedMovie;
    private BackgoundTask backgoundTask;
    private ArrayList<Video> videoMoviesList = null;
    private ArrayObjectAdapter mAdapter;
    private ClassPresenterSelector mPresenterSelector;

    @Override
    public void onCreate(Bundle savedInstanceState) {
        Log.d(TAG, "onCreate DetailsFragment");
        super.onCreate(savedInstanceState);

        backgoundTask = new BackgoundTask(getActivity());

        final String site_type = (String) getActivity().getIntent().getSerializableExtra(DetailsActivity.MOVIE_CATEGORY);
        String movie_uuid = (String) getActivity().getIntent().getSerializableExtra(DetailsActivity.MOVIE_UUID);

        ArrayList<Movie> movieList = ShareVideo.getInstance().selectedSite.getMovie_list();
        for (int i = 0; i < movieList.size(); i++) {
            if (movieList.get(i).getUuid().compareTo(movie_uuid) == 0) {
                mSelectedMovie = movieList.get(i);
            }
        }

        Thread thread = new Thread(new Runnable() {
            @Override
            public void run() {
                String page_url = mSelectedMovie.getVideoPage();
                Site sitePageParser = new Site();
                videoMoviesList = sitePageParser.doParser(page_url, mSelectedMovie);
                ShareVideo.getInstance().playMovieList.clear();
                for(int i = 0; i < videoMoviesList.size(); i++) {
                    PlayMovie playMovie = new PlayMovie();
                    if (videoMoviesList.get(i).getSiteMovieList().size() > 0) {
                        com.wind.tvplayer.model.video.Site siteMovie = videoMoviesList.get(i).getSiteMovieList().get(0); // For those video only have one site.
                        playMovie.setMovie_title(mSelectedMovie.getTitle());
                        playMovie.setVideo_intro(mSelectedMovie.getDescription());
                        playMovie.setVideo_title(videoMoviesList.get(i).getVideoTitle());
                        playMovie.setVideo_url(siteMovie.getSiteLink());
                        playMovie.setMovie_categoryIndex(mSelectedMovie.getCategoryIndex());
                        playMovie.setVideo_type(mSelectedMovie.getType());
                        ShareVideo.getInstance().playMovieList.add(playMovie);
                    }
                }
            }
        });

        try {
            thread.start();
            thread.join();
            setupAdapter();
            setupDetailsOverviewRow();
            setupDetailsOverviewRowPresenter();
            setOnItemViewClickedListener(new ItemViewClickedListener());
        } catch (InterruptedException e) {
            e.printStackTrace();
        }
    }

    @Override
    public void onStop() {
        super.onStop();
    }

    @Override
    public void onDestroy() {
        super.onDestroy();
        backgoundTask.Destroy();
    }

    private void setupAdapter() {
        mPresenterSelector = new ClassPresenterSelector();
        mAdapter = new ArrayObjectAdapter(mPresenterSelector);
        setAdapter(mAdapter);
    }

    private void setupDetailsOverviewRow() {
        Log.d(TAG, "doInBackground: " + mSelectedMovie.toString());
        final DetailsOverviewRow row = new DetailsOverviewRow(mSelectedMovie);
        row.setImageDrawable(
                ContextCompat.getDrawable(getActivity(), R.drawable.default_background));
        int width = convertDpToPixel(getActivity().getApplicationContext(), DETAIL_THUMB_WIDTH);
        int height = convertDpToPixel(getActivity().getApplicationContext(), DETAIL_THUMB_HEIGHT);
        Glide.with(getActivity())
                .load(mSelectedMovie.getCardImageUrl())
                .centerCrop()
                .error(R.drawable.default_background)
                .into(new SimpleTarget<GlideDrawable>(width, height) {
                    @Override
                    public void onResourceReady(GlideDrawable resource,
                                                GlideAnimation<? super GlideDrawable>
                                                        glideAnimation) {
                        Log.d(TAG, "details overview card image url ready: " + resource);
                        row.setImageDrawable(resource);
                        mAdapter.notifyArrayItemRangeChanged(0, mAdapter.size());
                    }
                });
        ArrayObjectAdapter actionAdapter = new ArrayObjectAdapter();
        for (int i = 0; i < ShareVideo.getInstance().playMovieList.size(); i++) {
            PlayMovie playMovie = ShareVideo.getInstance().playMovieList.get(i);
            actionAdapter.add(new Action(i, playMovie.getVideo_title()));
        }
        row.setActionsAdapter(actionAdapter);
        mAdapter.add(row);
    }

    private void setupDetailsOverviewRowPresenter() {
        // Set detail background.
        FullWidthDetailsOverviewRowPresenter detailsPresenter =
                new FullWidthDetailsOverviewRowPresenter(new DetailsDescriptionPresenter());
        detailsPresenter.setBackgroundColor(
                ContextCompat.getColor(getActivity(), R.color.selected_background));
        // Hook up transition element.
        FullWidthDetailsOverviewSharedElementHelper sharedElementHelper =
                new FullWidthDetailsOverviewSharedElementHelper();
        sharedElementHelper.setSharedElementEnterTransition(
                getActivity(), DetailsActivity.SHARED_ELEMENT_NAME);
        detailsPresenter.setListener(sharedElementHelper);
        detailsPresenter.setParticipatingEntranceTransition(true);
        detailsPresenter.setOnActionClickedListener(new OnActionClickedListener() {
            @Override
            public void onActionClicked(Action action) {

            }
        });
        mPresenterSelector.addClassPresenter(DetailsOverviewRow.class, detailsPresenter);
    }

    private int convertDpToPixel(Context context, int dp) {
        float density = context.getResources().getDisplayMetrics().density;
        return Math.round((float) dp * density);
    }

    private final class ItemViewClickedListener implements OnItemViewClickedListener {
        private MenuItem preferExtensionDecodersMenuItem;
        private MenuItem randomAbrMenuItem;
        private MenuItem tunnelingMenuItem;

        @Override
        public void onItemClicked(
                Presenter.ViewHolder itemViewHolder,
                Object item,
                RowPresenter.ViewHolder rowViewHolder,
                Row row) {

            if (item instanceof Action) {
                Log.d(TAG, "Item: " + item.toString());
                Action action = (Action) item;
                String actionIndex = Objects.toString(action.getId(), null);
                Intent intent = new Intent(getActivity(), PlayerActivity.class);
                intent.putExtra(
                        PlayerActivity.PREFER_EXTENSION_DECODERS_EXTRA,
                        isNonNullAndChecked(preferExtensionDecodersMenuItem));
                String abrAlgorithm =
                        isNonNullAndChecked(randomAbrMenuItem)
                                ? PlayerActivity.ABR_ALGORITHM_RANDOM
                                : PlayerActivity.ABR_ALGORITHM_DEFAULT;
                intent.putExtra(PlayerActivity.ABR_ALGORITHM_EXTRA, abrAlgorithm);
                intent.putExtra(PlayerActivity.TUNNELING_EXTRA, isNonNullAndChecked(tunnelingMenuItem));
                intent.putExtra(DetailsActivity.PLAY_MOVIE_INDEX, actionIndex);
                startActivity(intent);
            }
        }

        private boolean isNonNullAndChecked(@Nullable MenuItem menuItem) {
            // Temporary workaround for layouts that do not inflate the options menu.
            return menuItem != null && menuItem.isChecked();
        }
    }
}
