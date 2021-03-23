package com.frankenstein.screenx.ui;

import android.os.Bundle;
import android.view.GestureDetector;
import android.view.MotionEvent;
import android.view.View;

import com.frankenstein.screenx.helper.Logger;
import com.frankenstein.screenx.interfaces.ScreenTapListener;

import androidx.appcompat.app.AppCompatActivity;

public class ImmersiveActivity extends AppCompatActivity implements ScreenTapListener {
    private View _decorView;
    private Logger _logger = Logger.getInstance("ImmersiveActivity");;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        _decorView = getWindow().getDecorView();
        hideSystemUI();
    }

    public void onTap() {
        toggleSystemUI();
    }

    private void toggleSystemUI() {
        boolean visible = (_decorView.getSystemUiVisibility() & View.SYSTEM_UI_FLAG_HIDE_NAVIGATION) == 0;
        if (visible) {
            hideSystemUI();
        } else {
            showSystemUI();
        }
    }

    public void hideSystemUI() {
        _logger.log("Hiding status bar and nav bars");
        int uiOptions = View.SYSTEM_UI_FLAG_FULLSCREEN // Hiding the status bar
                | View.SYSTEM_UI_FLAG_LAYOUT_FULLSCREEN // displaying the content below the status bar once it comes up, Prevents resizing
                | View.SYSTEM_UI_FLAG_LAYOUT_STABLE // same as above ?
                | View.SYSTEM_UI_FLAG_HIDE_NAVIGATION // Hiding the navigation bar
                | View.SYSTEM_UI_FLAG_LAYOUT_HIDE_NAVIGATION // displaying the content below the navigation bar once it comes up, Prevents resizing;
                | View.SYSTEM_UI_FLAG_IMMERSIVE; // Provides full immersive experience
        _decorView.setSystemUiVisibility(uiOptions);
    }

    public void showSystemUI() {
        _logger.log("Showing status bar and nav bars");
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
