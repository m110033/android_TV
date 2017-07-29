package com.example.wind.mycomic.siteParser;

import com.example.wind.mycomic.ShareDataClass;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.Iterator;

/**
 * Created by wind on 2017/7/20.
 */

public class VideoSiteParser {
    public String doParser(final String url, final String video_site) {
        String final_video_url = "";

        /*
        if (url.indexOf("iframe") >= 0) {
            //url = ShareDataClass.getInstance().str_between(url, "src=\"", "\"]");
        } else if (url.indexOf("http") >= 0) {
            //
        } else {
            //
        }*/
        switch (video_site) {
            case "dailymotion":
                final_video_url = dailymotionParser("http://www.dailymotion.com/embed/video/" + url);
                break;
            case "youtube":
                final_video_url = youtubeParser("https://www.youtube.com/watch?v=" + url);
                break;
            case "html":
                break;
            default:
                break;
        }

        return final_video_url;
    }

    private String youtubeParser(String url) {
        String video_url = "";
        HashMap<Integer, String> video_url_map = new HashMap<Integer, String>();

        String fmtPrefs = "22,35,18,34,6,5";
        String[] formats = fmtPrefs.split(",");
        String html = ShareDataClass.getInstance().GetHttps(url);

        String[][] urlMapArr = new String[][] {
            {"\\\"url_encoded_fmt_stream_map\\\":\\\"", "\\\""},
            {"\\\"url_encoded_fmt_stream_map\\\": \\\"", "\\\""},
            {"\\\"url_encoded_fmt_stream_map\\\",\\\"", "\\\""},
            {"\\\"url_encoded_fmt_stream_map\\\", \\\"", "\\\""}
        };

        String[] urlList = null;
        for (int i = 0; i < urlMapArr.length; i++) {
            String cur_separator = urlMapArr[i][0];
            String cur_separator_end = urlMapArr[i][1];
            if (html.indexOf(cur_separator) >= 0) {
                String fmt_stream_str = ShareDataClass.getInstance().str_between(html, cur_separator, cur_separator_end).trim();
                urlList = fmt_stream_str.split(",");
                break;
            }
        }

        if (urlList != null) {
            for (int i = 0; i < urlList.length; i++) {
                String cur_url = urlList[i];
                cur_url = cur_url.replace("\\\\u0026", "&").replace("\\\\u003d", "=");
                String truly_url = ShareDataClass.getInstance().decode(cur_url);
                String i_tag =  ShareDataClass.getInstance().str_between(truly_url, "itag=", "&").trim();
                Integer prefKey = -1;
                for (int j = 0; j < formats.length; j++) {
                    String cur_format = formats[j];
                    if (i_tag.compareTo(cur_format) == 0) {
                        truly_url = ShareDataClass.getInstance().str_between(truly_url, "url=", "&").trim();
                        video_url_map.put(j,  truly_url);
                        break;
                    }
                }
            }
        }

        if (video_url_map.size() > 0) {
            video_url = video_url_map.entrySet().iterator().next().getValue();
        }

        return video_url;
    }

    private String dailymotionParser(String url) {
        String video_url = "";

        String fmtPrefs = "1080,720,480,360,240";
        String[] formats = fmtPrefs.split(",");

        String html = ShareDataClass.getInstance().GetHttps(url);
        html = html.replace("&deg;", "");
        String javascript_html = ShareDataClass.getInstance().str_between(html, "config = ", ";").trim();
        try {
            JSONObject movieJsonObj = new JSONObject(javascript_html);
            JSONObject videoQAs = movieJsonObj.getJSONObject("metadata").getJSONObject("qualities");
            Iterator<String> iter = videoQAs.keys();
            for(int i = 0; i < formats.length && video_url.compareTo("") == 0; i++) {
                if (videoQAs.has(formats[i])) {
                    String i_tag = formats[i];
                    JSONArray videoArr = videoQAs.getJSONArray(i_tag);
                    for (int j = 0; j < videoArr.length(); j++) {
                        String type = ((JSONObject) videoArr.get(j)).getString("type");
                        String v_link = ((JSONObject) videoArr.get(j)).getString("url");
                        if (type.compareTo("video/mp4") == 0) {
                            video_url = v_link;
                            break;
                        }
                    }
                }
            }
        } catch (JSONException e) {
            e.printStackTrace();
        }

        return video_url;
    }

    private String lineTVParser(String url) {
        String video_url = "";
        return video_url;
    }

}
