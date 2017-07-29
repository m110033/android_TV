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

package com.example.wind.mycomic;

import android.app.Activity;
import android.os.Bundle;
import android.view.Menu;
import android.view.MenuItem;
import android.widget.VideoView;

import com.example.wind.mycomic.custom.PlaybackController;
import com.example.wind.mycomic.utils.PlayMovie;
/**
 * PlaybackOverlayActivity for video playback that loads PlaybackOverlayFragment
 */
public class PlaybackOverlayActivity extends Activity {

    private static final String TAG = PlaybackOverlayActivity.class.getSimpleName();

    private VideoView mVideoView;
    private PlayMovie mPlayMovie;
    private int mCurrentItem;

    private PlaybackController mPlaybackController;

    private int mPosition = 0;
    private long mStartTimeMillis;
    private long mDuration = -1;

    public PlaybackController getPlaybackController() {
        return mPlaybackController;
    }

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);


        /* NOTE: setMediaController (in createMediaSession) must be executed
         * BEFORE inflating Fragment!
         */
        mPlaybackController = new PlaybackController(this);

        String site_type = (String) getIntent().getSerializableExtra(DetailsActivity.MOVIE_CATEGORY);
        String movie_uuid = (String) getIntent().getSerializableExtra(DetailsActivity.MOVIE_UUID);
        String video_index = (String) getIntent().getSerializableExtra(DetailsActivity.PLAY_MOVIE_INDEX);

        mCurrentItem = Integer.parseInt(video_index);
        mPlayMovie = ShareDataClass.getInstance().playMovieList.get(mCurrentItem);
        mPlaybackController.setCurrentItem(mCurrentItem);

        setContentView(R.layout.playback_controls);
        mVideoView = (VideoView) findViewById(R.id.videoView);
        mPlaybackController.setVideoView(mVideoView);
        mPlaybackController.setVideoPathWithHandle(mPlayMovie); // it must after video view setting

        loadViews();
    }

    private void loadViews() {
        mVideoView = (VideoView) findViewById(R.id.videoView);
        mVideoView.setFocusable(false);
        mVideoView.setFocusableInTouchMode(false);

        if(mPlayMovie != null) {
            mPlaybackController.setVideoPath(mPlayMovie.getTruly_link());
        }
    }

    @Override
    public void onDestroy() {
        super.onDestroy();
        mPlaybackController.finishPlayback();
    }
    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        // Inflate the menu; this adds items to the action bar if it is present.
        getMenuInflater().inflate(R.menu.menu_playback_overlay, menu);
        return true;
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        // Handle action bar item clicks here. The action bar will
        // automatically handle clicks on the Home/Up button, so long
        // as you specify a parent activity in AndroidManifest.xml.
        int id = item.getItemId();

        //noinspection SimplifiableIfStatement
        if (id == R.id.action_settings) {
            return true;
        }

        return super.onOptionsItemSelected(item);
    }

    @Override
    public void onPause() {
        super.onPause();
        if (!requestVisibleBehind(true)) {
            // Try to play behind launcher, but if it fails, stop playback.
            mPlaybackController.playPause(false);
        }
    }
}
