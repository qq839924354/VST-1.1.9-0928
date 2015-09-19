package com.vst.itv52.v1.player;

import android.content.Context;
import android.os.Handler;
import android.os.Message;
import android.util.AttributeSet;
import android.webkit.JavascriptInterface;
import android.webkit.WebSettings;
import android.webkit.WebView;
import android.webkit.WebViewClient;

public class TransformationWebView extends WebView {

    private OnLoadUrlListener listener;

    public TransformationWebView(Context context) {
        super(context);
        init(context);
    }

    public TransformationWebView(Context context, OnLoadUrlListener listener) {
        super(context);
        this.listener = listener;
        init(context);
    }

    public TransformationWebView(Context context, AttributeSet attrs) {
        super(context, attrs);
        init(context);
    }

    public TransformationWebView(Context context, AttributeSet attrs, int defStyle) {
        super(context, attrs, defStyle);
        init(context);
    }

    private void init(Context context) {
        WebSettings settings = this.getSettings();
        // 支持javascript
        settings.setJavaScriptEnabled(true);
        // 支持localstorage和essionStorage
        settings.setDomStorageEnabled(true);
        // 开启应用缓存
        settings.setAppCacheEnabled(true);

        String cacheDir = context.getApplicationContext()
            .getDir("cache", Context.MODE_PRIVATE).getPath();
        settings.setAppCachePath(cacheDir);
        settings.setCacheMode(WebSettings.LOAD_DEFAULT);
        // 设置缓冲大小
        settings.setAppCacheMaxSize(1024 * 1024 * 10);
        settings.setAllowFileAccess(true);

        this.setWebViewClient(mWebViewClient);

        settings.setUserAgentString(
            "Mozilla/5.0 (iPhone; CPU iPhone OS 7_1 like Mac OS X) AppleWebKit/537.51.2 (KHTML, like Gecko) Version/7.0 Mobile/11D5145e Safari/9537.53");

        this.addJavascriptInterface(new InJavaScriptLocalObj(), "js_method");
    }

    private WebViewClient mWebViewClient = new WebViewClient() {

        @Override
        public void onPageFinished(WebView view, String url) {

            view.loadUrl(
                "javascript:window.js_method.showSource(document.getElementsByTagName('video')[0].src);");
            super.onPageFinished(view, url);
        }

    };


    final class InJavaScriptLocalObj {
        @JavascriptInterface
        public void showSource(String html5url) {
            if (html5url != null) {
            	//http://data.vod.itc.cn/?new=/198/179/GIqXJZHBTBqvW9PRdNm77B.mp4&vid=2412970&plat=17&mkey=MHLTcmyXW6rhufT67cM_1G2wV3OZboc2&ch=tv&vid=2412969&uid=1435587899255573&plat=17&pt=5&prod=h5&pg=1&eye=0&cv=1.0.0&qd=680&src=1105&cateCode=101&type=mp4
                if (html5url.contains("type=flv")) {
                    html5url = html5url.replace("type=flv", "type=mp4");
                } else if (html5url.contains("type=mp4")) {
                } else {
                    html5url = html5url + "&type=mp4";
                }
                Message msg = new Message();
                msg.obj = html5url;
                handler.sendMessage(msg);
            }

        }
    }


    private Handler handler = new Handler() {
        public void handleMessage(android.os.Message msg) {
            String url = (String) msg.obj;
            if (listener != null) {
                listener.onFinish(url);
            }
        }

    };

    public void setOnLoadUrlListener(OnLoadUrlListener listener) {
        this.listener = listener;
    }

    @Override
    public void loadUrl(String url) {//http://tv.sohu.com/20150610/n414728214.shtml
    	//http://www.iqiyi.com/v_19rroheauw.html
        super.loadUrl(url);
    }


    public interface OnLoadUrlListener {
        public void onFinish(String url);
    }



}
