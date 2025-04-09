package com.wind.tvplayer.controller.parser;

import com.google.gson.Gson;
import com.wind.tvplayer.common.ShareData;
import com.wind.tvplayer.common.ShareVideo;
import com.wind.tvplayer.model.video.Movie;
import com.wind.tvplayer.model.video.VideoInfo;
import com.wind.tvplayer.model.video.VideoResponse;

import org.apache.commons.lang3.StringEscapeUtils;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.Map;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

public class Site {
    public String parserUrl = "";
    public String infoUrl = "";

    public ArrayList<Movie> getMovie_list() {
        return movie_list;
    }

    public void setMovie_list(ArrayList<Movie> movie_list) {
        this.movie_list = movie_list;
    }

    private ArrayList<Movie> movie_list = new ArrayList<Movie>();

    public void jsonParser(final String url, final String site_category) {
        movie_list = new ArrayList<Movie>();
        final Thread thread = new Thread(new Runnable() {
            @Override
            public void run() {
                String jsonStr = ShareData.getInstance().GetHttps(url, false);
                Gson gson = new Gson();
                com.wind.tvplayer.model.Gson.MovieSite movieSite = gson.fromJson(jsonStr, com.wind.tvplayer.model.Gson.MovieSite.class);
                for(int i = 0 ; i < movieSite.getMovies().length ; i++)
                {
                    com.wind.tvplayer.model.Gson.Movie movie = movieSite.getMovies()[i];
                    Movie cur_movie_obj = new Movie();

                    String movie_index = movie.getIndex();
                    String title = movie.getTitle();
                    title = StringEscapeUtils.unescapeJava(title);
                    String img = movie.getImg();
                    img = StringEscapeUtils.unescapeJava(img);
                    String page_link = movie.getPageLink();
                    String second_title = movie.getSecondTitle();
                    cur_movie_obj.setMovieDate(second_title);
                    cur_movie_obj.setViewNumber("0");
                    cur_movie_obj.setUuid(movie_index);
                    cur_movie_obj.setId(cur_movie_obj.getCount());
                    cur_movie_obj.incCount();
                    cur_movie_obj.setCardImageUrl(img);
                    cur_movie_obj.setTitle(title);
                    cur_movie_obj.setStudio(second_title);
                    cur_movie_obj.setVideoPage(page_link);
                    cur_movie_obj.setBackgroundImageUrl(ShareData.getInstance().default_fragment_background);
                    cur_movie_obj.setCategory(site_category);
                    cur_movie_obj.setType(site_category);
                    movie_list.add(cur_movie_obj);
                }
            }
        });
        try {
            thread.start();
            thread.join();
        } catch (InterruptedException e) {
            e.printStackTrace();
        }
    }

    public ArrayList<VideoInfo> doParser(String i_url, Movie movie) {
        ArrayList<VideoInfo> movie_list = new ArrayList<VideoInfo>();

        try {
            // Step 1: 準備 POST 請求參數
            Map<String, String> postParams = new HashMap<>();
            postParams.put("url", i_url);

            // Step 2: 發送 POST 請求
            String jsonStr = ShareData.getInstance().doPostJson(ShareVideo.selectedSite.infoUrl, postParams);

            // Step 3: 解析回傳結果
            Gson gson = new Gson();
            VideoResponse videoResponse = gson.fromJson(jsonStr, VideoResponse.class);
            movie_list = videoResponse.getVideoList();
        } catch (Exception e) {
            e.printStackTrace();
        }

        return movie_list != null ? movie_list : new ArrayList<>();
    }
}
