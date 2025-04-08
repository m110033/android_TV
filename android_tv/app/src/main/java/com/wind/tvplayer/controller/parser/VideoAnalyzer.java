package com.wind.tvplayer.controller.parser;

import android.net.Uri;
import android.util.Log;

import com.wind.tvplayer.common.ShareData;
import com.wind.tvplayer.common.ShareVideo;

import org.json.JSONObject;

import java.util.HashMap;
import java.util.Map;

public class VideoAnalyzer {
    private final String TAG = "videoAnalyzer";

    private final AnimeFetcher animeFetcher = new AnimeFetcher();

    public void doParser(String url) {
        videoLink = "";
        videoUrl = url;
        cookieMap.clear();
        Thread thread = new Thread(new Runnable() {
            @Override
            public void run() {
                if (videoUrl.indexOf("myself-bbs.com") >= 0) {
                    myselfParser();
                } else if (videoUrl.indexOf("ani.gamer.com.tw") >= 0) {
                    gamerParser();
                }
            }
        });
        try {
            thread.start();
            thread.join();
        } catch (InterruptedException e) {
            e.printStackTrace();
        }
    }

    private void myselfParser() {
        String video_site = "myself";
        Uri uri = Uri.parse(videoUrl);
        String url_origin = "https://" + uri.getAuthority();
        String url_referer = videoUrl;
        videoParser.doParser(videoUrl, video_site);
        videoLink = (videoParser.getVideoMap().size() > 0) ? videoParser.getVideoMap().entrySet().iterator().next().getValue().getSiteLink() : "";
//        cookieMap.put("Referer", url_referer);
//        cookieMap.put("Origin", url_origin);
        cookieMap.put("User-Agent", "Mozilla/5.0 (Windows NT 10.0; Win64; x64) AppleWebKit/537.36 (KHTML, like Gecko) Chrome/73.0.3683.103 Safari/537.36");
    }

    private void gamerParser() {
        try {
            JSONObject object = animeFetcher.getM3U8Dict(videoUrl);
            String m3u8Url = object.getString("m3u8Url");
            String url_referer = object.getString("referer");
            String url_origin = "https://ani.gamer.com.tw";
            videoParser.doParser(m3u8Url,"m3u8", url_referer, url_origin);
            videoLink = (videoParser.getVideoMap().size() > 0) ? videoParser.getVideoMap().entrySet().iterator().next().getValue().getSiteLink() : "";
            cookieMap.put("Referer", url_referer);
            cookieMap.put("Origin", url_origin);
            cookieMap.put("User-Agent", "Mozilla/5.0 (Windows NT 10.0; Win64; x64) AppleWebKit/537.36 (KHTML, like Gecko) Chrome/73.0.3683.103 Safari/537.36");
        } catch (Exception e) {
            Log.e(TAG, e.getMessage());
        }
    }

    public String getVideoLink() {
        return videoLink;
    }

    public Map<String, String> getCookieMap() {
        return cookieMap;
    }

    private String videoLink = "";  // Destination URL
    private String videoUrl = ""; // Source URL
    private Video videoParser = new Video();
    private Map<String, String> cookieMap = new HashMap<String, String>();
}
