package com.example.screensx;

import android.content.Intent;
import android.os.Bundle;
import android.view.View;
import android.widget.AdapterView;
import android.widget.GridView;

import androidx.appcompat.app.AppCompatActivity;

import java.util.ArrayList;

public class AppGroupActivity extends AppCompatActivity {

    private static final int REQUEST_READ_STORAGE = 0;

    private GridView _gridView;
    private ScreensAdapter _adapter;
    private Logger _logger;
    private ScreenFactory _sf;
    private String _appName;
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.screen_grid);
        _logger = Logger.getInstance("FILES");
        _gridView = (GridView)findViewById(R.id.grid_view);
        _sf = ScreenFactory.getInstance(getApplicationContext());
        _sf.initialize();
        _appName = getIntent().getStringExtra("APP_GROUP_NAME");
        displayScreens();
    }


    public void displayScreens() {
        ArrayList<Screenshot> screens = _sf.appgroups.get(_appName).screenshots;
        for (Screenshot s: screens)
            _logger.log(s.appName,s.name,s.file.getAbsolutePath());
        _logger.log("Displaying Scrrens of Appgroup", _appName, screens.size());
        _adapter = new ScreensAdapter(getApplicationContext(), screens);
        _gridView.setAdapter(_adapter);
        _gridView.setOnItemClickListener(new AdapterView.OnItemClickListener() {
            @Override
            public void onItemClick(AdapterView<?> adapterView, View view, int i, long l) {
                Screenshot selected = screens.get(i);
                _logger.log("The screen selected is", i, selected.name, "the appName: ", selected.appName);
                Intent intent = new Intent(getBaseContext(), ScreenActivity.class);
                intent.putExtra("SCREEN_NAME", selected.name);
                startActivity(intent);
            }
        });
    }
}
