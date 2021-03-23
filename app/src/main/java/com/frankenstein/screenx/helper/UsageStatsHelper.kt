package com.frankenstein.screenx.helper

import android.app.usage.UsageEvents
import android.app.usage.UsageStatsManager
import java.util.*
import kotlin.collections.ArrayList

class UsageStatsHelper {
    companion object {
        private val _logger = Logger.getInstance("UsageStatsHelper");

        fun getRecentAppViaUsage(usm: UsageStatsManager): String {
            var calendar = Calendar.getInstance()
            val endTime = calendar.timeInMillis
            calendar.add(Calendar.HOUR, -1)
            val startTime = calendar.timeInMillis

            var packageName = "";
            val appList = usm.queryUsageStats(UsageStatsManager.INTERVAL_DAILY, startTime, endTime);
            if (appList!= null) {
                appList.sortByDescending {it.lastTimeUsed }
                if (appList.size > 0) {
                    val item = appList[0];
                    packageName = item.packageName;
                }
            }
            return packageName;
        }

        private class ForeGroundAppEvent(timestamp: Long, packageName: String) {
            val mTimestamp: Long = timestamp
            val mPackageName: String = packageName
        }

        fun getRecentAppViaEvents(usm: UsageStatsManager): String {
            var calendar = Calendar.getInstance()
            val endTime = calendar.timeInMillis
            calendar.add(Calendar.HOUR, -1)
            val startTime = calendar.timeInMillis

            var packageName = "";
            val usageEvents = usm.queryEvents(startTime, endTime);
            var ev: UsageEvents.Event = UsageEvents.Event();
            val appList: ArrayList<ForeGroundAppEvent> = ArrayList()
            while (usageEvents.getNextEvent(ev)) {
                if (ev.eventType == UsageEvents.Event.MOVE_TO_FOREGROUND) {
                    packageName = ev.packageName;
                    appList.add(ForeGroundAppEvent(ev.timeStamp, ev.packageName))
                }
            }
            appList.sortByDescending { i -> i.mTimestamp }
            if (appList.size > 0)
                packageName = appList.get(0).mPackageName
            return packageName
        }
    }
}
