package com.example.wind.mycomic.utils;

import com.example.wind.mycomic.json.MovieSite;
import com.example.wind.mycomic.object.Movie;
import com.example.wind.mycomic.ShareDataClass;
import com.example.wind.mycomic.object.SeasonMovie;
import com.example.wind.mycomic.object.SiteMovie;
import com.example.wind.mycomic.object.VideoMovie;
import com.google.gson.Gson;

import org.apache.commons.lang3.StringEscapeUtils;

import java.util.ArrayList;
import java.util.regex.Pattern;

/**
 * Created by wind on 2017/1/4.
 */

public class SiteClass {
    private Integer maxHit;
    private ArrayList<Movie> movie_list;
    private ArrayList<String> movie_type_list;

    public ArrayList<String> getMovie_type_list() {
        return movie_type_list;
    }

    public void setMovie_type_list(ArrayList<String> movie_type_list) {
        this.movie_type_list = movie_type_list;
    }

    public ArrayList<Movie> getMovie_list() {
        return movie_list;
    }

    public void setMovie_list(ArrayList<Movie> movie_list) {
        this.movie_list = movie_list;
    }

    //private HashMap<String, Movie> movie_list;
    //private TreeMap<Integer, String> sortedIdList;

    /*public TreeMap<Integer, String> getSortedIdList() {
        return sortedIdList;
    }

    public void setSortedIdList(TreeMap<Integer, String> sortedIdList) {
        this.sortedIdList = sortedIdList;
    }*/

    public Integer getMaxHit() {
        return maxHit;
    }

    public void setMaxHit(Integer maxHit) {
        this.maxHit = maxHit;
    }
    /*
    public HashMap<String, Movie> getMovie_list() {
        return movie_list;
    }

    public void setMovie_list(HashMap<String, Movie> movie_list) {
        this.movie_list = movie_list;
    }
    */

    public void parserMovieSite(final String url, final String site_category) {
        movie_list = new ArrayList<Movie>();
        movie_type_list = new ArrayList<String>();

        /*this.movie_list = new HashMap<String, Movie>();
        this.sortedIdList = new TreeMap<Integer, String>(
            new Comparator<Integer>() {
                @Override
                public int compare(Integer o1, Integer o2) {
                    return o2.compareTo(o1);
                }
            }
        );*/
        final Thread thread = new Thread(new Runnable() {
            @Override
            public void run() {
                String source = ShareDataClass.getInstance().GetHttps(url);
                String movies = ShareDataClass.getInstance().str_between_l(source, "\"movies\": [", "]}");
                String first_chunk = ShareDataClass.getInstance().str_between(movies, "{\"", "\":");
                String[] tokens = movies.split(Pattern.quote("{\"" + first_chunk + "\""));
                maxHit = 0;
                for (int i = 1; i < tokens.length; i++) {
                    String jsonStr = "{\"" + first_chunk + "\"" + tokens[i];
                    jsonStr = jsonStr.trim();
                    jsonStr = jsonStr.substring(0, jsonStr.length() - 1);

                    Movie cur_movie_obj = new Movie();

                    String movie_date = ShareDataClass.getInstance().str_between(jsonStr, "\"movie_date\": \"", "\"");
                    movie_date = StringEscapeUtils.unescapeJava(movie_date);
                    String view_number = ShareDataClass.getInstance().str_between(jsonStr, "\"view_number\": \"", "\"");
                    if (view_number.trim().isEmpty()) {
                        view_number = "0";
                    }
                    String movie_type = ShareDataClass.getInstance().str_between(jsonStr, "\"movie_type\": \"", "\"");
                    movie_type = StringEscapeUtils.unescapeJava(movie_type);
                    String cur_movie_play = ShareDataClass.getInstance().str_between(jsonStr, "\"cur_movie_play\": \"", "\"");
                    cur_movie_play = StringEscapeUtils.unescapeJava(cur_movie_play);
                    String uuid = ShareDataClass.getInstance().str_between(jsonStr, "\"uuid\": \"", "\"");
                    String img = ShareDataClass.getInstance().str_between(jsonStr, "\"img\": \"", "\"");
                    img = StringEscapeUtils.unescapeJava(img);
                    String title = ShareDataClass.getInstance().str_between(jsonStr, "\"title\": \"", "\"");
                    title = StringEscapeUtils.unescapeJava(title);
                    String page_link = ShareDataClass.getInstance().str_between(jsonStr, "\"page_link\": \"", "\"");
                    String movie_info = ShareDataClass.getInstance().str_between(jsonStr, "\"movie_info\": \"", "\"");
                    movie_info = StringEscapeUtils.unescapeJava(movie_info);
                    String movie_total_number = ShareDataClass.getInstance().str_between(jsonStr, "\"movie_total_number\": \"", "\"");
                    movie_total_number = StringEscapeUtils.unescapeJava(movie_total_number);

                    String movie_videos_str = ShareDataClass.getInstance().str_between_l(jsonStr, "\"movie_videos\": [", "]");
                    String[] seasons_token = movie_videos_str.split(Pattern.quote("{\"season_sites\""));

                    for (int k = 1; k < seasons_token.length; k++) {
                        String seasonJsonStr = "{\"season_sites\"" + seasons_token[k];
                        seasonJsonStr = seasonJsonStr.trim();
                        seasonJsonStr = seasonJsonStr.substring(0, seasonJsonStr.length() - 1);
                        SeasonMovie seasonMovie = new SeasonMovie();

                        String season_name = ShareDataClass.getInstance().str_between(seasonJsonStr, "\"season_name\": \"", "\"");
                        season_name = StringEscapeUtils.unescapeJava(season_name);

                        String season_sites_str = ShareDataClass.getInstance().str_between_l(seasonJsonStr, "\"season_sites\": [", "]}");
                        String[] video_site_tokens = season_sites_str.split(Pattern.quote("{\"video_mirrors\""));
                        for (int video_index = 1; video_index < video_site_tokens.length; video_index++) {
                            String videoMirrorJsonStr = "{\"video_mirrors\"" + video_site_tokens[video_index];
                            videoMirrorJsonStr = videoMirrorJsonStr.trim();
                            videoMirrorJsonStr = videoMirrorJsonStr.substring(0, videoMirrorJsonStr.length() - 1);
                            VideoMovie videoMovie = new VideoMovie();

                            String video_name = ShareDataClass.getInstance().str_between(videoMirrorJsonStr, "\"video_name\": \"", "\"");
                            video_name = StringEscapeUtils.unescapeJava(video_name);

                            String video_mirrors_str = ShareDataClass.getInstance().str_between_l(videoMirrorJsonStr, "\"video_mirrors\": [", "]}");
                            String[] video_mirrors_token = video_mirrors_str.split(Pattern.quote("{\"video_mirror_name\""));
                            for (int l = 1; l < video_mirrors_token.length; l++) {
                                String videoSiteJsonStr = "{\"video_mirror_name\"" + video_mirrors_token[l];
                                videoSiteJsonStr = videoSiteJsonStr.trim();
                                videoSiteJsonStr = videoSiteJsonStr.substring(0, videoSiteJsonStr.length() - 1);
                                SiteMovie siteMovie = new SiteMovie();

                                String video_mirror_url = ShareDataClass.getInstance().str_between(videoSiteJsonStr, "\"video_mirror_url\": \"", "\"");
                                String video_mirror_name = ShareDataClass.getInstance().str_between(videoSiteJsonStr, "\"video_mirror_name\": \"", "\"");
                                video_mirror_name = StringEscapeUtils.unescapeJava(video_mirror_name);
                                siteMovie.setSiteName(video_mirror_name);
                                siteMovie.setSiteLink(video_mirror_url);
                                videoMovie.setSiteMovieList(siteMovie);
                            }
                            videoMovie.setVideoTitle(video_name);
                            seasonMovie.setVideoMovieList(videoMovie);
                        }
                        seasonMovie.setSeasonName(season_name);
                        seasonMovie.setSeasonImg("");
                        seasonMovie.setSeasonPageLink("");
                        cur_movie_obj.setSeasonMovieList(seasonMovie);
                    }

                    if (maxHit < Integer.parseInt(view_number)) {
                        maxHit = Integer.parseInt(view_number);
                    }

                    cur_movie_obj.setMovieDate(movie_date);
                    cur_movie_obj.setViewNumber(view_number);
                    cur_movie_obj.setType(movie_type);
                    cur_movie_obj.setUUID(uuid);
                    cur_movie_obj.setId(cur_movie_obj.getCount());
                    cur_movie_obj.incCount();
                    cur_movie_obj.setCardImageUrl(img);
                    cur_movie_obj.setDescription(movie_info);
                    cur_movie_obj.setTitle(title);
                    cur_movie_obj.setStudio(movie_type);
                    cur_movie_obj.setVideoPage(page_link);
                    cur_movie_obj.setBackgroundImageUrl(ShareDataClass.getInstance().default_fragment_background);
                    cur_movie_obj.setCategory(site_category);

                    if (cur_movie_obj.getSeasonMovieList().size() > 0) {
                        movie_list.add(cur_movie_obj);
                /*this.sortedIdList.put(Integer.parseInt(view_number), uuid);
                this.movie_list.put(uuid, cur_movie_obj);*/
                        // Add movie type
                        String[] cur_movie_type_arr = movie_type.split("/");
                        for (String cur_type : cur_movie_type_arr) {
                            cur_type = cur_type.trim();
                            if (!movie_type_list.contains(cur_type)) {
                                movie_type_list.add(cur_type);
                            }
                        }
                    }
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

    public void parserMovieSiteWithJson(final String url, final String site_category) {
        movie_list = new ArrayList<Movie>();
        movie_type_list = new ArrayList<String>();

        /*this.movie_list = new HashMap<String, Movie>();
        this.sortedIdList = new TreeMap<Integer, String>(
            new Comparator<Integer>() {
                @Override
                public int compare(Integer o1, Integer o2) {
                    return o2.compareTo(o1);
                }
            }
        );*/
        final Thread thread = new Thread(new Runnable() {
            @Override
            public void run() {
                String fileName = url.substring(url.lastIndexOf('/') + 1);
                if (fileName.indexOf("_") >= 0) {
                    fileName = fileName.substring(0, fileName.indexOf("_"));
                } else if (fileName.indexOf(".") >= 0) {
                    fileName = fileName.substring(0, fileName.indexOf("."));
                }
                String jsonStr = ShareDataClass.getInstance().GetHttps(url);
                Gson gson = new Gson();
                MovieSite movieSite = gson.fromJson(jsonStr, MovieSite.class);

                maxHit = 0;
                for(int i = 0 ; i < movieSite.getMovies().length ; i++)
                {
                    com.example.wind.mycomic.json.Movie movie = movieSite.getMovies()[i];
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
                    cur_movie_obj.setUUID(movie_index);
                    cur_movie_obj.setId(cur_movie_obj.getCount());
                    cur_movie_obj.incCount();
                    cur_movie_obj.setCardImageUrl(img);
                    cur_movie_obj.setTitle(title);
                    cur_movie_obj.setStudio(second_title);
                    cur_movie_obj.setVideoPage(page_link);
                    cur_movie_obj.setBackgroundImageUrl(ShareDataClass.getInstance().default_fragment_background);
                    cur_movie_obj.setCategory(site_category);
                    cur_movie_obj.setType(fileName);
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
}
