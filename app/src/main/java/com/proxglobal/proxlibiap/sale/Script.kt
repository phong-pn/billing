package com.proxglobal.proxlibiap.sale

import com.google.gson.annotations.Expose
import com.google.gson.annotations.SerializedName

class Script {
    var scriptName: String? = null

    @SerializedName("action_id")
    @Expose
    var actionId: Int = 0

    @SerializedName("show_condition_value")
    @Expose
    val showConditionValue: Any? = null

    var pricePlans: List<PricePlan> = listOf()

    var saleContent: SaleContent? = null
}