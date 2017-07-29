package com.example.wind.mycomic.utils;

/**
 * Created by wind on 2017/1/7.
 */

import java.net.URI;

public class PlayMovie {
    public String getVideo_title() {
        return video_title;
    }

    public void setVideo_title(String video_title) {
        this.video_title = video_title;
    }

    public String getVideo_url() {
        return video_url;
    }

    public void setVideo_url(String video_url) {
        this.video_url = video_url;
    }

    public String getVideo_intro() {
        return video_intro;
    }

    public void setVideo_intro(String video_intro) {
        this.video_intro = video_intro;
    }

    public String getMovie_category() {
        return movie_category;
    }

    public void setMovie_category(String movie_category) {
        this.movie_category = movie_category;
    }

    public String getTruly_link() {
        return truly_link;
    }

    public void setTruly_link(String truly_link) {
        this.truly_link = truly_link;
    }

    private String video_img;

    public URI getVideo_img_Uri() {
        return video_img_Uri;
    }

    public void setVideo_img_Uri(URI video_img_Uri) {
        this.video_img_Uri = video_img_Uri;
    }

    public String getVideo_img() {
        return video_img;
    }

    public void setVideo_img(String video_img) {
        this.video_img = video_img;
    }

    public String getMovie_title() {
        return movie_title;
    }

    public void setMovie_title(String movie_title) {
        this.movie_title = movie_title;
    }

    public long getDuration() {
        return duration;
    }

    public void setDuration(long duration) {
        this.duration = duration;
    }

    public Integer getMovie_categoryIndex() {
        return movie_categoryIndex;
    }

    public void setMovie_categoryIndex(Integer movie_categoryIndex) {
        this.movie_categoryIndex = movie_categoryIndex;
    }

    public String getVideo_type() {
        return video_type;
    }

    public void setVideo_type(String video_type) {
        this.video_type = video_type;
    }

    private Integer movie_categoryIndex = 0;
    private long duration = 0;
    private String movie_title = "";
    private String truly_link = "";
    private String video_title = "";
    private String video_url = "";
    private String video_intro = "";
    private String video_type = "";
    private String movie_category = "";
    private URI video_img_Uri;
}
