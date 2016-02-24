package com.mobstac.beaconstacexample.activity;

import android.app.Activity;
import android.content.Intent;
import android.os.Bundle;
import android.os.Handler;
import android.view.Window;
import android.view.WindowManager;

import com.mobstac.beaconstacexample.R;

/**
 * Created by Hiral on 1/26/2016.
 */

public class SplashActivity extends Activity {


    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        requestWindowFeature(Window.FEATURE_NO_TITLE);
        getWindow().setFlags(WindowManager.LayoutParams.FLAG_FULLSCREEN, WindowManager.LayoutParams.FLAG_FULLSCREEN);
        setContentView(R.layout.activity_splashscreen);

        executeSplash();
    }

    private void executeSplash() {
        new Handler().postDelayed(new Runnable() {

            @Override
            public void run() {
                Intent mIntent = new Intent(SplashActivity.this, MainActivity.class);
                startActivity(mIntent);
                finish();
            }
        }, 3000);
    }

}