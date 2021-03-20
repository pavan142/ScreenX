package com.frankenstein.screenx.helper;

import android.content.Context;
import android.content.SharedPreferences;

import static android.content.Context.MODE_PRIVATE;

public class SessionManager {

    private static SessionManager _mInstance;

    private SharedPreferences sharedPreferences;
    private SharedPreferences.Editor editor;
    private final String MY_PREF = "my_preferences";

    public static SessionManager getInstance(Context context) {
        if (SessionManager._mInstance == null) {
            SessionManager._mInstance = new SessionManager(context);
        }
        return SessionManager._mInstance;
    }

    private SessionManager(Context context) {
        sharedPreferences = context.getSharedPreferences(MY_PREF, MODE_PRIVATE);
    }

    public void firstTimeAsking(String permission, boolean isFirstTime) {
        doEdit();
        editor.putBoolean(permission, isFirstTime);
        doCommit();
    }

    public boolean isFirstTimeAsking(String permission) {
        return sharedPreferences.getBoolean(permission, true);
    }

    private void doEdit() {
        if (editor == null) {
            editor = sharedPreferences.edit();
        }
    }

    private void doCommit() {
        if (editor != null) {
            editor.commit();
            editor = null;
        }
    }
}
