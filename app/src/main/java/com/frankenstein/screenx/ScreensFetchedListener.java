package com.frankenstein.screenx;

import com.frankenstein.screenx.models.Screenshot;

import java.util.ArrayList;

public interface ScreensFetchedListener {
    void onScreensFetched(ArrayList<Screenshot> screens);
}
