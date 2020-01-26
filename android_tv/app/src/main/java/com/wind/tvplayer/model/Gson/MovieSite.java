package com.wind.tvplayer.model.Gson;

import com.google.gson.annotations.SerializedName;

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
