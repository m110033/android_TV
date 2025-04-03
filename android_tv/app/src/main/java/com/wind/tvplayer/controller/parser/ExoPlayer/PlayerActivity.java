package com.wind.tvplayer.controller.parser.ExoPlayer;

import android.content.Intent;
import android.net.Uri;
import android.os.Bundle;
import android.view.KeyEvent;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;

import com.google.android.exoplayer2.ExoPlaybackException;
import com.google.android.exoplayer2.MediaItem;
import com.google.android.exoplayer2.PlaybackException;
import com.google.android.exoplayer2.Player;
import com.google.android.exoplayer2.SimpleExoPlayer;
import com.google.android.exoplayer2.source.MediaSource;
import com.google.android.exoplayer2.source.ProgressiveMediaSource;
import com.google.android.exoplayer2.source.hls.HlsMediaSource;
import com.google.android.exoplayer2.trackselection.DefaultTrackSelector;
import com.google.android.exoplayer2.ui.PlayerView;
import com.google.android.exoplayer2.upstream.DataSource;
import com.google.android.exoplayer2.upstream.DefaultDataSource;
import com.google.android.exoplayer2.upstream.DefaultHttpDataSource;
import com.google.android.exoplayer2.util.Util;
import com.wind.tvplayer.DetailsActivity;
import com.wind.tvplayer.R;
import com.wind.tvplayer.common.ShareVideo;
import com.wind.tvplayer.model.video.PlayMovie;

import java.security.cert.X509Certificate;
import javax.net.ssl.SSLContext;
import javax.net.ssl.SSLSocketFactory;
import javax.net.ssl.TrustManager;
import javax.net.ssl.X509TrustManager;

import okhttp3.OkHttpClient;

public class PlayerActivity extends AppCompatActivity {

    private PlayerView playerView;
    private SimpleExoPlayer player;
    private DefaultTrackSelector trackSelector;

    private boolean startAutoPlay = true;
    private int startWindow = 0;
    private long startPosition = 0;

    private PlayMovie selectedPlayMovie;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.player_activity);

        // 初始化 PlayerView
        playerView = findViewById(R.id.player_view);

        // 獲取播放的影片 URL
        Intent intent = getIntent();
        Integer actionIndex = Integer.parseInt(intent.getStringExtra(DetailsActivity.PLAY_MOVIE_INDEX));
        selectedPlayMovie = ShareVideo.getInstance().playMovieList.get(actionIndex);

        if (savedInstanceState != null) {
            startAutoPlay = savedInstanceState.getBoolean("auto_play");
            startWindow = savedInstanceState.getInt("window");
            startPosition = savedInstanceState.getLong("position");
        }
    }

    @Override
    protected void onStart() {
        super.onStart();
        if (Util.SDK_INT > 23) {
            initializePlayer();
        }
    }

    @Override
    protected void onResume() {
        super.onResume();
        if (Util.SDK_INT <= 23 || player == null) {
            initializePlayer();
        }
    }

    @Override
    protected void onPause() {
        super.onPause();
        if (Util.SDK_INT <= 23) {
            releasePlayer();
        }
    }

    @Override
    protected void onStop() {
        super.onStop();
        if (Util.SDK_INT > 23) {
            releasePlayer();
        }
    }

    private void initializePlayer() {
        if (player == null) {
            // 初始化 TrackSelector
            trackSelector = new DefaultTrackSelector(this);
            trackSelector.setParameters(trackSelector.buildUponParameters().build());

            // 建立 DefaultHttpDataSource.Factory
            DefaultHttpDataSource.Factory httpDataSourceFactory = new DefaultHttpDataSource.Factory()
                    .setUserAgent(Util.getUserAgent(this, "YourAppName"))
                    .setConnectTimeoutMs(8000)  // 8 秒連接超時
                    .setReadTimeoutMs(8000)     // 8 秒讀取超時
                    .setAllowCrossProtocolRedirects(true);

            // 建立使用上述 HttpDataSource.Factory 的 DataSource.Factory
            DefaultDataSource.Factory dataSourceFactory = new DefaultDataSource.Factory(
                    this, httpDataSourceFactory);

            // 初始化 SimpleExoPlayer
            player = new SimpleExoPlayer.Builder(this)
                    .setTrackSelector(trackSelector)
                    .build();

            // 將 Player 附加到 PlayerView
            playerView.setPlayer(player);

            // 設定播放事件監聽器
            player.addListener(new Player.Listener() {
                @Override
                public void onPlaybackStateChanged(int playbackState) {
                    if (playbackState == Player.STATE_ENDED) {
                        showToast("播放結束");
                    }
                }

                @Override
                public void onPlayerError(@NonNull PlaybackException error) {
                    showToast("播放錯誤：" + error.getMessage());
                    error.printStackTrace();  // 打印更詳細的錯誤信息
                }
            });

            // 使用 HTTP URL，避免 SSL 問題
            String videoUrlStr = "https://bitdash-a.akamaihd.net/content/sintel/hls/playlist.m3u8";
            Uri videoUri = Uri.parse(videoUrlStr);

            // 根據 URL 格式選擇合適的 MediaSource
            MediaSource mediaSource = buildMediaSource(videoUri, dataSourceFactory);
            player.setMediaSource(mediaSource);

            // 恢復播放位置
            boolean haveStartPosition = startWindow != 0 || startPosition != 0;
            if (haveStartPosition) {
                player.seekTo(startWindow, startPosition);
            }

            // 準備播放器
            player.prepare();
            player.setPlayWhenReady(startAutoPlay);
        }
    }

    private MediaSource buildMediaSource(Uri uri, DataSource.Factory dataSourceFactory) {
        String url = uri.toString().toLowerCase();

        // 根據 URI 判斷是否為 HLS
        if (url.contains(".m3u8")) {
            // HLS 格式
            return new HlsMediaSource.Factory(dataSourceFactory)
                    .createMediaSource(MediaItem.fromUri(uri));
        } else {
            // MP4 或其他格式 (Progressive)
            return new ProgressiveMediaSource.Factory(dataSourceFactory)
                    .createMediaSource(MediaItem.fromUri(uri));
        }
    }

    private SSLSocketFactory getTrustAllCertsSSLSocketFactory() {
        try {
            // 創建一個信任所有證書的 SSLContext
            final SSLContext sslContext = SSLContext.getInstance("TLS");
            sslContext.init(null, new TrustManager[]{getTrustAllCertsTrustManager()}, new java.security.SecureRandom());
            return sslContext.getSocketFactory();
        } catch (Exception e) {
            throw new RuntimeException(e);
        }
    }

    private X509TrustManager getTrustAllCertsTrustManager() {
        return new X509TrustManager() {
            @Override
            public void checkClientTrusted(X509Certificate[] chain, String authType) {
            }

            @Override
            public void checkServerTrusted(X509Certificate[] chain, String authType) {
            }

            @Override
            public X509Certificate[] getAcceptedIssuers() {
                return new X509Certificate[]{};
            }
        };
    }

    private void releasePlayer() {
        if (player != null) {
            // 保存播放位置
            startAutoPlay = player.getPlayWhenReady();
            startWindow = player.getCurrentWindowIndex();
            startPosition = Math.max(0, player.getContentPosition());

            // 釋放播放器
            player.release();
            player = null;
            trackSelector = null;
        }
    }

    @Override
    public void onSaveInstanceState(@NonNull Bundle outState) {
        super.onSaveInstanceState(outState);
        outState.putBoolean("auto_play", startAutoPlay);
        outState.putInt("window", startWindow);
        outState.putLong("position", startPosition);
    }

    @Override
    public boolean dispatchKeyEvent(KeyEvent event) {
        // 處理快進、快退和暫停按鍵
        if (playerView != null && playerView.dispatchKeyEvent(event)) {
            return true;
        }
        return super.dispatchKeyEvent(event);
    }

    private void showToast(String message) {
        Toast.makeText(getApplicationContext(), message, Toast.LENGTH_LONG).show();
    }
}