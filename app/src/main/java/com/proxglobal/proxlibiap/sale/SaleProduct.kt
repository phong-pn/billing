package com.proxglobal.proxlibiap.sale

import com.google.gson.annotations.Expose
import com.google.gson.annotations.SerializedName

abstract class SaleProduct(
    @SerializedName("product_id")
    @Expose
    var productId: String
)

class SaleBasePlan(
    @SerializedName("tags")
    @Expose
    var tags: List<String>,

    @SerializedName("offer_tags")
    @Expose
    var offerTags: List<String>
)

class SaleSubscription(
    @SerializedName("base_plans")
    @Expose
    var saleBasePlans: List<SaleBasePlan>,
    productId: String
): SaleProduct(productId)

class SaleOneTimeProduct(
    productId: String
): SaleProduct(productId)