package com.proxglobal.purchase.model

import java.util.*

/**
 * Class that represent an offer in Google Play's billing system
 */
class OfferSubscription(
    var offerId: String,
    var basePlanSubscription: BasePlanSubscription,
    var pricingPhases: List<Phase>,
    productId: String,
    token: String
) : Subscription(productId, token) {
    fun getFreeTrialPhase(): Phase? = pricingPhases.find { it.isFreeTrial }

    /**
     * Return phase not is free trial, and not is phase of basePlan
     */
    fun getDiscountPhase(): Phase? = pricingPhases.find {
        !it.isFreeTrial && !Objects.deepEquals(it, basePlanSubscription.phase)
    }

    val discountPrice = getDiscountPhase()?.price
    val discountPriceWithoutCurrency = getDiscountPhase()?.priceAmount?.div(1000000f)


    val basePrice = basePlanSubscription.price
    val basePriceWithoutCurrency = basePlanSubscription.priceWithoutCurrency


}

