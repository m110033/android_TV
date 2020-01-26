package com.wind.tvplayer.model.video;

public class Site {
    public Site() {
    }

    public Site(String name, String link, String imgLink) {
        siteName = name;
        siteLink = link;
        siteImgLink = imgLink;
    }

    public String getSiteName() {
        return siteName;
    }

    public void setSiteName(String siteName) {
        this.siteName = siteName;
    }

    public String getSiteLink() {
        return siteLink;
    }

    public void setSiteLink(String siteLink) {
        this.siteLink = siteLink;
    }

    public String getSiteImgLink() {
        return siteImgLink;
    }

    public void setSiteImgLink(String siteImgLink) {
        this.siteImgLink = siteImgLink;
    }

    public Integer getSiteLength() {
        return siteLength;
    }

    public void setSiteLength(Integer siteLength) {
        this.siteLength = siteLength;
    }

    // Private Data Member
    private String siteName;
    private String siteLink;
    private Integer siteLength;
    private String siteImgLink;
}
