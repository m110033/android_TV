package com.example.wind.mycomic.siteParser;

import java.util.HashMap;
import java.util.Map;

/**
 * Created by User on 2018/2/18.
 */

public class m3u8Site {
    public Map<String, String> getVideo_list() {
        return video_list;
    }

    public void setVideo_list(Map<String, String> video_list) {
        this.video_list = video_list;
    }

    public Integer getDuration() {
        return duration;
    }

    public void setDuration(Integer duration) {
        this.duration = duration;
    }

    public String getVideoLink() {
        return videoLink;
    }

    public void setVideoLink(String videoLink) {
        this.videoLink = videoLink;
    }

    Map<String, String> video_list = new HashMap<String, String>();
    Integer duration; //millionseconds
    String videoLink;
}
