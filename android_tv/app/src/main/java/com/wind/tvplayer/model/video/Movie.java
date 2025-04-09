package com.wind.tvplayer.model.video;

import com.wind.tvplayer.common.ShareData;

import java.util.ArrayList;

public class Movie {
    public Movie() {
    }

    public String getMovieDate() {
        return movieDate;
    }

    public void setMovieDate(String movieDate) {
        this.movieDate = movieDate;
    }

    public long getCount() {
        return count;
    }

    public void incCount() {
        count++;
    }

    public long getId() {
        return id;
    }

    public void setId(long id) {
        this.id = id;
    }

    public String getUuid() {
        return uuid;
    }

    public void setUuid(String uuid) {
        this.uuid = uuid;
    }

    public String getTitle() {
        return title;
    }

    public void setTitle(String title) {
        this.title = title;
    }

    public String getType() {
        return type;
    }

    public void setType(String type) {
        this.type = type;
    }

    public String getViewNumber() {
        return viewNumber;
    }

    public void setViewNumber(String viewNumber) {
        this.viewNumber = viewNumber;
    }

    public String getDescription() {
        return description;
    }

    public void setDescription(String description) {
        this.description = description;
    }

    public String getBackgroundImageUrl() {
        return bgImageUrl;
    }

    public void setBackgroundImageUrl(String bgImageUrl) {
        this.bgImageUrl = bgImageUrl;
    }

    public String getCardImageUrl() {
        return cardImageUrl;
    }

    public void setCardImageUrl(String cardImageUrl) {
        this.cardImageUrl = cardImageUrl;
    }

    public String getStudio() {
        return studio;
    }

    public void setStudio(String studio) {
        this.studio = studio;
    }

    public Integer getCategoryIndex() {
        return ShareData.getInstance().siteNameList.indexOf(category);
    }

    public String getCategory() {
        return category;
    }

    public void setCategory(String category) {
        this.category = category;
    }

    public String getVideoPage() {
        return videoPage;
    }

    public void setVideoPage(String videoPage) {
        this.videoPage = videoPage;
    }

    private String movieDate;
    private long count = 0;
    private long id;
    private String uuid;
    private String title;
    private String type;
    private String viewNumber;
    private String description;
    private String bgImageUrl;
    private String cardImageUrl;
    private String studio;
    private String category;
    private String videoPage;
}
