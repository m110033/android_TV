package com.example.wind.mycomic.siteParser;

import android.os.Message;

import com.example.wind.mycomic.ShareDataClass;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.Date;

/**
 * Created by wind on 2017/7/17.
 */

public class Maplestage {
    public PlaySite getList(final String url) {
        final PlaySite curPlaySite = new PlaySite();

        Thread thread = new Thread(new Runnable() {
            @Override
            public void run() {
            String html = ShareDataClass.getInstance().GetHttps(url);
            html = html.replace("&amp;", "&");
            String dataObj = ShareDataClass.getInstance().str_between(html, "var pageData = ", ";");
            try {
                JSONObject movieJsonObj = new JSONObject(dataObj);
                JSONArray propsArray = movieJsonObj.getJSONArray("props");
                for(int i = 0; i < propsArray.length(); i++) {
                    JSONObject cur_json_obj = propsArray.getJSONObject(i);
                    String obj_name = cur_json_obj.getString("name");
                    if(obj_name.compareTo("model") == 0) {
                        JSONObject valueObj = cur_json_obj.getJSONObject("value");
                        JSONArray sourceArray = valueObj.getJSONArray("videoSources");
                        for (int j = 0; j < sourceArray.length(); j++) {
                            JSONObject video_obj = sourceArray.getJSONObject(j);
                            String update_date = video_obj.getString("updatedAt");
                            update_date = update_date.substring(0, 19);
                            String curVideoType = video_obj.getString("name");
                            SimpleDateFormat format = new SimpleDateFormat("yyyy-MM-dd'T'HH:mm:ss");
                            try {
                                Date date = format.parse(update_date);
                                PlayList curPlayList = new PlayList();
                                Integer existIndex = -1;
                                for (int k = 0; k < curPlaySite.getPlayList().size(); k++) {
                                    if (curVideoType.compareTo(curPlaySite.getPlayList().get(k).getPlayType()) == 0) {
                                        existIndex = k;
                                        break;
                                    }
                                }
                                JSONArray videoArray = video_obj.getJSONArray("videos");
                                for (int x = 0; x < videoArray.length(); x++) {
                                    JSONObject curVideoObj = videoArray.getJSONObject(x);
                                    String videoId = curVideoObj.getString("id");
                                    PlayClass curPlayClass = new PlayClass();
                                    curPlayClass.setIndex(x);
                                    curPlayClass.setVideoId(videoId);
                                    curPlayList.setPlayClass(curPlayClass);
                                }
                                curPlayList.setPlayType(curVideoType);
                                curPlayList.setPlayDate(date);

                                if (existIndex != -1 && curPlaySite.getPlayList().get(existIndex).getPlayDate().compareTo(date) <= 0) {
                                    curPlaySite.getPlayList().set(existIndex, curPlayList);
                                }
                                else if( existIndex == -1) {
                                    curPlaySite.getPlayList().add(curPlayList);
                                }
                            } catch (ParseException e) {
                                e.printStackTrace();
                            }
                        }
                    }
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

        return curPlaySite;
    }
}
