package com.wind.tvplayer.common;

import com.wind.tvplayer.controller.parser.Site;
import com.wind.tvplayer.model.video.PlayMovie;

import java.util.ArrayList;

public class ShareVideo {
    private static final ShareVideo holder = new ShareVideo();
    public static ShareVideo getInstance() {
        return holder;
    }
    public static Site selectedSite;
    public static ArrayList<PlayMovie> playMovieList = new ArrayList<PlayMovie>();
    public static String deviceID = "010a4d70ec1a81a0675217c99cccacdc5c9e4a5410d7b6945e2942d19160";
}
