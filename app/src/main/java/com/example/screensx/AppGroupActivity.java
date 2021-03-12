package com.example.screensx;

import android.Manifest;
import android.content.pm.PackageManager;
import android.os.Bundle;
import android.widget.GridView;

import androidx.appcompat.app.AppCompatActivity;
import androidx.core.app.ActivityCompat;
import androidx.core.content.ContextCompat;

import java.util.ArrayList;

public class AppGroupActivity extends AppCompatActivity {

    private static final int REQUEST_READ_STORAGE = 0;

    private GridView _gridView;
    private ScreensAdapter _adapter;
    private Logger _files;
    private ScreenFactory _sf;
    private String _appName;
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.screen_grid);
        _files = Logger.getInstance("FILES");
        _gridView = (GridView)findViewById(R.id.grid_view);
        _sf = ScreenFactory.getInstance(getApplicationContext());
        _sf.initialize();
        _appName = getIntent().getStringExtra("APP_GROUP_NAME");
        displayScreens();
    }


    public void displayScreens() {
        ArrayList<Screenshot> screens = _sf.appgroups.get(_appName).screenshots;
        for (Screenshot s: screens)
            _files.log(s.appName,s.name,s.file.getAbsolutePath());
        _adapter = new ScreensAdapter(getApplicationContext(), screens);
        _gridView.setAdapter(_adapter);
    }
}
