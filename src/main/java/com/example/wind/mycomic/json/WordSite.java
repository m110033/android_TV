package com.example.wind.mycomic.json;

import com.google.gson.annotations.SerializedName;

/**
 * Created by wind on 2017/2/26.
 */

public class WordSite {
    public WordCode[] getWordCode() {
        return wordCode;
    }

    public void setWordCode(WordCode[] wordCode) {
        this.wordCode = wordCode;
    }

    @SerializedName("words")
    private WordCode[] wordCode;
}
