package com.frankenstein.screenx.multithreading;

import android.os.AsyncTask;
import android.content.Context;

import com.frankenstein.screenx.ScreenXApplication;
import com.frankenstein.screenx.Utils;
import com.frankenstein.screenx.database.ScreenShotEntity;
import com.frankenstein.screenx.helper.Logger;
import com.frankenstein.screenx.helper.TimeLogger;
import com.frankenstein.screenx.models.Screenshot;
import com.google.firebase.perf.metrics.AddTrace;

import java.io.File;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import static com.frankenstein.screenx.helper.AppHelper.LabelMultipleScreens;
import static com.frankenstein.screenx.helper.FileHelper.getAllScreenshotFiles;

public class GetScreensAsyncTask extends AsyncTask<Object, Void, ArrayList<Screenshot>> {

    private Logger _mLogger = Logger.getInstance("GetScreensAsyncTask");;

    public GetScreensAsyncTask() {
        super();
    }

    @Override
    protected ArrayList<Screenshot> doInBackground(Object ...objects) {
        _mLogger.log("doInBackground");
        final Context context = (Context) objects[0];
        ArrayList<Screenshot> screens = new ArrayList<>();
        try {
            ArrayList<File> files = getAllScreenshotFiles();

            List<ScreenShotEntity> existingEntities = ScreenXApplication.textHelper.getAllScreenshotsInDatabase();
            Map<String, ScreenShotEntity> existingEntitiesMap = new HashMap<>();
            for (ScreenShotEntity entity: existingEntities)
                existingEntitiesMap.put(entity.filename, entity);

            ArrayList<File> filesToBeLabeled = new ArrayList<>();

            for (File file: files) {
                ScreenShotEntity entity = existingEntitiesMap.get(file.getName());
                if (entity == null || entity.appname == null)
                    filesToBeLabeled.add(file);
                else {
                    Screenshot newSreen = new Screenshot(entity.filename, file.getPath(), entity.appname);
                    screens.add(newSreen);
                }
            }

            ArrayList<Screenshot> newlyLabelledscreens = LabelMultipleScreens(screens, context, filesToBeLabeled);
            Long end = System.currentTimeMillis();
            _mLogger.log("Total Screens", screens.size(), "Existing Screens",
                    screens.size() - newlyLabelledscreens.size(),
                    "Newly Labelled Screens", newlyLabelledscreens.size());
            ScreenXApplication.screenFactory.analyzeScreens(screens);
            ScreenXApplication.textHelper.updateAppNames(newlyLabelledscreens);
        } catch (Exception e) {
            _mLogger.log("got an error: ", e.getMessage());
        }
        return screens;
    }
}
