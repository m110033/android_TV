package com.wind.tvplayer.model.video;

public class VideoInfo {
    private String title;
    private String videoUrl;
    private String siteName;

    public VideoInfo() {
    }

    public VideoInfo(String title, String videoUrl, String siteName) {
        this.title = title;
        this.videoUrl = videoUrl;
        this.siteName = siteName;
    }

    public String getTitle() {
        return title;
    }

    public String getVideoUrl() {
        return videoUrl;
    }

    public String getSiteName() {
        return siteName;
    }

    public void setTitle(String title) {
        this.title = title;
    }

    public void setVideoUrl(String videoUrl) {
        this.videoUrl = videoUrl;
    }

    public void setSiteName(String siteName) {
        this.siteName = siteName;
    }
}
