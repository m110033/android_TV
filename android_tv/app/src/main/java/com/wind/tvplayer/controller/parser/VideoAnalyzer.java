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
        cookieMap.put("Referer", url_referer);
        cookieMap.put("Origin", url_origin);
        cookieMap.put("User-Agent", "Mozilla/5.0 (Windows NT 10.0; Win64; x64) AppleWebKit/537.36 (KHTML, like Gecko) Chrome/73.0.3683.103 Safari/537.36");
    }

    private void gamerParser() {
        //Get device name
        String refSN = videoUrl.substring(videoUrl.indexOf("=") + 1);
        String url = "https://ani.gamer.com.tw/ajax/getdeviceid.php?id=" + ShareVideo.getInstance().deviceID;
        String devideHtml = ShareData.getInstance().GetHttps(url, false);
        ShareVideo.getInstance().deviceID = ShareData.getInstance().str_between(devideHtml, "\"deviceid\":\"", "\"}");
        String SN = "";
        if(videoUrl.indexOf("animeVideo") < 0) {
            String refHtml = "https://ani.gamer.com.tw/animeRef.php?sn=" + refSN;
            String videoHtml = ShareData.getInstance().GetHttps(refHtml, false);
            SN = ShareData.getInstance().str_between(videoHtml, "sn=", "\"");
        } else {
            SN = ShareData.getInstance().str_between(videoUrl, "sn=", "\"");
        }
        // https://i2.bahamut.com.tw/JS/ad/animeVideo2.js
        String adHtml = ShareData.getInstance().GetHttps("https://i2.bahamut.com.tw/JS/ad/animeVideo.js", false);
        String adID = ShareData.getInstance().str_between(adHtml, "id=", "\"");
        int count = 0;
        try {
            JSONObject readObj = null;
            while (true) {
                ShareData.getInstance().GetHttps("https://ani.gamer.com.tw/ajax/unlock.php?sn=" + SN + "&ttl=0", false);
                String checklock = ShareData.getInstance().GetHttps("https://ani.gamer.com.tw/ajax/checklock.php?device=" + ShareVideo.getInstance().deviceID + "&sn=" + SN, false);
                try {
                    readObj = new JSONObject(checklock);
                    if (readObj.getInt("error") == 0) {
                        break;
                    }
                } catch (Exception e) {
                    Log.e(TAG, "Try to get checklock failed");
                }
                Thread.sleep(2000);
                count++;
                if (count > 10) {
                    throw new RuntimeException("Maximum retry reached.");
                }
            }
            //Unlock AD
            readObj = null;
            count = 0;
            ShareData.getInstance().GetHttps("https://ani.gamer.com.tw/ajax/videoCastcishu.php?s=" + adID + "&sn=" + SN, false);
            ShareData.getInstance().GetHttps("https://ani.gamer.com.tw/ajax/unlock.php?sn=" + SN + "&ttl=0", false);
            while (true) {
                ShareData.getInstance().GetHttps("https://ani.gamer.com.tw/ajax/videoCastcishu.php?s=" + adID + "&sn=" + SN + "&ad=end", false);
                ShareData.getInstance().GetHttps("https://ani.gamer.com.tw/ajax/unlock.php?sn=" + SN + "&ttl=0", false);
                String m3u8Url = ShareData.getInstance().GetHttps("https://ani.gamer.com.tw/ajax/m3u8.php?sn=" + SN + "&device=" + ShareVideo.getInstance().deviceID, false);
                try {
                    readObj = new JSONObject(m3u8Url);
                } catch (Exception e) {
                    Log.e(TAG, "Try to get m3u8 URL failed");
                    Thread.sleep(2000);
                    count++;
                    if (count > 10) {
                        throw new RuntimeException("Maximum retry reached.");
                    }
                    continue;
                }
                String m3u8_src = readObj.has("src") ? readObj.getString("src") : "";
                String m3u8_error_code = readObj.has("error") ? readObj.getString("error") : "0";
                if (m3u8_error_code.compareTo("7") != 0 && m3u8_error_code.compareTo("15") != 0 && m3u8_src != "") {
                    ShareData.getInstance().GetHttps("https://ani.gamer.com.tw/ajax/videoCastcishu.php?s=" + adID + "&sn=" + SN + "&ad=end", false);
                    String m3u8_vido_url = "https:" + m3u8_src.replace("\\/", "/");
                    String url_referer = "https://ani.gamer.com.tw/animeVideo.php?sn=" + SN;
                    String url_origin = "https://ani.gamer.com.tw";
                    videoParser.doParser(m3u8_vido_url, "m3u8", url_referer, url_origin);
                    videoLink = (videoParser.getVideoMap().size() > 0) ? videoParser.getVideoMap().entrySet().iterator().next().getValue().getSiteLink() : "";
                    cookieMap.put("Referer", url_referer);
                    cookieMap.put("Origin", url_origin);
                    cookieMap.put("User-Agent", "Mozilla/5.0 (Windows NT 10.0; Win64; x64) AppleWebKit/537.36 (KHTML, like Gecko) Chrome/73.0.3683.103 Safari/537.36");
                    break;
                }
            }
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
