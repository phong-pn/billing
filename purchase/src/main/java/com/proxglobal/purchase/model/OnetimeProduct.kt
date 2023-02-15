package com.proxglobal.purchase.model

/**
 * Class that represent an one time product in Google Play's billing system
 */
data class OnetimeProduct(
    val productId: String,
    val price: String,
    val priceCurrencyCode: String
)