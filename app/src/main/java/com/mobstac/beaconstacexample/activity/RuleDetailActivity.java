package com.mobstac.beaconstacexample.activity;

import android.app.AlertDialog;
import android.content.Intent;
import android.net.Uri;
import android.os.Bundle;
import android.support.v7.app.AppCompatActivity;
import android.webkit.WebView;

import com.mobstac.beaconstacexample.R;

public class RuleDetailActivity extends AppCompatActivity {

    private int type;
    private String title;
    private String text;
    private String webUrl;
    private String mediaUrl;
    private AlertDialog dialogBuilder;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_rule_detail);
        Intent intent = getIntent();
        type = intent.getIntExtra("type", 1);
        title = intent.getStringExtra("title");
        if (type == 1)
            text = intent.getStringExtra("text");
        if (type == 2)
            webUrl = intent.getStringExtra("webUrl");
        if (type == 3)
            mediaUrl = intent.getStringExtra("mediaUrl");


        switch (type) {
            case 1:
//                dialogBuilder.setTitle(title).setMessage(text);
                break;
            case 2:
                dialogBuilder.setTitle(title);
                final WebView webView = new WebView(this);
                webView.loadUrl(webUrl);
                webView.getSettings().setBuiltInZoomControls(true);
                webView.setInitialScale(85);
                dialogBuilder.setView(webView);
                break;
            case 3:
                Uri uri = Uri.parse(mediaUrl);
                intent = new Intent(Intent.ACTION_VIEW, uri);
                startActivity(intent);
                break;
        }
    }


}
