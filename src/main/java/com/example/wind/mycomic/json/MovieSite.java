package com.example.wind.mycomic.json;

import com.google.gson.annotations.SerializedName;

/**
 * Created by wind on 2017/2/1.
 */

public class MovieSite {
    @SerializedName("movies")
    private Movie[] movies;

    public Movie[] getMovies() {
        return movies;
    }

    public void setMovies(Movie[] movies) {
        this.movies = movies;
    }
}
