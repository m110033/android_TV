package com.example.wind.mycomic.object;

import java.util.ArrayList;

/**
 * Created by wind on 2017/1/11.
 */

public class SeasonMovie {
    public SeasonMovie() {
        this.videoMovieList = new ArrayList<VideoMovie>();
    }

    public SeasonMovie(String _name, String _img, String _page, VideoMovie _videoMovie) {
        this.setSeasonName(_name);
        this.setSeasonImg(_img);
        this.setSeasonPageLink(_page);
        this.setVideoMovieList(_videoMovie);
    }

    public String getSeasonName() {
        return seasonName;
    }

    public void setSeasonName(String seasonName) {
        this.seasonName = seasonName;
    }

    public String getSeasonImg() {
        return seasonImg;
    }

    public void setSeasonImg(String seasonImg) {
        this.seasonImg = seasonImg;
    }

    public ArrayList<VideoMovie> getVideoMovieList() {
        return videoMovieList;
    }

    public void setVideoMovieList(VideoMovie videoMovie) {
        this.videoMovieList.add(videoMovie);
    }

    public String getSeasonPageLink() {
        return seasonPageLink;
    }

    public void setSeasonPageLink(String seasonPageLink) {
        this.seasonPageLink = seasonPageLink;
    }

    private String seasonName;
    private String seasonPageLink;
    private String seasonImg;
    private ArrayList<VideoMovie> videoMovieList;
}
