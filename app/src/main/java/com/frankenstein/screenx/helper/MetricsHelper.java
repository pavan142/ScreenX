package com.frankenstein.screenx.helper;

import android.util.DisplayMetrics;

import static java.lang.Math.ceil;
import static java.lang.Math.floor;

public class MetricsHelper {

    public static int dpToPx(DisplayMetrics displayMetrics, float dp) {
        return round((dp*displayMetrics.density));
    }

    private static int round(float input) {
        double output;
        if (input < 0) {
            output = ceil(input - 0.5f);
        } else {
            output = floor(input + 0.5f);
        }
        return (int) output;
    }
}
