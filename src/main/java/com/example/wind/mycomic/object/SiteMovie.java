package com.example.wind.mycomic.object;

import java.util.ArrayList;

/**
 * Created by wind on 2017/1/11.
 */

public class SiteMovie {
    public SiteMovie() {}

    public String getSiteLink() {
        return siteLink;
    }

    public void setSiteLink(String siteLink) {
        this.siteLink = siteLink;
    }

    public String getSiteName() {
        return siteName;
    }

    public void setSiteName(String siteName) {
        this.siteName = siteName;
    }

    private String siteName;
    private String siteLink;
}
