package com.mobstac.beaconstacexample.activity;

import android.content.pm.PackageInfo;
import android.content.pm.PackageManager;
import android.graphics.Color;
import android.graphics.drawable.ColorDrawable;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.view.Menu;
import android.view.MenuItem;
import android.widget.TextView;

import com.mobstac.beaconstacexample.R;

public class AboutActivity extends AppCompatActivity {

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_about);
        android.support.v7.app.ActionBar bar = getSupportActionBar();
        bar.setBackgroundDrawable(new ColorDrawable(Color.parseColor("#228FC8")));

        TextView tvAbout = (TextView) findViewById(R.id.about_tv_info);
        PackageInfo pInfo = null;

        try {
            pInfo = getPackageManager().getPackageInfo(getPackageName(), 0);
            String version = pInfo.versionName;
            tvAbout.setText("Proximo \nVersion " + version);
        } catch (PackageManager.NameNotFoundException e) {
            e.printStackTrace();
        }

    }


}
