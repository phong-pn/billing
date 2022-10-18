package com.proxglobal.proxlibiap.sale

import com.google.gson.annotations.Expose
import com.google.gson.annotations.SerializedName

class SaleImage {
    @SerializedName("name")
    @Expose
    var name: String = ""

    @SerializedName("url")
    @Expose
    var url: String = ""
}