package com.wind.tvplayer.controller.parser;

import com.google.gson.Gson;
import com.wind.tvplayer.common.ShareData;
import com.wind.tvplayer.model.video.Movie;
import com.wind.tvplayer.model.video.Video;

import org.apache.commons.lang3.StringEscapeUtils;

import java.net.MalformedURLException;
import java.net.URL;
import java.util.ArrayList;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

public class Site {
    public ArrayList<Movie> getMovie_list() {
        return movie_list;
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

    public ArrayList<Video> doParser(String i_url, Movie movie) {
        try {
            URL url = new URL(i_url);
            String domain = url.getHost();
            return (domain.indexOf("myself-bbs") >= 0) ? myselfParser(i_url, movie) :
                    (domain.indexOf("gamer") >= 0) ? gamerParser(i_url, movie) : new ArrayList<Video>();
        } catch (MalformedURLException e) {
            e.printStackTrace();
        }
        return new ArrayList<Video>();
    }

    private ArrayList<Video> myselfParser(String url, Movie movie) {
        String html = ShareData.getInstance().GetHttps(url, false);
        String intro = ShareData.getInstance().str_between(html, "<div id=\"info_introduction_text\" style=\"display:none;\">", "</div>");
        movie.setDescription(intro);

        String htmlCode = ShareData.getInstance().str_between(html, "<ul class=\"main_list\"><li>", "</div>");
        String[] videoBlock = htmlCode.split("<a href=\"javascript:;\"");
        ArrayList<Video> VideosList = new ArrayList<Video>();

        if (videoBlock.length >= 1) {
            for (int i = 1; i < videoBlock.length; i++) {
                String cur_video_html = videoBlock[i];
                String video_title = ShareData.getInstance().str_between(cur_video_html, ">", "</a>");
                Pattern pattern = Pattern.compile("data-href=\"(.*?)\r\" target=\"_blank\" class=\"(.*?)\">(.*?)</a></li>");
                Matcher matcher = pattern.matcher(cur_video_html);

                // Get all video
                Video Video = new Video();
                while (matcher.find()) {
                    com.wind.tvplayer.model.video.Site siteMovie = new com.wind.tvplayer.model.video.Site();
                    String video_site_link = matcher.group(1).trim();
                    String video_site_class = matcher.group(2).trim();
                    String video_site_name = matcher.group(3).trim();
                    video_site_name = ShareData.getInstance().stripHtml(video_site_name);
                    if (video_site_name.compareTo("站內") == 0) {
                        siteMovie.setSiteName(video_site_name);
                        siteMovie.setSiteLink(video_site_link);
                        Video.setSiteMovieList(siteMovie);
                    }
                }

                Video.setVideoTitle(video_title);
                VideosList.add(Video);
            }
        }

        return VideosList;
    }

    private ArrayList<Video> gamerParser(String url, Movie movie) {
        String html = ShareData.getInstance().GetHttps(url, false);
        String htmlCode = ShareData.getInstance().str_between(html, "<div class=\"anime_name\">", "<div class=\"link\">");
        String intro = ShareData.getInstance().str_between(htmlCode, "<div class='data_intro'>", "<div class=\"link\">").trim();
        intro = ShareData.getInstance().stripHtml(intro);
        movie.setDescription(intro);
        String videoHtml = ShareData.getInstance().str_between(htmlCode, "<section class=\"season\">", "</section>").trim();

        // Get all video
        ArrayList<Video> videoMoviesList = new ArrayList<Video>();
        Pattern pattern = Pattern.compile("href=\"(.*?)\">(.*?)</a>");
        Matcher matcher = pattern.matcher(videoHtml);

        while (matcher.find()) {
            Video videoMovie = new Video();
            com.wind.tvplayer.model.video.Site siteMovie = new com.wind.tvplayer.model.video.Site();
            String video_url = "http://ani.gamer.com.tw/animeVideo.php" + matcher.group(1);
            String video_title = matcher.group(2).trim();
            siteMovie.setSiteName("gamer");
            siteMovie.setSiteLink(video_url);
            videoMovie.setSiteMovieList(siteMovie);
            videoMovie.setVideoTitle(video_title);
            videoMoviesList.add(videoMovie);
        }

        return videoMoviesList;
    }
}
