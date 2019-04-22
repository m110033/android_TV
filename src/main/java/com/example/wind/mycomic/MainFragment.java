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

import java.io.IOException;
import java.net.CookieHandler;
import java.net.CookieManager;
import java.util.ArrayList;
import java.util.Collections;
import java.util.Comparator;

import android.content.Intent;
import android.graphics.Color;
import android.os.AsyncTask;
import android.os.Bundle;
import android.support.v17.leanback.app.BrowseFragment;
import android.support.v17.leanback.widget.ArrayObjectAdapter;
import android.support.v17.leanback.widget.HeaderItem;
import android.support.v17.leanback.widget.ListRow;
import android.support.v17.leanback.widget.ListRowPresenter;
import android.support.v17.leanback.widget.OnItemViewClickedListener;
import android.support.v17.leanback.widget.OnItemViewSelectedListener;
import android.support.v17.leanback.widget.Presenter;
import android.support.v17.leanback.widget.Row;
import android.support.v17.leanback.widget.RowPresenter;
import android.util.Log;
import android.view.Gravity;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;
import android.widget.Toast;

import com.example.wind.mycomic.custom.BackgoundTask;
import com.example.wind.mycomic.custom.SpinnerFragment;
import com.example.wind.mycomic.json.WordCode;
import com.example.wind.mycomic.json.WordSite;
import com.example.wind.mycomic.object.Movie;
import com.example.wind.mycomic.utils.HttpsUtil;
import com.example.wind.mycomic.utils.SiteClass;
import com.google.gson.Gson;

import org.json.JSONException;
import org.json.JSONObject;

public class MainFragment extends BrowseFragment {
    private static final String TAG = "MainFragment";

    private static final int GRID_ITEM_WIDTH = 200;
    private static final int GRID_ITEM_HEIGHT = 200;
    private static final int NUM_ROWS = 6;
    private static final int NUM_COLS = 15;
    private int movieIndex;

    private ArrayObjectAdapter mRowsAdapter;
    private BackgoundTask backgoundTask;
    private String sortType = "viewNumber";
    private SpinnerFragment mSinnerFragment = new SpinnerFragment();

    private  class SortMovieList implements Comparator<Movie> {
        //中文排序
        public int chineseCompare(String a, String b) {
            char[] obja = a.toCharArray();
            char[] objb = b.toCharArray();
            for (int k = 0; k < obja.length; k++) {
                String str1, str2;
                boolean aisenglish, bisenglish;
                boolean bshort = false;
                //檢查是不是英文
                aisenglish = obja[k] + 0 >= 32 && obja[k] + 0 <= 126 ? true : false;
                str1 = String.valueOf(obja[k]);
                try {    //如果發生字串2比字串1短的時候的處理方法
                    bisenglish = objb[k] + 0 >= 32 && objb[k] + 0 <= 126 ? true : false;
                    str2 = String.valueOf(objb[k]);
                } catch (Exception e) {
                    str2 = "";
                    bisenglish = aisenglish;    //如果是第一個字串是英文，就丟給String自己的comparable處裡
                    bshort = true;
                }
                if (!aisenglish && !bisenglish) {
                    int comval = 0;
                    int str1val, str2val;
                    try {
                        if(ShareDataClass.getInstance().wordDict.containsKey(str1)) {
                            str1val = ShareDataClass.getInstance().wordDict.get(str1).intValue();
                        } else {
                            str1val = 0;
                        }
                    } catch (Exception e) {
                        str1val = 0;
                    }
                    try {    //如果不在這張表裡面的處理方法
                        if(!bshort) {
                            if(ShareDataClass.getInstance().wordDict.containsKey(str1)) {
                                str2val = ShareDataClass.getInstance().wordDict.get(str2).intValue();
                            } else {
                                str2val = 0;
                            }
                        } else {
                            str2val = 0;
                        }
                    } catch (Exception e) {
                        str2val = str1val;
                        str1val = 0;
                    }
                    comval = str1val - str2val;    //比較的原則就是回傳值越大的，排在越後面
                    if (comval == 0) {
                        continue;
                    } else {
                        return comval;
                    }
                } else {
                    int comval = str1.compareTo(str2);
                    if (comval == 0) {
                        continue;
                    }
                    return comval;
                }
            }
            return obja.hashCode() - objb.hashCode();    //都沒辦法的時候只能交給Hashcode比較了
        }

        public int compare(Movie movieA, Movie movieB) {
            if(sortType.compareTo("viewNumber") == 0) {
                return Integer.parseInt(movieB.getViewNumber()) - Integer.parseInt(movieA.getViewNumber());
            } else if(sortType.compareTo("movieName") == 0) {
                return movieA.getTitle().compareTo(movieB.getTitle());
            } else if(sortType.compareTo("movieDate") == 0) {
                String yearA = movieA.getMovieDate().substring(0, 4);
                String yearB = movieB.getMovieDate().substring(0, 4);
                return Integer.parseInt(yearB) - Integer.parseInt(yearA);
            } else if(sortType.compareTo("chineseLen") == 0) {
                return chineseCompare(movieA.getTitle(), movieB.getTitle());
            } else {
                return Integer.parseInt(movieB.getViewNumber()) - Integer.parseInt(movieA.getViewNumber());
            }
        }
    }

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

        CookieManager cookieManager = new CookieManager();
        CookieHandler.setDefault(cookieManager);

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
        String site_link = ShareDataClass.getInstance().site_json_list[movieIndex];
        String site_type = ShareDataClass.getInstance().site_name_list[movieIndex];

        mRowsAdapter = new ArrayObjectAdapter(new ListRowPresenter());
        CardPresenter cardPresenter = new CardPresenter();

        //初始化
        if(getActivity() != null) {
            try {
                HttpsUtil.ssl_asset_stream = getActivity().getAssets().open("tvapp.cer");
            } catch (IOException e) {
                e.printStackTrace();
            }
        }
        ShareDataClass.getInstance().movieMaxHiits.clear();
        ShareDataClass.getInstance().movieList.clear();
        ShareDataClass.getInstance().playMovieList.clear();
        ShareDataClass.getInstance().movieTypeList.clear();

        // Load proxy site
        String proxy_json_str = ShareDataClass.getInstance().GetHttps("https://drive.google.com/uc?export=download&id=0B1_1ZUYYMDcreVh6eG5JaXNuaFk", false);
        JSONObject proxyObj = null;
        try {
            proxyObj = new JSONObject(proxy_json_str);
            ShareDataClass.getInstance().proxy_ip_address = proxyObj.getString("ip_address");
            ShareDataClass.getInstance().proxy_ip_port = proxyObj.getInt("port");
            ShareDataClass.getInstance().is_bsite_proxy = proxyObj.getBoolean("enable");
        } catch (JSONException e) {
            e.printStackTrace();
        }

        ShareDataClass.getInstance().site_name_to_type.put("楓林網 - 台灣戲劇", "variety");
        ShareDataClass.getInstance().site_name_to_type.put("楓林網 - 大陸戲劇", "variety");
        ShareDataClass.getInstance().site_name_to_type.put("楓林網 - 韓國戲劇", "variety");
        ShareDataClass.getInstance().site_name_to_type.put("楓林網 - 其他戲劇", "variety");
        ShareDataClass.getInstance().site_name_to_type.put("楓林網 - 台灣綜藝", "drama");
        ShareDataClass.getInstance().site_name_to_type.put("楓林網 - 大陸綜藝", "drama");
        ShareDataClass.getInstance().site_name_to_type.put("楓林網 - 韓國綜藝", "drama");

        // ShareDataClass.getInstance().GetHttps("https://bangumi.bilibili.com/player/web_api/playurl?cid=25193625&appkey=84956560bc028eb7&otype=json&type=&quality=80&module=bangumi&qn=80&sign=4e2a1f6341300324ad58b4eb876b0067");

        // Want Site
        SiteClass siteClass = new SiteClass();
        siteClass.parserMovieSiteWithJson(site_link, site_type);
        siteClass.getMovie_list();
        ShareDataClass.getInstance().movieMaxHiits.put(site_type, siteClass.getMaxHit());
        ShareDataClass.getInstance().movieList.put(site_type, siteClass.getMovie_list());

        if(site_type.compareTo(ShareDataClass.getInstance().site_name_list[0]) == 0 ||
                site_type.compareTo(ShareDataClass.getInstance().site_name_list[1]) == 0 ||
                site_type.compareTo(ShareDataClass.getInstance().site_name_list[3]) == 0) {
            // Add movie type
            for (String cur_type : siteClass.getMovie_type_list()) {
                cur_type = cur_type.trim();
                if (!ShareDataClass.getInstance().movieTypeList.containsKey(cur_type)) {
                    ShareDataClass.getInstance().movieTypeList.put(cur_type, ShareDataClass.getInstance().movieTypeList.size());
                }
            }
        }

        // Get sort list
        String jsonStr = ShareDataClass.getInstance().GetHttps("https://raw.githubusercontent.com/m110033/android_TV/master/video_site/word_code.json", false);
        Gson gson = new Gson();
        WordSite wordSite = gson.fromJson(jsonStr, WordSite.class);

        ShareDataClass.getInstance().wordDict.clear();
        for(int i = 0 ; i < wordSite.getWordCode().length ; i++) {
            WordCode wordCode = wordSite.getWordCode()[i];
            String c_word = wordCode.getWord();
            Integer c_word_len =  Integer.parseInt(wordCode.getLength());
            if(!ShareDataClass.getInstance().wordDict.containsKey(c_word)) {
                ShareDataClass.getInstance().wordDict.put(c_word, c_word_len);
            }
        }

        // Do sort
        int index = 0;
        String prev_alphabet = "";

        if(ShareDataClass.getInstance().movieList.containsKey(site_type)) {
            ArrayList<Movie> movie_list = ShareDataClass.getInstance().movieList.get(site_type);

            if(site_type.compareTo(ShareDataClass.getInstance().site_name_list[3]) == 0 ||
                site_type.compareTo(ShareDataClass.getInstance().site_name_list[4]) == 0 ||
                site_type.compareTo(ShareDataClass.getInstance().site_name_list[5]) == 0 ||
                site_type.compareTo(ShareDataClass.getInstance().site_name_list[6]) == 0 ||
                site_type.compareTo(ShareDataClass.getInstance().site_name_list[7]) == 0
                ) {
                sortType = "chineseLen";
                Collections.sort(movie_list, new SortMovieList());

                ArrayObjectAdapter listRowAdapter = new ArrayObjectAdapter(cardPresenter);

                int cur_len = 0, prev_len = 0;

                for (Integer j = 0; j < movie_list.size(); j++) {
                    Movie cur_movie = movie_list.get(j);
                    String cur_alphabet = cur_movie.getTitle().substring(0, 1);
                    if(ShareDataClass.getInstance().wordDict.containsKey(cur_alphabet)) {
                        cur_len = ShareDataClass.getInstance().wordDict.get(cur_alphabet).intValue();
                        if(cur_len != prev_len && prev_len == 0) {
                            prev_len = cur_len;
                            listRowAdapter.add(cur_movie);
                        } else if(cur_len != prev_len && prev_len != 0) {
                            HeaderItem header = new HeaderItem(index, prev_len + " 劃" + " (" + listRowAdapter.size() + ")");
                            mRowsAdapter.add(new ListRow(header, listRowAdapter));
                            index++;
                            prev_len = cur_len;
                            listRowAdapter = new ArrayObjectAdapter(cardPresenter);
                            listRowAdapter.add(cur_movie);
                        } else {
                            listRowAdapter.add(cur_movie);
                        }
                    } else {
                        if (cur_alphabet.compareTo(prev_alphabet) != 0 && prev_alphabet.compareTo("") == 0) {
                            prev_alphabet = cur_alphabet;
                            listRowAdapter.add(cur_movie);
                        } else if (cur_alphabet.compareTo(prev_alphabet) != 0 && prev_alphabet.compareTo("") != 0) {
                            HeaderItem header = new HeaderItem(index, prev_alphabet + " (" + listRowAdapter.size() + ")");
                            mRowsAdapter.add(new ListRow(header, listRowAdapter));
                            index++;
                            prev_alphabet = cur_alphabet;
                            listRowAdapter = new ArrayObjectAdapter(cardPresenter);
                            listRowAdapter.add(cur_movie);
                        } else {
                            listRowAdapter.add(cur_movie);
                        }
                    }
                }
            } else {
                sortType = "movieDate";
                Collections.sort(movie_list, new SortMovieList());

                ArrayObjectAdapter listRowAdapter = new ArrayObjectAdapter(cardPresenter);

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
        setBrandColor(getResources().getColor(R.color.fastlane_background));
        // set search icon color
        setSearchAffordanceColor(getResources().getColor(R.color.search_opaque));
    }

    private void setupEventListeners() {
        setOnSearchClickedListener(new View.OnClickListener() {

            @Override
            public void onClick(View view) {
                Intent intent = new Intent(getActivity(), SearchActivity.class);
                startActivity(intent);
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
                Intent intent = new Intent(getActivity(), SeasonActivity.class);

                intent.putExtra(DetailsActivity.MOVIE_CATEGORY, movie.getCategory());
                intent.putExtra(DetailsActivity.MOVIE_UUID, movie.getUUID());

                getActivity().startActivity(intent);
                /*
                Movie movie = (Movie) item;
                Log.d(TAG, "Item: " + item.toString());
                Intent intent = new Intent(getActivity(), DetailsActivity.class);

                intent.putExtra(DetailsActivity.MOVIE_CATEGORY, movie.getCategory());
                intent.putExtra(DetailsActivity.MOVIE_UUID, movie.getUUID());
                intent.putExtra(DetailsActivity.MOVIE_SEASON_NAME, movie.getSeasonMovieList().get(0).getSeasonName());

                getActivity().startActivity(intent);
                */
            } else if (item instanceof String) {
                if (((String) item).indexOf(getString(R.string.error_fragment)) >= 0) {
                    Intent intent = new Intent(getActivity(), BrowseErrorActivity.class);
                    startActivity(intent);
                } else {
                    Toast.makeText(getActivity(), ((String) item), Toast.LENGTH_SHORT)
                            .show();
                }
            }
        }
    }

    private final class ItemViewSelectedListener implements OnItemViewSelectedListener {
        @Override
        public void onItemSelected(Presenter.ViewHolder itemViewHolder, Object item,
                                   RowPresenter.ViewHolder rowViewHolder, Row row) {
            if (item instanceof Movie) {
                Movie selectedMovie = (Movie)item;
                ListRow listRow = (ListRow) row;
                ArrayObjectAdapter arrayObjectAdapter = (ArrayObjectAdapter)listRow.getAdapter();
                if (arrayObjectAdapter.size() > 0) {
                    Movie lastMove = (Movie) arrayObjectAdapter.get(arrayObjectAdapter.size() - 1);
                    if(selectedMovie == lastMove) {
                        /*
                        // Add more card to support the function of "LOAD MORE"
                        arrayObjectAdapter.add(selectedMovie);
                        arrayObjectAdapter.notifyArrayItemRangeChanged(0, arrayObjectAdapter.size());
                        */
                    }
                }
                //backgoundTask.startBackgroundTimer( ((Movie) item).getBackgroundImageURI());
            }

        }
    }

    private class GridItemPresenter extends Presenter {
        @Override
        public ViewHolder onCreateViewHolder(ViewGroup parent) {
            TextView view = new TextView(parent.getContext());
            view.setLayoutParams(new ViewGroup.LayoutParams(GRID_ITEM_WIDTH, GRID_ITEM_HEIGHT));
            view.setFocusable(true);
            view.setFocusableInTouchMode(true);
            view.setBackgroundColor(getResources().getColor(R.color.default_background));
            view.setTextColor(Color.WHITE);
            view.setGravity(Gravity.CENTER);
            return new ViewHolder(view);
        }

        @Override
        public void onBindViewHolder(ViewHolder viewHolder, Object item) {
            ((TextView) viewHolder.view).setText((String) item);
        }

        @Override
        public void onUnbindViewHolder(ViewHolder viewHolder) {
        }
    }

}
