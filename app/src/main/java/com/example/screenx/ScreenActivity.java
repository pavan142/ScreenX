package com.example.screenx;

import android.content.res.Resources;
import android.os.Bundle;
import android.view.View;
import android.widget.ImageButton;
import android.widget.LinearLayout;
import android.widget.RelativeLayout;

import androidx.viewpager.widget.ViewPager;

import java.util.ArrayList;

import static com.example.screenx.Constants.TOOLBAR_TRANSITION;

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

    public Resources resources;
    public Utils utils;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.image_slider);

        resources = getApplicationContext().getResources();

        utils = Utils.getInstance();
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

        _toolbar.setAlpha(0);
        alignToolbarWithNavbar();
        _screens = ag .screenshots;
        _adapter = new ViewPagerAdapter(getApplicationContext(), _screens);
        _viewpager.setAdapter(_adapter);
        _viewpager.setCurrentItem(_screenPosition);

        this.setupTapHandling(_viewpager);
    }

    private void alignToolbarWithNavbar() {
        int height = (int)getResources().getDimension(R.dimen.bottom_toolbar_height);
        RelativeLayout.LayoutParams params = new RelativeLayout.LayoutParams(
                RelativeLayout.LayoutParams.MATCH_PARENT,
                height
        );
        int navbarHeight = utils.getNavbarHeight();
        _logger.log("Method2: Navigation Bar Height is", navbarHeight, "and the height is", height);
        params.bottomMargin= navbarHeight;
        params.addRule(RelativeLayout.ALIGN_PARENT_BOTTOM);
        _toolbar.setLayoutParams(params);
    }

    @Override
    public void hideSystemUI() {
        super.hideSystemUI();
        if (_toolbar == null)
            return;
        _logger.log("SETTING VISIBILITY OF TOOLBAR TO INVISIBLE");
        _toolbar.animate().alpha(0).setDuration(TOOLBAR_TRANSITION);
    }

    @Override
    public void showSystemUI() {
        super.showSystemUI();
        _logger.log("CALLING SHOW SYSTEM UI");
        if (_toolbar == null)
            return;
        _logger.log("SETTING VISIBILITY OF TOOLBAR TO VISIBLE");
        alignToolbarWithNavbar();
        _toolbar.animate().alpha(1).setDuration(TOOLBAR_TRANSITION);
    }
}
