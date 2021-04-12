package com.frankenstein.screenx.models;

import com.frankenstein.screenx.interfaces.TimeSortable;
import com.frankenstein.screenx.overlay.Screen;

import java.io.File;

import androidx.annotation.Nullable;

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

    @Override
    public boolean equals(@Nullable Object obj) {
        if (!(obj instanceof Screenshot))
            return false;
        Screenshot s = (Screenshot) obj;
        return this.name.equals(s.name);
    }
}
