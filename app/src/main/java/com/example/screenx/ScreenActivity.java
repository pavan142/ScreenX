package com.example.screenx;

import android.os.Bundle;
import android.view.GestureDetector;
import android.view.MotionEvent;
import android.view.View;
import android.view.Window;
import android.view.WindowManager;
import android.widget.FrameLayout;
import android.widget.ImageView;

import com.bumptech.glide.Glide;

import androidx.appcompat.app.AppCompatActivity;
import androidx.viewpager.widget.ViewPager;

import java.io.File;
import java.util.ArrayList;

public class ScreenActivity extends AppCompatActivity {

    private Logger _logger;
    private ViewPager _viewpager;
    private ViewPagerAdapter _adapter;
    private ScreenFactory _sf;
    private Screenshot _screen;
    private String _screenName;
    private int _screenPosition;
    private ArrayList<Screenshot> _screens;
    private GestureDetector _clickDetector;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        hideSystemUI();
        setContentView(R.layout.image_slider);
        _logger = Logger.getInstance("FILES");
        _viewpager = (ViewPager) findViewById(R.id.view_pager);
        _sf = ScreenFactory.getInstance(getApplicationContext());
        _sf.initialize();
        _screenName = getIntent().getStringExtra("SCREEN_NAME");
        _screenPosition = getIntent().getIntExtra("SCREEN_POSITION", 0);
        _screen = _sf.findScreenByName(_screenName);
        AppGroup ag =_sf.appgroups.get(_screen.appName);
        if (ag == null) {
            finish();
        }
        _screens = ag .screenshots;
        _adapter = new ViewPagerAdapter(getApplicationContext(), _screens);
        _viewpager.setAdapter(_adapter);
        _viewpager.setCurrentItem(_screenPosition);
        setTapHandling();
    }

    @Override
    public boolean dispatchTouchEvent(MotionEvent ev) {
        _clickDetector.onTouchEvent(ev);
        return super.dispatchTouchEvent(ev);
    }

    private void setTapHandling() {
        View decorView = getWindow().getDecorView();
        _clickDetector = new GestureDetector(this,
                new GestureDetector.SimpleOnGestureListener() {
                    @Override
                    public boolean onSingleTapUp(MotionEvent e) {
                        boolean visible = (decorView.getSystemUiVisibility() & View.SYSTEM_UI_FLAG_HIDE_NAVIGATION) == 0;
                        if (visible) {
                            hideSystemUI();
                        } else {
                            showSystemUI();
                        }
                        return true;
                    }
                });
    }

    private void hideSystemUI() {
        View decorView = getWindow().getDecorView();
        int uiOptions = View.SYSTEM_UI_FLAG_FULLSCREEN // Hiding the status bar
                | View.SYSTEM_UI_FLAG_LAYOUT_FULLSCREEN // displaying the content below the status bar once it comes up, Prevents resizing
                | View.SYSTEM_UI_FLAG_LAYOUT_STABLE // same as above ?
                | View.SYSTEM_UI_FLAG_HIDE_NAVIGATION // Hiding the navigation bar
                | View.SYSTEM_UI_FLAG_LAYOUT_HIDE_NAVIGATION
                | View.SYSTEM_UI_FLAG_IMMERSIVE; // displaying the content below the navigation bar once it comes up, Prevents resizing;
        decorView.setSystemUiVisibility(uiOptions);
    }

    private void showSystemUI() {
        View decorView = getWindow().getDecorView();
        int uiOptions = View.SYSTEM_UI_FLAG_LAYOUT_FULLSCREEN // displaying the content below the status bar once it comes up, Prevents resizing
                | View.SYSTEM_UI_FLAG_LAYOUT_STABLE // same as above ?
                | View.SYSTEM_UI_FLAG_LAYOUT_HIDE_NAVIGATION; // displaying the content below the navigation bar once it comes up, Prevents resizing;
        decorView.setSystemUiVisibility(uiOptions);
    }

    @Override
    protected void onResume() {
        super.onResume();
        hideSystemUI();
    }
}
