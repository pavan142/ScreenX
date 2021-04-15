package com.frankenstein.screenx.helper

import android.app.usage.UsageEvents
import android.app.usage.UsageStatsManager
import com.frankenstein.screenx.ScreenFactory
import com.frankenstein.screenx.ScreenXApplication
import com.frankenstein.screenx.interfaces.TimeSortable
import com.frankenstein.screenx.models.Screenshot
import java.util.*
import kotlin.collections.ArrayList

public class SortHelper {
    companion object {

        @JvmStatic
        fun<T: TimeSortable> DESC_TIME(input: ArrayList<T>) {
            input.sortByDescending { it -> it.lastModified }
        }

        @JvmStatic
        fun<T: TimeSortable> ASC_TIME(input: ArrayList<T>) {
            input.sortBy{it -> it.lastModified}
        }

        @JvmStatic
        fun DESC_SCREENS_BY_TIME(input: MutableList<String>) {
            input.sortByDescending{it -> ScreenXApplication.screenFactory.findScreenByName(it).lastModified}
        }

        @JvmStatic
        fun ASC_SCREENS_BY_TIME(input: ArrayList<String>) {
            input.sortBy{it -> ScreenXApplication.screenFactory.findScreenByName(it).lastModified}
        }

    }
}
