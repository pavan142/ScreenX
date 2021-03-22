package com.frankenstein.screenx.ui;

import android.os.Bundle;
import android.view.GestureDetector;
import android.view.MotionEvent;
import android.view.View;

import com.frankenstein.screenx.helper.Logger;

import androidx.appcompat.app.AppCompatActivity;

public class ImmersiveActivity extends AppCompatActivity {
    private GestureDetector _tapDetector;
    private View _decorView;
    private Logger _logger;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        _decorView = getWindow().getDecorView();
        _logger = Logger.getInstance("FILES");
        _logger.log("in ImmersiveActivity onCreate");
        hideSystemUI();
    }

    protected void setupTapHandling(View targetView) {
        targetView.setOnTouchListener((view, motionEvent) -> _tapDetector.onTouchEvent(motionEvent));
        _tapDetector = new GestureDetector(this, new GestureDetector.SimpleOnGestureListener() {
            @Override
            public boolean onSingleTapUp(MotionEvent e) {
                boolean visible = (_decorView.getSystemUiVisibility() & View.SYSTEM_UI_FLAG_HIDE_NAVIGATION) == 0;
                _logger.log("Parent got onSingleTapUp Event", visible);
                if (visible) {
                    hideSystemUI();
                } else {
                    showSystemUI();
                }
                return true;
            }
        });
    }

    public void hideSystemUI() {
        _logger.log("Parent is calling hidesystemui");
        int uiOptions = View.SYSTEM_UI_FLAG_FULLSCREEN // Hiding the status bar
                | View.SYSTEM_UI_FLAG_LAYOUT_FULLSCREEN // displaying the content below the status bar once it comes up, Prevents resizing
                | View.SYSTEM_UI_FLAG_LAYOUT_STABLE // same as above ?
                | View.SYSTEM_UI_FLAG_HIDE_NAVIGATION // Hiding the navigation bar
                | View.SYSTEM_UI_FLAG_LAYOUT_HIDE_NAVIGATION // displaying the content below the navigation bar once it comes up, Prevents resizing;
                | View.SYSTEM_UI_FLAG_IMMERSIVE; // Provides full immersive experience
        _decorView.setSystemUiVisibility(uiOptions);
    }

    public void showSystemUI() {
        _logger.log("Parent is calling showsystemui");
        int uiOptions = View.SYSTEM_UI_FLAG_LAYOUT_FULLSCREEN // displaying the content below the status bar once it comes up, Prevents resizing
                | View.SYSTEM_UI_FLAG_LAYOUT_STABLE // same as above ?
                | View.SYSTEM_UI_FLAG_LAYOUT_HIDE_NAVIGATION; // displaying the content below the navigation bar once it comes up, Prevents resizing;
        _decorView.setSystemUiVisibility(uiOptions);
    }

    @Override
    protected void onResume() {
        super.onResume();
        hideSystemUI();
    }
}