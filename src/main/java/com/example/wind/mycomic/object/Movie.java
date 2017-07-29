/*
 * Copyright (C) 2014 The Android Open Source Project
 *
 * Licensed under the Apache License, Version 2.0 (the "License"); you may not use this file except
 * in compliance with the License. You may obtain a copy of the License at
 *
 * http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software distributed under the License
 * is distributed on an "AS IS" BASIS, WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express
 * or implied. See the License for the specific language governing permissions and limitations under
 * the License.
 */

package com.example.wind.mycomic.object;

import android.util.Log;

import com.example.wind.mycomic.ShareDataClass;

import java.net.URI;
import java.net.URISyntaxException;
import java.util.ArrayList;

/*
 * Movie class represents video entity with title, description, image thumbs and video url.
 *
 */
public class Movie {
    public ArrayList<SeasonMovie> getSeasonMovieList() {
        return seasonMovieList;
    }

    public void setSeasonMovieList(SeasonMovie seasonMovie) {
        this.seasonMovieList.add(seasonMovie);
    }

    public Movie() {
        this.seasonMovieList = new ArrayList<SeasonMovie>();
    }

    public long getCount() {
        return count;
    }

    public void incCount() {
        count++;
    }

    public long getId() {
        return id;
    }

    public void setId(long id) {
        this.id = id;
    }

    public String getUUID() {
        return uuid;
    }

    public void setUUID(String uuid) {
        this.uuid = uuid;
    }

    public String getTitle() {
        return title;
    }

    public void setTitle(String title) {
        this.title = title;
    }

    public String getDescription() {
        return description;
    }

    public void setDescription(String description) {
        this.description = description;
    }

    public String getStudio() {
        return studio;
    }

    public void setStudio(String studio) {
        this.studio = studio;
    }

    public String getCurrentMovieUrl() {
        return current_movie_url;
    }

    public void setCurrentMovieUrl(String current_movie_url) {
        this.current_movie_url = current_movie_url;
    }

    public String getBackgroundImageUrl() {
        return bgImageUrl;
    }

    public void setBackgroundImageUrl(String bgImageUrl) {
        this.bgImageUrl = bgImageUrl;
    }

    public String getCardImageUrl() {
        return cardImageUrl;
    }

    public void setCardImageUrl(String cardImageUrl) {
        this.cardImageUrl = cardImageUrl;
    }

    public Integer getCategoryIndex() {
        Integer index = -1;
        for(int i = 0 ; i < ShareDataClass.getInstance().site_name_list.length; i++) {
            String cur_type = ShareDataClass.getInstance().site_name_list[i];
            if(category.compareTo(cur_type) == 0) {
                index = i;
                break;
            }
        }
        return index;
    }

    public String getCategory() {
        return category;
    }

    public void setCategory(String category) {
        this.category = category;
    }

    public URI getBackgroundImageURI() {
        try {
            Log.d("BACK MOVIE: ", bgImageUrl);
            return new URI(getBackgroundImageUrl());
        } catch (URISyntaxException e) {
            Log.d("URI exception: ", bgImageUrl);
            return null;
        }
    }

    public URI getCardImageURI() {
        try {
            return new URI(getCardImageUrl());
        } catch (URISyntaxException e) {
            return null;
        }
    }

    public String getVideoPage() {
        return videoPage;
    }

    public void setVideoPage(String videoPage) {
        this.videoPage = videoPage;
    }

    public String getViewNumber() {
        return viewNumber;
    }

    public void setViewNumber(String viewNumber) {
        this.viewNumber = viewNumber;
    }

    public String getType() {
        return type;
    }

    public void setType(String type) {
        this.type = type;
    }

    public String getMovieDate() {
        return movieDate;
    }

    public void setMovieDate(String movieDate) {
        this.movieDate = movieDate;
    }

    private String movieDate;
    private long count = 0;
    private long id;
    private String uuid;
    private String title;
    private String type;
    private String viewNumber;
    private String description;
    private String bgImageUrl;
    private String cardImageUrl;
    private String studio;
    private String category;
    private String current_movie_url;
    private String videoPage;
    private ArrayList<SeasonMovie> seasonMovieList;
}
