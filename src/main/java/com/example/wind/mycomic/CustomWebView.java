package com.example.wind.mycomic;

import android.annotation.TargetApi;
import android.app.Activity;
import android.app.ProgressDialog;
import android.content.Context;
import android.graphics.Bitmap;
import android.os.Build;
import android.os.Bundle;
import android.view.View;
import android.view.ViewGroup;
import android.view.WindowManager;
import android.webkit.JavascriptInterface;
import android.webkit.WebResourceRequest;
import android.webkit.WebResourceResponse;
import android.webkit.WebView;
import android.webkit.WebViewClient;

import com.example.wind.mycomic.utils.PlayMovie;
import com.example.wind.mycomic.utils.VideoEnabledWebChromeClient;
import com.example.wind.mycomic.utils.VideoEnabledWebView;

import java.io.IOException;
import java.net.MalformedURLException;
import java.net.URL;
import java.net.URLConnection;
import java.util.HashMap;
import java.util.Map;

/**
 * Created by wind on 2017/1/29.
 */

public class CustomWebView extends Activity {
    private VideoEnabledWebView webView;
    private VideoEnabledWebChromeClient webChromeClient;
    private PlayMovie mPlayMovie;
    private ProgressDialog progressBar;
    private static int hcount = 0;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.custom_webview);

        ShareDataClass.getInstance().ajax_activity = this;
        String page_url = (String) getIntent().getSerializableExtra(DetailsActivity.MOVIE_VIDEO_PAGE_URL);
        String video_url = page_url;

        if(page_url.indexOf("preview") >= 0) {
            video_url = page_url.substring(0, page_url.indexOf("preview"));
        }

        //Save the webview
        webView = (VideoEnabledWebView)findViewById(R.id.webView);

        // Initialize the VideoEnabledWebChromeClient and set event handlers
        View nonVideoLayout = findViewById(R.id.nonVideoLayout); // Your own view, read class comments
        ViewGroup videoLayout = (ViewGroup)findViewById(R.id.videoLayout); // Your own view, read class comments
        //noinspection all
        View loadingView = getLayoutInflater().inflate(R.layout.view_loading_video, null); // Your own view, read class comments
        webChromeClient = new VideoEnabledWebChromeClient(nonVideoLayout, videoLayout, loadingView, webView) // See all available constructors...
        {
            // Subscribe to standard events, such as onProgressChanged()...
            @Override
            public void onProgressChanged(WebView view, int progress) {
                // Your code...
            }
        };

        webChromeClient.setOnToggledFullscreen(new VideoEnabledWebChromeClient.ToggledFullscreenCallback()
        {
            @Override
            public void toggledFullscreen(boolean fullscreen)
            {
                // Your code to handle the full-screen change, for example showing and hiding the title bar. Example:
                if (fullscreen)
                {
                    WindowManager.LayoutParams attrs = getWindow().getAttributes();
                    attrs.flags |= WindowManager.LayoutParams.FLAG_FULLSCREEN;
                    attrs.flags |= WindowManager.LayoutParams.FLAG_KEEP_SCREEN_ON;
                    getWindow().setAttributes(attrs);
                    if (android.os.Build.VERSION.SDK_INT >= 14)
                    {
                        //noinspection all
                        getWindow().getDecorView().setSystemUiVisibility(View.SYSTEM_UI_FLAG_LOW_PROFILE);
                    }
                }
                else
                {
                    WindowManager.LayoutParams attrs = getWindow().getAttributes();
                    attrs.flags &= ~WindowManager.LayoutParams.FLAG_FULLSCREEN;
                    attrs.flags &= ~WindowManager.LayoutParams.FLAG_KEEP_SCREEN_ON;
                    getWindow().setAttributes(attrs);
                    if (android.os.Build.VERSION.SDK_INT >= 14)
                    {
                        //noinspection all
                        getWindow().getDecorView().setSystemUiVisibility(View.SYSTEM_UI_FLAG_VISIBLE);
                    }
                }

            }
        });

        webView.getSettings().setJavaScriptEnabled(true);
        webView.addJavascriptInterface(new MyJavaScriptInterface(this), "HtmlViewer");
        webView.getSettings().setDomStorageEnabled(true);
        webView.setWebChromeClient(webChromeClient);
        // Call private class InsideWebViewClient
        webView.setWebViewClient(new InsideWebViewClient());

        // Navigate anywhere you want, but consider that this classes have only been tested on YouTube's mobile site
        //video_url = "http://tx.acgvideo.com/1/27/16828499-1-hd.mp4?txTime=1501478086&platform=pc&txSecret=588d44b30ee21077932804b9468774f4&oi=998213733&rate=1280000&hfb=b99ffc3c5c68f00a33123bb25f882d5b";
        webView.loadUrl(video_url);
        //progressBar = ProgressDialog.show(CustomWebView.this, "自動跳轉解析", "讀取中...");
/*
        webview.getSettings().setJavaScriptEnabled(true);
        webview.addJavascriptInterface(new MyJavaScriptInterface(this), "HtmlViewer");
        webview.getSettings().setJavaScriptCanOpenWindowsAutomatically(true);
        webview.getSettings().setPluginState(WebSettings.PluginState.ON);
        webview.getSettings().setDomStorageEnabled(true);
        webview.getSettings().setMediaPlaybackRequiresUserGesture(false);
        webview.setWebChromeClient(new WebChromeClient());
        webview.setWebViewClient(new WebViewClient() {
            @Override
            public void onPageFinished(WebView view, String url) {
                webview.loadUrl("javascript:window.HtmlViewer.showHTML" +
                        "('<html>'+document.getElementsByTagName('html')[0].innerHTML+'</html>');");
            }
        });
        webview.loadUrl(video_url);
*/
    }

    private void setFullScreen() {
        WindowManager.LayoutParams attrs = getWindow().getAttributes();
        attrs.flags |= WindowManager.LayoutParams.FLAG_FULLSCREEN;
        attrs.flags |= WindowManager.LayoutParams.FLAG_KEEP_SCREEN_ON;
        getWindow().setAttributes(attrs);
        if (android.os.Build.VERSION.SDK_INT >= 14)
        {
            //noinspection all
            getWindow().getDecorView().setSystemUiVisibility(View.SYSTEM_UI_FLAG_LOW_PROFILE);
        }
    }

    @Override
    public void onStop () {
        if (progressBar.isShowing()) {
            progressBar.dismiss();
        }
        super.onStop();
    }

    private class InsideWebViewClient extends WebViewClient {
        private boolean isRedirected;
        /*
        @TargetApi(Build.VERSION_CODES.LOLLIPOP)
        @Override
        public WebResourceResponse shouldInterceptRequest(WebView view, WebResourceRequest request) {
            if (request != null && request.getUrl() != null && request.getMethod().equalsIgnoreCase("get")) {
                String scheme = request.getUrl().getScheme().trim();
                if (scheme.equalsIgnoreCase("http") || scheme.equalsIgnoreCase("https")) {
                    try {
                        URL url = new URL(request.getUrl().toString());
                        URLConnection connection = url.openConnection();
                        connection.setRequestProperty("User-Agent", "Mozilla/5.0 (Windows NT 10.0; Win64; x64) AppleWebKit/537.36 (KHTML, like Gecko) Chrome/62.0.3169.0 Safari/537.36");

                        if (hcount == 0) {
                            connection.setRequestProperty("Referer", "http://bangumi.bilibili.com/anime/5998/play#103898");
                            hcount = hcount + 1;
                        } else {
                            connection.setRequestProperty("Referer", request.getUrl().toString());
                        }
                        return new WebResourceResponse(connection.getContentType(), connection.getHeaderField("encoding"), connection.getInputStream());
                    } catch (MalformedURLException e) {
                        e.printStackTrace();
                    } catch (IOException e) {
                        e.printStackTrace();
                    }
                }
            }
            return null;
        }
        */
        @Override
        // Force links to be opened inside WebView and not in Default Browser
        // Thanks http://stackoverflow.com/a/33681975/1815624
        public boolean shouldOverrideUrlLoading(WebView view, String url) {
            view.loadUrl(url);
            isRedirected = true;
            return true;
        }

        @Override
        public void onPageStarted(WebView view, String url, Bitmap favicon) {

            if (!isRedirected) {
                //Do something you want when starts loading
            }

            isRedirected = false;
        }

        @Override
        public void onPageFinished(WebView view, String url) {
            /*
            String js = "javascript:";
            js += "document.getElementsByTagName('video')[0].play()";
            js += "javascript:window.HtmlViewer.showHTML" +
                    "('<html>'+document.getElementsByTagName('html')[0].innerHTML+'</html>');";
            */
            boolean con_d_1 = (url.indexOf("docs.google.com") >= 0) && !isRedirected;
            boolean con_d_2 = (url.indexOf("192.168.1.108") >= 0) && !isRedirected;

            if(url.indexOf("docs.google.com") >= 0 && url.indexOf("videoplayback") >= 0) {
                webView.loadUrl("javascript:document.getElementsByTagName('video')[0].play();document.getElementsByTagName('video')[0].webkitRequestFullscreen();");
                if (progressBar.isShowing()) {
                    progressBar.dismiss();
                }
            } else if(url.indexOf("googlevideo.com") >= 0 && url.indexOf("videoplayback") >= 0 && !isRedirected) {
                ShareDataClass.getInstance().drama8_truly_link = url;
                ShareDataClass.getInstance().ajax_activity = null;
                finish();
            } else if (con_d_1 || con_d_2) {
                webView.loadUrl("javascript:window.HtmlViewer.showHTML" +
                        "('<html>'+document.getElementsByTagName('html')[0].innerHTML+'</html>');");
            }
        }
    }

    @Override
    public void onBackPressed()
    {
        // Notify the VideoEnabledWebChromeClient, and handle it ourselves if it doesn't handle it
        if (!webChromeClient.onBackPressed())
        {
            if (webView.canGoBack())
            {
                webView.goBack();
            }
            else
            {
                // Standard back button implementation (for example this could close the app)
                super.onBackPressed();
            }
        }
    }

    class MyJavaScriptInterface {
        private Context cxt;

        MyJavaScriptInterface(Context _ctx) {
            cxt = _ctx;
        }

        @JavascriptInterface
        public void showHTML(String html) {
            String video_html = html;
            String fmt_stream_str = ShareDataClass.getInstance().str_between(video_html, "fmt_stream_map\",\"", "\"]");
            fmt_stream_str = ShareDataClass.getInstance().decode(fmt_stream_str).trim();
            if(fmt_stream_str.compareTo("") != 0) {
                Map<Integer, Integer> resolution_map = new HashMap<Integer, Integer>();
                resolution_map.put(22, 1);
                resolution_map.put(35, 2);
                resolution_map.put(59, 3);
                resolution_map.put(18, 4);
                resolution_map.put(34, 5);
                String[] url = fmt_stream_str.split(",");
                String final_url = "";

                int target_resolution = 6;
                for (int i = 0; i < url.length; i++) {
                    String v_tag = url[i].split("\\|")[0];
                    String v_url = url[i].split("\\|")[1];
                    if (resolution_map.get(Integer.parseInt(v_tag)) < target_resolution) {
                        target_resolution = resolution_map.get(Integer.parseInt(v_tag));
                        final_url = v_url;
                    }
                }

                final String final_url1 = final_url;
                webView.post(new Runnable() {
                    @Override
                    public void run() {
                        webView.loadUrl(final_url1);
                    }
                });
            } else {
                if (progressBar.isShowing()) {
                    progressBar.dismiss();
                }
            }
        }
    }
}
