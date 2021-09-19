package com.frankenstein.screenx.models;


import android.graphics.drawable.Drawable;

import androidx.lifecycle.MutableLiveData;

public class PermissionDetail {
    public String name;
    public String title;
    public Drawable drawable;
    public String description;
    public MutableLiveData<Boolean> hasPermission = new MutableLiveData<>(false);
    public PermissionRequestListener listener;

    public PermissionDetail(String name, String title, Drawable drawable, String description, PermissionRequestListener listener) {
        this.name = name;
        this.title = title;
        this.drawable = drawable;
        this.description = description;
        this.listener = listener;
    }

    public interface PermissionRequestListener {
        void onPermissionRequested(String permissionName);
    }
}
