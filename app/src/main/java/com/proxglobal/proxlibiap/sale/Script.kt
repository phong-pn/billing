package com.proxglobal.proxlibiap.sale

import com.google.gson.annotations.Expose
import com.google.gson.annotations.SerializedName

class Script {
    @SerializedName("script_name")
    @Expose
    var scriptName: String? = null

    @SerializedName("action_id")
    @Expose
    var actionId: Int = 0

    @SerializedName("show_condition_value")
    @Expose
    val showConditionValue: Any? = null

    @SerializedName("content")
    @Expose
    var saleContent: SaleContent? = null

    @SerializedName("image")
    @Expose
    var saleImages: List<SaleImage> = listOf()


}