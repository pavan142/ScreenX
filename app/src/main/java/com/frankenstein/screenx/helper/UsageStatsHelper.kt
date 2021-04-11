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

        public class ForeGroundAppEvent(timestamp: Long, packageName: String) {
            val mTimestamp: Long = timestamp
            val mPackageName: String = packageName
        }

        fun lastForegroundApp(usm: UsageStatsManager): String {
            var calendar = Calendar.getInstance()
            val endTime = calendar.timeInMillis
            calendar.add(Calendar.HOUR, -1)
            val startTime = calendar.timeInMillis
            var packageName = "";
            val appList: ArrayList<ForeGroundAppEvent> = filterFgEvents(usm, startTime, endTime);
            appList.sortByDescending { i -> i.mTimestamp }
            if (appList.size > 0)
                packageName = appList.get(0).mPackageName
            return packageName
        }

        @JvmStatic
        fun getArchivedFgEventTimeline(usm: UsageStatsManager): ArrayList<ForeGroundAppEvent> {
            var calendar = Calendar.getInstance()
            val endTime = calendar.timeInMillis
            calendar.add(Calendar.MONTH, -1)
            val startTime = calendar.timeInMillis
            return filterFgEvents(usm, startTime, endTime);
        }

        @JvmStatic
        fun getLiveFgEventTimeline(usm: UsageStatsManager): ArrayList<ForeGroundAppEvent> {
            var calendar = Calendar.getInstance()
            val endTime = calendar.timeInMillis
            calendar.add(Calendar.HOUR, -1)
            val startTime = calendar.timeInMillis
            return filterFgEvents(usm, startTime, endTime);
        }

        fun filterFgEvents(usm: UsageStatsManager, startTime: Long, endTime: Long): ArrayList<ForeGroundAppEvent> {
            val usageEvents = usm.queryEvents(startTime, endTime);
            var ev: UsageEvents.Event = UsageEvents.Event();
            val appList: ArrayList<ForeGroundAppEvent> = ArrayList()
            while (usageEvents.getNextEvent(ev)) {
                if (ev.eventType == UsageEvents.Event.MOVE_TO_FOREGROUND) {
                    appList.add(ForeGroundAppEvent(ev.timeStamp, ev.packageName))
                }
            }
            return appList;
        }
    }
}
