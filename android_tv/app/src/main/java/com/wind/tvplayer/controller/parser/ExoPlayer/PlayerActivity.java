package com.wind.tvplayer.controller.parser.ExoPlayer;

import android.content.Intent;
import android.net.Uri;
import android.os.Bundle;
import android.util.Log;
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

import org.json.JSONObject;

import java.io.IOException;
import java.security.cert.X509Certificate;
import java.util.Collections;
import java.util.HashMap;
import java.util.Map;
import java.util.concurrent.TimeUnit;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

import javax.net.ssl.SSLContext;
import javax.net.ssl.SSLSocketFactory;
import javax.net.ssl.TrustManager;
import javax.net.ssl.X509TrustManager;

import okhttp3.OkHttpClient;
import okhttp3.Request;
import okhttp3.RequestBody;

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
            trackSelector = new DefaultTrackSelector(this);
            trackSelector.setParameters(trackSelector.buildUponParameters().build());

            String tempUrl = selectedPlayMovie.getVideo_url();
            Uri uri = Uri.parse(tempUrl);

            // 建立播放器
            player = new SimpleExoPlayer.Builder(this)
                    .setTrackSelector(trackSelector)
                    .build();

            playerView.setPlayer(player);

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
                    error.printStackTrace();
                }
            });

            // 直接傳 URL 去 parser
            fetchM3u8Url(tempUrl);
        }
    }

    private void fetchM3u8Url(String url) {
        OkHttpClient client = new OkHttpClient.Builder()
                .connectTimeout(120, TimeUnit.SECONDS)
                .readTimeout(120, TimeUnit.SECONDS)
                .writeTimeout(120, TimeUnit.SECONDS)
                .build();

        try {
            JSONObject jsonObject = new JSONObject();
            jsonObject.put("url", url); // 傳入 URL

            RequestBody requestBody = RequestBody.create(
                    jsonObject.toString(),
                    okhttp3.MediaType.get("application/json; charset=utf-8")
            );

            Request request = new Request.Builder()
                    .url("https://video-parser-k1y9.onrender.com/parser/gamer")
                    .post(requestBody)
                    .build();

            client.newCall(request).enqueue(new okhttp3.Callback() {
                @Override
                public void onFailure(@NonNull okhttp3.Call call, @NonNull IOException e) {
                    runOnUiThread(() -> showToast("請求失敗：" + e.getMessage()));
                }

                @Override
                public void onResponse(@NonNull okhttp3.Call call, @NonNull okhttp3.Response response) throws IOException {
                    if (response.isSuccessful()) {
                        String responseBody = response.body().string();

                        try {
                            JSONObject jsonResponse = new JSONObject(responseBody);
                            boolean success = jsonResponse.getBoolean("success");

                            if (success) {
                                String m3u8Url = jsonResponse.getString("m3u8Url");
                                String referer = jsonResponse.optString("referer", url); // 後端提供的 referer，fallback 給原本的 url

                                runOnUiThread(() -> {
                                    // 動態設置 headers（改用後端給的 referer）
                                    Map<String, String> headers = new HashMap<>();
                                    headers.put("Referer", referer);
                                    headers.put("User-Agent", "Mozilla/5.0 (Windows NT 10.0; Win64; x64) AppleWebKit/537.36 (KHTML, like Gecko) Chrome/120.0.0.0 Safari/537.36");

                                    DefaultHttpDataSource.Factory httpDataSourceFactory = new DefaultHttpDataSource.Factory()
                                            .setConnectTimeoutMs(120_000)
                                            .setReadTimeoutMs(120_000)
                                            .setAllowCrossProtocolRedirects(true)
                                            .setDefaultRequestProperties(headers);

                                    DefaultDataSource.Factory dataSourceFactory = new DefaultDataSource.Factory(
                                            getApplicationContext(), httpDataSourceFactory);

                                    Uri videoUri = Uri.parse(m3u8Url);
                                    MediaSource mediaSource = buildMediaSource(videoUri, dataSourceFactory);

                                    boolean haveStartPosition = startWindow != 0 || startPosition != 0;
                                    if (haveStartPosition) {
                                        player.seekTo(startWindow, startPosition);
                                    }

                                    player.setMediaSource(mediaSource);
                                    player.prepare();
                                    player.setPlayWhenReady(startAutoPlay);
                                });

                            } else {
                                runOnUiThread(() -> showToast("解析失敗，找不到影片網址"));
                            }
                        } catch (Exception e) {
                            runOnUiThread(() -> showToast("JSON解析錯誤：" + e.getMessage()));
                        }
                    } else {
                        runOnUiThread(() -> showToast("伺服器錯誤：" + response.code()));
                    }
                }
            });
        } catch (Exception e) {
            showToast("例外錯誤：" + e.getMessage());
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