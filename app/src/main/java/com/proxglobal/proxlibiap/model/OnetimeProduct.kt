package com.proxglobal.proxlibiap.model

import java.util.Currency

data class OnetimeProduct(
    val productId: String,
    val formattedPrice: String,
    val priceCurrencyCode: String
)