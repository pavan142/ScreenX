package com.frankenstein.screenx.helper

import java.util.*

class TimeHelper {
    companion object {
        @JvmStatic
        fun getReadableTime(time: Long): String {
            val date = Date(time);
            return date.toString();
        }
    }
}
