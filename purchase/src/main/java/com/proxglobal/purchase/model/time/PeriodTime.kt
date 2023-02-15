package com.proxglobal.purchase.model.time

import com.google.gson.annotations.SerializedName
import com.proxglobal.purchase.util.parseTime

class StatusByTime {
    @SerializedName("start_time")
    var startTime: String = ""

    @SerializedName("end_time")
    var endTime: String = ""

    @SerializedName("enable_in_period")
    var statusInTime: Boolean = true

    private val isExpired: Boolean
        get() {
            var startTimeInMilli: Long = 0
            var endTimeInMilli: Long = 0
            if (startTime.isNotBlank()) {
                startTimeInMilli = parseTime(startTime)
            }
            if (endTime.isNotBlank()) {
                endTimeInMilli = parseTime(endTime)
            }
            //If start_time and end_time is not set, return false
            if (startTimeInMilli == 0L && endTimeInMilli == 0L) return true

            //If end_time is not set, return true if current is future of start_time
            if (endTimeInMilli == 0L) {
                return System.currentTimeMillis() >= startTimeInMilli
            }

            //If start_time is not set, return true if current is past of end_time
            if (startTimeInMilli == 0L) {
                return System.currentTimeMillis() <= endTimeInMilli
            }

            //If start_time and end_time is set, and end_time is past of start_time, throw exception
            if (endTimeInMilli < startTimeInMilli) throw java.lang.IllegalStateException("Period is not valid. Start time is greater than end time")

            //Return if current is in range of start_time and end_time
            return System.currentTimeMillis() in startTimeInMilli until endTimeInMilli
        }

    val currentStatus: Boolean
        get() {
            return if (isExpired) statusInTime else !statusInTime
        }
}