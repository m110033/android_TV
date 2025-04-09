package com.wind.tvplayer.model.video;

public class Site {
    public Site() {
    }

    public Site(String name, String link, String imgLink, String info, String parser) {
        siteName = name;
        siteLink = link;
        siteImgLink = imgLink;
        siteInfo = info;
        siteParser = parser;
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

    public String getSiteParser() {
        return siteParser;
    }
    public void setSiteParser(String siteParser) {
        this.siteParser = siteParser;
    }
    public String getSiteInfo() {
        return siteInfo;
    }
    public void setSiteInfo(String siteInfo) {
        this.siteInfo = siteInfo;
    }
    // Private Data Member
    private String siteName;
    private String siteLink;
    private Integer siteLength;
    private String siteImgLink;
    private String siteParser;
    private String siteInfo;
}
