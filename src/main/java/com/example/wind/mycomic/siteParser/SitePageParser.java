package com.example.wind.mycomic.siteParser;

import com.example.wind.mycomic.ShareDataClass;
import com.example.wind.mycomic.object.Movie;
import com.example.wind.mycomic.object.SiteMovie;
import com.example.wind.mycomic.object.VideoMovie;
import com.example.wind.mycomic.utils.PlayMovie;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import java.net.MalformedURLException;
import java.net.URL;
import java.util.ArrayList;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

/**
 * Created by wind on 2017/7/24.
 */

public class SitePageParser {
    public ArrayList<VideoMovie> doParser(String i_url, Movie movie) {
        ArrayList<VideoMovie> result_movie_list = null;
        try {
            URL url = new URL(i_url);
            String domain = url.getHost();
            if (domain.indexOf("myself-bbs") >= 0) {
                result_movie_list = myselfParser(i_url, movie);
            } else if (domain.indexOf("gamer") >= 0) {
                result_movie_list = gamerParser(i_url, movie);
            } else if (domain.indexOf("maplestage") >= 0) {
                result_movie_list = mapleStageParser(i_url, movie);
            } else if (domain.indexOf("anime1.me") >= 0) {
                result_movie_list = anime1Parser(i_url, movie);
            }
        } catch (MalformedURLException e) {
            e.printStackTrace();
        }
        finally {
            return result_movie_list;
        }
    }

    private ArrayList<VideoMovie> myselfParser(String url, Movie movie) {
        String html = ShareDataClass.getInstance().GetHttps(url, false);
        String intro = ShareDataClass.getInstance().str_between(html, "<div id=\"info_introduction_text\" style=\"display:none;\">", "</div>");
        movie.setDescription(intro);

        String htmlCode = ShareDataClass.getInstance().str_between(html, "<ul class=\"main_list\"><li>", "</div>");
        String[] videoBlock = htmlCode.split("<a href=\"javascript:;\"");
        ArrayList<VideoMovie> videoMoviesList = new ArrayList<VideoMovie>();

        if (videoBlock.length >= 1) {
            for (int i = 1; i < videoBlock.length; i++) {
                String cur_video_html = videoBlock[i];
                String video_title = ShareDataClass.getInstance().str_between(cur_video_html, ">", "</a>");
                Pattern pattern = Pattern.compile("data-href=\"(.*?)\r\" target=\"_blank\" class=\"(.*?)\">(.*?)</a></li>");
                Matcher matcher = pattern.matcher(cur_video_html);

                // Get all video
                VideoMovie videoMovie = new VideoMovie();
                while (matcher.find()) {
                    SiteMovie siteMovie = new SiteMovie();
                    String video_site_link = matcher.group(1).trim();
                    String video_site_class = matcher.group(2).trim();
                    String video_site_name = matcher.group(3).trim();
                    video_site_name = ShareDataClass.getInstance().stripHtml(video_site_name);
                    if (video_site_name.compareTo("站內") == 0) {
                        siteMovie.setSiteName(video_site_name);
                        siteMovie.setSiteLink(video_site_link);
                        videoMovie.setSiteMovieList(siteMovie);
                    }
                }

                videoMovie.setVideoTitle(video_title);
                videoMoviesList.add(videoMovie);
            }
        }

        return videoMoviesList;
    }

    private ArrayList<VideoMovie> gamerParser(String url, Movie movie) {
        String html = ShareDataClass.getInstance().GetHttps(url, false);
        String htmlCode = ShareDataClass.getInstance().str_between(html, "<div class=\"anime_name\">", "<div class=\"link\">");
        String intro = ShareDataClass.getInstance().str_between(htmlCode, "<div class='data_intro'>", "<div class=\"link\">").trim();
        intro = ShareDataClass.getInstance().stripHtml(intro);
        movie.setDescription(intro);
        String videoHtml = ShareDataClass.getInstance().str_between(htmlCode, "<section class=\"season\">", "</section>").trim();

        // Get all video
        ArrayList<VideoMovie> videoMoviesList = new ArrayList<VideoMovie>();
        Pattern pattern = Pattern.compile("href=\"(.*?)\">(.*?)</a>");
        Matcher matcher = pattern.matcher(videoHtml);

        while (matcher.find()) {
            VideoMovie videoMovie = new VideoMovie();
            SiteMovie siteMovie = new SiteMovie();
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

    private ArrayList<VideoMovie> mapleStageParser(String url, Movie movie) {
        String title = movie.getTitle();
        if (title.indexOf("/") >= 0) {
            title = title.substring(0, title.indexOf("/")).trim();
        }
        String query_header = "{\"queries\":[{\"name\":\"episodes\",\"query\":{\"sort\":\"top\",\"take\":100,";
        String query_footer = "}}]}";
        String query_data = query_header + "\"type\":\"" + ShareDataClass.getInstance().site_name_to_type.get(movie.getType()) + "\",\"slug\":\"" + title + "\"" + query_footer;
        String html = ShareDataClass.getInstance().PostHttps("http://maplestage.com/v1/query", query_data, "application/json");

        /*
        * POST:
            http://maplestage.com/v1/query
        HEADER:
            Content-Type: application/json
            Host: maplestage.com
        PARAMS:
            {"queries":[{"name":"episodes","query":{"sort":"top","take":100,"type":"楓林網 - 台灣戲劇","slug":"1989一念間(愛上不能愛的人)"}}]}
        * */

        // Get all video
        ArrayList<VideoMovie> videoMoviesList = new ArrayList<VideoMovie>();

        try {
            JSONObject jObject = new JSONObject(html);
            JSONArray jArray = jObject.getJSONArray("episodes");
            for (int i = 0 ; i < jArray.length(); i++) {
                VideoMovie videoMovie = new VideoMovie();
                SiteMovie siteMovie = new SiteMovie();
                JSONObject vObj = jArray.getJSONObject(i);
                String v_title = vObj.getString("title");
                String v_link = vObj.getString("href");
                siteMovie.setSiteName("Maplestage");
                siteMovie.setSiteLink("http://maplestage.com/" + v_link);
                videoMovie.setSiteMovieList(siteMovie);
                videoMovie.setVideoTitle(v_title);
                videoMoviesList.add(videoMovie);
            }
        } catch (JSONException e) {
            e.printStackTrace();
        }

        return videoMoviesList;
    }

    private ArrayList<VideoMovie> anime1Parser(String url, Movie movie) {
        String html = ShareDataClass.getInstance().GetHttps(url, false);
        String videoHtml = ShareDataClass.getInstance().str_between(html, "<main id=\"main\"", "</main>");
        movie.setDescription("");
        // Get all video
        ArrayList<VideoMovie> videoMoviesList = new ArrayList<VideoMovie>();
        Pattern pattern = Pattern.compile("<h2 class=\"entry-title\"><a href=\"(.+?)\".*?\">(.+?)</a></h2>");
        Matcher matcher = pattern.matcher(videoHtml);

        while (matcher.find()) {
            VideoMovie videoMovie = new VideoMovie();
            SiteMovie siteMovie = new SiteMovie();
            String video_url = matcher.group(1).trim();
            String video_title = matcher.group(2).trim();
            siteMovie.setSiteName("anime1");
            siteMovie.setSiteLink(video_url);
            videoMovie.setSiteMovieList(siteMovie);
            videoMovie.setVideoTitle(video_title);
            videoMoviesList.add(videoMovie);
        }

        return videoMoviesList;
    }
}
