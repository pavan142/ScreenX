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

import java.io.File;

public class ScreenActivity extends AppCompatActivity {

    private static final int REQUEST_READ_STORAGE = 0;

    private Logger _logger;
    private ImageView _image;
    private ScreenFactory _sf;
    private Screenshot _screen;
    private String _screenName;
    private GestureDetector _clickDetector;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        hideSystemUI();
        setContentView(R.layout.screen_expanded);
        _logger = Logger.getInstance("FILES");
        _image = (ImageView)findViewById(R.id.image);
        _sf = ScreenFactory.getInstance(getApplicationContext());
        _sf.initialize();
        _screenName = getIntent().getStringExtra("SCREEN_NAME");
        _screen = _sf.findScreenByName(_screenName);
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
        displayScreen();
    }

    @Override
    public boolean dispatchTouchEvent(MotionEvent ev) {
        return _clickDetector.onTouchEvent(ev);
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

    public void displayScreen() {
        File file = _screen.file;
        _logger.log("Loading View for", _screen.appName,_screen.name, _screen.file.getAbsolutePath());
        Glide.with(getApplicationContext()).load(file).into(_image);
    }
}
