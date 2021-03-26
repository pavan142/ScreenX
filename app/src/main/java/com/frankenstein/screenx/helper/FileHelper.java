package com.frankenstein.screenx.helper;

import android.os.Environment;

import com.frankenstein.screenx.models.Screenshot;

import java.io.File;
import java.util.ArrayList;

import static com.frankenstein.screenx.Constants.SCREENSHOT_DIR;

public class FileHelper {

    private static final Logger _mLogger = Logger.getInstance("FileHelper");
    private static final Logger _mTimeLogger = Logger.getInstance("TIME");

    //(TODO) Store it in preferences the system folder for screenshots, instead of looking everytime at both places in DCIM/Screenshots and Pictures/Screenshots
    public static final File SYSTEM_SCREENSHOT_DIR1 = new File(Environment.getExternalStoragePublicDirectory(Environment.DIRECTORY_DCIM), "Screenshots");
    public static final File SYSTEM_SCREENSHOT_DIR2 = new File(Environment.getExternalStoragePublicDirectory(Environment.DIRECTORY_PICTURES), "Screenshots");
    public static final File CUSTOM_SCREENSHOT_DIR = new File(Environment.getExternalStoragePublicDirectory(Environment.DIRECTORY_PICTURES), SCREENSHOT_DIR);

    public static boolean deleteScreenshot(Screenshot screen) {
        File file = screen.file;
        if (!(file.exists() && file.isFile() && file.canWrite()))
            return false;
        return file.delete();
    }

    public static ArrayList<Boolean> deleteScreenshotList(ArrayList<Screenshot> deleteList) {
        ArrayList<Boolean> results = new ArrayList<>();
        for (Screenshot screen: deleteList) {
            File file = screen.file;
            if (!(file.exists() && file.isFile() && file.canWrite())) {
                results.add(false);
            }
            results.add(file.delete());
        }
        return results;
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
        _mLogger.log("Permissions for File Read", SYSTEM_SCREENSHOT_DIR1.canRead(), SYSTEM_SCREENSHOT_DIR2.canRead(), CUSTOM_SCREENSHOT_DIR.canRead());

        Long start = System.currentTimeMillis();
        File[] systemFiles1 = SYSTEM_SCREENSHOT_DIR1.listFiles();
        File[] systemFiles2 = SYSTEM_SCREENSHOT_DIR2.listFiles();
        File[] customFiles = CUSTOM_SCREENSHOT_DIR.listFiles();
        ArrayList<File> result = new ArrayList<>();
        File[] emptyArray = {};

        systemFiles1 = (systemFiles1 == null)? emptyArray: systemFiles1;
        systemFiles2 = (systemFiles2 == null)? emptyArray: systemFiles2;
        customFiles = (customFiles == null)? emptyArray: customFiles;

        for (int i = 0; i < systemFiles1.length; i++) {
            result.add(systemFiles1[i]);
        }

        for (int i = 0; i < systemFiles2.length; i++) {
            result.add(systemFiles2[i]);
        }

        for (int i = 0; i < customFiles.length; i++) {
            result.add(customFiles[i]);
        }
        _mLogger.log("Total: ", result.size(), "System Files: DCIM/Screenshots ", systemFiles1.length, "Pictures/Screenshots", systemFiles2.length ,"Custom Files: ", customFiles.length);
        Long delta = System.currentTimeMillis() - start;
        _mTimeLogger.log("Time taken for reading all screenshot files is", delta);
        return result;
    }
}
