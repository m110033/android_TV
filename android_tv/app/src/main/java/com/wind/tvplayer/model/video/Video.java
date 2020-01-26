package com.wind.tvplayer.model.video;

import java.util.ArrayList;

public class Video {
    public Video() {
        this.siteMovieList = new ArrayList<Site>();
    }

    public String getVideoTitle() {
        return videoTitle;
    }

    public void setVideoTitle(String videoTitle) {
        this.videoTitle = videoTitle;
    }

    public ArrayList<Site> getSiteMovieList() {
        return siteMovieList;
    }

    public void setSiteMovieList(Site siteMovie) {
        this.siteMovieList.add(siteMovie);
    }

    // Private Data Member
    private ArrayList<Site> siteMovieList;
    private String videoTitle;
}
