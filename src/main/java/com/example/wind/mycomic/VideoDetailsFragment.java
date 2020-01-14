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

import android.app.AlertDialog;
import android.content.DialogInterface;
import android.content.Intent;
import android.os.Bundle;
import android.support.v17.leanback.app.DetailsFragment;
import android.support.v17.leanback.widget.ArrayObjectAdapter;
import android.support.v17.leanback.widget.ClassPresenterSelector;
import android.support.v17.leanback.widget.HeaderItem;
import android.support.v17.leanback.widget.ListRow;
import android.support.v17.leanback.widget.ListRowPresenter;
import android.support.v17.leanback.widget.OnItemViewClickedListener;
import android.support.v17.leanback.widget.OnItemViewSelectedListener;
import android.support.v17.leanback.widget.Presenter;
import android.support.v17.leanback.widget.Row;
import android.support.v17.leanback.widget.RowPresenter;
import android.support.v7.view.ContextThemeWrapper;
import android.util.Log;
import android.view.View;

import com.bumptech.glide.Glide;
import com.bumptech.glide.load.resource.drawable.GlideDrawable;
import com.bumptech.glide.request.animation.GlideAnimation;
import com.bumptech.glide.request.target.SimpleTarget;
import com.example.wind.mycomic.custom.BackgoundTask;
import com.example.wind.mycomic.custom.CustomAction;
import com.example.wind.mycomic.custom.CustomDetailsOverviewRow;
import com.example.wind.mycomic.custom.CustomDetailsOverviewRowPresenter;
import com.example.wind.mycomic.custom.FullWidthDetailsOverviewRowPresenter;
import com.example.wind.mycomic.custom.OnCustomActionClickedListener;
import com.example.wind.mycomic.object.Movie;
import com.example.wind.mycomic.object.SiteMovie;
import com.example.wind.mycomic.object.VideoMovie;
import com.example.wind.mycomic.siteParser.Maplestage;
import com.example.wind.mycomic.siteParser.PlayClass;
import com.example.wind.mycomic.siteParser.PlayList;
import com.example.wind.mycomic.siteParser.PlaySite;
import com.example.wind.mycomic.siteParser.SitePageParser;
import com.example.wind.mycomic.utils.PlayMovie;

import java.util.ArrayList;
import java.util.Arrays;

/*
 * LeanbackDetailsFragment extends DetailsFragment, a Wrapper fragment for leanback details screens.
 * It shows a detailed view of video and its meta plus related videos.
 */
public class VideoDetailsFragment extends DetailsFragment {
    private static final String TAG = "VideoDetailsFragment";

    private static final int DETAIL_THUMB_WIDTH = 274;
    private static final int DETAIL_THUMB_HEIGHT = 274;

    private Movie mSelectedMovie;
    private Integer mSelectedSeasonIndex;
    private ArrayList<VideoMovie> videoMoviesList = null;

    private ArrayObjectAdapter mAdapter;
    private FullWidthDetailsOverviewRowPresenter mFwdorPresenter;
    private ClassPresenterSelector mPresenterSelector;

    private BackgoundTask backgoundTask;

    @Override
    public void onCreate(Bundle savedInstanceState) {
        Log.d(TAG, "onCreate DetailsFragment");
        super.onCreate(savedInstanceState);

        backgoundTask = new BackgoundTask(getActivity());

        final String site_type = (String) getActivity().getIntent().getSerializableExtra(DetailsActivity.MOVIE_CATEGORY);
        String movie_uuid = (String) getActivity().getIntent().getSerializableExtra(DetailsActivity.MOVIE_UUID);

        ArrayList<Movie> movieList = ShareDataClass.getInstance().movieList.get(site_type);
        for (int i = 0; i < movieList.size(); i++) {
            if (movieList.get(i).getUUID().compareTo(movie_uuid) == 0) {
                mSelectedMovie = movieList.get(i);
            }
        }

        if (mSelectedMovie != null) {
            Thread thread = new Thread(new Runnable() {
                @Override
                public void run() {
                    String page_url = mSelectedMovie.getVideoPage();
                    SitePageParser sitePageParser = new SitePageParser();
                    videoMoviesList = sitePageParser.doParser(page_url, mSelectedMovie);
                    ShareDataClass.getInstance().playMovieList.clear();
                    for(int i = 0; i < videoMoviesList.size(); i++) {
                        PlayMovie playMovie = new PlayMovie();
                        if (videoMoviesList.get(i).getSiteMovieList().size() > 0) {
                            SiteMovie siteMovie = videoMoviesList.get(i).getSiteMovieList().get(0); // For those video only have one site.
                            playMovie.setMovie_title(mSelectedMovie.getTitle());
                            playMovie.setVideo_intro(mSelectedMovie.getDescription());
                            playMovie.setVideo_title(videoMoviesList.get(i).getVideoTitle());
                            playMovie.setVideo_url(siteMovie.getSiteLink());
                            playMovie.setMovie_categoryIndex(mSelectedMovie.getCategoryIndex());
                            playMovie.setVideo_type(mSelectedMovie.getType());
                            ShareDataClass.getInstance().playMovieList.add(playMovie);
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
                //setupMovieListRow();
                //setupMovieListRowPresenter();
                setOnItemViewClickedListener(new ItemViewClickedListener());
            } catch (InterruptedException e) {
                e.printStackTrace();
            }
        } else {
            Intent intent = new Intent(getActivity(), MainActivity.class);
            startActivity(intent);
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
        final CustomDetailsOverviewRow row = new CustomDetailsOverviewRow(mSelectedMovie, getActivity());
        row.setImageDrawable(getResources().getDrawable(R.drawable.default_background));
        int width = Utils.convertDpToPixel(getActivity()
                .getApplicationContext(), DETAIL_THUMB_WIDTH);
        int height = Utils.convertDpToPixel(getActivity()
                .getApplicationContext(), DETAIL_THUMB_HEIGHT);
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

        for (int i = 0; i < videoMoviesList.size(); i++) {
            VideoMovie videoMovie = videoMoviesList.get(i);
            row.addAction(new CustomAction(i, videoMovie));
        }

        mAdapter.add(row);
    }

    private void setupDetailsOverviewRowPresenter() {
        // Set detail background and style.
        mFwdorPresenter = new FullWidthDetailsOverviewRowPresenter(new DetailsDescriptionPresenter());
        final CustomDetailsOverviewRowPresenter detailsPresenter =
                new CustomDetailsOverviewRowPresenter(new DetailsDescriptionPresenter());
        detailsPresenter.setBackgroundColor(getResources().getColor(R.color.selected_background));
        detailsPresenter.setStyleLarge(true);

        // Hook up transition element.
        detailsPresenter.setSharedElementEnterTransition(getActivity(),
                DetailsActivity.SHARED_ELEMENT_NAME);

        detailsPresenter.setOnActionClickedListener(new OnCustomActionClickedListener() {
            @Override
            public void onActionClicked(final CustomAction action, View v) {
            }
        });

        mFwdorPresenter.setOnActionClickedListener(new OnCustomActionClickedListener() {
            @Override
            public void onActionClicked(final CustomAction action, View v) {
            }
        });

        //mPresenterSelector.addClassPresenter(CustomDetailsOverviewRow.class, detailsPresenter);
        mPresenterSelector.addClassPresenter(CustomDetailsOverviewRow.class, mFwdorPresenter);
    }

    private void setupMovieListRow() {
        String title = "推薦影片";
        ArrayList<Movie> movie_list = ShareDataClass.getInstance().movieList.get(mSelectedMovie.getCategory());
        ArrayObjectAdapter listRowAdapter = new ArrayObjectAdapter(new CardPresenter());
        for (int i = 0; i < movie_list.size(); i++) {
            // Searched movie type
            Movie cur_movie = movie_list.get(i);
            String movie_type = cur_movie.getType();
            String[] movie_type_arr = movie_type.split("/");

            // Set the related rate
            double similar_rate = 0.6;

            // Selected movie type
            String cur_movie_type = mSelectedMovie.getType();
            String[] cur_movie_type_arr = cur_movie_type.split("/");
            Integer[] a = new Integer[ShareDataClass.getInstance().movieTypeList.size()];
            Arrays.fill(a, 0);
            Integer[] b = new Integer[ShareDataClass.getInstance().movieTypeList.size()];
            Arrays.fill(b, 0);
            for (String cur_type : cur_movie_type_arr) {
                cur_type = cur_type.trim();
                Integer index = ShareDataClass.getInstance().movieTypeList.get(cur_type);
                if (index != null) {
                    a[index] = 1;
                }
            }
            for (String cur_type : movie_type_arr) {
                cur_type = cur_type.trim();
                Integer index = ShareDataClass.getInstance().movieTypeList.get(cur_type);
                if (index != null) {
                    b[index] = 1;
                }
            }
            double simirate = ShareDataClass.getInstance().calCosineSimilarity(a, b);
            if (cur_movie.getUUID().compareTo(mSelectedMovie.getUUID()) != 0 &&
                    simirate > similar_rate) {
                listRowAdapter.add(cur_movie);
            }
        }
        HeaderItem header = new HeaderItem(0, title);
        mAdapter.add(new ListRow(header, listRowAdapter));
    }

    private void setupMovieListRowPresenter() {
        mPresenterSelector.addClassPresenter(ListRow.class, new ListRowPresenter());
    }

    private String decode(String in) {
        String working = in;
        int index;
        index = working.indexOf("\\u");
        while (index > -1) {
            int length = working.length();
            if (index > (length - 6)) break;
            int numStart = index + 2;
            int numFinish = numStart + 4;
            String substring = working.substring(numStart, numFinish);
            int number = Integer.parseInt(substring, 16);
            String stringStart = working.substring(0, index);
            String stringEnd = working.substring(numFinish);
            working = stringStart + ((char) number) + stringEnd;
            index = working.indexOf("\\u");
        }
        return working;
    }

    private final class ItemViewClickedListener implements OnItemViewClickedListener {
        @Override
        public void onItemClicked(Presenter.ViewHolder itemViewHolder, Object item,
                                  RowPresenter.ViewHolder rowViewHolder, Row row) {

            if (item instanceof CustomAction) {
                CustomAction action = (CustomAction) item;

                String video_index = action.getId().toString();

                if (mSelectedMovie.getCategoryIndex() >= 0 && mSelectedMovie.getCategoryIndex() <= 1) {
                    final Integer id = action.getId();
                    final VideoMovie videoMovie = action.getVideoMovie();

                    AlertDialog.Builder alertDialog = new AlertDialog.Builder(new ContextThemeWrapper(getActivity(), R.style.siteDialogTheme));
                    alertDialog.setTitle("請選擇分流");

                    ArrayList<String> options = new ArrayList<String>();
                    for(int i = 0; i < videoMovie.getSiteMovieList().size(); i++) {
                        String cur_site_link = videoMovie.getSiteMovieList().get(i).getSiteLink();
                        options.add(videoMovie.getSiteMovieList().get(i).getSiteName());
                    }


                    //建立選擇的事件
                    DialogInterface.OnClickListener ListClick = new DialogInterface.OnClickListener()
                    {
                        public void onClick(DialogInterface dialog, int index) {
                            String target_url = videoMovie.getSiteMovieList().get(index).getSiteLink();
                            //Intent viewIntent = new Intent(Intent.ACTION_VIEW, Uri.parse(target_url));
                            //if (null != viewIntent.resolveActivity(getActivity().getPackageManager())) {
                            //    startActivity(viewIntent);
                            //}
                            ShareDataClass.getInstance().playMovieList = new ArrayList<PlayMovie>();
                            PlayMovie playMovie = new PlayMovie();
                            playMovie.setMovie_category(mSelectedMovie.getCategory());
                            playMovie.setMovie_categoryIndex(mSelectedMovie.getCategoryIndex());
                            playMovie.setVideo_intro(mSelectedMovie.getDescription());
                            playMovie.setVideo_title(videoMovie.getVideoTitle());
                            playMovie.setVideo_url(target_url);
                            playMovie.setVideo_img(mSelectedMovie.getCardImageUrl());
                            playMovie.setVideo_img_Uri(mSelectedMovie.getCardImageURI());
                            playMovie.setTruly_link("");
                            playMovie.setMovie_title(mSelectedMovie.getTitle());
                            ShareDataClass.getInstance().playMovieList.add(playMovie);

                            if (target_url.indexOf("preview") >= 0) {   //google vidoe
                                Intent intent = new Intent(getActivity(), CustomWebView.class);
                                intent.putExtra(DetailsActivity.MOVIE_VIDEO_PAGE_URL, target_url);
                                startActivity(intent);
                            } else {
                                Intent intent = new Intent(getActivity(), PlaybackOverlayActivity.class);
                                intent.putExtra(DetailsActivity.MOVIE_CATEGORY, mSelectedMovie.getCategory());
                                intent.putExtra(DetailsActivity.MOVIE_UUID, mSelectedMovie.getUUID());
                                intent.putExtra(DetailsActivity.PLAY_MOVIE_INDEX, "0");
                                startActivity(intent);
                            }
                        }
                    };

                    String[] optionsArr = options.toArray(new String[0]);
                    alertDialog.setItems(optionsArr, ListClick);
                    alertDialog.show();
                } else if (mSelectedMovie.getCategoryIndex() == 2) {
                    Intent intent = new Intent(getActivity(), PlaybackOverlayActivity.class);
                    intent.putExtra(DetailsActivity.MOVIE_CATEGORY, mSelectedMovie.getCategory());
                    intent.putExtra(DetailsActivity.MOVIE_UUID, mSelectedMovie.getUUID());
                    intent.putExtra(DetailsActivity.PLAY_MOVIE_INDEX, video_index);
                    startActivity(intent);
                } else if (mSelectedMovie.getCategoryIndex() >= 3 && mSelectedMovie.getCategoryIndex() <= 9) {
                    final VideoMovie videoMovie = action.getVideoMovie();
                    if (videoMovie.getSiteMovieList().size() > 0) {
                        String cur_page_url = videoMovie.getSiteMovieList().get(0).getSiteLink();

                        if (cur_page_url.indexOf("http") >= 0) {
                            cur_page_url = cur_page_url.replace("//", "/").replace("http:/", "http://");
                        } else if (cur_page_url.indexOf("https") >= 0) {
                            cur_page_url = cur_page_url.replace("//", "/").replace("https:/", "https://");
                        }

                        Maplestage maplestage = new Maplestage();
                        final PlaySite playSite = maplestage.getList(cur_page_url);

                        AlertDialog.Builder alertDialog = new AlertDialog.Builder(new ContextThemeWrapper(getActivity(), R.style.siteDialogTheme));
                        alertDialog.setTitle("請選擇分流");

                        ArrayList<String> options = new ArrayList<String>();
                        for(int j = 0; j < playSite.getPlayList().size(); j++) {
                            PlayList playList = playSite.getPlayList().get(j);
                            String sizeType = playList.getPlayType();
                            if (sizeType.compareTo("youtube") == 0 || sizeType.compareTo("dailymotion") == 0) {
                                options.add(sizeType);
                            }
                        }

                        //建立選擇的事件
                        DialogInterface.OnClickListener ListClick = new DialogInterface.OnClickListener()
                        {
                            public void onClick(DialogInterface dialog, int index) {
                                ShareDataClass.getInstance().playMovieList = new ArrayList<PlayMovie>();
                                for(int k = 0; k < playSite.getPlayList().get(index).getPlayClass().size(); k++) {
                                    PlayClass playClass = playSite.getPlayList().get(index).getPlayClass().get(k);
                                    String siteType = playSite.getPlayList().get(index).getPlayType();
                                    String video_url = playClass.getVideoId();

                                    PlayMovie playMovie = new PlayMovie();
                                    playMovie.setVideo_type(siteType);
                                    playMovie.setMovie_category(mSelectedMovie.getCategory());
                                    playMovie.setMovie_categoryIndex(mSelectedMovie.getCategoryIndex());
                                    playMovie.setVideo_intro(mSelectedMovie.getDescription());
                                    playMovie.setVideo_title("片段: " + (k + 1));
                                    playMovie.setVideo_url(video_url);
                                    playMovie.setVideo_img(mSelectedMovie.getCardImageUrl());
                                    playMovie.setVideo_img_Uri(mSelectedMovie.getCardImageURI());
                                    playMovie.setTruly_link("");
                                    playMovie.setMovie_title(mSelectedMovie.getTitle());
                                    ShareDataClass.getInstance().playMovieList.add(playMovie);
                                }
                                Intent intent = new Intent(getActivity(), PlaybackOverlayActivity.class);
                                intent.putExtra(DetailsActivity.MOVIE_CATEGORY, mSelectedMovie.getCategory());
                                intent.putExtra(DetailsActivity.MOVIE_UUID, mSelectedMovie.getUUID());
                                intent.putExtra(DetailsActivity.PLAY_MOVIE_INDEX, "0");
                                startActivity(intent);
                            }
                        };
                        String[] optionsArr = options.toArray(new String[0]);
                        alertDialog.setItems(optionsArr, ListClick);
                        alertDialog.show();
                    }
                } else {
                    Intent intent = new Intent(getActivity(), PlaybackOverlayActivity.class);
                    intent.putExtra(DetailsActivity.MOVIE_CATEGORY, mSelectedMovie.getCategory());
                    intent.putExtra(DetailsActivity.MOVIE_UUID, mSelectedMovie.getUUID());
                    intent.putExtra(DetailsActivity.PLAY_MOVIE_INDEX, video_index);
                    startActivity(intent);
                }
            }
        }
    }

    private final class ItemViewSelectedListener implements OnItemViewSelectedListener {
        @Override
        public void onItemSelected(Presenter.ViewHolder itemViewHolder, Object item,
                                   RowPresenter.ViewHolder rowViewHolder, Row row) {
            if (item instanceof CustomAction) {
                //backgoundTask.startBackgroundTimer(ShareDataClass.getInstance().default_fragment_background);
            }
        }
    }
}
