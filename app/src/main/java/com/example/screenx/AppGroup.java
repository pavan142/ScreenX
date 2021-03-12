package com.example.screenx;

import java.util.ArrayList;

class AppGroup {
    public String appName;
    public ArrayList<Screenshot> screenshots;
    public Screenshot mascot;

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
}
