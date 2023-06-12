package com.sang.metamap.utils

import kotlin.math.round
import kotlin.math.roundToInt


object TimeUtil {
    /**
     * Convert estimated time received from MapBox api to normal words
     */
    fun Double.toEstimatedTime(): String {
        if (this < 60) return "${this.roundToInt()} sec"
        val minute = round(this / 60).toInt()
        if (minute < 60) {
            return "$minute min"
        } else {
            val hour = minute / 60
            val remainMinute = minute - 60 * hour
            return "$hour hr $remainMinute min"
        }
    }

    /**
     * Convert estimated distance received from MapBox api to normal words
     */
    fun Double.toEstimatedDistance(): String {
        if (this < 1000) return "${this.roundToInt()} m"
        val km = round(this / 1000 * 10) / 10 // Round a double to 1 decimal place
        return "$km km"
    }
}