package com.example.screenx;

import java.io.File;

class FileHelper {
    public static boolean deleteScreenshot(Screenshot screen) {
        File file = screen.file;
        if (!(file.exists() && file.isFile() && file.canWrite()))
            return false;
        return file.delete();
    }
}
