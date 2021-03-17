package com.frankenstein.screenx.helper;

import android.os.Environment;

import com.frankenstein.screenx.Logger;
import com.frankenstein.screenx.models.Screenshot;

import java.io.File;
import java.util.ArrayList;
import java.util.List;

import static com.frankenstein.screenx.Constants.SCREENSHOT_DIR;

public class FileHelper {

    private static final Logger _mLogger = Logger.getInstance("FILES-IO");
    public static final File SYSTEM_SCREENSHOT_DIR = new File(Environment.getExternalStoragePublicDirectory(Environment.DIRECTORY_DCIM), "Screenshots");
    public static final File CUSTOM_SCREENSHOT_DIR = new File(Environment.getExternalStoragePublicDirectory(Environment.DIRECTORY_PICTURES), SCREENSHOT_DIR);

    public static boolean deleteScreenshot(Screenshot screen) {
        File file = screen.file;
        if (!(file.exists() && file.isFile() && file.canWrite()))
            return false;
        return file.delete();
    }

    public static void createIfNot(File dir) {
        try {
            if (dir.exists() && dir.isDirectory()) {
                _mLogger.log(dir.getAbsolutePath(), ": directory already exists");
            } else {
                dir.mkdirs();
            }
        } catch (SecurityException e) {
            _mLogger.log("Error while Ensuring Directory", dir.getAbsolutePath());
            _mLogger.log(e.toString());
        }
    }

    public static ArrayList<File> getAllScreenshotFiles() {
        File[] systemFiles = SYSTEM_SCREENSHOT_DIR.listFiles();
        File[] customFiles = CUSTOM_SCREENSHOT_DIR.listFiles();
        ArrayList<File> result = new ArrayList<>();
        File[] emptyArray = {};

        systemFiles = (systemFiles == null)? emptyArray: systemFiles;
        customFiles = (customFiles == null)? emptyArray: customFiles;

        for (int i = 0; i < systemFiles.length; i++) {
            result.add(systemFiles[i]);
        }
        for (int i = 0; i < customFiles.length; i++) {
            result.add(customFiles[i]);
        }
        _mLogger.log("Total: ", result.size(), "System Files: ", systemFiles.length, "Custom Files: ", customFiles.length);
        return result;
    }
}
