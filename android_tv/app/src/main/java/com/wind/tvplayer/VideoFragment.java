/*
 * Copyright (C) 2017 The Android Open Source Project
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

import android.content.Intent;
import android.os.AsyncTask;
import android.os.Bundle;
import android.util.Log;
import android.view.View;

import androidx.core.content.ContextCompat;
import androidx.leanback.app.BrowseFragment;
import androidx.leanback.widget.ArrayObjectAdapter;
import androidx.leanback.widget.HeaderItem;
import androidx.leanback.widget.ListRow;
import androidx.leanback.widget.ListRowPresenter;
import androidx.leanback.widget.OnItemViewClickedListener;
import androidx.leanback.widget.OnItemViewSelectedListener;
import androidx.leanback.widget.Presenter;
import androidx.leanback.widget.Row;
import androidx.leanback.widget.RowPresenter;

import com.wind.tvplayer.common.BackgoundTask;
import com.wind.tvplayer.common.ShareData;
import com.wind.tvplayer.common.ShareVideo;
import com.wind.tvplayer.common.SpinnerFragment;
import com.wind.tvplayer.model.video.Movie;
import com.wind.tvplayer.model.video.Site;

import org.json.JSONException;
import org.json.JSONObject;

import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collections;
import java.util.Comparator;
import java.util.Date;


public class VideoFragment extends BrowseFragment {
    private static final String TAG = "VideoFragment";
    private BackgoundTask backgoundTask;
    private ArrayObjectAdapter mRowsAdapter;
    private SpinnerFragment mSinnerFragment = new SpinnerFragment();
    private int movieIndex;

    private class ShowSpinnerTask extends AsyncTask<Void, Void, Void> {
        @Override
        protected void onPreExecute() {
            getActivity().getFragmentManager().beginTransaction().add(R.id.main_browse_fragment, mSinnerFragment).commit();
        }

        @Override
        protected Void doInBackground(Void... params) {
            loadRows();
            return null;
        }

        @Override
        protected void onPostExecute(Void aVoid) {
            getFragmentManager().beginTransaction().remove(mSinnerFragment).commit();
            setAdapter(mRowsAdapter);
        }
    }

    @Override
    public void onActivityCreated(Bundle savedInstanceState) {
        Log.i(TAG, "onCreate");
        super.onActivityCreated(savedInstanceState);

        backgoundTask = new BackgoundTask(getActivity());

        movieIndex = (int) getActivity().getIntent().getSerializableExtra(DetailsActivity.MOVIE_UUID);

        setupUIElements();

        new ShowSpinnerTask().execute();

        setupEventListeners();
    }

    @Override
    public void onDestroy() {
        super.onDestroy();
        backgoundTask.Destroy();
    }

    private void loadRows() {
        Site siteObj = ShareData.getInstance().siteCardList.get(movieIndex);
        String site_link = siteObj.getSiteLink();
        String site_type = siteObj.getSiteName();

        mRowsAdapter = new ArrayObjectAdapter(new ListRowPresenter());
        CardPresenter cardPresenter = new CardPresenter();

        // Load proxy site
        String proxy_json_str = ShareData.getInstance().GetHttps("https://drive.google.com/uc?export=download&id=0B1_1ZUYYMDcreVh6eG5JaXNuaFk", false);
        JSONObject proxyObj = null;
        try {
            proxyObj = new JSONObject(proxy_json_str);
            ShareData.getInstance().proxy_ip_address = proxyObj.getString("ip_address");
            ShareData.getInstance().proxy_ip_port = proxyObj.getInt("port");
        } catch (JSONException e) {
            e.printStackTrace();
        }

        // Get Site
        com.wind.tvplayer.controller.parser.Site siteClass = new com.wind.tvplayer.controller.parser.Site();
        siteClass.jsonParser(site_link, site_type);
        ShareVideo.getInstance().selectedSite = siteClass; // For movieList

        // Do Sort
        ArrayList<Movie> movie_list = ShareVideo.getInstance().selectedSite.getMovie_list();
        Collections.sort(movie_list, new SortMovieList());

        ArrayObjectAdapter listRowAdapter = new ArrayObjectAdapter(cardPresenter);
        String prev_alphabet = "";
        int index = 0;
        for (Integer j = 0; j < movie_list.size(); j++) {
            Movie cur_movie = movie_list.get(j);
            String cur_alphabet = cur_movie.getMovieDate().substring(0, 4);

            if(cur_alphabet.compareTo(prev_alphabet) != 0 && prev_alphabet.compareTo("") == 0) {
                prev_alphabet = cur_alphabet;
                listRowAdapter.add(cur_movie);
            } else if(cur_alphabet.compareTo(prev_alphabet) != 0 && prev_alphabet.compareTo("") != 0) {
                HeaderItem header = new HeaderItem(index, prev_alphabet + " (" + listRowAdapter.size() + ")");
                mRowsAdapter.add(new ListRow(header, listRowAdapter));
                index++;
                prev_alphabet = cur_alphabet;
                listRowAdapter  = new ArrayObjectAdapter(cardPresenter);
                listRowAdapter.add(cur_movie);
            } else {
                listRowAdapter.add(cur_movie);
            }
        }
    }

    private void setupUIElements() {
        // setBadgeDrawable(getActivity().getResources().getDrawable(
        // R.drawable.videos_by_google_banner));
        setTitle(getString(R.string.browse_title)); // Badge, when set, takes precedent
        // over title
        setHeadersState(HEADERS_ENABLED);
        setHeadersTransitionOnBackEnabled(true);

        // set fastLane (or headers) background color
        setBrandColor(ContextCompat.getColor(getActivity(), R.color.fastlane_background));
        // set search icon color
        setSearchAffordanceColor(ContextCompat.getColor(getActivity(), R.color.search_opaque));
    }

    private void setupEventListeners() {
        setOnSearchClickedListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {

            }
        });

        setOnItemViewClickedListener(new ItemViewClickedListener());
        setOnItemViewSelectedListener(new ItemViewSelectedListener());
    }

    private final class ItemViewClickedListener implements OnItemViewClickedListener {
        @Override
        public void onItemClicked(Presenter.ViewHolder itemViewHolder, Object item,
                                  RowPresenter.ViewHolder rowViewHolder, Row row) {

            if (item instanceof Movie) {
                Movie movie = (Movie) item;
                Log.d(TAG, "Item: " + item.toString());
                Intent intent = new Intent(getActivity(), VideoCardActivity.class);
                intent.putExtra(DetailsActivity.MOVIE_CATEGORY, movie.getCategory());
                intent.putExtra(DetailsActivity.MOVIE_UUID, movie.getUuid());
                getActivity().startActivity(intent);
            }
        }
    }

    private final class ItemViewSelectedListener implements OnItemViewSelectedListener {
        @Override
        public void onItemSelected(
                Presenter.ViewHolder itemViewHolder,
                Object item,
                RowPresenter.ViewHolder rowViewHolder,
                Row row) {
            if (item instanceof Movie) {
                // TBD...
            }
        }
    }

    private  class SortMovieList implements Comparator<Movie> {
        public Date parseDate(String dateString) {
            String[] formatStrings = {"yyyy/MM/dd", "yyyy-MM-dd", "yyyy-MM", "yyyy/MM", "yyyy"};
            for (String formatString : formatStrings)
            {
                try
                {
                    return new SimpleDateFormat(formatString).parse(dateString);
                }
                catch (ParseException e) {}
            }
            return new Date(1911, 1, 1);
        }

        public int compare(Movie movieA, Movie movieB) {
            Date dateA = parseDate(movieA.getMovieDate());
            Date dateB = parseDate(movieB.getMovieDate());
            return dateB.compareTo(dateA);
        }
    }
}
