package com.frankenstein.screenx.models;

import com.frankenstein.screenx.interfaces.TimeSortable;

import java.io.File;
import java.util.Calendar;

import androidx.annotation.Nullable;

public class Screenshot extends TimeSortable {
    public String name;
    public File file;
    public String appName;
    public String filePath;
    public Calendar calendar;
    public long size;

    public Screenshot(String name, String filePath, String appName) {
        this.name = name;
        this.appName = appName;
        this.filePath = filePath;
        this.file = new File(filePath);
        this.lastModified = this.file.lastModified();
        this.calendar = Calendar.getInstance();
        this.calendar.setTimeInMillis(this.lastModified);
        this.size = this.file.length();
    }

    @Override
    public boolean equals(@Nullable Object obj) {
        if (!(obj instanceof Screenshot))
            return false;
        Screenshot s = (Screenshot) obj;
        return this.name.equals(s.name);
    }
}
