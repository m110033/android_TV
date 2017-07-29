package com.example.wind.mycomic;

import android.app.Activity;
import android.os.Bundle;
import com.example.wind.mycomic.service.UpdateChecker;

public class SiteActivity extends Activity {

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_site);
        UpdateChecker.checkForDialog(this);
    }
}
