package com.example.wind.mycomic.siteParser;

import java.util.ArrayList;
import java.util.Date;

/**
 * Created by wind on 2017/7/17.
 */

public class PlayList {
    private ArrayList<PlayClass> playClass = new ArrayList<PlayClass>();
    private String playType;
    private Date playDate;

    public ArrayList<PlayClass> getPlayClass() {
        return playClass;
    }

    public void setPlayClass(PlayClass playClass) {
        this.playClass.add(playClass);
    }

    public String getPlayType() {
        return playType;
    }

    public void setPlayType(String playType) {
        this.playType = playType;
    }

    public Date getPlayDate() {
        return playDate;
    }

    public void setPlayDate(Date playDate) {
        this.playDate = playDate;
    }
}
