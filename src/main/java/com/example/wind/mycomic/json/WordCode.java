package com.example.wind.mycomic.json;

import com.google.gson.annotations.SerializedName;

/**
 * Created by wind on 2017/2/26.
 */

public class WordCode {
    public String getWord() {
        return word;
    }

    public void setWord(String word) {
        this.word = word;
    }

    public String getLength() {
        return length;
    }

    public void setLength(String length) {
        this.length = length;
    }

    @SerializedName("word")
    private String word;
    @SerializedName("word_length")
    private String length;
}
