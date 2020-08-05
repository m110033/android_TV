package com.wind.tvplayer.controller.parser;

import android.net.Uri;

import com.wind.tvplayer.common.ShareData;
import com.wind.tvplayer.model.video.Site;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.Iterator;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.TreeMap;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

public class Video {
    public void doParser(final String url, final String video_site, final String refer, final String origin) {
        videoMap.clear();
        videoMap = new LinkedHashMap<String, Site>();
        switch (video_site.toLowerCase()) {
            case "m3u8":
                m3u8Parser(url, refer, origin);
                break;
            default:
                if (url.matches("^(http|https|ftp)://docs.google.com/.*")) {
                    youtubeParser(url);
                }
                break;
        }
    }

    public void doParser(final String url, final String video_site) {
        switch (video_site.toLowerCase()) {
            case "youtube":
                youtubeParser("https://www.youtube.com/watch?v=" + url);
                break;
            case "b站":
                BStieParser(url);
                break;
            case "m3u8":
                m3u8Parser(url);
                break;
            case "html":
                break;
            case "myself":
                myselfParser(url);
                break;
            default:
                if (url.matches("^(http|https|ftp)://docs.google.com/.*")) {
                    youtubeParser(url);
                }
                break;
        }
        return;
    }

    private void BStieParser(String url) {
        String bilibili_key = "94aba54af9065f71de72f5508f1cd42e";
        Uri uri = Uri.parse(url);
        String cid = uri.getQueryParameter("cid");
        String quality = uri.getQueryParameter("quality");
        if (cid == null) {
            String aid = uri.getQueryParameter("aid");
            if (aid != null) {
                String cid_json_str = ShareData.getInstance().GetHttps("http://www.bilibili.com/widget/getPageList?aid=" + aid, false);
                try {
                    JSONArray rootArr = new JSONArray(cid_json_str);
                    if (rootArr.length() > 0 ) {
                        cid = ((JSONObject) rootArr.get(0)).getString("cid");
                    }
                } catch (JSONException e) {
                    e.printStackTrace();
                }
            }
        }
        if (cid != null) {
            Map<String, String> treeMap = new TreeMap<String, String>();
            treeMap.put("appkey", "84956560bc028eb7");
            treeMap.put("cid", cid);
            treeMap.put("otype", "json");
            treeMap.put("quality", quality); // [112,80,48,16]
            treeMap.put("module", "bangumi");
            treeMap.put("qn", quality);
            //treeMap.put("format", "mp4");
            treeMap.put("type", "");
            String payload = "";
            for (Map.Entry<String, String> entry : treeMap.entrySet()) {
                String key = entry.getKey();
                String value = entry.getValue();
                payload += key + "=" + value + "&";
            }
            payload = payload.substring(0, payload.length() - 1);
            String sign = ShareData.getInstance().md5(payload + bilibili_key);
            String json_str = ShareData.getInstance().GetHttps("https://bangumi.bilibili.com/player/web_api/playurl?" + payload + "&sign=" + sign, false);
            try {
                JSONObject rootObj = new JSONObject(json_str);
                if (rootObj.has("durl")) {
                    JSONArray videoArr = rootObj.getJSONArray("durl");
                    JSONArray qualityArr = rootObj.getJSONArray("accept_quality");
                    qualityList.clear();
                    for (int j = 0; j < qualityArr.length(); j++) {
                        qualityList.add(qualityArr.getInt(j));
                    }
                    for (int j = 0; j < videoArr.length(); j++) {
                        // Integer cur_video_size = ((JSONObject) videoArr.get(j)).getInt("size");
                        Site siteVideoDetail = new Site();
                        siteVideoDetail.setSiteLink(((JSONObject) videoArr.get(j)).getString("url"));
                        siteVideoDetail.setSiteLength(((JSONObject) videoArr.get(j)).getInt("length"));
                        videoMap.put(((JSONObject) videoArr.get(j)).getString("order"), siteVideoDetail);
                    }
                }
            } catch (JSONException e) {
                e.printStackTrace();
            }
        }
    }

    private void youtubeParser(String url) {
        Map<String, Site> tempMap = new LinkedHashMap<String, Site>();
        String fmtPrefs = "22,35,18,34,6,5";
        String[] formats = fmtPrefs.split(",");
        String html = ShareData.getInstance().GetHttps(url, false);
        String[][] urlMapArr = new String[][] {
                {"\\\"url_encoded_fmt_stream_map\\\":\\\"", "\\\""},
                {"\\\"url_encoded_fmt_stream_map\\\": \\\"", "\\\""},
                {"\\\"url_encoded_fmt_stream_map\\\",\\\"", "\\\""},
                {"\\\"url_encoded_fmt_stream_map\\\", \\\"", "\\\""},
                {"\"url_encoded_fmt_stream_map\":\"", "\""},
                {"\"url_encoded_fmt_stream_map\": \"", "\""},
                {"\"url_encoded_fmt_stream_map\",\"", "\""},
                {"\"url_encoded_fmt_stream_map\", \"", "\""}
        };
        String[] urlList = null;
        for (int i = 0; i < urlMapArr.length; i++) {
            String cur_separator = urlMapArr[i][0];
            String cur_separator_end = urlMapArr[i][1];
            if (html.indexOf(cur_separator) >= 0) {
                String fmt_stream_str = ShareData.getInstance().str_between(html, cur_separator, cur_separator_end).trim();
                urlList = fmt_stream_str.split(",");
                break;
            }
        }

        if (urlList != null) {
            for (int i = 0; i < urlList.length; i++) {
                String cur_url = urlList[i];
                cur_url = cur_url.replace("\\\\u0026", "&").replace("\\\\u003d", "=");
                cur_url = cur_url.replace("\\u0026", "&").replace("\\u003d", "=");
                String[] urlData = cur_url.split("&");
                String v_tag = "";
                String v_link = "";
                for(int x = 0; x < urlData.length; x++) {
                    if(urlData[x].indexOf("=") >= 0) {
                        String tag = urlData[x].split("=")[0];
                        String tag_value = urlData[x].split("=")[1];
                        if (tag.compareTo("itag") == 0) {
                            v_tag = tag_value;
                        } else if (tag.compareTo("url") == 0) {
                            v_link = tag_value;
                        }
                    }
                }
                String truly_url = ShareData.getInstance().decode(v_link);
                Site siteVideoDetail = new Site();
                siteVideoDetail.setSiteLink(truly_url);
                siteVideoDetail.setSiteLength(0);
                tempMap.put(v_tag, siteVideoDetail);
            }
        }

        for(int i = 0; i < formats.length; i++) {
            String i_tag = formats[i];
            if (tempMap.containsKey(i_tag)) {
                videoMap.put(i_tag, tempMap.get(i_tag));
            }
        }
    }

    private void m3u8Parser(String url) {
        m3u8Parser(url, null, null);
    }

    private void m3u8Parser(String url, String refer, String origin) {
        Map<String, Site> tempMap = new LinkedHashMap<String, Site>();
        //Parser m3u8
        String m3u8_str = "";
        Map<String, String> video_list = new HashMap<String, String>();
        if(refer != null && origin != null) {
            m3u8_str = ShareData.getInstance().GetHttps(url, false, refer, origin);
        } else {
            m3u8_str = ShareData.getInstance().GetHttps(url, false);
        }
        String[] m3u8_data = m3u8_str.split(Pattern.quote("#EXT-X-STREAM-INF"));
        String fmtPrefs = "1920x1080,1280x720,960x540,640x360";
        String[] formats = fmtPrefs.split(",");
        for(int j = 1; j < m3u8_data.length; j++) {
            String[] lineData = m3u8_data[j].split("\n");
            String cur_data = lineData[0];
            String[] streamData = cur_data.split(",");
            for(int k = 0; k < streamData.length; k++) {
                if(streamData[k].contains("RESOLUTION")) {
                    String resolution = streamData[k].substring(streamData[k].indexOf("RESOLUTION") + "RESOLUTION".length() + 1);
                    String m3u8_link = lineData[1];
                    if(m3u8_link.substring(0, 4).compareTo("http") != 0) {
                        String prefixSite = url.substring(0, url.lastIndexOf("/") + 1);
                        m3u8_link = prefixSite + m3u8_link;
                    }
                    Site siteVideoDetail = new Site();
                    siteVideoDetail.setSiteLink(m3u8_link);
                    // Get duration
                    int duration = 0;
                    String digitRegex = "\\d+";
                    Pattern p = Pattern.compile(digitRegex);
                    String m3u8Data = "";
                    if(refer != null && origin != null) {
                        m3u8Data = ShareData.getInstance().GetHttps(m3u8_link, false, refer, origin);
                    } else {
                        m3u8Data = ShareData.getInstance().GetHttps(m3u8_link, false);
                    }
                    String[] lines = m3u8Data.split(System.getProperty("line.separator"));
                    for(int m = 0; m < lines.length; m++) {
                        String cur_line = lines[m];
                        if(cur_line.equals("#EXTM3U")){ //start of m3u8
                            //
                        }else if(cur_line.contains("#EXTINF")){ //once found EXTINFO use runner to get the next line which contains the media file, parse duration of the segment
                            Matcher matcher = p.matcher(cur_line);
                            matcher.find(); //find the first matching digit, which represents the duration of the segment, dont call .find() again that will throw digit which may be contained in the description.
                            if( (m + 1) < lines.length) {
                                String net_line = lines[++m];
                                duration += Integer.parseInt(matcher.group(0));
                            }
                        }
                    }
                    duration *= 1000; //seconds to millionseconds
                    siteVideoDetail.setSiteLength(duration);
                    tempMap.put(resolution, siteVideoDetail);
                }
            }
        }
        for(int i = 0; i < formats.length; i++) {
            String i_tag = formats[i];
            if (tempMap.containsKey(i_tag)) {
                videoMap.put(i_tag, tempMap.get(i_tag));
            }
        }
    }

    private void myselfParser(String url) {
        String host_url = url.replace("player/play", "vpx");
        String videoHtml = ShareData.getInstance().GetHttps(host_url, false, url, null);
        try {
            JSONObject videoObj = new JSONObject(videoHtml);
            JSONObject resolutionObj = videoObj.getJSONObject("video");
            JSONArray hostArr = videoObj.getJSONArray("host");
            if(resolutionObj.length() > 0) {
                String host = hostArr.getJSONObject(0).getString("host");
                Iterator<String> iter = resolutionObj.keys();
                while (iter.hasNext()) {
                    String key = iter.next();
                    if(key.compareTo("auto") == 0) {
                        continue;
                    }
                    try {
                        String value = resolutionObj.getString(key);
                        String video_link = host + value.replace("\\/", "/");
                        Site siteVideoDetail = new Site();
                        // Get duration
                        int duration = 0;
                        String digitRegex = "\\d+";
                        Pattern p = Pattern.compile(digitRegex);
                        String m3u8Data = ShareData.getInstance().GetHttps(video_link, false, url, null);
                        String[] lines = m3u8Data.split(System.getProperty("line.separator"));
                        for(int m = 0; m < lines.length; m++) {
                            String cur_line = lines[m];
                            if(cur_line.equals("#EXTM3U")){ //start of m3u8
                                //
                            }else if(cur_line.contains("#EXTINF")){ //once found EXTINFO use runner to get the next line which contains the media file, parse duration of the segment
                                Matcher matcher = p.matcher(cur_line);
                                matcher.find(); //find the first matching digit, which represents the duration of the segment, dont call .find() again that will throw digit which may be contained in the description.
                                if( (m + 1) < lines.length) {
                                    String net_line = lines[++m];
                                    duration += Integer.parseInt(matcher.group(0));
                                }
                            }
                        }
                        duration *= 1000; //seconds to millionseconds
                        siteVideoDetail.setSiteLength(duration);
                        siteVideoDetail.setSiteLink(video_link);
                        videoMap.put(key, siteVideoDetail);
                    } catch (JSONException e) {
                        e.printStackTrace();
                    }
                }
            }
        } catch (JSONException e) {
            e.printStackTrace();
        }
    }

    public List<Integer> getQualityList() {
        return qualityList;
    }

    public Map<String, Site> getVideoMap() {
        return videoMap;
    }

    private List<Integer> qualityList = new ArrayList<Integer>();
    private Map<String, Site> videoMap = new LinkedHashMap<String, Site>();
}
