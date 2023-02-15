package com.proxglobal.purchase.util

import java.text.DateFormat
import java.text.SimpleDateFormat

/**
 * Parse [timeInString] to a date and return milliseconds that represent this day.
 * Note that [timeInString] must have format "yyyy-MM-dd'T'HH:mm:ssZ"
 * Example 2022-10-11T10:00:00-0700 -> 10h, 11/10/2022, GMT -07:00
 * @param timeInString a string have format "yyyy-MM-dd'T'HH:mm:ssZ", represent the day in
 * ISO 8601 format
 */
fun parseTime(timeInString: String): Long {
    val df1: DateFormat = SimpleDateFormat("yyyy-MM-dd'T'HH:mm:ssZ")
    val result1 = df1.parse(timeInString)
    return result1.time
}