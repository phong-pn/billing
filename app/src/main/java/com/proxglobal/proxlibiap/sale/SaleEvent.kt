package com.proxglobal.proxlibiap.sale

import com.google.gson.annotations.Expose
import com.google.gson.annotations.SerializedName

class SaleEvent(
    var isSaleOff: Boolean = false,

    @SerializedName("start_time")
    @Expose
    var startTime: String? = null,

    @SerializedName("end_time")
    @Expose
    var endTime: String? = null,

    @SerializedName("content_default")
    @Expose
    var saleDefaultContent: SaleContent? = null,

    @SerializedName("enable")
    @Expose
    var enable: Boolean? = null,

    @SerializedName("script")
    @Expose
    var saleScripts: List<Script> = listOf(),

    @SerializedName("plans")
    @Expose
    var pricePlans: List<PricePlan> = listOf()
) {
    fun getAllOneTimeProduct(): List<SaleOneTimeProduct> {
        val result = arrayListOf<SaleOneTimeProduct>()
        pricePlans.forEach {
            result.addAll(it.saleOneTimeProducts)
        }
        return result
    }

    fun getAllSubscription(): List<SaleSubscription> {
        val result = arrayListOf<SaleSubscription>()
        pricePlans.forEach {
            result.addAll(it.saleSubscriptions)
        }
        return result
    }
}