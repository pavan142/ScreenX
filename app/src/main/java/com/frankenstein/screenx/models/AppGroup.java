package com.frankenstein.screenx.models;

import com.frankenstein.screenx.interfaces.TimeSortable;

import java.util.ArrayList;
import java.util.Collections;

import static com.frankenstein.screenx.helper.SortHelper.DESC_TIME;

public class AppGroup extends TimeSortable {
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

    public void sort() {
        DESC_TIME(screenshots);
    }
}
