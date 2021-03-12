package com.example.screenx;

import java.io.File;

class Screenshot {
    public String name;
    public File file;
    public String appName;

    public Screenshot(String name, String filePath, String appName) {
        this.name = name;
        this.file = new File(filePath);
        this.appName = appName;
    }
}
