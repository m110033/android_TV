package com.example.wind.mycomic.siteParser;

import java.util.ArrayList;

/**
 * Created by wind on 2017/7/18.
 */

public class PlaySite {
    public ArrayList<PlayList> getPlayList() {
        return playList;
    }

    public void setPlayList(PlayList playList) {
        this.playList.add(playList);
    }

    private ArrayList<PlayList> playList = new ArrayList<PlayList>();
}
