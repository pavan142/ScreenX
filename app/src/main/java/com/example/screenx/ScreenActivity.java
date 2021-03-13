package com.example.screenx;

import android.os.Bundle;
import android.view.GestureDetector;
import android.view.MotionEvent;
import android.view.View;

import androidx.appcompat.app.AppCompatActivity;
import androidx.viewpager.widget.ViewPager;

import java.util.ArrayList;

public class ScreenActivity extends ImmersiveActivity {

    private Logger _logger;
    private ViewPager _viewpager;
    private ViewPagerAdapter _adapter;
    private ScreenFactory _sf;
    private Screenshot _screen;
    private String _screenName;
    private int _screenPosition;
    private ArrayList<Screenshot> _screens;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
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
    }
}
