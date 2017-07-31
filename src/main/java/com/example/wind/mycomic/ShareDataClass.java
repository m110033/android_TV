package com.example.wind.mycomic;

import android.app.Activity;
import android.media.MediaMetadataRetriever;
import android.os.Build;
import android.text.Html;
import android.util.Log;

import com.example.wind.mycomic.object.Movie;
import com.example.wind.mycomic.object.SiteMovie;
import com.example.wind.mycomic.utils.PlayMovie;

import java.io.BufferedReader;
import java.io.DataOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.net.HttpCookie;
import java.net.HttpURLConnection;
import java.net.URL;
import java.nio.charset.StandardCharsets;
import java.security.MessageDigest;
import java.security.NoSuchAlgorithmException;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

/**
 * Created by wind on 2016/11/19.
 */

public class ShareDataClass {
    public static String[] site_json_list = {
            "https://raw.githubusercontent.com/m110033/android_TV/master/video_site/myself/online_comic.json",
            "https://raw.githubusercontent.com/m110033/android_TV/master/video_site/myself/end_comic.json",
            "https://raw.githubusercontent.com/m110033/android_TV/master/video_site/gamer/gamer.json",
            "https://raw.githubusercontent.com/m110033/android_TV/master/video_site/maplestage/drama_tw.json",
            "https://raw.githubusercontent.com/m110033/android_TV/master/video_site/maplestage/drama_cn.json",
            "https://raw.githubusercontent.com/m110033/android_TV/master/video_site/maplestage/drama_kr.json",
            "https://raw.githubusercontent.com/m110033/android_TV/master/video_site/maplestage/drama_ot.json",
            "https://raw.githubusercontent.com/m110033/android_TV/master/video_site/maplestage/variety_tw.json",
            "https://raw.githubusercontent.com/m110033/android_TV/master/video_site/maplestage/variety_cn.json",
            "https://raw.githubusercontent.com/m110033/android_TV/master/video_site/maplestage/variety_tw.json",
    };

    public static String[] site_name_list = {
            "動漫 - 連載中",
            "動漫 - 已完結",
            "巴哈動畫瘋",
            "楓林網 - 台灣戲劇",
            "楓林網 - 大陸戲劇",
            "楓林網 - 韓國戲劇",
            "楓林網 - 其他戲劇",
            "楓林網 - 台灣綜藝",
            "楓林網 - 大陸綜藝",
            "楓林網 - 韓國綜藝"
    };

    public static String[] site_img_list = {
            "http://myself-bbs.com/template/yeei_dream1/css/yeei/logo.png",
            "http://myself-bbs.com/template/yeei_dream1/css/yeei/logo.png",
            "https://i2.bahamut.com.tw/baha_logo5.png",
            "https://pbs.twimg.com/profile_images/696495587110289408/l7IZlrOl.png",
            "https://pbs.twimg.com/profile_images/696495587110289408/l7IZlrOl.png",
            "https://pbs.twimg.com/profile_images/696495587110289408/l7IZlrOl.png",
            "https://pbs.twimg.com/profile_images/696495587110289408/l7IZlrOl.png",
            "https://pbs.twimg.com/profile_images/696495587110289408/l7IZlrOl.png",
            "https://pbs.twimg.com/profile_images/696495587110289408/l7IZlrOl.png",
            "https://pbs.twimg.com/profile_images/696495587110289408/l7IZlrOl.png"
    };

    public static Activity ajax_activity = null;
    public static String drama8_truly_link = "";
    public static String default_fragment_background = "https://wallpaperscraft.com/image/android_red_rocks_backpack_30945_2560x1080.jpg";
    public static ArrayList<PlayMovie> playMovieList = new ArrayList<PlayMovie>();
    public static HashMap<String, Integer> movieTypeList = new HashMap<String, Integer>();
    public static HashMap<String, Integer> movieMaxHiits = new HashMap<String, Integer>();
    public static HashMap<String, ArrayList<Movie>> movieList = new HashMap<String, ArrayList<Movie>>();
    public static Map<String, String> cookieMap = new HashMap<String, String>();
    public static Map<String, Integer> wordDict = new HashMap<String, Integer>();

    public String stripHtml(String html) {
        if (android.os.Build.VERSION.SDK_INT >= android.os.Build.VERSION_CODES.N) {
            return Html.fromHtml(html, Html.FROM_HTML_MODE_LEGACY).toString();
        } else {
            return Html.fromHtml(html).toString();
        }
    }

    public double calCosineSimilarity(Integer[] a, Integer[] b) {
        double u_d = 0;
        double d_d = 0;
        double d_d_l = 0;
        double d_d_r = 0;
        for(int i = 0; i < a.length; i++) {
            double a_d = (double) a[i];
            double b_d= (double) b[i];
            u_d += a_d * b_d;
            d_d_l += a_d * a_d;
            d_d_r += b_d * b_d;
        }
        d_d = Math.sqrt(d_d_l) * Math.sqrt(d_d_r);
        return u_d / d_d;
    }

    public  String str_between_l(String src, String start, String end) {
        String result = "";
        int sIndex = src.indexOf(start) + start.length();
        if (sIndex >= 0) {
            result = src.substring(sIndex);
            int eIndex = result.lastIndexOf(end);
            if(eIndex >= 0){
                result = result.substring(0, eIndex);
            }
        }
        return result;
    }

    public  String str_between(String src, String start, String end) {
        String result = "";
        if(src.contains(start)) {
            int sIndex = src.indexOf(start) + start.length();
            if (sIndex >= 0) {
                result = src.substring(sIndex);
                if(result.contains(end)) {
                    int eIndex = result.indexOf(end);
                    if (eIndex >= 0) {
                        result = result.substring(0, eIndex);
                    }
                }
            }
        }
        return result;
    }

    public String decode(String in) {
        String working = in;
        int index;
        index = working.indexOf("\\u");
        while (index > -1) {
            int length = working.length();
            if (index > (length - 6)) break;
            int numStart = index + 2;
            int numFinish = numStart + 4;
            String substring = working.substring(numStart, numFinish);
            int number = Integer.parseInt(substring, 16);
            String stringStart = working.substring(0, index);
            String stringEnd = working.substring(numFinish);
            working = stringStart + ((char) number) + stringEnd;
            index = working.indexOf("\\u");
        }
        return working;
    }

    private static final ShareDataClass holder = new ShareDataClass();

    public static ShareDataClass getInstance() {
        return holder;
    }

    public static long getDuration(String videoUrl) {
        try {
            MediaMetadataRetriever mmr = new MediaMetadataRetriever();
            if(videoUrl.compareTo("") != 0) {
                if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.ICE_CREAM_SANDWICH) {
                    mmr.setDataSource(videoUrl, new HashMap<String, String>());
                } else {
                    mmr.setDataSource(videoUrl);
                }
            }
            return Long.parseLong(mmr.extractMetadata(MediaMetadataRetriever.METADATA_KEY_DURATION));
            /*
            FFmpegMediaMetadataRetriever ffmmr = new FFmpegMediaMetadataRetriever();
            if (videoUrl.compareTo("") != 0) {
                if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.ICE_CREAM_SANDWICH) {
                    ffmmr.setDataSource(videoUrl, new HashMap<String, String>());
                } else {
                    ffmmr.setDataSource(videoUrl);
                }
            }
            String time = ffmmr.extractMetadata(FFmpegMediaMetadataRetriever.METADATA_KEY_DURATION);
            return Long.parseLong(time);
            */
        } catch (Exception e) {
            Log.d("Exception", e.getMessage());
        }
        return 10800000;
    }

    public String PostHttps(String url, String data, String content_type) {
        String result = "";

        byte[] postData = data.getBytes( StandardCharsets.UTF_8 );
        int postDataLength = postData.length;

        try
        {
            URL connectto = new URL(url);
            HttpURLConnection conn = (HttpURLConnection) connectto.openConnection();
            conn.setRequestMethod( "POST" );
            conn.setRequestProperty( "Content-Length", Integer.toString( postDataLength ));
            conn.setRequestProperty( "Content-Type", content_type);
            conn.setRequestProperty( "charset", "utf-8");
            conn.setUseCaches(false);
            conn.setAllowUserInteraction(false);
            conn.setInstanceFollowRedirects( false );
            conn.setDoOutput( true );

            DataOutputStream wr = new DataOutputStream(conn.getOutputStream());
            wr.write(postData, 0, postDataLength);
            wr.flush();
            wr.close();

            int responseCode = conn.getResponseCode();

            if (responseCode == 200 || responseCode == 301 || responseCode == 302)
            {
                BufferedReader br = new BufferedReader(new InputStreamReader(conn.getInputStream()));
                StringBuilder sb = new StringBuilder();
                String line;

                while ((line = br.readLine()) != null) {
                    sb.append(line + "\n");
                }

                br.close();
                result = sb.toString();
            }
        }
        catch (IOException e)
        {
            e.printStackTrace();
        }

        return result;
    }

    public String GetHttps(String _url) {
        String result = "";

        try {
            if (_url.indexOf("https") >= 0) {
                _url = _url.replace("//", "/").replace("https:/", "https://");
            } else if (_url.indexOf("http") >= 0) {
                _url = _url.replace("//", "/").replace("http:/", "http://");
            }
            String link = _url.trim();
            URL url = new URL(link);

            HttpURLConnection connection = (HttpURLConnection) url.openConnection();
            String redirect = connection.getHeaderField("Location");
            if (redirect != null){
                URL redirect_url = new URL(redirect);
                connection = (HttpURLConnection) redirect_url.openConnection();
            }

            connection.setReadTimeout(10000);
            connection.setConnectTimeout(15000);
            connection.setRequestMethod("GET");
            connection.connect();

            int code = connection.getResponseCode();
            InputStream is = connection.getInputStream();

            if (code == 200 || code == 301) {
                try {
                    //cookieMap.clear();

                    //Get Cookie
                    Map<String, List<String>> headerFields = connection.getHeaderFields();
                    String COOKIES_HEADER = "Set-Cookie";
                    List<String> cookiesHeader = headerFields.get(COOKIES_HEADER);
                    if (cookiesHeader != null) {
                        for (String cookie : cookiesHeader) {
                            if (cookie.startsWith("DRIVE_STREAM")) {
                                HttpCookie httpCookie = HttpCookie.parse(cookie).get(0);
                                cookieMap.put("DRIVE_STREAM", httpCookie.getValue());
                            }
                        }
                    }

                    BufferedReader reader = new BufferedReader(new InputStreamReader(is, "UTF-8"));
                    final int bufferSize = 1024;
                    final char[] buffer = new char[bufferSize];
                    final StringBuilder out = new StringBuilder();

                    for (; ; ) {
                        int rsz = reader.read(buffer, 0, buffer.length);
                        if (rsz < 0)
                            break;
                        out.append(buffer, 0, rsz);
                    }

                    result =  out.toString();
                } finally {
                    try {
                        is.close();
                    } catch (Throwable ignore) {
                    }
                }
            }
        } catch (Exception e) {
            Log.d("Exception", e.getMessage());
        } finally {
            return result;
        }
    }

    public String md5(final String s) {
        final String MD5 = "MD5";
        try {
            // Create MD5 Hash
            MessageDigest digest = java.security.MessageDigest
                    .getInstance(MD5);
            digest.update(s.getBytes());
            byte messageDigest[] = digest.digest();

            // Create Hex String
            StringBuilder hexString = new StringBuilder();
            for (byte aMessageDigest : messageDigest) {
                String h = Integer.toHexString(0xFF & aMessageDigest);
                while (h.length() < 2)
                    h = "0" + h;
                hexString.append(h);
            }
            return hexString.toString();

        } catch (NoSuchAlgorithmException e) {
            e.printStackTrace();
        }
        return "";
    }
}
