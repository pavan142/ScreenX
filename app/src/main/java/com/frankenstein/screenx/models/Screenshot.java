package com.frankenstein.screenx.models;

import com.frankenstein.screenx.interfaces.TimeSortable;

import java.io.File;

public class Screenshot extends TimeSortable {
    public String name;
    public File file;
    public String appName;
    public String filePath;

    public Screenshot(String name, String filePath, String appName) {
        this.name = name;
        this.appName = appName;
        this.filePath = filePath;
        this.file = new File(filePath);
        this.lastModified = this.file.lastModified();
    }
}
