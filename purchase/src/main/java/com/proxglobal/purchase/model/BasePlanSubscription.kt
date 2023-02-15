package com.proxglobal.purchase.model

import com.android.billingclient.api.ProductDetails.SubscriptionOfferDetails


/**
 * Class represent for a BasePlan in Google Play's billing system.
 */
class BasePlanSubscription(
    val id: String,
    val tags: List<String> = listOf(),
    internal val phase: Phase,
    productId: String,
    token: String,
) : Subscription(productId, token) {
    val price: String = phase.price
    val priceCurrencyCode = phase.currencyCode

    lateinit var offers: List<OfferSubscription>
        internal set
}