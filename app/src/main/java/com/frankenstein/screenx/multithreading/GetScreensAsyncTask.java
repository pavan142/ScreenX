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

    private Logger _mLogger;

    protected ScreensFetchedListener mlistener;

    public GetScreensAsyncTask() {
        super();
        _mLogger = Logger.getInstance("GetScreensAsyncTask");
    }

    @Override
    protected ArrayList<Screenshot> doInBackground(Object ...objects) {
        final Context context = (Context) objects[0];
        mlistener = (ScreensFetchedListener) objects[1];

        ArrayList<Screenshot> screens = new ArrayList<>();
        try {

            ArrayList<File> files = getAllScreenshotFiles();
            for (File file : files) {
                Screenshot screen = getScreenFromFile(context, file);
                screens.add(screen);
            }
            ScreenFactory.getInstance().analyzeScreens(screens);
        } catch (Exception e) {
            _mLogger.log("got an error: ", e.getMessage());
        }
        return screens;
    }

    @Override
    protected void onPostExecute(ArrayList<Screenshot> screens) {
        mlistener.onScreensFetched(screens);
    }

    public interface ScreensFetchedListener {
        void onScreensFetched(ArrayList<Screenshot> screens);
    }
}
