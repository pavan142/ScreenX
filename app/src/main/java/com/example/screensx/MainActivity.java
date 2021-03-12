package com.example.screensx;

import android.Manifest;
import android.content.pm.PackageManager;
import android.os.Bundle;
import android.widget.GridView;

import androidx.appcompat.app.AppCompatActivity;
import androidx.core.app.ActivityCompat;
import androidx.core.content.ContextCompat;

import java.util.ArrayList;

public class MainActivity extends AppCompatActivity {

    private static final int REQUEST_READ_STORAGE = 0;

    private GridView _gridView;
    private ImageAdapter _adapter;
    private Logger _files;
    private ScreenFactory _sf;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);
        _files = Logger.getInstance("FILES");
        _gridView = (GridView)findViewById(R.id.grid_view);
        requestStoragePermission();
        _sf = ScreenFactory.getInstance(getApplicationContext());
        _sf.initialize();
        displayAppGroups();
    }


    public void displayAppGroups() {
        ArrayList<Screenshot> screens = new ArrayList<>();
        for (AppGroup ag: _sf.appgroups.values())
            screens.add(ag.mascot);
//        for (Screenshot s: screens)
//            _files.log(s.appName,s.name,s.file.getAbsolutePath());
        _adapter = new ImageAdapter(getApplicationContext(), screens);
        _gridView.setAdapter(_adapter);
    }

    private void requestStoragePermission() {
        // Getting Permission for reading exteernal storage
        if (ContextCompat.checkSelfPermission(this, Manifest.permission.READ_EXTERNAL_STORAGE)
                != PackageManager.PERMISSION_GRANTED) {
            ActivityCompat.requestPermissions(
                    this, new String[] {Manifest.permission.READ_EXTERNAL_STORAGE}, REQUEST_READ_STORAGE);
        }
    }
}
