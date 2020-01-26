package com.wind.tvplayer.model.Gson;

import com.google.gson.annotations.SerializedName;

public class Movie {
    @SerializedName("m_idx")
    private String index;
    @SerializedName("m_img")
    private String img;
    @SerializedName("m_t")
    private String title;
    @SerializedName("m_p_l")
    private String pageLink;
    @SerializedName("m_s_t")
    private String secondTitle;

    public String getIndex() {
        return index;
    }

    public void setIndex(String index) {
        this.index = index;
    }

    public String getImg() {
        return img;
    }

    public void setImg(String img) {
        this.img = img;
    }

    public String getTitle() {
        return title;
    }

    public void setTitle(String title) {
        this.title = title;
    }

    public String getPageLink() {
        return pageLink;
    }

    public void setPageLink(String pageLink) {
        this.pageLink = pageLink;
    }

    public String getSecondTitle() {
        return secondTitle;
    }

    public void setSecondTitle(String secondTitle) {
        this.secondTitle = secondTitle;
    }
}
