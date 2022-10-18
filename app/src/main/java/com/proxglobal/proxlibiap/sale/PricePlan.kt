package com.proxglobal.proxlibiap.sale

import com.google.gson.annotations.Expose
import com.google.gson.annotations.SerializedName
import java.text.DateFormat
import java.text.SimpleDateFormat

class PricePlan(
    @SerializedName("name")
    @Expose
    var name: String,

    @SerializedName("start_time")
    @Expose
    var startTime: String? = null,

    @SerializedName("end_time")
    @Expose
    var endTime: String? = null,

    @SerializedName("subscriptions")
    @Expose
    var saleSubscriptions: List<SaleSubscription>,

    @SerializedName("one_time_product")
    @Expose
    var saleOneTimeProducts: List<SaleOneTimeProduct>
) {
    companion object {
        fun parseTime(timeInString: String): Long {
            val df1: DateFormat = SimpleDateFormat("yyyy-MM-dd'T'HH:mm:ssZ")
            val date = "2001-07-04T12:08:56.235-0700"
            val result1 = df1.parse(timeInString)
            return result1.time
        }
    }
}

fun String.parseTime(): Long {
    val df1: DateFormat = SimpleDateFormat("yyyy-MM-dd'T'HH:mm:ssZ")
    val result1 = df1.parse(this)
    return result1.time
}