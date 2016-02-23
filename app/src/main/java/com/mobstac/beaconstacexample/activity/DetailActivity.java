package com.mobstac.beaconstacexample.activity;

import android.content.Context;
import android.graphics.Color;
import android.graphics.drawable.ColorDrawable;
import android.os.Bundle;
import android.os.Handler;
import android.support.v4.view.PagerAdapter;
import android.support.v4.view.ViewPager;
import android.support.v7.app.AppCompatActivity;
import android.text.TextUtils;
import android.view.LayoutInflater;
import android.view.MotionEvent;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.RelativeLayout;
import android.widget.TextView;

import com.bumptech.glide.Glide;
import com.mobstac.beaconstacexample.R;

import java.util.ArrayList;
import java.util.Timer;
import java.util.TimerTask;

public class DetailActivity extends AppCompatActivity {

    ViewPager viewPager;
    ViewPagerAdapter adapter;
    Handler handler = new Handler();
    private Timer swipeTimer;
    private int currentPage = 0;
    Runnable Update ;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_detail);

        android.support.v7.app.ActionBar bar = getSupportActionBar();
        bar.setBackgroundDrawable(new ColorDrawable(Color.parseColor("#228FC8")));

        String images = getIntent().getStringExtra("images");
        final String[] imageList = images.split(",http");
        viewPager = (ViewPager) findViewById(R.id.viewpager);
        // Pass results to ViewPagerAdapter Class
        adapter = new ViewPagerAdapter(this, imageList);
        // Binds the Adapter to the ViewPager
        viewPager.setAdapter(adapter);
        Update = new Runnable() {
            public void run() {
                if (currentPage == imageList.length) {
                    currentPage = 0;
                }
                viewPager.setCurrentItem(currentPage++, true);
            }
        };
        swipeTimer = new Timer();
        swipeTimer.schedule(new TimerTask() {

            @Override
            public void run() {
                handler.post(Update);
            }
        }, 1600, 1000);

        viewPager.setOnTouchListener(new View.OnTouchListener() {
            @Override
            public boolean onTouch(View view, MotionEvent motionEvent) {
                swipeTimer.cancel();
                return false;
            }
        });
    }


    private class ViewPagerAdapter extends PagerAdapter {
        private final String[] urls;
        // Declare Variables
        Context context;
        LayoutInflater inflater;

        public ViewPagerAdapter(Context context, String[] urls) {
            this.context = context;
            this.urls = urls;
        }

        @Override
        public int getCount() {
            return urls.length;
        }

        @Override
        public boolean isViewFromObject(View view, Object object) {
            return view == ((RelativeLayout) object);
        }

        @Override
        public Object instantiateItem(ViewGroup container, int position) {
            // Declare Variables
            ImageView imgflag;
            inflater = (LayoutInflater) context.getSystemService(Context.LAYOUT_INFLATER_SERVICE);
            View itemView = inflater.inflate(R.layout.row_viewpager, container, false);
            // Locate the ImageView in viewpager_item.xml
            imgflag = (ImageView) itemView.findViewById(R.id.iv_slider);
            // Capture position and set to the ImageView
            String url = urls[position];
            if (!url.startsWith("http"))
                url = "http" + url;
            Glide.with(context).load(url).into(imgflag);

            // Add viewpager_item.xml to ViewPager
            ((ViewPager) container).addView(itemView);

            return itemView;
        }

        @Override
        public void destroyItem(ViewGroup container, int position, Object object) {
            // Remove viewpager_item.xml from ViewPager
            ((ViewPager) container).removeView((RelativeLayout) object);

        }
    }

}
