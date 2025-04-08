package com.wind.tvplayer.common;

import android.text.Html;
import android.util.Log;

import com.google.gson.Gson;
import com.wind.tvplayer.model.video.Movie;
import com.wind.tvplayer.model.video.Site;

import org.apache.commons.lang3.StringEscapeUtils;
import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import java.io.BufferedReader;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.net.HttpCookie;
import java.net.HttpURLConnection;
import java.net.InetSocketAddress;
import java.net.Proxy;
import java.net.URL;
import java.security.MessageDigest;
import java.security.NoSuchAlgorithmException;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

public class ShareData {
    private static final ShareData holder = new ShareData();

    public static ShareData getInstance() {
        return holder;
    }

    public void Init () {
        final Thread thread = new Thread(new Runnable() {
            @Override
            public void run() {
                String url = "https://drive.google.com/uc?export=download&id=1XSnXmI7w__hCophWJ5Bp9fkirkJLH_nN";
                String jsonStr = ShareData.getInstance().GetHttps(url, false);
                try {
                    JSONObject rootObj = new JSONObject(jsonStr);
                    siteUrl = rootObj.getString("url");
                    JSONArray siteArray = rootObj.getJSONArray("routes");
                    for (int i = 0; i < siteArray.length(); i++) {
                        JSONObject siteObj = siteArray.getJSONObject(i);
                        String img = siteObj.getString("img");
                        String list = siteUrl + siteObj.getString("list");
                        String title = siteObj.getString("title");
                        siteNameList.add(title);
                        String parser = siteUrl + siteObj.getString("parser");
                        siteCardList.add(new Site(title, list, img, parser));
                    }
                } catch (JSONException e) {
                    e.printStackTrace();
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


    public static List<String> siteNameList = new ArrayList<String>();

    public static ArrayList<Site> siteCardList = new ArrayList<Site>();

    public static String default_fragment_background = "https://wallpaperscraft.com/image/android_red_rocks_backpack_30945_2560x1080.jpg";

    public static String siteUrl = "";

    public boolean debugMode = false;

    // String Processor
    public String stripHtml(String html) {
        if (android.os.Build.VERSION.SDK_INT >= android.os.Build.VERSION_CODES.N) {
            return Html.fromHtml(html, Html.FROM_HTML_MODE_LEGACY).toString();
        } else {
            return Html.fromHtml(html).toString();
        }
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

    // For Http(s) Requester
    public static Map<String, String> cookieMap = new HashMap<String, String>();
    public static String proxy_ip_address = "";
    public static Integer proxy_ip_port = 80;

    public String GetHttps(String _url, Boolean is_proxy) {
        return GetHttps(_url, is_proxy, null, null);
    }

    public String GetHttps(String _url, Boolean is_proxy, String refer, String origin) {
        String result = "";

        try {
            if (_url.indexOf("https") >= 0) {
                _url = _url.replace("//", "/").replace("https:/", "https://");
            } else if (_url.indexOf("http") >= 0) {
                _url = _url.replace("//", "/").replace("http:/", "http://");
            }
            String link = _url.trim();
            URL url = new URL(link);

            HttpURLConnection connection = null;
            if (is_proxy) {
                Proxy proxy = new Proxy(Proxy.Type.HTTP, new InetSocketAddress(proxy_ip_address, proxy_ip_port));
                connection = (HttpURLConnection) url.openConnection(proxy);
            } else {
                connection = (HttpURLConnection) url.openConnection();
            }
            connection.setRequestProperty("User-Agent", "Mozilla/5.0 (Windows NT 10.0; Win64; x64) AppleWebKit/537.36 (KHTML, like Gecko) Chrome/73.0.3683.103 Safari/537.36");
            if(refer != null) {
                connection.setRequestProperty("Referer", refer);
            }
            if(origin != null) {
                connection.setRequestProperty("Origin", origin);
            }
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
                    if (connection != null) {
                        connection.disconnect();
                    }
                    connection = null;
                }
            }
        } catch (Exception e) {
            Log.d("Exception", e.getMessage());
        } finally {
            return result;
        }
    }

    // Others
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
