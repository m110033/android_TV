package com.wind.tvplayer.model.video;

import java.util.ArrayList;

public class VideoResponse {
    private String description;
    private ArrayList<VideoInfo> videoList;

    public String getDescription() {
        return description;
    }

    public ArrayList<VideoInfo> getVideoList() {
        return videoList;
    }

    public void setDescription(String description) {
        this.description = description;
    }

    public void setVideoList(ArrayList<VideoInfo> videoList) {
        this.videoList = videoList;
    }
}
