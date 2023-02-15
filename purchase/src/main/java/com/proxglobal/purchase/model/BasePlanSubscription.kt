package com.proxglobal.purchase.model


/**
 * Class represent for a BasePlan in Google Play's billing system.
 */
class BasePlanSubscription(
    val basePlanId: String,
    val tags: List<String> = listOf(),
    internal val phase: Phase,
    productId: String,
    token: String,
) : Subscription(productId, token) {
    val price: String = phase.price
    val priceWithoutCurrency = phase.priceAmount / 1000000f
    val priceCurrencyCode = phase.currencyCode

    lateinit var offers: List<OfferSubscription>
        internal set
}