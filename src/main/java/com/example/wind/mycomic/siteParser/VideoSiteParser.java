package com.example.wind.mycomic.siteParser;

import android.net.Uri;

import com.example.wind.mycomic.ShareDataClass;
import com.example.wind.mycomic.object.SiteMovie;
import com.example.wind.mycomic.object.VideoMovie;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import java.util.HashMap;
import java.util.Iterator;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.TreeMap;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

/**
 * Created by wind on 2017/7/20.
 */

public class VideoSiteParser {
    public Map<String, SiteVideoDetail> doParser(final String url, final String video_site, final String refer, final String origin) {
        Map<String, SiteVideoDetail> videoMap = null;
        switch (video_site.toLowerCase()) {
            case "m3u8":
                videoMap = m3u8Parser(url, refer, origin);
                break;
            default:
                if (url.matches("^(http|https|ftp)://docs.google.com/.*")) {
                    videoMap = youtubeParser(url);
                }
                break;
        }
        return videoMap;
    }

    public Map<String, SiteVideoDetail> doParser(final String url, final String video_site) {
        Map<String, SiteVideoDetail> videoMap = null;
        switch (video_site.toLowerCase()) {
            case "dailymotion":
                videoMap = dailymotionParser("http://www.dailymotion.com/embed/video/" + url);
                break;
            case "youtube":
                videoMap = youtubeParser("https://www.youtube.com/watch?v=" + url);
                break;
            case "b站":
                videoMap = BStieParser(url);
                break;
            case "m3u8":
                videoMap = m3u8Parser(url);
                break;
            case "anime1":
                videoMap = anime1Parser(url);
                break;
            case "html":
                break;
            case "myself":
                videoMap = myselfParser(url);
                break;
            default:
                if (url.matches("^(http|https|ftp)://docs.google.com/.*")) {
                    videoMap = youtubeParser(url);
                }
                break;
        }
        return videoMap;
    }

    private Map<String, SiteVideoDetail> BStieParser(String url) {
        Map<String, SiteVideoDetail> videoMap = new LinkedHashMap<String, SiteVideoDetail>();

        String bilibili_key = "94aba54af9065f71de72f5508f1cd42e";
        Uri uri = Uri.parse(url);
        String cid = uri.getQueryParameter("cid");
        String quality = uri.getQueryParameter("quality");
        if (cid == null) {
            String aid = uri.getQueryParameter("aid");
            if (aid != null) {
                String cid_json_str = ShareDataClass.getInstance().GetHttps("http://www.bilibili.com/widget/getPageList?aid=" + aid, false);
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
            String sign = ShareDataClass.getInstance().md5(payload + bilibili_key);
            String json_str = ShareDataClass.getInstance().GetHttps("https://bangumi.bilibili.com/player/web_api/playurl?" + payload + "&sign=" + sign, ShareDataClass.getInstance().is_bsite_proxy);
            //String json_str = ShareDataClass.getInstance().GetHttps("https://bangumi.bilibili.com/player/web_api/playurl?" + payload + "&sign=" + sign, true);

            try {
                JSONObject rootObj = new JSONObject(json_str);
                if (rootObj.has("durl")) {
                    JSONArray videoArr = rootObj.getJSONArray("durl");
                    JSONArray qualityArr = rootObj.getJSONArray("accept_quality");

                    ShareDataClass.getInstance().qualityList.clear();
                    for (int j = 0; j < qualityArr.length(); j++) {
                        ShareDataClass.getInstance().qualityList.add(qualityArr.getInt(j));
                    }
                    for (int j = 0; j < videoArr.length(); j++) {
                        // Integer cur_video_size = ((JSONObject) videoArr.get(j)).getInt("size");
                        SiteVideoDetail siteVideoDetail = new SiteVideoDetail();
                        siteVideoDetail.setVideo_link(((JSONObject) videoArr.get(j)).getString("url"));
                        siteVideoDetail.setVideo_timelength(((JSONObject) videoArr.get(j)).getInt("length"));
                        videoMap.put(((JSONObject) videoArr.get(j)).getString("order"), siteVideoDetail);
                    }
                }
            } catch (JSONException e) {
                e.printStackTrace();
            }
        }

        return videoMap;
    }

    private Map<String, SiteVideoDetail> youtubeParser(String url) {
        Map<String, SiteVideoDetail> videoMap = new LinkedHashMap<String, SiteVideoDetail>();
        Map<String, SiteVideoDetail> tempMap = new LinkedHashMap<String, SiteVideoDetail>();

        String fmtPrefs = "22,35,18,34,6,5";
        String[] formats = fmtPrefs.split(",");
        String html = ShareDataClass.getInstance().GetHttps(url, false);

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
                String fmt_stream_str = ShareDataClass.getInstance().str_between(html, cur_separator, cur_separator_end).trim();
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
                String truly_url = ShareDataClass.getInstance().decode(v_link);
                SiteVideoDetail siteVideoDetail = new SiteVideoDetail();
                siteVideoDetail.setVideo_link(truly_url);
                siteVideoDetail.setVideo_timelength(0);
                tempMap.put(v_tag, siteVideoDetail);
            }
        }

        for(int i = 0; i < formats.length; i++) {
            String i_tag = formats[i];
            if (tempMap.containsKey(i_tag)) {
                videoMap.put(i_tag, tempMap.get(i_tag));
            }
        }

        return videoMap;
    }

    private Map<String, SiteVideoDetail> dailymotionParser(String url) {
        Map<String, SiteVideoDetail> videoMap = new LinkedHashMap<String, SiteVideoDetail>();
        Map<String, SiteVideoDetail> tempMap = new LinkedHashMap<String, SiteVideoDetail>();

        String fmtPrefs = "1080,720,480,380,360,240,144,auto";
        String[] formats = fmtPrefs.split(",");

        String html = ShareDataClass.getInstance().GetHttps(url, false);
        html = html.replace("&deg;", "");
        String javascript_html = ShareDataClass.getInstance().str_between(html, "config = ", ";").trim();
        try {
            JSONObject movieJsonObj = new JSONObject(javascript_html);
            JSONObject videoQAs = movieJsonObj.getJSONObject("metadata").getJSONObject("qualities");
            for(int i = 0; i < formats.length; i++) {
                String i_tag = formats[i];
                if(videoQAs.has(i_tag)) {
                    JSONArray videoArr = videoQAs.getJSONArray(i_tag);
                    for (int j = 0; j < videoArr.length(); j++) {
                        String type = ((JSONObject) videoArr.get(j)).getString("type");
                        String v_link = ((JSONObject) videoArr.get(j)).getString("url");
                        if (type.compareTo("video/mp4") == 0) {
                            SiteVideoDetail siteVideoDetail = new SiteVideoDetail();
                            siteVideoDetail.setVideo_link(v_link);
                            siteVideoDetail.setVideo_timelength(0);
                            tempMap.put(i_tag, siteVideoDetail);
                            break;
                        }
                    }
                }
            }
        } catch (JSONException e) {
            e.printStackTrace();
        }

        for(int i = 0; i < formats.length; i++) {
            String i_tag = formats[i];
            if (tempMap.containsKey(i_tag)) {
                videoMap.put(i_tag, tempMap.get(i_tag));
            }
        }

        return videoMap;
    }

    private Map<String, SiteVideoDetail> m3u8Parser(String url) {
        return m3u8Parser(url, null, null);
    }

    private Map<String, SiteVideoDetail> m3u8Parser(String url, String refer, String origin) {
        Map<String, SiteVideoDetail> videoMap = new LinkedHashMap<String, SiteVideoDetail>();
        Map<String, SiteVideoDetail> tempMap = new LinkedHashMap<String, SiteVideoDetail>();

        //Parser m3u8
        String m3u8_str = "";
        Map<String, String> video_list = new HashMap<String, String>();
        if(refer != null && origin != null) {
            m3u8_str = ShareDataClass.getInstance().GetHttps(url, false, refer, origin);
        } else {
            m3u8_str = ShareDataClass.getInstance().GetHttps(url, false);
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
                    SiteVideoDetail siteVideoDetail = new SiteVideoDetail();
                    siteVideoDetail.setVideo_link(m3u8_link);
                    // Get duration
                    int duration = 0;
                    String digitRegex = "\\d+";
                    Pattern p = Pattern.compile(digitRegex);
                    String m3u8Data = "";
                    if(refer != null && origin != null) {
                        m3u8Data = ShareDataClass.getInstance().GetHttps(m3u8_link, false, refer, origin);
                    } else {
                        m3u8Data = ShareDataClass.getInstance().GetHttps(m3u8_link, false);
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
                    siteVideoDetail.setVideo_timelength(duration);
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

        return videoMap;
    }

    private Map<String, SiteVideoDetail> myselfParser(String url) {
        Map<String, SiteVideoDetail> videoMap = new LinkedHashMap<String, SiteVideoDetail>();
        Uri uri = Uri.parse(url);
        String host_url = "https://" + uri.getAuthority() + "/api/files/index";
        List<String> pathSeg = uri.getPathSegments();
        for(int i = pathSeg.size() - 2; i < pathSeg.size(); i++) {
            host_url += "/" + pathSeg.get(i);
        }
        String videoHtml = ShareDataClass.getInstance().GetHttps(host_url, false, url, null);
        try {
            JSONObject videoObj = new JSONObject(videoHtml);
            JSONObject resolutionObj = videoObj.getJSONObject("video");
            JSONArray hostArr = videoObj.getJSONArray("host");
            if(hostArr.length() > 0) {
                String host = hostArr.get(0).toString().replace("\\/", "/");
                Iterator<String> iter = resolutionObj.keys();
                while (iter.hasNext()) {
                    String key = iter.next();
                    if(key.compareTo("auto") == 0) {
                        continue;
                    }
                    try {
                        String value = resolutionObj.getString(key);
                        String video_link = host + value.replace("\\/", "/");
                        SiteVideoDetail siteVideoDetail = new SiteVideoDetail();
                        // Get duration
                        int duration = 0;
                        String digitRegex = "\\d+";
                        Pattern p = Pattern.compile(digitRegex);
                        String m3u8Data = ShareDataClass.getInstance().GetHttps(video_link, false, url, null);
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
                        siteVideoDetail.setVideo_timelength(duration);
                        siteVideoDetail.setVideo_link(video_link);
                        videoMap.put(key, siteVideoDetail);
                    } catch (JSONException e) {
                        e.printStackTrace();
                    }
                }
            }
        } catch (JSONException e) {
            e.printStackTrace();
        }
        return videoMap;
    }

    private Map<String, SiteVideoDetail> anime1Parser(String url) {
        //url = "https://anime1.me/4435";
        Map<String, SiteVideoDetail> videoMap = new LinkedHashMap<String, SiteVideoDetail>();
        Map<String, SiteVideoDetail> tempMap = new LinkedHashMap<String, SiteVideoDetail>();

        String videoPageHtml = ShareDataClass.getInstance().GetHttps(url, false);
        if(videoPageHtml.indexOf("acpwd-pass") >= 0) {
            videoPageHtml = ShareDataClass.getInstance().PostHttps(url, "acpwd-pass=anime1.me", "application/x-www-form-urlencoded");
        }
        Pattern pattern = Pattern.compile("<iframe src=\"(.+?)\"");
        Matcher matcher = pattern.matcher(videoPageHtml);
        while (matcher.find()) {
            if(!videoMap.isEmpty()) {
                break;
            }
            String strdata = matcher.group(1).trim();
            if (strdata.indexOf("p.anime1.me") >= 0){
                if (strdata.indexOf("pic.php") >= 0){
                    String videoHtml = ShareDataClass.getInstance().GetHttps(strdata, false);
                    String javascript_html = "[" + ShareDataClass.getInstance().str_between(videoHtml, "sources:[", "}]").trim() + "}]";
                    try {
                        JSONArray videoArr = new JSONArray(javascript_html);
                        for (int j = 0; j < videoArr.length(); j++) {
                            String type = ((JSONObject) videoArr.get(j)).getString("type");
                            String v_link = ((JSONObject) videoArr.get(j)).getString("src");
                            String v_quality = ((JSONObject) videoArr.get(j)).getString("label");
                            if (type.compareTo("video/mp4") == 0) {
                                SiteVideoDetail siteVideoDetail = new SiteVideoDetail();
                                siteVideoDetail.setVideo_link(v_link);
                                siteVideoDetail.setVideo_timelength(0);
                                tempMap.put(v_quality, siteVideoDetail);
                            }
                        }
                        String fmtPrefs = "HD,SD";
                        String[] formats = fmtPrefs.split(",");
                        for(int i = 0; i < formats.length; i++) {
                            String i_tag = formats[i];
                            if (tempMap.containsKey(i_tag)) {
                                videoMap.put(i_tag, tempMap.get(i_tag));
                            }
                        }
                    } catch (JSONException e) {
                        e.printStackTrace();
                    }
                } else {
                    strdata = "https://p.anime1.me/godrive.php" + strdata.substring(strdata.indexOf("?"));
                    String videoHtml = ShareDataClass.getInstance().GetHttps(strdata, false);
                    Matcher videomatcher = Pattern.compile("'id': '(.+?)'").matcher(videoHtml);
                    if (videomatcher.find()) {
                        String v_link = "https://drive.google.com/file/d/" + videomatcher.group(1).trim() + "/edit";
                        videoMap = this.youtubeParser(v_link);
                    }
                }
            }
        }

        return videoMap;
    }

    private String lineTVParser(String url) {
        String video_url = "";
        return video_url;
    }

}
