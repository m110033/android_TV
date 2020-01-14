package com.example.wind.mycomic.object;

import java.util.ArrayList;

/**
 * Created by wind on 2017/1/11.
 */

public class VideoMovie {
    public VideoMovie() {
        this.siteMovieList = new ArrayList<SiteMovie>();
    }

    public VideoMovie(String _title, SiteMovie _siteMovie) {
        this.setVideoTitle(_title);
        this.setSiteMovieList(_siteMovie);
    }

    public String getVideoTitle() {
        return videoTitle;
    }

    public void setVideoTitle(String videoTitle) {
        this.videoTitle = videoTitle;
    }

    public ArrayList<SiteMovie> getSiteMovieList() {
        return siteMovieList;
    }

    public void setSiteMovieList(SiteMovie siteMovie) {
        this.siteMovieList.add(siteMovie);
    }

    private String videoTitle;
    private ArrayList<SiteMovie> siteMovieList;
}
