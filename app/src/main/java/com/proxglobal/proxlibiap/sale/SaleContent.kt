package com.proxglobal.proxlibiap.sale

import com.google.gson.annotations.Expose
import com.google.gson.annotations.SerializedName

class SaleContent {
    @SerializedName("title")
    @Expose
    var title: String? = null

    @SerializedName("description")
    @Expose
    var description: String? = null

    @SerializedName("feature")
    @Expose
    var features: List<String>? = null

    @SerializedName("cta")
    @Expose
    var cta: String? = null
}