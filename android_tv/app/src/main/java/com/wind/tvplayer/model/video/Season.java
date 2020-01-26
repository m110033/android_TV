package com.wind.tvplayer.model.video;

import java.util.ArrayList;

public class Season {
    public Season() {
        this.videoMovieList = new ArrayList<Video>();
    }

    public String getSeasonName() {
        return seasonName;
    }

    public void setSeasonName(String seasonName) {
        this.seasonName = seasonName;
    }

    public String getSeasonPageLink() {
        return seasonPageLink;
    }

    public void setSeasonPageLink(String seasonPageLink) {
        this.seasonPageLink = seasonPageLink;
    }

    public String getSeasonImg() {
        return seasonImg;
    }

    public void setSeasonImg(String seasonImg) {
        this.seasonImg = seasonImg;
    }

    public ArrayList<Video> getVideoMovieList() {
        return videoMovieList;
    }

    public void setVideoMovieList(ArrayList<Video> videoMovieList) {
        this.videoMovieList = videoMovieList;
    }

    // Private Data Member
    private String seasonName;
    private String seasonPageLink;
    private String seasonImg;
    private ArrayList<Video> videoMovieList;
}
