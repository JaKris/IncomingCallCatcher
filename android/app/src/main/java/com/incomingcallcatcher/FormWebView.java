package com.incomingcallcatcher;

import android.app.Activity;
import android.os.Bundle;
import android.webkit.WebView;

import androidx.annotation.Nullable;

public class FormWebView extends Activity {
    @Override
    protected void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        String url = getIntent().getStringExtra("url");
        byte[] postData = getIntent().getByteArrayExtra("postData");
        WebView webView = new WebView(this);
        setContentView(webView);
        webView.postUrl(url,postData);
    }
}
