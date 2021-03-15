package com.frankenstein.screenx;

import java.io.File;

class Screenshot {
    public String name;
    public File file;
    public String appName;
    public String filePath;
    public long lastModified;

    public Screenshot(String name, String filePath, String appName) {
        this.name = name;
        this.appName = appName;
        this.filePath = filePath;
        this.file = new File(filePath);
        this.lastModified = this.file.lastModified();
    }
}
