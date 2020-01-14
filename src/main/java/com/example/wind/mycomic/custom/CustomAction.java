package com.example.wind.mycomic.custom;

import com.example.wind.mycomic.object.VideoMovie;

/**
 * Created by wind on 2016/12/31.
 */

public class CustomAction  {
    Integer id = -1;
    String title = "";
    VideoMovie videoMovie;

    public CustomAction(Integer id, VideoMovie videoMovie) {
        this.id = id;
        this.title = videoMovie.getVideoTitle();
        this.videoMovie = videoMovie;
    }

    public void setId(Integer id) { this.id = id; }

    public Integer getId() { return this.id; }

    public void setTitle(String title) { this.title = title; }

    public String getTitle() { return this.title; }

    public VideoMovie getVideoMovie() {
        return videoMovie;
    }

    public void setVideoMovie(VideoMovie videoMovie) {
        this.videoMovie = videoMovie;
    }

}