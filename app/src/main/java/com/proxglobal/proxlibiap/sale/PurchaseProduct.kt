package com.proxglobal.proxlibiap.sale

abstract class PurchaseProduct(
    productId: String
)

class BasePlan(
    var tags: List<String>,
    var offerTags: List<String>
)

class Subscription(
    var basePlans: List<BasePlan>,
    productId: String
): PurchaseProduct(productId)

class OneTimeProduct(
    productId: String
): PurchaseProduct(productId)