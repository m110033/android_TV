package com.example.wind.mycomic.siteParser;

/**
 * Created by wind on 2017/11/7.
 */

public class SiteVideoDetail {
    public String getVideo_link() {
        return video_link;
    }

    public void setVideo_link(String video_link) {
        this.video_link = video_link;
    }

    public Integer getVideo_timelength() {
        return video_timelength;
    }

    public void setVideo_timelength(Integer video_timelength) {
        this.video_timelength = video_timelength;
    }

    public String getVideo_name() {
        return video_name;
    }

    public void setVideo_name(String video_name) {
        this.video_name = video_name;
    }

    private String video_link;
    private Integer video_timelength;
    private String video_name;
}
