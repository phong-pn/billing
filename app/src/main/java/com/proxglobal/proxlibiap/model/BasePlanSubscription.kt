package com.proxglobal.proxlibiap.model

class BasePlanSubscription(
    val tags: List<String> = listOf(),
    val price: String,
    val priceCurrencyCode: String,
    productId: String,
    token: String
): Subscription(productId, token)