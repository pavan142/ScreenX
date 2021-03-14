package com.example.screenx;

import android.content.res.Resources;
import android.os.Bundle;
import android.view.View;
import android.widget.ImageButton;
import android.widget.LinearLayout;

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
    private ImageButton _deleteButton;
    private ImageButton _shareButton;
    private LinearLayout _toolbar;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.image_slider);
        _logger = Logger.getInstance("FILES");
        _viewpager = findViewById(R.id.view_pager);
        _sf = ScreenFactory.getInstance();
        _screenName = getIntent().getStringExtra("SCREEN_NAME");
        _screenPosition = getIntent().getIntExtra("SCREEN_POSITION", 0);
        _screen = _sf.findScreenByName(_screenName);
        AppGroup ag =_sf.appgroups.get(_screen.appName);
        if (ag == null) {
            finish();
        }

        _toolbar = findViewById(R.id.toolbar);
        _deleteButton = findViewById(R.id.delete);
        _shareButton = findViewById(R.id.share);

        _toolbar.setVisibility(View.INVISIBLE);
        alignToolbarWithNavbar();
        _screens = ag .screenshots;
        _adapter = new ViewPagerAdapter(getApplicationContext(), _screens);
        _viewpager.setAdapter(_adapter);
        _viewpager.setCurrentItem(_screenPosition);
    }

        private int getNavbarHeight() {
        Resources resources = getApplicationContext().getResources();
        int resourceId = resources.getIdentifier("navigation_bar_height", "dimen", "android");
        if (resourceId > 0) {
            return resources.getDimensionPixelSize(resourceId);
        }
        return 0;
    }

    private void alignToolbarWithNavbar() {
//        FrameLayout.LayoutParams params = new FrameLayout.LayoutParams(
//                FrameLayout.LayoutParams.MATCH_PARENT,
//                FrameLayout.LayoutParams.WRAP_CONTENT
//        );
        int navbarheight = getNavbarHeight();
//        params.setMargins(0, 0, 0, navbarheight);
//        _toolbar.setLayoutParams(params);
        _toolbar.setBottom(navbarheight);
    }

    @Override
    public void hideSystemUI() {
        super.hideSystemUI();
        if (_toolbar == null)
            return;
        _logger.log("SETTING VISIBILITY OF TOOLBAR TO INVISIBLE");
        _toolbar.setVisibility(View.INVISIBLE);
    }

    @Override
    public void showSystemUI() {
        super.showSystemUI();
        _logger.log("CALLING SHOW SYSTEM UI");
        if (_toolbar == null)
            return;
        _logger.log("SETTING VISIBILITY OF TOOLBAR TO VISIBLE");
        _toolbar.setVisibility(View.VISIBLE);
    }
}
