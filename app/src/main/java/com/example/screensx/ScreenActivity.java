package com.example.screensx;

import android.os.Bundle;
import android.view.View;
import android.view.Window;
import android.view.WindowManager;
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

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        View decorView = getWindow().getDecorView();
        int uiOptions = View.SYSTEM_UI_FLAG_HIDE_NAVIGATION
                | View.SYSTEM_UI_FLAG_FULLSCREEN;
        decorView.setSystemUiVisibility(uiOptions);
        setContentView(R.layout.screen_expanded);
        _logger = Logger.getInstance("FILES");
        _image = (ImageView)findViewById(R.id.image);
        _sf = ScreenFactory.getInstance(getApplicationContext());
        _sf.initialize();
        _screenName = getIntent().getStringExtra("SCREEN_NAME");
        _screen = _sf.findScreenByName(_screenName);
        displayScreen();
    }


    public void displayScreen() {
        File file = _screen.file;
        _logger.log("Loading View for", _screen.appName,_screen.name, _screen.file.getAbsolutePath());
        Glide.with(getApplicationContext()).load(file).into(_image);
    }
}
