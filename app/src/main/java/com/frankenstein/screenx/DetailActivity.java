package com.frankenstein.screenx;

import android.os.Bundle;

import com.frankenstein.screenx.helper.Logger;
import com.frankenstein.screenx.models.Screenshot;
import com.frankenstein.screenx.ui.DetailComponentView;

import java.text.DateFormat;
import java.text.DecimalFormat;
import java.text.SimpleDateFormat;
import java.util.Calendar;
import java.util.Locale;

import androidx.appcompat.app.AppCompatActivity;
import androidx.appcompat.widget.Toolbar;

public class DetailActivity extends AppCompatActivity {

    private Logger _logger = Logger.getInstance("DetailActivity");
    public static final String INTENT_SCREEN_NAME="INTENT_SCREEN_NAME";

    private DetailComponentView _mDate;
    private DetailComponentView _mApp;
    private DetailComponentView _mName;
    private DetailComponentView _mPath;
    private DetailComponentView _mSize;
    private Toolbar _mToolbar;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        setContentView(R.layout.detailpage);
        super.onCreate(savedInstanceState);

        _mDate = findViewById(R.id.date);
        _mApp = findViewById(R.id.app);
        _mName = findViewById(R.id.name);
        _mPath = findViewById(R.id.path);
        _mSize = findViewById(R.id.size);
        _mToolbar = findViewById(R.id.toolbar);
        _mToolbar.setNavigationOnClickListener((view) -> {
            finish();
        });
        String screenName = getIntent().getStringExtra(INTENT_SCREEN_NAME);
        Screenshot screen = ScreenXApplication.screenFactory.findScreenByName(screenName);
        if (screen == null)
            return;
        _mPath.setData(screen.filePath);
        _mApp.setData(screen.appName);
        _mName.setData(screen.name);
        _mDate.setData(readableDate(screen.calendar));
        _mSize.setData(readableFileSize(screen.size));
    }

    public String readableDate(Calendar calendar) {
        String output = "";
        output+= calendar.get(Calendar.DAY_OF_MONTH) + " ";
        output += calendar.getDisplayName(Calendar.MONTH, Calendar.LONG, Locale.getDefault()) + " ";
        output += calendar.get(Calendar.YEAR) + "\n";
        output += calendar.getDisplayName(Calendar.DAY_OF_WEEK, Calendar.LONG, Locale.getDefault()) + "  ";
        output += calendar.get(Calendar.HOUR_OF_DAY)+":";
        output += calendar.get(Calendar.MINUTE);
        return output;
    }

    public String readableFileSize(long size) {
        if(size <= 0) return "0";
        final String[] units = new String[] { "B", "KB", "MB", "GB", "TB" };
        int digitGroups = (int) (Math.log10(size)/Math.log10(1024));
        _logger.log("readableFileSize", size, digitGroups);
        return new DecimalFormat("#,##0.#").format(size/Math.pow(1024, digitGroups)) + " " + units[digitGroups];
    }
}
