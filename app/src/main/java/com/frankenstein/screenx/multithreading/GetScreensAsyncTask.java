package com.frankenstein.screenx.multithreading;

import android.os.AsyncTask;
import android.content.Context;

import com.frankenstein.screenx.helper.Logger;
import com.frankenstein.screenx.ScreenFactory;
import com.frankenstein.screenx.models.Screenshot;

import java.io.File;
import java.util.ArrayList;

import static com.frankenstein.screenx.helper.AppHelper.getScreenFromFile;
import static com.frankenstein.screenx.helper.FileHelper.getAllScreenshotFiles;

public class GetScreensAsyncTask extends AsyncTask<Object, Void, ArrayList<Screenshot>> {

    private Logger _mLogger = Logger.getInstance("GetScreensAsyncTask");;
    private static final Logger _mTimeLogger = Logger.getInstance("TIME");

    public GetScreensAsyncTask() {
        super();
    }

    @Override
    protected ArrayList<Screenshot> doInBackground(Object ...objects) {
        Long start = System.currentTimeMillis();
        _mLogger.log("doInBackground", Thread.currentThread().toString());
        final Context context = (Context) objects[0];
        ArrayList<Screenshot> screens = new ArrayList<>();
        try {

            ArrayList<File> files = getAllScreenshotFiles();
            for (File file : files) {
                Screenshot screen = getScreenFromFile(context, file);
                screens.add(screen);
            }
            Long end = System.currentTimeMillis();
            _mTimeLogger.log("Time taken for processing screenshot files in background =", (end-start));
            ScreenFactory.getInstance().analyzeScreens(screens);
        } catch (Exception e) {
            _mLogger.log("got an error: ", e.getMessage());
        }
        return screens;
    }
}
