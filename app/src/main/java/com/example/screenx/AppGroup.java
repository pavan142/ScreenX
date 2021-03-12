package com.example.screenx;

import java.util.ArrayList;
import java.util.Collections;
import java.util.Comparator;

class AppGroup {
    public String appName;
    public ArrayList<Screenshot> screenshots;
    public Screenshot mascot;
    public long lastModified;

    public AppGroup(String _appName) {
        this.appName = _appName;
        this.screenshots = new ArrayList<>();
    }

    public String print() {
        String output = "----------" + this.appName + "----------" + "\n";
        for (Screenshot s: this.screenshots) {
            output += s.name + "\n";
        }
        return output;
    }

    public void sort() {
        Collections.sort(screenshots, new Comparator<Screenshot>() {
            @Override
            public int compare(Screenshot screenshot, Screenshot t1) {
                long result = (t1.file.lastModified() - screenshot.file.lastModified());
                int output = (result >=0 ) ? 1: -1;
                return output;
            }
        });
    }
}
