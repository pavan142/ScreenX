package com.frankenstein.screenx.helper;

import com.frankenstein.screenx.models.Screenshot;
import com.frankenstein.screenx.overlay.Screen;

import java.util.ArrayList;

public class ArrayHelper {
    public static boolean Same(ArrayList<? extends Object> arr1, ArrayList<? extends Object> arr2) {

        boolean sameData = arr1.size() == arr2.size();
        if (sameData) {
            for (int i = 0; i < arr1.size(); i++) {
                if (!arr1.get(i).equals(arr2.get(i))) {
                    sameData = false;
                    break;
                }
            }
        }

        return sameData;
    }
}
