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
import android.app.AlertDialog;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.graphics.Bitmap;
import android.graphics.drawable.BitmapDrawable;
import android.graphics.drawable.Drawable;
import android.media.MediaMetadata;
import android.media.session.MediaController;
import android.media.session.PlaybackState;
import android.net.Uri;
import android.os.Bundle;
import android.os.Handler;
import android.os.Looper;
import android.os.Message;
import android.support.v17.leanback.widget.Action;
import android.support.v17.leanback.widget.ArrayObjectAdapter;
import android.support.v17.leanback.widget.ClassPresenterSelector;
import android.support.v17.leanback.widget.ControlButtonPresenterSelector;
import android.support.v17.leanback.widget.HeaderItem;
import android.support.v17.leanback.widget.ListRow;
import android.support.v17.leanback.widget.ListRowPresenter;
import android.support.v17.leanback.widget.OnActionClickedListener;
import android.support.v17.leanback.widget.OnItemViewClickedListener;
import android.support.v17.leanback.widget.OnItemViewSelectedListener;
import android.support.v17.leanback.widget.PlaybackControlsRow;
import android.support.v17.leanback.widget.PlaybackControlsRowPresenter;
import android.support.v17.leanback.widget.Presenter;
import android.support.v17.leanback.widget.Row;
import android.support.v17.leanback.widget.RowPresenter;
import android.support.v7.view.ContextThemeWrapper;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;

import com.example.wind.mycomic.custom.PlaybackController;
import com.example.wind.mycomic.object.Movie;
import com.example.wind.mycomic.utils.PlayMovie;
import com.squareup.picasso.Picasso;
import com.squareup.picasso.Target;

import java.net.URI;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.Set;

/*
 * Class for video playback with media control
 */
public class PlaybackOverlayFragment extends android.support.v17.leanback.app.PlaybackOverlayFragment {

    private static final String TAG = PlaybackOverlayFragment.class.getSimpleName();
    private static final int SIMULATED_BUFFERED_TIME = 10000;
    private static final int DEFAULT_UPDATE_PERIOD = 1000;
    private static final int UPDATE_PERIOD = 16;
    private static final int CARD_WIDTH = 200;
    private static final int CARD_HEIGHT = 240;
    private static final boolean SHOW_IMAGE = true;

    private static Context sContext;

    private PlayMovie mPlayMovie;
    private PlaybackController mPlaybackController;

    public PlaybackControlsRow getmPlaybackControlsRow() {
        return mPlaybackControlsRow;
    }

    private PlaybackControlsRow mPlaybackControlsRow;
    private ArrayObjectAdapter mPrimaryActionsAdapter;
    private ArrayObjectAdapter mSecondaryActionsAdapter;
    private Handler mHandler;
    private Runnable mRunnable;

    private PlaybackControlsRow.PlayPauseAction mPlayPauseAction;
    private PlaybackControlsRow.RepeatAction mRepeatAction;
    private PlaybackControlsRow.ThumbsUpAction mThumbsUpAction;
    private PlaybackControlsRow.ThumbsDownAction mThumbsDownAction;
    private PlaybackControlsRow.ShuffleAction mShuffleAction;
    private PlaybackControlsRow.SkipNextAction mSkipNextAction;
    private PlaybackControlsRow.SkipPreviousAction mSkipPreviousAction;
    private PlaybackControlsRow.FastForwardAction mFastForwardAction;
    private PlaybackControlsRow.RewindAction mRewindAction;
    private PlaybackControlsRow.HighQualityAction mHighQualityAction;
    private PlaybackControlsRow.ClosedCaptioningAction mClosedCaptioningAction;
    private PlaybackControlsRow.MoreActions mMoreActions;
    private PicassoPlaybackControlsRowTarget mPlaybackControlsRowTarget;
    private PlaybackOverlayActivity activity;

    private Movie mSelectedMovie;
    private MediaController mMediaController;
    private MediaController.Callback mMediaControllerCallback = new MediaControllerCallback();

    @Override
    public View onInflateTitleView(LayoutInflater inflater, ViewGroup parent,
                                   Bundle savedInstanceState) {
        View view = super.inflateTitle(inflater, parent, savedInstanceState);
        return view;
    }

    @Override
    public void onCreate(Bundle savedInstanceState) {
        Log.i(TAG, "onCreate");
        super.onCreate(savedInstanceState);

        String site_type = (String) getActivity().getIntent().getSerializableExtra(DetailsActivity.MOVIE_CATEGORY);
        String movie_uuid = (String) getActivity().getIntent().getSerializableExtra(DetailsActivity.MOVIE_UUID);
        String video_index = (String) getActivity().getIntent().getSerializableExtra(DetailsActivity.PLAY_MOVIE_INDEX);

        ArrayList<Movie> movieList = ShareDataClass.getInstance().movieList.get(site_type);
        for (int i = 0; i < movieList.size(); i++) {
            if (movieList.get(i).getUUID().compareTo(movie_uuid) == 0) {
                mSelectedMovie = movieList.get(i);
                break;
            }
        }

        sContext = getActivity();
        activity = (PlaybackOverlayActivity) getActivity();
        mHandler = new Handler(Looper.getMainLooper());

        mPlaybackController = activity.getPlaybackController();
        mPlaybackController.setPlaybackOverlayFragment(this);
        mPlaybackController.setmItems(ShareDataClass.getInstance().playMovieList);
        mPlaybackController.setCurrentItem(Integer.parseInt(video_index));
        mPlayMovie = ShareDataClass.getInstance().playMovieList.get(mPlaybackController.getCurrentItem());

        setBackgroundType(PlaybackOverlayFragment.BG_LIGHT);
        setFadingEnabled(true);

        setUpRows();
        setOnItemViewSelectedListener(new OnItemViewSelectedListener() {
            @Override
            public void onItemSelected(Presenter.ViewHolder itemViewHolder, Object item,
                                       RowPresenter.ViewHolder rowViewHolder, Row row) {
                Log.i(TAG, "onItemSelected: " + item + " row " + row);

            }
        });

        setOnItemViewClickedListener(new ItemViewClickedListener());
        mMediaController.getTransportControls().play();
    }

    @Override
    public void onAttach(Activity activity) {
        super.onAttach(activity);
        mMediaController = ((PlaybackOverlayActivity) getActivity()).getMediaController();
        Log.d(TAG, "register callback of mediaController");
        if(mMediaController == null){
            Log.e(TAG, "mMediaController is null");
        }
        mMediaController.registerCallback(mMediaControllerCallback);

    }

    @Override
    public void onActivityCreated(Bundle savedInstanceState) {
        Log.d(TAG, "onActivityCreated");
        super.onActivityCreated(savedInstanceState);

    }

    @Override
    public void onDetach() {
        if (mMediaController != null) {
            Log.d(TAG, "unregister callback of mediaController");
            mMediaController.unregisterCallback(mMediaControllerCallback);
        }
        super.onDetach();
    }

    @Override
    public void onStart() {
        startProgressAutomation();
        super.onStart();

    }

    @Override
    public void onStop() {
        //mRowsAdapter = null;
        super.onStop();
    }

    @Override
    public void onResume() {
        super.onResume();
    }

    @Override
    public void onDestroy() {
        stopProgressAutomation();
        super.onDestroy();
    }

    public ArrayObjectAdapter getmRowsAdapter() {
        return mRowsAdapter;
    }

    private ArrayObjectAdapter mRowsAdapter;

    private static Uri replaceUriParameter(Uri uri, String key, String newValue) {
        final Set<String> params = uri.getQueryParameterNames();
        final Uri.Builder newUri = uri.buildUpon().clearQuery();
        for (String param : params) {
            String value;
            if (param.equals(key)) {
                value = newValue;
            } else {
                value = uri.getQueryParameter(param);
            }
            newUri.appendQueryParameter(param, value);
        }
        if (!params.contains("quality")) {
            newUri.appendQueryParameter("quality", newValue);
        }
        return newUri.build();
    }

    private void setUpRows() {
        ClassPresenterSelector ps = new ClassPresenterSelector();

        PlaybackControlsRowPresenter playbackControlsRowPresenter;
        playbackControlsRowPresenter = new PlaybackControlsRowPresenter(new DetailsDescriptionPresenter());

        ps.addClassPresenter(PlaybackControlsRow.class, playbackControlsRowPresenter);
        ps.addClassPresenter(ListRow.class, new ListRowPresenter());
        mRowsAdapter = new ArrayObjectAdapter(ps);

        /*
         * Add PlaybackControlsRow to mRowsAdapter, which makes video control UI.
         * PlaybackControlsRow is supposed to be first Row of mRowsAdapter.
         */
        addPlaybackControlsRow();
        /* add ListRow to second row of mRowsAdapter */
        //addOtherRows();

        /* onClick */
        playbackControlsRowPresenter.setOnActionClickedListener(new OnActionClickedListener() {
            public void onActionClicked(Action action) {
                if (action.getId() == mPlayPauseAction.getId()) {
                    /* PlayPause action */
                    if (mPlayPauseAction.getIndex() == PlaybackControlsRow.PlayPauseAction.PLAY) {
                        mMediaController.getTransportControls().play();
                    } else if (mPlayPauseAction.getIndex() == PlaybackControlsRow.PlayPauseAction.PAUSE) {
                        mMediaController.getTransportControls().pause();
                    }
                } else if (action.getId() == mSkipNextAction.getId()) {
                    /* SkipNext action */
                    mMediaController.getTransportControls().skipToNext();
                } else if (action.getId() == mSkipPreviousAction.getId()) {
                    /* SkipPrevious action */
                    mMediaController.getTransportControls().skipToPrevious();
                } else if (action.getId() == mFastForwardAction.getId()) {
                    /* FastForward action  */
                    mMediaController.getTransportControls().fastForward();
                } else if (action.getId() == mRewindAction.getId()) {
                    /* Rewind action */
                    mMediaController.getTransportControls().rewind();
                } else if (action.getId() == mHighQualityAction.getId()) {
                    AlertDialog.Builder alertDialog = new AlertDialog.Builder(new ContextThemeWrapper(getActivity(), R.style.siteDialogTheme));
                    alertDialog.setTitle("請選擇畫質");

                    final List keys = new ArrayList(mPlaybackController.getVideoSiteDetailMap().keySet());

                    ArrayList<String> options = new ArrayList<String>();
                    if (mPlayMovie.getMovie_categoryIndex() >= 0 && mPlayMovie.getMovie_categoryIndex() <= 1 && mPlayMovie.getTruly_link().indexOf("blibli") >= 0) {
                        for (int i = 0; i < ShareDataClass.getInstance().qualityList.size(); i++) {
                            String video_size_str = ShareDataClass.getInstance().qualityList.get(i).toString();
                            options.add(video_size_str);
                        }
                    } else {
                        for (int i = 0; i < keys.size(); i++) {
                            String video_size_str = (String) keys.get(i);
                            options.add(video_size_str);
                        }
                    }

                    //建立選擇的事件
                    DialogInterface.OnClickListener ListClick = new DialogInterface.OnClickListener()
                    {
                        public void onClick(DialogInterface dialog, int index) {
                            if (mPlayMovie.getMovie_categoryIndex() >= 0 && mPlayMovie.getMovie_categoryIndex() <= 1 && mPlayMovie.getTruly_link().indexOf("blibli") >= 0) {
                                mPlayMovie = ShareDataClass.getInstance().playMovieList.get(mPlaybackController.getCurrentItem());
                                String temp_video_url = mPlayMovie.getVideo_url();
                                temp_video_url = temp_video_url.replace("&amp;", "&");
                                Uri uri = Uri.parse(temp_video_url);
                                temp_video_url = replaceUriParameter(uri, "quality", ShareDataClass.getInstance().qualityList.get(index).toString()).toString();
                                mPlayMovie.setVideo_url(temp_video_url);
                                mPlayMovie.setTruly_link("");
                                mPlaybackController.setCurrentItem(0);
                                // Clear truly video link
                                for(int i = 0; i < ShareDataClass.getInstance().playMovieList.size(); i++) {
                                    ShareDataClass.getInstance().playMovieList.get(i).setTruly_link("");
                                }
                                mPlaybackController.setVideoPathWithHandle(mPlayMovie);
                            } else {
                                String video_link = mPlaybackController.getVideoSiteDetailMap().get(keys.get(index)).getVideo_link();
                                PlayMovie cur_play_movie = new PlayMovie();
                                cur_play_movie.setTruly_link(video_link);
                                int cur_play_index = mPlaybackController.getCurrentItem();
                                mPlayMovie = ShareDataClass.getInstance().playMovieList.get(mPlaybackController.getCurrentItem());
                                mPlayMovie.setTruly_link(video_link);
                                mPlaybackController.setVideoPathWithHandle(mPlayMovie);
                            }
                        }
                    };

                    String[] optionsArr = options.toArray(new String[0]);
                    alertDialog.setItems(optionsArr, ListClick);
                    alertDialog.show();
                }

                if (action instanceof PlaybackControlsRow.MultiAction) {
                    /* Following action is subclass of MultiAction
                     * - PlayPauseAction
                     * - FastForwardAction
                     * - RewindAction
                     * - ThumbsAction
                     * - RepeatAction
                     * - ShuffleAction
                     * - HighQualityAction
                     * - ClosedCaptioningAction
                     */
                    notifyChanged(action);

                    /* Change icon */
                    if (action instanceof PlaybackControlsRow.ThumbsUpAction ||
                            action instanceof PlaybackControlsRow.ThumbsDownAction ||
                            action instanceof PlaybackControlsRow.RepeatAction ||
                            action instanceof PlaybackControlsRow.ShuffleAction ||
                            action instanceof PlaybackControlsRow.HighQualityAction ||
                            action instanceof PlaybackControlsRow.ClosedCaptioningAction) {
                        ((PlaybackControlsRow.MultiAction) action).nextIndex();
                    }
                }
            }
        });

        setAdapter(mRowsAdapter);

    }

    private void notifyChanged(Action action) {
        ArrayObjectAdapter adapter = mPrimaryActionsAdapter;
        if (adapter.indexOf(action) >= 0) {
            adapter.notifyArrayItemRangeChanged(adapter.indexOf(action), 1);
            return;
        }
        adapter = mSecondaryActionsAdapter;
        if (adapter.indexOf(action) >= 0) {
            adapter.notifyArrayItemRangeChanged(adapter.indexOf(action), 1);
            return;
        }
    }

    private int getUpdatePeriod() {
        if (getView() == null || mPlaybackControlsRow.getTotalTime() <= 0) {
            return DEFAULT_UPDATE_PERIOD;
        }
        return Math.max(UPDATE_PERIOD, mPlaybackControlsRow.getTotalTime() / getView().getWidth());
    }

    private void startProgressAutomation() {
        if (mRunnable == null) {
            mRunnable = new Runnable() {
                @Override
                public void run() {
                    int updatePeriod = getUpdatePeriod();
                    int currentTime = mPlaybackControlsRow.getCurrentTime() + updatePeriod;
                    int totalTime = mPlaybackControlsRow.getTotalTime();
                    mPlaybackControlsRow.setCurrentTime(currentTime);
                    mPlaybackControlsRow.setBufferedProgress(currentTime + SIMULATED_BUFFERED_TIME);

                    if (totalTime > 0 && totalTime <= currentTime) {
                        stopProgressAutomation();
                        //next(true);
                    } else {
                        mHandler.postDelayed(this, updatePeriod);
                    }
                }
            };
            mHandler.postDelayed(mRunnable, getUpdatePeriod());
        }
    }

    private void stopProgressAutomation() {
        if (mHandler != null && mRunnable != null) {
            mHandler.removeCallbacks(mRunnable);
            mRunnable = null;
        }
    }

    private void addPlaybackControlsRow() {
        PlayMovie movieItem = new PlayMovie();
        mPlaybackControlsRow = new PlaybackControlsRow(movieItem);
        mRowsAdapter.add(mPlaybackControlsRow);

        ControlButtonPresenterSelector presenterSelector = new ControlButtonPresenterSelector();
        mPrimaryActionsAdapter = new ArrayObjectAdapter(presenterSelector);
        mSecondaryActionsAdapter = new ArrayObjectAdapter(presenterSelector);
        mPlaybackControlsRow.setPrimaryActionsAdapter(mPrimaryActionsAdapter);
        mPlaybackControlsRow.setSecondaryActionsAdapter(mSecondaryActionsAdapter);

        Activity activity = getActivity();
        mPlayPauseAction = new PlaybackControlsRow.PlayPauseAction(activity);
        mRepeatAction = new PlaybackControlsRow.RepeatAction(activity);
        mThumbsUpAction = new PlaybackControlsRow.ThumbsUpAction(activity);
        mThumbsDownAction = new PlaybackControlsRow.ThumbsDownAction(activity);
        mShuffleAction = new PlaybackControlsRow.ShuffleAction(activity);
        mSkipNextAction = new PlaybackControlsRow.SkipNextAction(activity);
        mSkipPreviousAction = new PlaybackControlsRow.SkipPreviousAction(activity);
        mFastForwardAction = new PlaybackControlsRow.FastForwardAction(activity);
        mRewindAction = new PlaybackControlsRow.RewindAction(activity);
        mHighQualityAction = new PlaybackControlsRow.HighQualityAction(activity);
        mClosedCaptioningAction = new PlaybackControlsRow.ClosedCaptioningAction(activity);
        mMoreActions = new PlaybackControlsRow.MoreActions(activity);

        /* PrimaryAction setting */
        mPrimaryActionsAdapter.add(mSkipPreviousAction);
        mPrimaryActionsAdapter.add(mRewindAction);
        mPrimaryActionsAdapter.add(mPlayPauseAction);
        mPrimaryActionsAdapter.add(mFastForwardAction);
        mPrimaryActionsAdapter.add(mSkipNextAction);

        /* SecondaryAction setting */
        mSecondaryActionsAdapter.add(mThumbsUpAction);
        mSecondaryActionsAdapter.add(mThumbsDownAction);
        mSecondaryActionsAdapter.add(mRepeatAction);
        mSecondaryActionsAdapter.add(mShuffleAction);
        mSecondaryActionsAdapter.add(mHighQualityAction);
        mSecondaryActionsAdapter.add(mClosedCaptioningAction);
        mSecondaryActionsAdapter.add(mMoreActions);

        //updatePlaybackRow();
        mPlaybackController.updateMetadata();
    }

    private void addOtherRows() {
        ArrayObjectAdapter listRowAdapter = new ArrayObjectAdapter(new CardPresenter());
        for(PlayMovie movie : ShareDataClass.getInstance().playMovieList) {
            listRowAdapter.add(movie);
        }

        HeaderItem header = new HeaderItem(0, "Other Movies");
        mRowsAdapter.add(new ListRow(header, listRowAdapter));
    }

    private void updatePlaybackRow() {
        Log.d(TAG, "updatePlaybackRow");
        if (mPlaybackControlsRow.getItem() != null) {
            mPlayMovie = ShareDataClass.getInstance().playMovieList.get(mPlaybackController.getCurrentItem());
            if (mPlayMovie != null) {
                //new ShowSpinnerTask().execute(mPlayMovie);
            }
        }
        if (SHOW_IMAGE) {
            mPlaybackControlsRowTarget = new PicassoPlaybackControlsRowTarget(mPlaybackControlsRow);
            updateVideoImage(ShareDataClass.getInstance().playMovieList.get(mPlaybackController.getCurrentItem()).getVideo_img());
        }
    }

    private void updateMovieView(String title, String studio, String cardImageUrl, long duration) {
        Log.d(TAG, "updateMovieView");

        if (mPlaybackControlsRow.getItem() != null) {
            PlayMovie item = (PlayMovie) mPlaybackControlsRow.getItem();
            item.setVideo_title(title);
            item.setMovie_category(studio);
        } else {
            Log.e(TAG, "mPlaybackControlsRow.getItem is null!");
        }

        mPlaybackControlsRow.setTotalTime((int) duration);
        mPlaybackControlsRow.setCurrentTime(0);
        mPlaybackControlsRow.setBufferedProgress(0);
        mRowsAdapter.notifyArrayItemRangeChanged(0, mRowsAdapter.size());

        // Show the video card image if there is enough room in the UI for it.
        // If you have many primary actions, you may not have enough room.
        if (SHOW_IMAGE) {
            mPlaybackControlsRowTarget = new PicassoPlaybackControlsRowTarget(mPlaybackControlsRow);
            updateVideoImage(cardImageUrl);
        }
    }

    /* For cardImage loading to playbackRow */
    public class PicassoPlaybackControlsRowTarget implements Target {
        PlaybackControlsRow mPlaybackControlsRow;

        public PicassoPlaybackControlsRowTarget(PlaybackControlsRow playbackControlsRow) {
            mPlaybackControlsRow = playbackControlsRow;
        }

        @Override
        public void onBitmapLoaded(Bitmap bitmap, Picasso.LoadedFrom loadedFrom) {
            Drawable bitmapDrawable = new BitmapDrawable(sContext.getResources(), bitmap);
            mPlaybackControlsRow.setImageDrawable(bitmapDrawable);
        }

        @Override
        public void onBitmapFailed(Drawable drawable) {
            mPlaybackControlsRow.setImageDrawable(drawable);
        }

        @Override
        public void onPrepareLoad(Drawable drawable) {
            // Do nothing, default_background manager has its own transitions
        }
    }

    protected void updateVideoImage(URI uri) {
        Picasso.with(sContext)
                .load(uri.toString())
                .resize(Utils.convertDpToPixel(sContext, CARD_WIDTH),
                        Utils.convertDpToPixel(sContext, CARD_HEIGHT))
                .into(mPlaybackControlsRowTarget);
    }

    protected void updateVideoImage(String url) {
        try {
            URI uri = new URI(url);
            updateVideoImage(uri);
        } catch (Exception e) {
            Log.e(TAG, e.toString());
        }
    }

    private final class ItemViewClickedListener implements OnItemViewClickedListener {
        @Override
        public void onItemClicked(Presenter.ViewHolder itemViewHolder, Object item,
                                  RowPresenter.ViewHolder rowViewHolder, Row row) {

            if (item instanceof PlayMovie) {
                PlayMovie movie = (PlayMovie) item;
                Log.d(TAG, "Item: " + item.toString());

                Intent intent = new Intent(getActivity(), PlaybackOverlayActivity.class);
                intent.putExtra(DetailsActivity.MOVIE_CATEGORY, mSelectedMovie.getCategory());
                intent.putExtra(DetailsActivity.MOVIE_UUID, mSelectedMovie.getUUID());
                intent.putExtra(DetailsActivity.PLAY_MOVIE_INDEX, mPlaybackController.getCurrentItem());
                getActivity().startActivity(intent);
            }
        }
    }

    private class MediaControllerCallback extends MediaController.Callback {
        @Override
        public void onPlaybackStateChanged(final PlaybackState state) {
            Log.d(TAG, "playback state changed: " + state.toString());
            mHandler.post(new Runnable() {
                @Override
                public void run() {
                    if (state.getState() == PlaybackState.STATE_PLAYING) {
                        mPlaybackController.setCurrentPlaybackState(PlaybackState.STATE_PLAYING);
                        startProgressAutomation();
                        // setFadingEnabled(false);
                        mPlayPauseAction.setIndex(PlaybackControlsRow.PlayPauseAction.PAUSE);
                        mPlayPauseAction.setIcon(mPlayPauseAction.getDrawable(PlaybackControlsRow.PlayPauseAction.PAUSE));
                        notifyChanged(mPlayPauseAction);
                    } else if (state.getState() == PlaybackState.STATE_PAUSED) {
                        mPlaybackController.setCurrentPlaybackState(PlaybackState.STATE_PAUSED);
                        stopProgressAutomation();
                        // setFadingEnabled(false);
                        mPlayPauseAction.setIndex(PlaybackControlsRow.PlayPauseAction.PLAY);
                        mPlayPauseAction.setIcon(mPlayPauseAction.getDrawable(PlaybackControlsRow.PlayPauseAction.PLAY));
                        notifyChanged(mPlayPauseAction);
                    }

                    int currentTime = (int) state.getPosition();
                    mPlaybackControlsRow.setCurrentTime(currentTime);
                    // mPlaybackControlsRow.setBufferedProgress(currentTime + SIMULATED_BUFFERED_TIME);
                    mPlaybackControlsRow.setBufferedProgress(mPlaybackController.calcBufferedTime(currentTime));
                }
            });
        }

        @Override
        public void onMetadataChanged(final MediaMetadata metadata) {
            Log.d(TAG, "received update of media metadata");
            updateMovieView(
                    metadata.getString(MediaMetadata.METADATA_KEY_DISPLAY_TITLE),
                    metadata.getString(MediaMetadata.METADATA_KEY_DISPLAY_SUBTITLE),
                    metadata.getString(MediaMetadata.METADATA_KEY_DISPLAY_ICON_URI),
                    metadata.getLong(MediaMetadata.METADATA_KEY_DURATION)
            );
        }
    }

}


