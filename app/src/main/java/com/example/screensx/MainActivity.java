package com.example.screensx;

import android.Manifest;
import android.content.pm.ApplicationInfo;
import android.content.pm.PackageManager;
import android.os.Bundle;
import android.os.Environment;
import android.widget.GridView;

import androidx.appcompat.app.AppCompatActivity;
import androidx.core.app.ActivityCompat;
import androidx.core.content.ContextCompat;

import java.io.File;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.regex.Matcher;
import java.util.regex.Pattern;
import java.util.Map;

public class MainActivity extends AppCompatActivity {

    private static final int REQUEST_READ_STORAGE = 0;
    private ArrayList<Screenshot> _screenshots;
    private Map<String, AppGroup> _appgroups;
    private GridView _gridView;
    private ImageAdapter _adapter;
    private Logger _files;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);
        _screenshots = new ArrayList<>();
        _appgroups = new HashMap<>();
        _files = Logger.getInstance("FILES");
        _gridView = (GridView)findViewById(R.id.grid_view);
        requestStoragePermission();
        analyzeFiles();
        displayAppGroups();
    }

    private String getAppName(String packageId) {
        final PackageManager pm = getApplicationContext().getPackageManager();
        ApplicationInfo ai;
        try {
            ai = pm.getApplicationInfo( packageId, 0);
        } catch (final PackageManager.NameNotFoundException e) {
            ai = null;
        }
        final String applicationName = (String) (ai != null ? pm.getApplicationLabel(ai) : "");
        return applicationName;
    }

    private String getSourceApp(String filename) {
        Pattern pattern = Pattern.compile("_[[a-z]*.]*.jpg$");
        Matcher matcher = pattern.matcher(filename);

        if (matcher.find()) {
            String matched = matcher.group();
            String packageId = matched.substring(1, matched.length() - 4);
            String appName = getAppName(packageId);
            appName = (appName == "") ? "Miscellaneous" : appName;
            return appName;
        }
        return "Miscellaneous";
    }

    public void displayAppGroups() {
        ArrayList<Screenshot> screens = new ArrayList<>();
        for (AppGroup ag: _appgroups.values())
            screens.add(ag.mascot);
//        for (Screenshot s: screens)
//            _files.log(s.appName,s.name,s.file.getAbsolutePath());
        _adapter = new ImageAdapter(getApplicationContext(), screens);
        _gridView.setAdapter(_adapter);
    }

    public void analyzeFiles() {
        try {
            String path = Environment.getExternalStorageDirectory().toString() + "/DCIM/Screenshots";
            _files.log("Path: ", path);
            File directory = new File(path);

            if (directory.exists())
                _files.log("the directory exists: ", directory.getAbsolutePath());
            else
                _files.log("the directory does not exist: " + directory.getAbsolutePath());

            _files.log("Size: ", directory.canRead(), directory.canWrite(), directory.canExecute());
            File[] files = directory.listFiles();
            _files.log("Size: "+ files.length);

            for (int i = 0; i < files.length; i++) {
                String fileName = files[i].getName();
                String appName = getSourceApp(fileName);
                AppGroup ag;
                if (!_appgroups.containsKey(appName)) {
                    ag = new AppGroup(appName);
                    _appgroups.put(appName, ag);
                }
                ag = _appgroups.get(appName);
                String filePath = path+"/"+fileName;
                Screenshot screen = new Screenshot(fileName, filePath, appName);
                ag.screenshots.add(screen);
            }

            for (AppGroup ag : _appgroups.values()) {
                ag.mascot = ag.screenshots.get(ag.screenshots.size()-1);
//                _files.log(ag.print());
            }
        } catch (Exception e) {
            _files.log("got an error: ", e.getMessage());
        }
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
