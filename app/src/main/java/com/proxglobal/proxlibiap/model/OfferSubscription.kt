package com.proxglobal.proxlibiap.model

import com.android.billingclient.api.ProductDetails.PricingPhase

class OfferSubscription(
    var tags: List<String> = listOf(),
    var pricingPhases: List<Phase>,
    productId: String,
    token: String
): Subscription(productId, token)