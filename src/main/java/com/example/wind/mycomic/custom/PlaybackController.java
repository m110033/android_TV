package com.example.wind.mycomic.custom;

import android.app.Activity;
import android.graphics.Bitmap;
import android.media.MediaMetadata;
import android.media.MediaPlayer;
import android.media.session.MediaController;
import android.media.session.MediaSession;
import android.media.session.PlaybackState;
import android.net.Uri;
import android.os.AsyncTask;
import android.os.Bundle;
import android.os.Handler;
import android.os.Message;
import android.os.SystemClock;
import android.util.Log;
import android.widget.VideoView;

import com.bumptech.glide.Glide;
import com.bumptech.glide.request.animation.GlideAnimation;
import com.bumptech.glide.request.target.SimpleTarget;
import com.example.wind.mycomic.PlaybackOverlayFragment;
import com.example.wind.mycomic.R;
import com.example.wind.mycomic.ShareDataClass;
import com.example.wind.mycomic.siteParser.VideoSiteParser;
import com.example.wind.mycomic.utils.PlayMovie;

import java.lang.reflect.Field;
import java.net.URI;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.Iterator;
import java.util.Map;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

/**
 * Created by wind on 2017/1/7.
 */

public class PlaybackController {

    /* Constants */
    private static final String TAG = PlaybackController.class.getSimpleName();
    private static final String MEDIA_SESSION_TAG = "AndroidTVappTutorialSession";

    /* Attributes */
    private PlayMovie handlePlayMove;
    private Activity mActivity;
    private MediaSession mSession;
    private MediaSessionCallback mMediaSessionCallback;
    private VideoView mVideoView;
    private static ArrayList<PlayMovie> mItems =  ShareDataClass.getInstance().playMovieList; // new ArrayList<Movie>();

    /* Global variables */
    private int mCurrentPlaybackState = PlaybackState.STATE_NONE;
    private int mCurrentItem; // index of current item
    private int mPosition = 0;
    private long mStartTimeMillis;
    private long mDuration = -1;
    private static boolean THREAD_IS_LOADING = false;
    private static final int THREAD_LOADING_MOVIE_FINISH = 0;
    private SpinnerFragment mSinnerFragment = new SpinnerFragment();
    private HashMap<String, Integer> m3u8SegmentsMap;

    public PlaybackOverlayFragment getPlaybackOverlayFragment() {
        return playbackOverlayFragment;
    }

    public void setPlaybackOverlayFragment(PlaybackOverlayFragment playbackOverlayFragment) {
        this.playbackOverlayFragment = playbackOverlayFragment;
    }

    private PlaybackOverlayFragment playbackOverlayFragment;

    public int getCurrentPlaybackState() {
        return mCurrentPlaybackState;
    }

    public void setCurrentPlaybackState(int currentPlaybackState) {
        this.mCurrentPlaybackState = currentPlaybackState;
    }

    public void setDuration(long duration) {
        this.mDuration = duration;
    }

    public long getDuration() {
        return mDuration;
    }

    public int getCurrentItem() {
        return mCurrentItem;
    }

    public PlaybackController(Activity activity) {
        mActivity = activity;
        // mVideoView = (VideoView) activity.findViewById(VIDEO_VIEW_RESOURCE_ID);

        createMediaSession(mActivity);
    }

    public PlaybackController(Activity activity, int currentItemIndex, ArrayList<PlayMovie> items) {
        mActivity = activity;
        // mVideoView = (VideoView) activity.findViewById(VIDEO_VIEW_RESOURCE_ID);
        this.setPlaylist(currentItemIndex, items);
        createMediaSession(mActivity);
    }

    public void setPlaylist(int currentItemIndex, ArrayList<PlayMovie> items) {
        mCurrentItem = currentItemIndex;
        mItems = items;
        if(mItems == null){
            Log.e(TAG, "mItems null!!");
        }
    }

    private void createMediaSession(Activity activity) {
        if (mSession == null) {
            mSession = new MediaSession(activity, MEDIA_SESSION_TAG);
            mMediaSessionCallback = new MediaSessionCallback();
            mSession.setCallback(mMediaSessionCallback);
            mSession.setFlags(MediaSession.FLAG_HANDLES_MEDIA_BUTTONS |
                    MediaSession.FLAG_HANDLES_TRANSPORT_CONTROLS);

            mSession.setActive(true);
            activity.setMediaController(new MediaController(activity, mSession.getSessionToken()));
        }
    }

    public MediaSessionCallback getMediaSessionCallback() {
        return mMediaSessionCallback;
    }

    public void setVideoView (VideoView videoView) {
        mVideoView = videoView;

        /* Callbacks for mVideoView */
        setupCallbacks();

    }

    Handler playMovieHandler = new Handler ( ) {
        @Override
        public void handleMessage ( Message msg ) {
            switch ( msg.what )
            {
                case THREAD_LOADING_MOVIE_FINISH:
                    String truly_url = handlePlayMove.getTruly_link();

                    if(truly_url.compareTo("") != 0) {
                        /* total time is necessary to show video playing time progress bar */
                        playbackOverlayFragment.setTitle("");

                        int duration = 0;
                        if(m3u8SegmentsMap != null) {
                            Iterator it = m3u8SegmentsMap.entrySet().iterator();
                            while (it.hasNext()) {
                                Map.Entry entry = (Map.Entry) it.next();
                                duration += (int) entry.getValue();
                            }
                            duration *= 1000;
                        } else {
                            duration = (int) ShareDataClass.getInstance().getDuration(truly_url);
                        }
                        mStartTimeMillis = 0;

                        /* for blibli
                        String cid = "10186429";
                        String video_url = "http://www.bilibili.tv/video/av" + cid;
                        String appkey = "84956560bc028eb7";
                        String secret = "94aba54af9065f71de72f5508f1cd42e";
                        Long tsLong = System.currentTimeMillis()/1000;
                        String ts = tsLong.toString();
                        String para = "appkey=" + appkey + "&cid=" + cid + "&otype=json&quality=2&type=mp4";
                        String sign = ShareDataClass.getInstance().md5(para + secret);
                        String api = "http://interface.bilibili.com/playurl?" + para + "&sign=" + sign;
                        truly_url = "http://tx.acgvideo.com/1/27/16828499-1-hd.mp4?txTime=1501478086&platform=pc&txSecret=588d44b30ee21077932804b9468774f4&oi=998213733&rate=1280000&hfb=b99ffc3c5c68f00a33123bb25f882d5b";
                        Map<String, String> headerMap = new HashMap<String, String>();
                        headerMap.put("User-Agent", "Mozilla/5.0 (Windows NT 10.0; WOW64; rv:51.0) Gecko/20100101 Firefox/51.0");
                        headerMap.put("Referer", "http://bangumi.bilibili.com/anime/5998/play#103898");
                        try {
                            Field field = VideoView.class.getDeclaredField("mHeaders");
                            field.setAccessible(true);
                            field.set(mVideoView,  headerMap);
                        } catch (Exception e) {
                            e.printStackTrace();
                        }
                        mVideoView.setVideoURI(Uri.parse(truly_url), headerMap);
                        */

                        if (ShareDataClass.getInstance().cookieMap.size() > 0) {
                            Map<String, String> headers = new HashMap<String, String>();
                            mVideoView.setVideoURI(Uri.parse(truly_url), ShareDataClass.getInstance().cookieMap);
                        } else {
                            mVideoView.setVideoPath(truly_url);
                        }

                        PlayMovie movie = mItems.get(getCurrentItem());
                        if(movie.getDuration() == 0) {
                            movie.setDuration(duration);
                        }
                        playbackOverlayFragment.getmPlaybackControlsRow().setTotalTime(duration);
                        playbackOverlayFragment.getmPlaybackControlsRow().setCurrentTime(0);
                        playbackOverlayFragment.getmPlaybackControlsRow().setBufferedProgress(0);
                        playbackOverlayFragment.getmRowsAdapter().notifyArrayItemRangeChanged(0, playbackOverlayFragment.getmRowsAdapter().size());
                        updateMetadata();
                        setCurrentPlaybackState(PlaybackState.STATE_PLAYING);
                        playPause(mCurrentPlaybackState == PlaybackState.STATE_PLAYING);
                    } else {
                        playbackOverlayFragment.setTitle("無法找到片源");
                    }
                    try {
                        playbackOverlayFragment.getFragmentManager().beginTransaction().remove(mSinnerFragment).commit();
                    } catch (Exception e) { }
                    break;
                default:
                    playbackOverlayFragment.getFragmentManager().beginTransaction().remove(mSinnerFragment).commit();
            }
            THREAD_IS_LOADING = false;
        }
    };

    private class ShowSpinnerTask extends AsyncTask<PlayMovie, Void, Void> {
        @Override
        protected void onPreExecute() {
            playbackOverlayFragment.getFragmentManager().beginTransaction().add(R.id.playback_controls_fragment, mSinnerFragment).commit();
        }

        @Override
        protected Void doInBackground(final PlayMovie... params) {
            handlePlayMove = (PlayMovie) params[0];

            if(!THREAD_IS_LOADING ) {
                if (handlePlayMove.getTruly_link().compareTo("") != 0) {
                    String truly_url = handlePlayMove.getTruly_link();
                    handlePlayMove.setTruly_link(truly_url);
                    Message m = new Message();
                    m.what = THREAD_LOADING_MOVIE_FINISH;
                    playMovieHandler.sendMessage(m);
                } else {
                    if (handlePlayMove.getMovie_categoryIndex() >= 0 && handlePlayMove.getMovie_categoryIndex() <= 1) {
                        Thread thread = new Thread(new Runnable() {
                            @Override
                            public void run() {
                                final String page_url = handlePlayMove.getVideo_url();
                                String cur_video_url = page_url.substring(0, page_url.indexOf("preview"));
                                VideoSiteParser videoSiteParser = new VideoSiteParser();
                                String truly_video_link = videoSiteParser.doParser(cur_video_url, "");
                                handlePlayMove.setTruly_link(truly_video_link);
                                Message m = new Message();
                                m.what = THREAD_LOADING_MOVIE_FINISH;
                                playMovieHandler.sendMessage(m);
                            }
                        });
                        try {
                            thread.start();
                            thread.join();
                        } catch (InterruptedException e) {
                            e.printStackTrace();
                        }
                    } else if (handlePlayMove.getMovie_categoryIndex() == 2) {
                        final String page_url = handlePlayMove.getVideo_url();
                        Thread thread = new Thread(new Runnable() {
                            @Override
                            public void run() {
                                //Get device name
                                String refSN = page_url.substring(page_url.indexOf("=") + 1);
                                String url = "http://ani.gamer.com.tw/ajax/getdeviceid.php";
                                String devideHtml = ShareDataClass.getInstance().GetHttps(url);
                                String truly_url = "";

                                if (devideHtml.compareTo("") != 0) {
                                    String deviceID = ShareDataClass.getInstance().str_between(devideHtml, "\"deviceid\":\"", "\"}");
                                    String SN = "";
                                    if(page_url.indexOf("animeVideo") < 0) {
                                        String refHtml = "http://ani.gamer.com.tw/animeRef.php?sn=" + refSN;
                                        String videoHtml = ShareDataClass.getInstance().GetHttps(refHtml);
                                        SN = ShareDataClass.getInstance().str_between(videoHtml, "sn=", "\"");
                                    } else {
                                        SN = ShareDataClass.getInstance().str_between(page_url, "sn=", "\"");
                                    }
                                    String adHtml = ShareDataClass.getInstance().GetHttps("https://i2.bahamut.com.tw/JS/ad/animeVideo.js");
                                    String adID = ShareDataClass.getInstance().str_between(adHtml, "id=", "\"");
                                    //Unlock AD
                                    ShareDataClass.getInstance().GetHttps("http://ani.gamer.com.tw/ajax/videoCastcishu.php?s=" + adID + "&sn=" + SN);

                                    THREAD_IS_LOADING = true;
                                    try {
                                        while(true) {
                                            ShareDataClass.getInstance().GetHttps("http://ani.gamer.com.tw/ajax/videoCastcishu.php?s=" + adID + "&sn=" + SN + "&ad=end");
                                            String m3u8Url = ShareDataClass.getInstance().GetHttps("http://ani.gamer.com.tw/ajax/m3u8.php?sn=" + SN + "&device=" + deviceID);
                                            String error_code = ShareDataClass.getInstance().str_between(m3u8Url, "{\"src\":\"", "\"}");
                                            if(m3u8Url.indexOf("error\":7") < 0 && m3u8Url.indexOf("error\":15") < 0) {
                                                ShareDataClass.getInstance().GetHttps("http://ani.gamer.com.tw/ajax/videoCastcishu.php?s=" + adID + "&sn=" + SN + "&ad=end");

                                                //Get truly url
                                                truly_url = ShareDataClass.getInstance().str_between(m3u8Url, "{\"src\":\"", "\"}");
                                                truly_url = "http:" + truly_url.replace("\\/", "/");

                                                Map<String, Integer> resolution_map = new HashMap<String, Integer>();
                                                resolution_map.put("640x360", 4);
                                                resolution_map.put("960x540", 3);
                                                resolution_map.put("1280x720", 1);
                                                resolution_map.put("1920x1080", 2);

                                                Map<String, String> video_list = new HashMap<String, String>();
                                                //Parser m3u8
                                                String m3u8_str = ShareDataClass.getInstance().GetHttps(truly_url);
                                                String[] m3u8_data = m3u8_str.split(Pattern.quote("#EXT-X-STREAM-INF"));
                                                for(int j = 1; j < m3u8_data.length; j++) {
                                                    String cur_data = m3u8_data[j];
                                                    String resolution = ShareDataClass.getInstance().str_between(cur_data, "RESOLUTION=", "gamer").trim();
                                                    String m3u8_link = cur_data.substring(cur_data.indexOf("gamer"));
                                                    video_list.put(resolution, m3u8_link);
                                                }

                                                int priv_index = 10;
                                                Iterator it = video_list.entrySet().iterator();
                                                while (it.hasNext()) {
                                                    Map.Entry pair = (Map.Entry)it.next();
                                                    String resolu = (String) pair.getKey();
                                                    if(resolution_map.containsKey(resolu)) {
                                                        if (priv_index > resolution_map.get(resolu)) {
                                                            priv_index = resolution_map.get(resolu);
                                                            m3u8_str = (String) pair.getValue();
                                                        }
                                                    }
                                                    if(priv_index == 10 && !it.hasNext()) {
                                                        m3u8_str = (String) pair.getValue();
                                                    }
                                                }

                                                // Get duration
                                                // Get domain
                                                String temp_str = m3u8_str;
                                                String domain_str = "" + m3u8_str.substring(0, m3u8_str.indexOf("-video"));
                                                m3u8_str = "http://gamer-cds.cdn.hinet.net/vod/gamer/" + domain_str + "/hls-ae-2s/" + temp_str;

                                                m3u8SegmentsMap = null;
                                                String digitRegex = "\\d+";
                                                Pattern p = Pattern.compile(digitRegex);
                                                m3u8_str = ShareDataClass.getInstance().GetHttps(m3u8_str);
                                                String[] lines = m3u8_str.split(System.getProperty("line.separator"));
                                                for(int j = 0; j < lines.length; j++) {
                                                    String cur_line = lines[j];
                                                    if(cur_line.equals("#EXTM3U")){ //start of m3u8
                                                        m3u8SegmentsMap = new HashMap<String, Integer>();
                                                    }else if(cur_line.contains("#EXTINF")){ //once found EXTINFO use runner to get the next line which contains the media file, parse duration of the segment
                                                        Matcher matcher = p.matcher(cur_line);
                                                        matcher.find(); //find the first matching digit, which represents the duration of the segment, dont call .find() again that will throw digit which may be contained in the description.
                                                        if( (j + 1) < lines.length) {
                                                            String net_line = lines[++j];
                                                            m3u8SegmentsMap.put(net_line, Integer.parseInt(matcher.group(0)));
                                                        }
                                                    }
                                                }
                                                handlePlayMove.setTruly_link(truly_url);
                                                Message m = new Message();
                                                m.what = THREAD_LOADING_MOVIE_FINISH;
                                                playMovieHandler.sendMessage(m);
                                                break;
                                            } else {
                                                Thread.sleep(2000);
                                            }
                                        }
                                    } catch (InterruptedException e) {
                                        e.printStackTrace();
                                    } catch (Exception e) {
                                        truly_url = "";
                                        handlePlayMove.setTruly_link(truly_url);
                                        Message m = new Message();
                                        m.what = THREAD_LOADING_MOVIE_FINISH;
                                        playMovieHandler.sendMessage(m);
                                    }
                                }
                            }
                        });
                        thread.start();
                        SystemClock.sleep(5000);
                    } else if (handlePlayMove.getMovie_categoryIndex() >= 3 && handlePlayMove.getMovie_categoryIndex() <= 9) {
                        final String video_url = handlePlayMove.getVideo_url();
                        final String video_type = handlePlayMove.getVideo_type();
                        Thread thread = new Thread(new Runnable() {
                            @Override
                            public void run() {
                            try {
                                VideoSiteParser videoSiteParser = new VideoSiteParser();
                                String truly_video_link = videoSiteParser.doParser(video_url, video_type);
                                handlePlayMove.setTruly_link(truly_video_link);
                                Message m = new Message();
                                m.what = THREAD_LOADING_MOVIE_FINISH;
                                playMovieHandler.sendMessage(m);
                            } catch (Exception e) {
                                handlePlayMove.setTruly_link("");
                                Message m = new Message();
                                m.what = THREAD_LOADING_MOVIE_FINISH;
                                playMovieHandler.sendMessage(m);
                            }
                            }
                        });
                        thread.start();
                    } else {
                        Message m = new Message();
                        m.what = -1;
                        playMovieHandler.sendMessage(m);
                    }
                }
            }

            return null;
        }

        @Override
        protected void onPostExecute(Void aVoid) {
            //playbackOverlayFragment.getFragmentManager().beginTransaction().remove(mSpinnerFragment).commit();
        }
    }

    public void setMovie (PlayMovie movie) {
        // Log.v(TAG, "setMovie: " + movie.toString());
        //mVideoView.setVideoPath(movie.getTruly_link());
    }

    public void setVideoPathWithHandle(PlayMovie movie) {
        setPosition(0);
        new ShowSpinnerTask().execute(movie);
    }

    public void setVideoPath(String videoUrl) {
        setPosition(0);
        //mVideoView.setVideoPath(videoUrl);
    }

    public void setPosition(int position) {
        if (position > mDuration) {
            Log.d(TAG, "position: " + position + ", mDuration: " + mDuration);
            mPosition = (int) mDuration;
        } else if (position < 0) {
            mPosition = 0;
            mStartTimeMillis = System.currentTimeMillis();
        } else {
            mPosition = position;
        }
        mStartTimeMillis = System.currentTimeMillis();
        Log.d(TAG, "position set to " + mPosition);
    }

    public int getPosition() {
        return mPosition;
    }

    public int getCurrentPosition() {
        return mVideoView.getCurrentPosition();
    }

    private void updatePlaybackState() {
        PlaybackState.Builder stateBuilder = new PlaybackState.Builder()
                .setActions(getAvailableActions());
        int state = PlaybackState.STATE_PLAYING;
        if (mCurrentPlaybackState == PlaybackState.STATE_PAUSED || mCurrentPlaybackState == PlaybackState.STATE_NONE) {
            state = PlaybackState.STATE_PAUSED;
        }
        // stateBuilder.setState(state, mPosition, 1.0f);
        stateBuilder.setState(state, getCurrentPosition(), 1.0f);
        mSession.setPlaybackState(stateBuilder.build());
    }

    private long getAvailableActions() {
        long actions = PlaybackState.ACTION_PLAY |
                PlaybackState.ACTION_PAUSE |
                PlaybackState.ACTION_PLAY_PAUSE |
                PlaybackState.ACTION_REWIND |
                PlaybackState.ACTION_FAST_FORWARD |
                PlaybackState.ACTION_SKIP_TO_PREVIOUS |
                PlaybackState.ACTION_SKIP_TO_NEXT |
                PlaybackState.ACTION_PLAY_FROM_MEDIA_ID |
                PlaybackState.ACTION_PLAY_FROM_SEARCH;
        return actions;
    }

    /**
     * should be called Activity's onDestroy
     */
    public void finishPlayback() {
        if (mVideoView != null) {
            mVideoView.stopPlayback();
            mVideoView.suspend();
            mVideoView.setVideoURI(null);
        }
        releaseMediaSession();
    }


    public int getBufferPercentage() {
        return mVideoView.getBufferPercentage();
    }

    public int calcBufferedTime(int currentTime) {
        int bufferedTime;
        bufferedTime = currentTime + (int) ((mDuration - currentTime) * getBufferPercentage()) / 100;
        return bufferedTime;
    }

    private void setupCallbacks() {

        mVideoView.setOnErrorListener(new MediaPlayer.OnErrorListener() {

            @Override
            public boolean onError(MediaPlayer mp, int what, int extra) {
                mVideoView.stopPlayback();
                mCurrentPlaybackState = PlaybackState.STATE_NONE;
                return false;
            }
        });

        mVideoView.setOnPreparedListener(new MediaPlayer.OnPreparedListener() {
            @Override
            public void onPrepared(MediaPlayer mp) {
                if (mCurrentPlaybackState == PlaybackState.STATE_PLAYING) {
                    mVideoView.start();
                }
            }
        });

        mVideoView.setOnCompletionListener(new MediaPlayer.OnCompletionListener() {
            @Override
            public void onCompletion(MediaPlayer mp) {
                mCurrentPlaybackState = PlaybackState.STATE_NONE;
            }
        });
    }

    public void setCurrentItem(int currentItem) {
        Log.v(TAG, "setCurrentItem: " + currentItem);
        this.mCurrentItem = currentItem;
    }

    public void updateMetadata() {
        Log.i(TAG, "updateMetadata: getCurrentItem" + getCurrentItem());
        if(mItems.size() > getCurrentItem()) {
            PlayMovie movie = mItems.get(getCurrentItem());
            if (movie.getDuration() != 0) {
                mDuration = movie.getDuration();
            } else {
                mDuration = ShareDataClass.getInstance().getDuration(movie.getTruly_link());
            }
            updateMetadata(movie);
        }
    }

    public void updateMetadata(PlayMovie movie) {
        final MediaMetadata.Builder metadataBuilder = new MediaMetadata.Builder();

        //metadataBuilder.putString(MediaMetadata.METADATA_KEY_MEDIA_ID, Long.toString(movie.getId()));
        metadataBuilder.putString(MediaMetadata.METADATA_KEY_DISPLAY_TITLE,  movie.getVideo_title());
        metadataBuilder.putString(MediaMetadata.METADATA_KEY_DISPLAY_SUBTITLE, movie.getMovie_title());
        metadataBuilder.putString(MediaMetadata.METADATA_KEY_DISPLAY_DESCRIPTION, movie.getVideo_intro());
        metadataBuilder.putString(MediaMetadata.METADATA_KEY_DISPLAY_ICON_URI, movie.getVideo_img());
        metadataBuilder.putLong(MediaMetadata.METADATA_KEY_DURATION, mDuration);

        // And at minimum the title and artist for legacy support
        metadataBuilder.putString(MediaMetadata.METADATA_KEY_TITLE,  movie.getVideo_title());
        metadataBuilder.putString(MediaMetadata.METADATA_KEY_ARTIST, movie.getMovie_category());

        Glide.with(mActivity)
                .load(movie.getVideo_img())
                .asBitmap()
                .into(new SimpleTarget<Bitmap>(500, 500) {
                    @Override
                    public void onResourceReady(Bitmap bitmap, GlideAnimation anim) {

                        metadataBuilder.putBitmap(MediaMetadata.METADATA_KEY_ART, bitmap);
                        mSession.setMetadata(metadataBuilder.build());
                    }
                });
    }

    public void releaseMediaSession() {
        if(mSession != null) {
            mSession.release();
        }
    }

    public void playPause(boolean doPlay) {

        if (mCurrentPlaybackState == PlaybackState.STATE_NONE) {
            /* Callbacks for mVideoView */
            setupCallbacks();
        }

        //if (doPlay && mCurrentPlaybackState != PlaybackState.STATE_PLAYING) {
        if (doPlay) { // Play
            Log.d(TAG, "playPause: play");
            if(mCurrentPlaybackState == PlaybackState.STATE_PLAYING) {
                /* if current state is already playing, do nothing */
                return;
            } else {
                mCurrentPlaybackState = PlaybackState.STATE_PLAYING;
                mVideoView.start();
                mStartTimeMillis = System.currentTimeMillis();
            }
        } else { // Pause
            Log.d(TAG, "playPause: pause");
            if(mCurrentPlaybackState == PlaybackState.STATE_PAUSED) {
                /* if current state is already paused, do nothing */
                return;
            } else {
                mCurrentPlaybackState = PlaybackState.STATE_PAUSED;
            }
            setPosition(mVideoView.getCurrentPosition());
            mVideoView.pause();

        }

        updatePlaybackState();
    }

    public void fastForward() {
        if (mDuration != -1) {
            // Fast forward 10 seconds.
            mMediaSessionCallback.onSeekTo(getCurrentPosition() + (10 * 1000));
            /*setPosition(getCurrentPosition() + (10 * 1000));
            mVideoView.seekTo(mPosition);*/
        }

    }

    public void rewind() {
        // rewind 10 seconds
        mMediaSessionCallback.onSeekTo(getCurrentPosition() - (10 * 1000));
        /*
        setPosition(getCurrentPosition() - (10 * 1000));
        mVideoView.seekTo(mPosition);
        */
    }


    private class MediaSessionCallback extends MediaSession.Callback {
        @Override
        public void onPlay() {
            playPause(true);
        }

        @Override
        public void onPause() {
            playPause(false);
        }

        @Override
        public void onSkipToNext() {
            if(!THREAD_IS_LOADING) {
                if (++mCurrentItem >= mItems.size()) { // Current Item is set to next here
                    mCurrentItem = 0;
                }
                Log.d(TAG, "onSkipToNext: " + mCurrentItem);
                PlayMovie movie = mItems.get(mCurrentItem);
                if (movie != null) {
                    setVideoPathWithHandle(movie);
                    updateMetadata();
                    playPause(mCurrentPlaybackState == PlaybackState.STATE_PLAYING);
                } else {
                    Log.e(TAG, "onSkipToNext movie is null!");
                }
            }
        }


        @Override
        public void onSkipToPrevious() {
            if(!THREAD_IS_LOADING) {
                if (--mCurrentItem < 0) { // Current Item is set to previous here
                    mCurrentItem = mItems.size() - 1;
                }

                PlayMovie movie = mItems.get(mCurrentItem);
                if (movie != null) {
                    setVideoPathWithHandle(movie);
                } else {
                    Log.e(TAG, "onSkipToPrevious movie is null!");
                }
            }
        }

        @Override
        public void onPlayFromMediaId(String mediaId, Bundle extras) {
            if(!THREAD_IS_LOADING) {
                mCurrentItem = Integer.parseInt(mediaId);
                PlayMovie movie = mItems.get(mCurrentItem);
                if (movie != null) {
                    setVideoPathWithHandle(movie);
                } else {
                    Log.e(TAG, "onSkipToPrevious movie is null!");
                }
            }
        }

        @Override
        public void onSeekTo(long pos) {
            setPosition((int) pos);
            mVideoView.seekTo(mPosition);
            updatePlaybackState();
        }

        @Override
        public void onFastForward() {
            fastForward();
        }

        @Override
        public void onRewind() {
            rewind();
        }
    }

}