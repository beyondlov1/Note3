package com.beyond.fly.note3;

import android.content.Intent;
import android.net.Uri;
import android.os.Bundle;
import android.support.annotation.Nullable;
import android.support.v7.app.ActionBar;
import android.support.v7.app.AppCompatActivity;
import android.webkit.WebSettings;
import android.webkit.WebView;
import android.webkit.WebViewClient;
import android.widget.TextView;

import java.util.regex.Pattern;

/**
 * Created by beyond on 2017/12/29.
 */

public class ContentActivity extends AppCompatActivity {

    public static String contentActivityAction="com.beyond.action.CONTENT_ACTIVITY";

    private TextView textView14;
    private TextView textView15;
    private WebView webView;

    @Override
    protected void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.content_activity);
        hideActionBar();
        initViews();
        Intent intent=getIntent();
        if (contentActivityAction.equals(intent.getAction())){
            textView14.setText(intent.getStringExtra("title"));
            textView15.setText(intent.getStringExtra("content"));
            if (!textView14.getText().toString().equals("")||!textView15.getText().toString().equals("")) {
                handleIntent();
            }
        }
    }

    private void hideActionBar(){
        ActionBar actionBar=getSupportActionBar();
        if (actionBar!=null){
            actionBar.hide();
        }
    }

    private void handleIntent(){
        String things=textView14.getText().toString()+textView15.getText().toString();
        String urlWeGet=null;
        if (things.contains("http://")||things.contains("https://")){
            //含网址的获取网址
            //网址正则式
            Pattern pattern = Pattern.compile("^(http|https|ftp)\\://([a-zA-Z0-9\\.\\-]+(\\:[a-zA-Z0-9\\.&%\\$\\-]+)*@)?((25[0-5]|2[0-4][0-9]|[0-1]{1}[0-9]{2}|[1-9]{1}[0-9]{1}|[1-9])\\.(25[0-5]|2[0-4][0-9]|[0-1]{1}[0-9]{2}|[1-9]{1}[0-9]{1}|[1-9]|0)\\.(25[0-5]|2[0-4][0-9]|[0-1]{1}[0-9]{2}|[1-9]{1}[0-9]{1}|[1-9]|0)\\.(25[0-5]|2[0-4][0-9]|[0-1]{1}[0-9]{2}|[1-9]{1}[0-9]{1}|[0-9])|([a-zA-Z0-9\\-]+\\.)*[a-zA-Z0-9\\-]+\\.[a-zA-Z]{2,4})(\\:[0-9]+)?(/[^/][a-zA-Z0-9\\.\\,\\?\\'\\\\/\\+&%\\$#\\=~_\\-@]*)*$");

            if (things.length()<200){
                //小于200有网址的获取网址
                if (pattern.matcher(things.substring(things.indexOf("http"), things.length())).matches()){
                    urlWeGet=things.substring(things.indexOf("http"), things.length());
                }else {
                    for (int i = things.length(); !pattern.matcher(things.substring(things.indexOf("http"), i)).matches(); i--) {
                        urlWeGet = things.substring(things.indexOf("http"), i);
                    }
                }
            }else {
                //大于200的有网址的获取网址
                String shortThings=things.substring(0,200);
                for (int i = shortThings.length(); !pattern.matcher(things.substring(shortThings.indexOf("http"), i)).matches(); i--) {
                    urlWeGet = shortThings.substring(shortThings.indexOf("http"), i);
                }
            }
        }else if (things.contains("content://")){
            Uri uri=Uri.parse(things);

            urlWeGet=things;

        } else {
            //不含网址的搜索
            if (things.length()>32){
                urlWeGet=null;
            }else {
                urlWeGet = "http://www.bing.com/search?q=" + things;
            }
        }

        WebSettings webSettings=webView.getSettings();
        webSettings.setJavaScriptEnabled(true);
        webSettings.setAppCacheEnabled(true);
        webSettings.setCacheMode(WebSettings.LOAD_DEFAULT);
        webSettings.setDomStorageEnabled(true);
        //不添加以下会用自带浏览器打开
        webView.setWebViewClient(new WebViewClient(){
            @Override
            public boolean shouldOverrideUrlLoading(WebView view, String url) {
                view.loadUrl(url);

                //return false 由webView加载url，return true 由应用代码处理url
                return false;
            }
        });

        webView.loadUrl(urlWeGet);
    }

    private void initViews(){
        textView14=findViewById(R.id.textView14);
        textView15=findViewById(R.id.textView15);
        webView=findViewById(R.id.webView);
    }
}
